package com.bedtimesaver.service

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

data class SleepModeState(
    val isActive: Boolean = false,
    val activeDate: String? = null,
    val startedAtMillis: Long? = null,
    val emergencyUnlockUntilMillis: Long = 0L,
) {
    fun isTemporarilyUnlocked(nowMillis: Long = System.currentTimeMillis()): Boolean {
        return emergencyUnlockUntilMillis > nowMillis
    }
}

object SleepModeStore {
    private const val PREFS_NAME = "sleep_mode_state"
    private const val KEY_ACTIVE = "active"
    private const val KEY_ACTIVE_DATE = "active_date"
    private const val KEY_STARTED_AT = "started_at"
    private const val KEY_UNLOCK_UNTIL = "unlock_until"

    private val alwaysAllowedPackages = setOf(
        "android",
        "com.android.systemui",
        "com.android.settings",
        "com.google.android.permissioncontroller",
        "com.android.permissioncontroller",
        "com.google.android.apps.nexuslauncher",
        "com.android.launcher",
        "com.android.launcher3",
        "com.miui.home",
        "com.huawei.android.launcher",
        "com.oppo.launcher",
        "com.vivo.launcher",
        "com.samsung.android.app.telephonyui",
        "com.google.android.dialer",
        "com.android.dialer",
        "com.android.deskclock",
        "com.google.android.deskclock",
        "com.sec.android.app.clockpackage",
        "com.miui.clock",
        "com.huawei.deskclock",
        "com.coloros.alarmclock",
        "com.oppo.alarmclock",
        "com.vivo.alarmclock",
        "com.oneplus.deskclock",
        "com.motorola.timeweatherwidget",
    )

    fun observe(context: Context): Flow<SleepModeState> = callbackFlow {
        val prefs = prefs(context)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(getState(context))
        }
        trySend(getState(context))
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.distinctUntilChanged()

    fun getState(context: Context): SleepModeState {
        val prefs = prefs(context)
        val startedAt = prefs.getLong(KEY_STARTED_AT, 0L).takeIf { it > 0L }
        return SleepModeState(
            isActive = prefs.getBoolean(KEY_ACTIVE, false),
            activeDate = prefs.getString(KEY_ACTIVE_DATE, null),
            startedAtMillis = startedAt,
            emergencyUnlockUntilMillis = prefs.getLong(KEY_UNLOCK_UNTIL, 0L),
        )
    }

    fun activate(
        context: Context,
        activeDate: String,
        startedAtMillis: Long = System.currentTimeMillis(),
    ) {
        prefs(context).edit()
            .putBoolean(KEY_ACTIVE, true)
            .putString(KEY_ACTIVE_DATE, activeDate)
            .putLong(KEY_STARTED_AT, startedAtMillis)
            .putLong(KEY_UNLOCK_UNTIL, 0L)
            .apply()
    }

    fun deactivate(context: Context) {
        prefs(context).edit()
            .putBoolean(KEY_ACTIVE, false)
            .remove(KEY_ACTIVE_DATE)
            .remove(KEY_STARTED_AT)
            .putLong(KEY_UNLOCK_UNTIL, 0L)
            .apply()
    }

    fun allowTemporaryUnlock(
        context: Context,
        durationMillis: Long,
        nowMillis: Long = System.currentTimeMillis(),
    ) {
        prefs(context).edit()
            .putLong(KEY_UNLOCK_UNTIL, nowMillis + durationMillis)
            .apply()
    }

    fun shouldBlockPackage(
        context: Context,
        packageName: String,
        nowMillis: Long = System.currentTimeMillis(),
    ): Boolean {
        val state = getState(context)
        if (!state.isActive || state.isTemporarilyUnlocked(nowMillis)) return false
        if (packageName == context.packageName) return false
        return packageName !in alwaysAllowedPackages
    }

    private fun prefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
