package com.bedtimesaver.service

import android.content.ComponentName
import android.content.Context
import android.provider.Settings

object AccessibilityPermission {
    fun isEnabled(context: Context): Boolean {
        val expected = ComponentName(
            context,
            BedtimeAccessibilityService::class.java,
        ).flattenToString()
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false

        return enabledServices
            .split(':')
            .any { it.equals(expected, ignoreCase = true) }
    }
}
