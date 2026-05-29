package com.bedtimesaver.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bedtimesaver.MainActivity
import com.bedtimesaver.data.BedtimeDatabase
import com.bedtimesaver.data.BedtimeSettings
import com.bedtimesaver.data.SleepRepository
import com.bedtimesaver.domain.SleepDatePolicy
import com.bedtimesaver.domain.TargetBedtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BedtimeAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_TRIGGER_BEDTIME && intent?.action != null) return
        val bedtimeMillis = intent?.getLongExtra(EXTRA_BEDTIME_MILLIS, 0L)
            ?.takeIf { it > 0L }
            ?: System.currentTimeMillis()
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = BedtimeDatabase.getInstance(context)
                val repository = SleepRepository(
                    context = context.applicationContext,
                    dao = database.sleepRecordDao(),
                    settings = BedtimeSettings(context),
                )
                if (repository.startAutomaticBedtime(bedtimeMillis)) {
                    withContext(Dispatchers.Main) {
                        goHome(context)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 2300
        private const val SHOW_REQUEST_CODE = 2301
        private const val ACTION_TRIGGER_BEDTIME = "com.bedtimesaver.action.TRIGGER_BEDTIME"
        private const val EXTRA_BEDTIME_MILLIS = "bedtime_millis"

        fun schedule(
            context: Context,
            targetBedtime: TargetBedtime,
            nowMillis: Long = System.currentTimeMillis(),
        ) {
            cancel(context)
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            val triggerAtMillis = SleepDatePolicy.nextTriggerMillis(targetBedtime, nowMillis)
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent(context)),
                createTriggerIntent(context, triggerAtMillis),
            )
        }

        fun cancel(context: Context) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            listOf(
                findTriggerIntent(context),
                legacyIntent(context),
            ).forEach { pendingIntent ->
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                }
            }
        }

        private fun goHome(context: Context) {
            val intent = Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        private fun createTriggerIntent(context: Context, bedtimeMillis: Long): PendingIntent {
            return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                Intent(context, BedtimeAlarmReceiver::class.java)
                    .setAction(ACTION_TRIGGER_BEDTIME)
                    .putExtra(EXTRA_BEDTIME_MILLIS, bedtimeMillis),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        private fun findTriggerIntent(context: Context): PendingIntent? {
            return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                Intent(context, BedtimeAlarmReceiver::class.java)
                    .setAction(ACTION_TRIGGER_BEDTIME),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        private fun showIntent(context: Context): PendingIntent {
            return PendingIntent.getActivity(
                context,
                SHOW_REQUEST_CODE,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        private fun legacyIntent(context: Context): PendingIntent? {
            return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                Intent(context, BedtimeAlarmReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}
