package com.bedtimesaver.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bedtimesaver.data.DailySleepRecord
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private enum class HomeSection {
    Tonight,
    Records,
}

private object BedtimeTokens {
    val background = Color(0xFF131313)
    val card = Color(0xFF1A1A1A)
    val cardLow = Color(0xFF1C1B1B)
    val cardHigh = Color(0xFF242424)
    val border = Color(0xFF37474F)
    val divider = Color(0xFF2C3338)
    val text = Color(0xFFE5E2E1)
    val muted = Color(0xFFC1C7CF)
    val dim = Color(0xFF8B9199)
    val primary = Color(0xFFC5E3FF)
    val primaryButton = Color(0xFFBFE0FF)
    val green = Color(0xFFA2D3A4)
    val greenContainer = Color(0xFF24502C)
    val red = Color(0xFFFFB4AB)
    val redContainer = Color(0xFF93000A)
    val emptyDot = Color(0xFF353534)
    val navActive = Color(0xFF22522D)
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onSleepClick: () -> Unit,
    onWakeClick: () -> Unit,
    onDeleteRecord: (String) -> Unit,
    onSupplementRecord: (LocalDate, LocalTime, LocalTime) -> Unit,
    onTargetHourDelta: (Int) -> Unit,
    onTargetMinuteDelta: (Int) -> Unit,
    onOpenAccessibilityClick: () -> Unit,
    onRefreshAccessibilityClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var section by rememberSaveable { mutableStateOf(HomeSection.Tonight) }
    var pendingDelete by remember { mutableStateOf<DailySleepRecord?>(null) }
    var supplementDialogVisible by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BedtimeTokens.background,
        bottomBar = {
            BottomTabs(
                selected = section,
                onSelect = { section = it },
            )
        },
    ) { innerPadding ->
        when (section) {
            HomeSection.Tonight -> TonightPanel(
                uiState = uiState,
                onSleepClick = onSleepClick,
                onWakeClick = onWakeClick,
                onTargetHourDelta = onTargetHourDelta,
                onTargetMinuteDelta = onTargetMinuteDelta,
                onOpenAccessibilityClick = onOpenAccessibilityClick,
                onRefreshAccessibilityClick = onRefreshAccessibilityClick,
                modifier = Modifier.padding(innerPadding),
            )

            HomeSection.Records -> RecordsPanel(
                uiState = uiState,
                onDeleteClick = { pendingDelete = it },
                onSupplementClick = { supplementDialogVisible = true },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }

    pendingDelete?.let { record ->
        DeleteRecordDialog(
            record = record,
            onDismiss = { pendingDelete = null },
            onConfirm = {
                onDeleteRecord(record.date)
                pendingDelete = null
            },
        )
    }

    if (supplementDialogVisible) {
        SupplementRecordDialog(
            onDismiss = { supplementDialogVisible = false },
            onConfirm = { date, bedtime, wakeTime ->
                onSupplementRecord(date, bedtime, wakeTime)
                supplementDialogVisible = false
            },
        )
    }
}

@Composable
private fun TonightPanel(
    uiState: HomeUiState,
    onSleepClick: () -> Unit,
    onWakeClick: () -> Unit,
    onTargetHourDelta: (Int) -> Unit,
    onTargetMinuteDelta: (Int) -> Unit,
    onOpenAccessibilityClick: () -> Unit,
    onRefreshAccessibilityClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BedtimeTokens.background),
    ) {
        AppTopBar(
            title = "睡前救星",
            trailing = {
                StreakChip(streak = uiState.currentStreak)
                Spacer(modifier = Modifier.width(10.dp))
                BarGlyph()
            },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatusRow(uiState)
            TargetTimeCard(
                targetText = uiState.targetBedtime.displayText(),
                onHourDelta = onTargetHourDelta,
                onMinuteDelta = onTargetMinuteDelta,
            )
            PermissionNotice(
                enabled = uiState.accessibilityEnabled,
                onOpenAccessibilityClick = onOpenAccessibilityClick,
                onRefreshAccessibilityClick = onRefreshAccessibilityClick,
            )
            ActionCard(
                uiState = uiState,
                onSleepClick = onSleepClick,
                onWakeClick = onWakeClick,
            )
            QuickStatsRow(uiState = uiState)
            SleepQualityStandardCard()
        }
    }
}

