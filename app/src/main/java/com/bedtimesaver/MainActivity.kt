package com.bedtimesaver

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bedtimesaver.data.BedtimeDatabase
import com.bedtimesaver.data.BedtimeSettings
import com.bedtimesaver.data.SleepRepository
import com.bedtimesaver.ui.HomeScreen
import com.bedtimesaver.ui.MainViewModel
import com.bedtimesaver.ui.theme.BedtimeSaverTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        val database = BedtimeDatabase.getInstance(applicationContext)
        val repository = SleepRepository(
            context = applicationContext,
            dao = database.sleepRecordDao(),
            settings = BedtimeSettings(applicationContext),
        )
        MainViewModel.Factory(applicationContext, repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.syncScheduledAlarms()

        setContent {
            BedtimeSaverTheme {
                Surface {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    HomeScreen(
                        uiState = uiState,
                        onSleepClick = viewModel::checkInBed,
                        onWakeClick = viewModel::checkInWakeUp,
                        onDeleteRecord = viewModel::deleteRecord,
                        onSupplementRecord = viewModel::supplementRecord,
                        onTargetHourDelta = viewModel::changeTargetHour,
                        onTargetMinuteDelta = viewModel::changeTargetMinute,
                        onOpenAccessibilityClick = {
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        },
                        onRefreshAccessibilityClick = viewModel::refreshAccessibilityStatus,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshAccessibilityStatus()
    }
}
