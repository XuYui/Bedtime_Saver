package com.bedtimesaver.ui

import com.bedtimesaver.data.DailySleepRecord
import com.bedtimesaver.domain.TargetBedtime
import com.bedtimesaver.service.SleepModeState

data class HomeUiState(
    val targetBedtime: TargetBedtime = TargetBedtime(),
    val records: List<DailySleepRecord> = emptyList(),
    val sleepModeState: SleepModeState = SleepModeState(),
    val activeRecord: DailySleepRecord? = null,
    val currentStreak: Int = 0,
    val accessibilityEnabled: Boolean = false,
    val historyDays: List<HistoryDay> = emptyList(),
)

data class HistoryDay(
    val date: String,
    val dayOfMonth: Int,
    val record: DailySleepRecord?,
)