@Composable
private fun RecordsPanel(
    uiState: HomeUiState,
    onDeleteClick: (DailySleepRecord) -> Unit,
    onSupplementClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BedtimeTokens.background),
    ) {
        AppTopBar(
            title = "打卡记录",
            trailing = { BarGlyph() },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            StatsSummary(uiState)
            SupplementActionCard(onSupplementClick = onSupplementClick)
            CalendarCard(
                title = uiState.calendarTitle,
                days = uiState.historyDays,
            )
            RecentRecordsCard(
                records = uiState.records.take(10),
                onDeleteClick = onDeleteClick,
            )
        }
    }
}

@Composable
private fun AppTopBar(
    title: String,
    trailing: @Composable RowScope.() -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = BedtimeTokens.background,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MoonGlyph(size = 34)
                    Text(
                        text = title,
                        color = BedtimeTokens.primary,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    content = trailing,
                )
            }
            HorizontalDivider(color = BedtimeTokens.divider)
        }
    }
}

@Composable
private fun StatusRow(uiState: HomeUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (uiState.sleepModeState.isActive) BedtimeTokens.green else BedtimeTokens.dim),
            )
            Text(
                text = if (uiState.sleepModeState.isActive) "监督已开启" else "监督未开启",
                color = BedtimeTokens.muted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(
            text = "目标: ${uiState.targetBedtime.displayText()}",
            color = BedtimeTokens.muted,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun StreakChip(streak: Int) {
    Surface(
        color = BedtimeTokens.cardHigh,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BedtimeTokens.border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "↯",
                color = BedtimeTokens.green,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "连续 $streak 天",
                color = BedtimeTokens.text,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun TargetTimeCard(
    targetText: String,
    onHourDelta: (Int) -> Unit,
    onMinuteDelta: (Int) -> Unit,
) {
    TonalCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 26.dp),
        ) {
            Text(
                text = "☾",
                modifier = Modifier.align(Alignment.TopEnd),
                color = BedtimeTokens.divider.copy(alpha = 0.62f),
                fontSize = 116.sp,
                lineHeight = 116.sp,
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "目标就寝时间",
                    color = BedtimeTokens.muted,
                    style = MaterialTheme.typography.labelMedium,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    VerticalStepper(
                        onPlus = { onHourDelta(1) },
                        onMinus = { onHourDelta(-1) },
                    )
                    Text(
                        text = targetText,
                        color = BedtimeTokens.text,
                        fontSize = 64.sp,
                        lineHeight = 70.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    VerticalStepper(
                        onPlus = { onMinuteDelta(5) },
                        onMinus = { onMinuteDelta(-5) },
                    )
                }
            }
        }
    }
}

@Composable
private fun VerticalStepper(
    onPlus: () -> Unit,
    onMinus: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextButton(
            onClick = onPlus,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(48.dp),
        ) {
            Text(
                text = "+",
                color = BedtimeTokens.primary,
                fontSize = 36.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Light,
            )
        }
        TextButton(
            onClick = onMinus,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(48.dp),
        ) {
            Text(
                text = "−",
                color = BedtimeTokens.primary,
                fontSize = 40.sp,
                lineHeight = 40.sp,
                fontWeight = FontWeight.Light,
            )
        }
    }
}

@Composable
private fun PermissionNotice(
    enabled: Boolean,
    onOpenAccessibilityClick: () -> Unit,
    onRefreshAccessibilityClick: () -> Unit,
) {
    if (enabled) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BedtimeTokens.redContainer.copy(alpha = 0.28f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, BedtimeTokens.red.copy(alpha = 0.45f)),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "△",
                color = BedtimeTokens.red,
                fontSize = 34.sp,
                fontWeight = FontWeight.Light,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "无障碍权限未开启",
                    color = BedtimeTokens.text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "开启后可自动记录锁定时间",
                    color = BedtimeTokens.muted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Button(
                onClick = onOpenAccessibilityClick,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BedtimeTokens.red,
                    contentColor = Color(0xFF2F0606),
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
            ) {
                Text("去开启")
            }
            TextButton(
                onClick = onRefreshAccessibilityClick,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(48.dp),
            ) {
                Text(
                    text = "↻",
                    color = BedtimeTokens.muted,
                    fontSize = 26.sp,
                )
            }
        }
    }
}

