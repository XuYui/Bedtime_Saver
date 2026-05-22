package com.bedtimesaver.ui

import android.app.KeyguardManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.bedtimesaver.data.BedtimeDatabase
import com.bedtimesaver.data.BedtimeSettings
import com.bedtimesaver.data.SleepRepository
import com.bedtimesaver.ui.theme.BedtimeSaverTheme
import kotlinx.coroutines.launch

class AlarmRingActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var isStopping = false

    private val repository: SleepRepository by lazy {
        val database = BedtimeDatabase.getInstance(applicationContext)
        SleepRepository(
            context = applicationContext,
            dao = database.sleepRecordDao(),
            settings = BedtimeSettings(applicationContext),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        prepareWakeLockScreen()
        startAlarmSignal()

        setContent {
            BedtimeSaverTheme {
                BackHandler(enabled = true) {}
                AlarmRingScreen(onStopAlarm = ::stopAndCheckIn)
            }
        }
    }

    override fun onDestroy() {
        stopAlarmSignal()
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    private fun prepareWakeLockScreen() {
        setVolumeControlStream(AudioManager.STREAM_ALARM)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
        )
        getSystemService(KeyguardManager::class.java).requestDismissKeyguard(this, null)
    }

    private fun startAlarmSignal() {
        val alarmUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (alarmUri != null) {
            runCatching {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build(),
                    )
                    setDataSource(this@AlarmRingActivity, alarmUri)
                    isLooping = true
                    prepare()
                    start()
                }
            }.onFailure {
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }

        vibrator = getSystemService(Vibrator::class.java)
        vibrator?.vibrate(
            VibrationEffect.createWaveform(
                longArrayOf(0L, 700L, 450L, 700L, 900L),
                0,
            ),
        )
    }

    private fun stopAlarmSignal() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        vibrator = null
    }

    private fun stopAndCheckIn() {
        if (isStopping) return
        isStopping = true
        stopAlarmSignal()
        lifecycleScope.launch {
            try {
                repository.checkInWakeUp()
            } finally {
                finish()
            }
        }
    }
}

@Composable
private fun AlarmRingScreen(
    onStopAlarm: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF131313))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 28.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "MORNING ALARM",
                color = Color(0xFF8B9199),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                Text(
                    text = "◷",
                    color = Color(0xFFC5E3FF),
                    fontSize = 58.sp,
                    lineHeight = 58.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "该起床了",
                    color = Color(0xFFE5E2E1),
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 34.sp,
                    lineHeight = 42.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "关闭闹钟后会自动完成晨起打卡，并结束本次睡眠监督。",
                    color = Color(0xFFC1C7CF),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = onStopAlarm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFBFE0FF),
                        contentColor = Color(0xFF06253A),
                    ),
                ) {
                    Text(
                        text = "关闭闹钟并完成起床打卡",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )
                }
                Text(
                    text = "如果只是按 Home 键，闹钟不会被视为关闭。",
                    color = Color(0xFF8B9199),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
