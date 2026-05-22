package com.bedtimesaver.ui

import com.bedtimesaver.data.DailySleepRecord
import com.bedtimesaver.domain.TargetBedtime
import com.bedtimesaver.service.SleepModeState

data class HomeUiState(
    val targetBedtime: TargetBedtime = TargetBedtime(),
    val wakeAlarmTime: TargetBedtime = TargetBedtime(hour = 7, minute = 0),
    val records: List<DailySleepRecord> = emptyList(),
    val sleepModeState: SleepModeState = SleepModeState(),
    val activeRecord: DailySleepRecord? = null,
    val currentStreak: Int = 0,
    val accessibilityEnabled: Boolean = false,
    val historyDays: List<HistoryDay> = emptyList(),
    val calendarTitle: String = "",
)

data class HistoryDay(
    val date: String,
    val dayOfMonth: Int,
    val record: DailySleepRecord?,
)
