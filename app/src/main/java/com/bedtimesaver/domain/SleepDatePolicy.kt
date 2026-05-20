package com.bedtimesaver.domain

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object SleepDatePolicy {
    private val zone: ZoneId = ZoneId.systemDefault()

    fun sleepDateFor(millis: Long = System.currentTimeMillis()): LocalDate {
        val dateTime = Instant.ofEpochMilli(millis).atZone(zone).toLocalDateTime()
        return if (dateTime.hour < 12) {
            dateTime.toLocalDate().minusDays(1)
        } else {
            dateTime.toLocalDate()
        }
    }

    fun sleepDateStringFor(millis: Long = System.currentTimeMillis()): String {
        return sleepDateFor(millis).toString()
    }

    fun targetDateTimeForSleepDate(
        sleepDate: LocalDate,
        targetBedtime: TargetBedtime,
    ): LocalDateTime {
        val targetDate = if (targetBedtime.hour < 12) sleepDate.plusDays(1) else sleepDate
        return LocalDateTime.of(targetDate, targetBedtime.asLocalTime())
    }

    fun isAtOrBeforeTarget(
        checkInMillis: Long,
        targetBedtime: TargetBedtime,
    ): Boolean {
        val sleepDate = sleepDateFor(checkInMillis)
        val targetDateTime = targetDateTimeForSleepDate(sleepDate, targetBedtime)
        val actual = Instant.ofEpochMilli(checkInMillis).atZone(zone).toLocalDateTime()
        return !actual.isAfter(targetDateTime)
    }

    fun nextTriggerMillis(
        targetBedtime: TargetBedtime,
        nowMillis: Long = System.currentTimeMillis(),
    ): Long {
        val now = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDateTime()
        var next = LocalDateTime.of(now.toLocalDate(), targetBedtime.asLocalTime())
        if (!next.isAfter(now)) {
            next = next.plusDays(1)
        }
        return next.atZone(zone).toInstant().toEpochMilli()
    }
}