@Composable
private fun ActionCard(
    uiState: HomeUiState,
    onSleepClick: () -> Unit,
    onWakeClick: () -> Unit,
) {
    TonalCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "□",
                    color = BedtimeTokens.green,
                    fontSize = 28.sp,
                )
                Text(
                    text = "今晚打卡记录",
                    color = BedtimeTokens.text,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            HorizontalDivider(color = BedtimeTokens.divider)
            SleepSummary(record = uiState.activeRecord)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onSleepClick,
                    enabled = !uiState.sleepModeState.isActive,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BedtimeTokens.primaryButton,
                        contentColor = Color(0xFF06253A),
                        disabledContainerColor = BedtimeTokens.cardHigh,
                        disabledContentColor = BedtimeTokens.dim,
                    ),
                ) {
                    Text(
                        text = "☾ 我要睡了",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                OutlinedButton(
                    onClick = onWakeClick,
                    enabled = uiState.sleepModeState.isActive,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BedtimeTokens.outlineOrDim()),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = BedtimeTokens.green,
                        disabledContentColor = BedtimeTokens.dim,
                    ),
                ) {
                    Text(
                        text = "☼ 我起床了",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SleepSummary(record: DailySleepRecord?) {
    val goalValue = when {
        record == null || record.bedtimeCheckInMillis == null -> "待定"
        record.metGoal -> "已达标"
        else -> "未达标"
    }

    Column {
        TableRow("睡前", formatTime(record?.bedtimeCheckInMillis))
        TableRow("晨起", formatTime(record?.wakeUpCheckInMillis))
        TableRow("时长", formatDurationCompact(record?.sleepDurationMinutes))
        TableRow("是否达标", goalValue, highlight = record?.metGoal)
    }
}

@Composable
private fun TableRow(
    label: String,
    value: String,
    highlight: Boolean? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .border(BorderStroke(0.dp, Color.Transparent))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = BedtimeTokens.muted,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = value,
            color = when (highlight) {
                true -> BedtimeTokens.green
                false -> BedtimeTokens.red
                null -> BedtimeTokens.text
            },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
    }
    HorizontalDivider(color = BedtimeTokens.divider)
}

@Composable
private fun QuickStatsRow(uiState: HomeUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MiniStatCard(
            label = "睡眠质量",
            value = sleepQualityGrade(uiState.activeRecord),
            caption = if (uiState.activeRecord?.sleepDurationMinutes == null) "今晚" else "本次",
            modifier = Modifier.weight(1f),
        )
        MiniStatCard(
            label = "目标表现",
            value = if (uiState.activeRecord?.metGoal == true) "达标" else "待定",
            caption = uiState.targetBedtime.displayText(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SleepQualityStandardCard() {
    TonalCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "睡眠质量评测标准",
                color = BedtimeTokens.text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            QualityRuleRow("A+", "7-9h 且早睡达标")
            QualityRuleRow("A", "7-9h，睡眠时长充足")
            QualityRuleRow("B", "6-7h，基本恢复")
            QualityRuleRow("C", "少于 6h 或记录不完整")
        }
    }
}

@Composable
private fun QualityRuleRow(
    grade: String,
    description: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            color = if (grade == "A+" || grade == "A") {
                BedtimeTokens.greenContainer
            } else {
                BedtimeTokens.cardHigh
            },
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, BedtimeTokens.border),
        ) {
            Text(
                text = grade,
                color = if (grade == "A+" || grade == "A") BedtimeTokens.green else BedtimeTokens.muted,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .width(42.dp)
                    .padding(vertical = 6.dp),
                textAlign = TextAlign.Center,
            )
        }
        Text(
            text = description,
            color = BedtimeTokens.muted,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun MiniStatCard(
    label: String,
    value: String,
    caption: String,
    modifier: Modifier = Modifier,
) {
    TonalCard(modifier = modifier.height(132.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                color = BedtimeTokens.text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = if (value == "达标" || value == "A+") BedtimeTokens.green else BedtimeTokens.text,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = caption,
                    color = BedtimeTokens.muted,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun SupplementActionCard(
    onSupplementClick: () -> Unit,
) {
    TonalCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = "补充打卡",
                    color = BedtimeTokens.text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "漏记时可手动补录睡眠日、入睡和起床时间",
                    color = BedtimeTokens.muted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Button(
                onClick = onSupplementClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BedtimeTokens.primaryButton,
                    contentColor = Color(0xFF06253A),
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            ) {
                Text("补录")
            }
        }
    }
}

@Composable
private fun StatsSummary(uiState: HomeUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SummaryStatCard(
            label = "连续打卡",
            value = uiState.currentStreak.toString(),
            suffix = "天",
            valueColor = BedtimeTokens.primary,
            modifier = Modifier.weight(1f),
        )
        SummaryStatCard(
            label = "累计记录",
            value = uiState.records.size.toString(),
            suffix = "条",
            valueColor = BedtimeTokens.text,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SummaryStatCard(
    label: String,
    value: String,
    suffix: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    TonalCard(modifier = modifier.height(110.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = label,
                color = BedtimeTokens.muted,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = valueColor,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = suffix,
                    color = BedtimeTokens.muted,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun CalendarCard(
    title: String,
    days: List<HistoryDay>,
) {
    TonalCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "打卡日历",
                    color = BedtimeTokens.text,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegendDot(color = BedtimeTokens.greenContainer, text = "达标")
                    LegendDot(color = BedtimeTokens.redContainer, text = "未达标")
                }
            }
            Text(
                text = title.ifBlank { "本月" },
                color = BedtimeTokens.muted,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                listOf("一", "二", "三", "四", "五", "六", "日").forEach {
                    Text(
                        text = it,
                        modifier = Modifier.weight(1f),
                        color = BedtimeTokens.dim,
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(((days.size / 7).coerceAtLeast(5) * 46).dp),
                userScrollEnabled = false,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                items(days) { day ->
                    CalendarDay(day = day)
                }
            }
            Text(
                text = "历史记录长期保存在本地数据库，可在最近历史中查看和删除。",
                color = BedtimeTokens.dim,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun CalendarDay(day: HistoryDay) {
    if (day.dayOfMonth == 0) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp),
        )
        return
    }

    val dotColor = when {
        day.record?.metGoal == true -> BedtimeTokens.greenContainer
        day.record?.bedtimeCheckInMillis != null -> BedtimeTokens.redContainer
        else -> BedtimeTokens.emptyDot
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
        ) {
            Text(
                text = day.dayOfMonth.toString(),
                color = BedtimeTokens.muted,
                style = MaterialTheme.typography.labelMedium,
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
        }
    }
}

@Composable
private fun LegendDot(color: Color, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = BedtimeTokens.muted,
        )
    }
}

@Composable
private fun RecentRecordsCard(
    records: List<DailySleepRecord>,
    onDeleteClick: (DailySleepRecord) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "最近历史",
            color = BedtimeTokens.text,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        if (records.isEmpty()) {
            TonalCard {
                Text(
                    text = "还没有打卡记录",
                    color = BedtimeTokens.muted,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(18.dp),
                )
            }
        } else {
            records.forEach { record ->
                RecentRecordRow(
                    record = record,
                    onDeleteClick = { onDeleteClick(record) },
                )
            }
        }
    }
}

@Composable
private fun RecentRecordRow(
    record: DailySleepRecord,
    onDeleteClick: () -> Unit,
) {
    TonalCard(color = BedtimeTokens.cardLow) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    text = formatDate(record.date),
                    color = BedtimeTokens.text,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "入睡: ${formatTime(record.bedtimeCheckInMillis)}   时长: ${formatDurationCompact(record.sleepDurationMinutes)}",
                    color = BedtimeTokens.muted,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
            StatusChip(metGoal = record.metGoal)
            TextButton(
                onClick = onDeleteClick,
                contentPadding = PaddingValues(horizontal = 6.dp),
                modifier = Modifier.height(48.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = BedtimeTokens.red,
                ),
            ) {
                Text("删除")
            }
        }
    }
}

@Composable
private fun StatusChip(metGoal: Boolean) {
    val color = if (metGoal) BedtimeTokens.greenContainer else BedtimeTokens.redContainer
    val text = if (metGoal) "已达标" else "未达标"
    val textColor = if (metGoal) BedtimeTokens.green else BedtimeTokens.red

    Surface(
        color = color,
        shape = RoundedCornerShape(6.dp),
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun BottomTabs(
    selected: HomeSection,
    onSelect: (HomeSection) -> Unit,
) {
    Surface(
        modifier = Modifier.navigationBarsPadding(),
        color = BedtimeTokens.card,
        border = BorderStroke(1.dp, BedtimeTokens.border),
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
                .padding(horizontal = 42.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomTabItem(
                selected = selected == HomeSection.Tonight,
                icon = "☾",
                label = "今晚",
                onClick = { onSelect(HomeSection.Tonight) },
            )
            BottomTabItem(
                selected = selected == HomeSection.Records,
                icon = "↺",
                label = "记录",
                onClick = { onSelect(HomeSection.Records) },
            )
        }
    }
}

@Composable
private fun BottomTabItem(
    selected: Boolean,
    icon: String,
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = if (selected) BedtimeTokens.navActive else Color.Transparent,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .width(112.dp)
            .height(72.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically),
        ) {
            Box(
                modifier = Modifier.height(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = icon,
                    color = if (selected) BedtimeTokens.green else BedtimeTokens.muted,
                    fontSize = 24.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center,
                )
            }
            Text(
                text = label,
                color = if (selected) BedtimeTokens.green else BedtimeTokens.muted,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun TonalCard(
    modifier: Modifier = Modifier,
    color: Color = BedtimeTokens.card,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, BedtimeTokens.border),
        content = content,
    )
}

@Composable
private fun MoonGlyph(size: Int) {
    Text(
        text = "☾",
        color = BedtimeTokens.primary,
        fontSize = size.sp,
        lineHeight = size.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun BarGlyph() {
    Row(
        modifier = Modifier.size(width = 36.dp, height = 34.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
    ) {
        listOf(18.dp, 28.dp, 12.dp).forEach { barHeight ->
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(barHeight)
                    .background(BedtimeTokens.muted),
            )
        }
    }
}

@Composable
private fun DeleteRecordDialog(
    record: DailySleepRecord,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BedtimeTokens.cardHigh,
        titleContentColor = BedtimeTokens.text,
        textContentColor = BedtimeTokens.muted,
        title = {
            Text("删除这条记录？")
        },
        text = {
            Text("将删除 ${formatDate(record.date)} 的睡前打卡、晨起打卡和连续天数记录。此操作不可撤销。")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = BedtimeTokens.red,
                ),
            ) {
                Text("删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun SupplementRecordDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalTime, LocalTime) -> Unit,
) {
    var dateText by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var bedtimeText by rememberSaveable { mutableStateOf("23:00") }
    var wakeTimeText by rememberSaveable { mutableStateOf("07:00") }
    var attemptedSave by rememberSaveable { mutableStateOf(false) }

    val parsedDate = parseDateOrNull(dateText)
    val parsedBedtime = parseTimeOrNull(bedtimeText)
    val parsedWakeTime = parseTimeOrNull(wakeTimeText)
    val hasError = parsedDate == null || parsedBedtime == null || parsedWakeTime == null

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BedtimeTokens.cardHigh,
        titleContentColor = BedtimeTokens.text,
        textContentColor = BedtimeTokens.muted,
        title = {
            Text("补充打卡")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "用于漏记时补录。保存后会覆盖同一睡眠日已有记录，并重算连续早睡天数。",
                    color = BedtimeTokens.muted,
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it.trim() },
                    label = { Text("睡眠日期") },
                    placeholder = { Text("2026-05-21") },
                    isError = attemptedSave && parsedDate == null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    supportingText = {
                        if (attemptedSave && parsedDate == null) {
                            Text("格式应为 yyyy-MM-dd")
                        }
                    },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = bedtimeText,
                        onValueChange = { bedtimeText = it.trim() },
                        label = { Text("入睡") },
                        placeholder = { Text("23:00") },
                        isError = attemptedSave && parsedBedtime == null,
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        supportingText = {
                            if (attemptedSave && parsedBedtime == null) {
                                Text("HH:mm")
                            }
                        },
                    )
                    OutlinedTextField(
                        value = wakeTimeText,
                        onValueChange = { wakeTimeText = it.trim() },
                        label = { Text("起床") },
                        placeholder = { Text("07:00") },
                        isError = attemptedSave && parsedWakeTime == null,
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        supportingText = {
                            if (attemptedSave && parsedWakeTime == null) {
                                Text("HH:mm")
                            }
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    attemptedSave = true
                    if (!hasError) {
                        onConfirm(parsedDate!!, parsedBedtime!!, parsedWakeTime!!)
                    }
                },
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

private fun BedtimeTokens.outlineOrDim(): Color = border

private fun sleepQualityGrade(record: DailySleepRecord?): String {
    val minutes = record?.sleepDurationMinutes ?: return "待完成"
    return when {
        minutes in (7 * 60)..(9 * 60) && record.metGoal -> "A+"
        minutes in (7 * 60)..(9 * 60) -> "A"
        minutes >= 6 * 60 -> "B"
        else -> "C"
    }
}

private fun parseDateOrNull(value: String): LocalDate? {
    return runCatching {
        LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
    }.getOrNull()
}

private fun parseTimeOrNull(value: String): LocalTime? {
    return runCatching {
        LocalTime.parse(value, DateTimeFormatter.ofPattern("H:mm"))
    }.getOrNull()
}

private fun formatTime(millis: Long?): String {
    if (millis == null) return "--:--"
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm"))
}

private fun formatDurationCompact(minutes: Long?): String {
    if (minutes == null) return "0h 0m"
    val hours = minutes / 60
    val mins = minutes % 60
    return "${hours}h ${mins}m"
}

private fun formatDate(date: String): String {
    return runCatching {
        LocalDate.parse(date).format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))
    }.getOrDefault(date)
}
