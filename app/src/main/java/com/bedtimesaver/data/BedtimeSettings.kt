package com.bedtimesaver.data

import android.content.Context
import android.content.SharedPreferences
import com.bedtimesaver.domain.TargetBedtime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class BedtimeSettings(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    val targetBedtimeFlow: Flow<TargetBedtime> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_TARGET_HOUR || key == KEY_TARGET_MINUTE) {
                trySend(getTargetBedtime())
            }
        }
        trySend(getTargetBedtime())
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.distinctUntilChanged()

    val wakeAlarmTimeFlow: Flow<TargetBedtime> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_WAKE_HOUR || key == KEY_WAKE_MINUTE) {
                trySend(getWakeAlarmTime())
            }
        }
        trySend(getWakeAlarmTime())
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.distinctUntilChanged()

    fun getTargetBedtime(): TargetBedtime {
        return TargetBedtime(
            hour = prefs.getInt(KEY_TARGET_HOUR, 23).coerceIn(0, 23),
            minute = prefs.getInt(KEY_TARGET_MINUTE, 0).coerceIn(0, 59),
        )
    }

    fun setTargetBedtime(targetBedtime: TargetBedtime) {
        prefs.edit()
            .putInt(KEY_TARGET_HOUR, targetBedtime.hour.coerceIn(0, 23))
            .putInt(KEY_TARGET_MINUTE, targetBedtime.minute.coerceIn(0, 59))
            .apply()
    }

    fun getWakeAlarmTime(): TargetBedtime {
        return TargetBedtime(
            hour = prefs.getInt(KEY_WAKE_HOUR, 7).coerceIn(0, 23),
            minute = prefs.getInt(KEY_WAKE_MINUTE, 0).coerceIn(0, 59),
        )
    }

    fun setWakeAlarmTime(wakeAlarmTime: TargetBedtime) {
        prefs.edit()
            .putInt(KEY_WAKE_HOUR, wakeAlarmTime.hour.coerceIn(0, 23))
            .putInt(KEY_WAKE_MINUTE, wakeAlarmTime.minute.coerceIn(0, 59))
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "bedtime_settings"
        private const val KEY_TARGET_HOUR = "target_hour"
        private const val KEY_TARGET_MINUTE = "target_minute"
        private const val KEY_WAKE_HOUR = "wake_hour"
        private const val KEY_WAKE_MINUTE = "wake_minute"
    }
}
