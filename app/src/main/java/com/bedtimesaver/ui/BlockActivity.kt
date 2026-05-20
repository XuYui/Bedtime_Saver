package com.bedtimesaver.ui

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bedtimesaver.service.SleepModeStore
import com.bedtimesaver.ui.theme.BedtimeSaverTheme
import kotlinx.coroutines.delay

class BlockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
        )

        val blockedPackage = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE).orEmpty()

        setContent {
            BedtimeSaverTheme {
                BackHandler(enabled = true) {}
                BlockScreen(
                    blockedPackage = blockedPackage,
                    onGoHome = ::goHome,
                    onTemporaryUnlock = {
                        SleepModeStore.allowTemporaryUnlock(
                            context = this,
                            durationMillis = TEMPORARY_UNLOCK_MILLIS,
                        )
                        finish()
                    },
                )
            }
        }
    }

    private fun goHome() {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_HOME)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "blocked_package"
        private const val TEMPORARY_UNLOCK_MILLIS = 5 * 60 * 1_000L
    }
}

@Composable
private fun BlockScreen(
    blockedPackage: String,
    onGoHome: () -> Unit,
    onTemporaryUnlock: () -> Unit,
) {
    var challengeStarted by rememberSaveable { mutableStateOf(false) }
    var remainingSeconds by rememberSaveable { mutableIntStateOf(60) }
    val challengeDone = challengeStarted && remainingSeconds == 0
    val blockedText = remember(blockedPackage) {
        blockedPackage.ifBlank { "当前应用" }
    }

    LaunchedEffect(challengeStarted) {
        if (!challengeStarted) return@LaunchedEffect
        remainingSeconds = 60
        while (remainingSeconds > 0) {
            delay(1_000L)
            remainingSeconds -= 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF131313))
            .padding(horizontal = 28.dp, vertical = 44.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.padding(top = 34.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "☾",
                    color = Color(0xFF8B9199),
                    fontSize = 28.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "DEEP REST MODE",
                    color = Color(0xFF8B9199),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                Text(
                    text = "☾",
                    color = Color(0xFFC5E3FF),
                    fontSize = 50.sp,
                    lineHeight = 50.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "睡眠监督中",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFE5E2E1),
                    fontSize = 34.sp,
                    lineHeight = 42.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "深夜是身体修复的最佳时机。检测到你正尝试打开 $blockedText，请放下手机，安心入睡。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFC1C7CF),
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (challengeStarted) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Text(
                                text = "剩余挑战时间",
                                color = Color(0xFFC1C7CF),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = if (challengeDone) "完成" else "${remainingSeconds}s",
                                color = if (challengeDone) Color(0xFFA2D3A4) else Color(0xFFC5E3FF),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        LinearProgressIndicator(
                            progress = { remainingSeconds / 60f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp),
                            color = if (challengeDone) Color(0xFFA2D3A4) else Color(0xFFC5E3FF),
                            trackColor = Color(0xFF353534),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = onGoHome,
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
                        text = "回到桌面",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
                OutlinedButton(
                    onClick = {
                        if (challengeDone) {
                            onTemporaryUnlock()
                        } else {
                            challengeStarted = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF41474E)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFC5E3FF),
                    ),
                ) {
                    Text(
                        text = if (challengeDone) "临时解锁" else "我要解锁，开始 60 秒挑战",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Text(
                    text = "完成冷静挑战以获得 5 分钟紧急使用权限",
                    color = Color(0xFF43535B).copy(alpha = 0.64f),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
