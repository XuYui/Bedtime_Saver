package com.bedtimesaver.data

import android.content.Context
import com.bedtimesaver.domain.SleepDatePolicy
import com.bedtimesaver.domain.TargetBedtime
import com.bedtimesaver.service.BedtimeAlarmReceiver
import com.bedtimesaver.service.SleepModeStore
import com.bedtimesaver.service.WakeAlarmReceiver
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.max

class SleepRepository(
    private val context: Context,
    private val dao: SleepRecordDao,
    private val settings: BedtimeSettings,
) {
    val recordsFlow = dao.observeRecords()
    val targetBedtimeFlow = settings.targetBedtimeFlow
    val wakeAlarmTimeFlow = settings.wakeAlarmTimeFlow
    val sleepModeFlow = SleepModeStore.observe(context)

    suspend fun checkInBed(nowMillis: Long = System.currentTimeMillis()) {
        val target = settings.getTargetBedtime()
        val date = SleepDatePolicy.sleepDateStringFor(nowMillis)
        val metGoal = SleepDatePolicy.isAtOrBeforeTarget(nowMillis, target)
        val existing = dao.getByDate(date)
        val streak = computeStreak(date, metGoal)
        val updated = (existing ?: DailySleepRecord(date = date)).copy(
            bedtimeCheckInMillis = nowMillis,
            targetBedtimeMinutes = target.asMinutesOfDay(),
            metGoal = metGoal,
            streakCount = streak,
            sleepDurationMinutes = existing?.wakeUpCheckInMillis?.let {
                max(0L, (it - nowMillis) / MILLIS_PER_MINUTE)
            },
        )

        dao.upsert(updated)
        SleepModeStore.activate(context, activeDate = date, startedAtMillis = nowMillis)
        WakeAlarmReceiver.scheduleNextWake(context, settings.getWakeAlarmTime())
        BedtimeAlarmReceiver.cancel(context)
    }

    suspend fun checkInWakeUp(nowMillis: Long = System.currentTimeMillis()) {
        val activeDate = SleepModeStore.getState(context).activeDate
            ?: SleepDatePolicy.sleepDateStringFor(nowMillis)
        val existing = dao.getByDate(activeDate)
        val duration = existing?.bedtimeCheckInMillis?.let {
            max(0L, (nowMillis - it) / MILLIS_PER_MINUTE)
        }
        val record = (existing ?: DailySleepRecord(date = activeDate)).copy(
            wakeUpCheckInMillis = nowMillis,
            sleepDurationMinutes = duration,
        )

        dao.upsert(record)
        SleepModeStore.deactivate(context)
        WakeAlarmReceiver.cancel(context)
        BedtimeAlarmReceiver.cancel(context)
    }

    suspend fun deleteRecord(date: String) {
        dao.deleteByDate(date)
        if (SleepModeStore.getState(context).activeDate == date) {
            SleepModeStore.deactivate(context)
            WakeAlarmReceiver.cancel(context)
        }
        rebuildStreaks()
    }

    suspend fun supplementRecord(
        sleepDate: LocalDate,
        bedtime: LocalTime,
        wakeTime: LocalTime,
    ) {
        val target = settings.getTargetBedtime()
        val bedtimeDate = if (bedtime.hour < 12) sleepDate.plusDays(1) else sleepDate
        val bedtimeDateTime = LocalDateTime.of(bedtimeDate, bedtime)
        var wakeDateTime = LocalDateTime.of(
            if (wakeTime.hour < 12) sleepDate.plusDays(1) else sleepDate,
            wakeTime,
        )
        if (!wakeDateTime.isAfter(bedtimeDateTime)) {
            wakeDateTime = wakeDateTime.plusDays(1)
        }

        val bedtimeMillis = bedtimeDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val wakeMillis = wakeDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val metGoal = SleepDatePolicy.isAtOrBeforeTarget(bedtimeMillis, target)
        val duration = max(0L, (wakeMillis - bedtimeMillis) / MILLIS_PER_MINUTE)

        dao.upsert(
            DailySleepRecord(
                date = sleepDate.toString(),
                bedtimeCheckInMillis = bedtimeMillis,
                wakeUpCheckInMillis = wakeMillis,
                targetBedtimeMinutes = target.asMinutesOfDay(),
                metGoal = metGoal,
                sleepDurationMinutes = duration,
            ),
        )
        rebuildStreaks()
    }

    fun updateTargetBedtime(targetBedtime: TargetBedtime) {
        settings.setTargetBedtime(targetBedtime)
        BedtimeAlarmReceiver.cancel(context)
    }

    fun updateWakeAlarmTime(wakeAlarmTime: TargetBedtime) {
        settings.setWakeAlarmTime(wakeAlarmTime)
        if (SleepModeStore.getState(context).isActive) {
            WakeAlarmReceiver.scheduleNextWake(context, wakeAlarmTime)
        }
    }

    fun syncScheduledAlarms() {
        BedtimeAlarmReceiver.cancel(context)
        if (SleepModeStore.getState(context).isActive) {
            WakeAlarmReceiver.scheduleNextWake(context, settings.getWakeAlarmTime())
        }
    }

    private suspend fun computeStreak(date: String, metGoal: Boolean): Int {
        if (!metGoal) return 0
        val previousDate = LocalDate.parse(date).minusDays(1).toString()
        val previous = dao.getByDate(previousDate)
        return if (previous?.metGoal == true) previous.streakCount + 1 else 1
    }

    private suspend fun rebuildStreaks() {
        var streak = 0
        dao.getAllOnce()
            .sortedBy { it.date }
            .forEach { record ->
                streak = if (record.metGoal) streak + 1 else 0
                if (record.streakCount != streak) {
                    dao.upsert(record.copy(streakCount = streak))
                }
            }
    }

    private companion object {
        const val MILLIS_PER_MINUTE = 60_000L
    }
}
