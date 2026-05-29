package com.bedtimesaver.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bedtimesaver.data.BedtimeSettings

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            if (SleepModeStore.getState(context).isActive) {
                BedtimeAlarmReceiver.cancel(context)
            } else {
                BedtimeAlarmReceiver.schedule(context, BedtimeSettings(context).getTargetBedtime())
            }
        }
    }
}
