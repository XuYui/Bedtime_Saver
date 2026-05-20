package com.bedtimesaver.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_sleep_records")
data class DailySleepRecord(
    @PrimaryKey val date: String,
    val bedtimeCheckInMillis: Long? = null,
    val wakeUpCheckInMillis: Long? = null,
    val targetBedtimeMinutes: Int = 23 * 60,
    val metGoal: Boolean = false,
    val streakCount: Int = 0,
    val sleepDurationMinutes: Long? = null,
)
