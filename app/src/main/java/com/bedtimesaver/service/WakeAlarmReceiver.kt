package com.bedtimesaver.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bedtimesaver.data.BedtimeSettings
import com.bedtimesaver.domain.TargetBedtime
import com.bedtimesaver.ui.AlarmRingActivity
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class WakeAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val ringIntent = Intent(context, AlarmRingActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(ringIntent)
    }

    companion object {
        private const val REQUEST_CODE = 7301
        private const val SHOW_REQUEST_CODE = 7302

        fun scheduleNextWake(
            context: Context,
            wakeTime: TargetBedtime = BedtimeSettings(context).getWakeAlarmTime(),
            nowMillis: Long = System.currentTimeMillis(),
        ) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            val operation = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                Intent(context, WakeAlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val showIntent = PendingIntent.getActivity(
                context,
                SHOW_REQUEST_CODE,
                Intent(context, AlarmRingActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val triggerAt = nextTriggerMillis(wakeTime, nowMillis)

            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAt, showIntent),
                operation,
            )
        }

        fun cancel(context: Context) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            val operation = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                Intent(context, WakeAlarmReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )
            if (operation != null) {
                alarmManager.cancel(operation)
                operation.cancel()
            }
        }

        private fun nextTriggerMillis(
            wakeTime: TargetBedtime,
            nowMillis: Long,
        ): Long {
            val zone = ZoneId.systemDefault()
            val now = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDateTime()
            var next = LocalDateTime.of(now.toLocalDate(), wakeTime.asLocalTime())
            if (!next.isAfter(now)) {
                next = next.plusDays(1)
            }
            return next.atZone(zone).toInstant().toEpochMilli()
        }
    }
}
