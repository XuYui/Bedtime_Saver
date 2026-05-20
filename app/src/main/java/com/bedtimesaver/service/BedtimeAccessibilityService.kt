package com.bedtimesaver.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.bedtimesaver.ui.BlockActivity

class BedtimeAccessibilityService : AccessibilityService() {
    private var lastBlockedPackage: String? = null
    private var lastBlockedAtMillis: Long = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val type = event.eventType
        if (
            type != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            type != AccessibilityEvent.TYPE_WINDOWS_CHANGED
        ) {
            return
        }

        val packageName = event.packageName?.toString() ?: return
        val now = System.currentTimeMillis()
        if (!SleepModeStore.shouldBlockPackage(this, packageName, now)) return
        if (isDuplicateBlock(packageName, now)) return

        lastBlockedPackage = packageName
        lastBlockedAtMillis = now
        val intent = Intent(this, BlockActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(BlockActivity.EXTRA_BLOCKED_PACKAGE, packageName)
        startActivity(intent)
    }

    override fun onInterrupt() = Unit

    private fun isDuplicateBlock(packageName: String, nowMillis: Long): Boolean {
        return lastBlockedPackage == packageName &&
            nowMillis - lastBlockedAtMillis < BLOCK_DEBOUNCE_MILLIS
    }

    private companion object {
        const val BLOCK_DEBOUNCE_MILLIS = 1_500L
    }
}
