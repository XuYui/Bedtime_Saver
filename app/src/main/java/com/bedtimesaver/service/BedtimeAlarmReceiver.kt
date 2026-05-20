package com.bedtimesaver.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bedtimesaver.data.BedtimeSettings
import com.bedtimesaver.domain.SleepDatePolicy
import com.bedtimesaver.domain.TargetBedtime

class BedtimeAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val now = System.currentTimeMillis()
        val target = BedtimeSettings(context).getTargetBedtime()
        SleepModeStore.activate(
            context = context,
            activeDate = SleepDatePolicy.sleepDateStringFor(now),
            startedAtMillis = now,
        )
        scheduleNext(context, target, nowMillis = now + 1_000L)
    }

    companion object {
        private const val REQUEST_CODE = 2300

        fun scheduleNext(
            context: Context,
            targetBedtime: TargetBedtime = BedtimeSettings(context).getTargetBedtime(),
            nowMillis: Long = System.currentTimeMillis(),
        ) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                Intent(context, BedtimeAlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                SleepDatePolicy.nextTriggerMillis(targetBedtime, nowMillis),
                pendingIntent,
            )
        }
    }
}
