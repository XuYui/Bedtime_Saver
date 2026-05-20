package com.bedtimesaver.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.bedtimesaver.data.DailySleepRecord
import com.bedtimesaver.data.SleepRepository
import com.bedtimesaver.domain.SleepDatePolicy
import com.bedtimesaver.service.AccessibilityPermission
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val appContext: Context,
    private val repository: SleepRepository,
) : ViewModel() {
    private val accessibilityEnabled = MutableStateFlow(
        AccessibilityPermission.isEnabled(appContext),
    )

    val uiState = combine(
        repository.recordsFlow,
        repository.targetBedtimeFlow,
        repository.sleepModeFlow,
        accessibilityEnabled,
    ) { records, target, sleepMode, accessEnabled ->
        val byDate = records.associateBy { it.date }
        val currentSleepDate = SleepDatePolicy.sleepDateStringFor()
        val activeDate = sleepMode.activeDate ?: currentSleepDate
        val activeRecord = byDate[activeDate] ?: byDate[currentSleepDate]

        HomeUiState(
            targetBedtime = target,
            records = records,
            sleepModeState = sleepMode,
            activeRecord = activeRecord,
            currentStreak = visibleStreak(records),
            accessibilityEnabled = accessEnabled,
            historyDays = buildHistoryDays(byDate),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HomeUiState(),
    )

    fun refreshAccessibilityStatus() {
        accessibilityEnabled.value = AccessibilityPermission.isEnabled(appContext)
    }

    fun scheduleNextGuard() {
        repository.scheduleNextGuard()
    }

    fun checkInBed() {
        viewModelScope.launch {
            repository.checkInBed()
        }
    }

    fun checkInWakeUp() {
        viewModelScope.launch {
            repository.checkInWakeUp()
        }
    }

    fun deleteRecord(date: String) {
        viewModelScope.launch {
            repository.deleteRecord(date)
        }
    }

    fun changeTargetHour(delta: Int) {
        repository.updateTargetBedtime(uiState.value.targetBedtime.withHourDelta(delta))
    }

    fun changeTargetMinute(delta: Int) {
        repository.updateTargetBedtime(uiState.value.targetBedtime.withMinuteDelta(delta))
    }

    private fun visibleStreak(records: List<DailySleepRecord>): Int {
        val latest = records.maxByOrNull { it.date } ?: return 0
        return if (latest.metGoal) latest.streakCount else 0
    }

    private fun buildHistoryDays(byDate: Map<String, DailySleepRecord>): List<HistoryDay> {
        val anchor = SleepDatePolicy.sleepDateFor()
        return (34 downTo 0).map { offset ->
            val date = anchor.minusDays(offset.toLong())
            val key = date.toString()
            HistoryDay(
                date = key,
                dayOfMonth = date.dayOfMonth,
                record = byDate[key],
            )
        }
    }

    class Factory(
        private val appContext: Context,
        private val repository: SleepRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras,
        ): T {
            return MainViewModel(appContext, repository) as T
        }
    }
}
