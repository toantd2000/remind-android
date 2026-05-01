package vn.io.litever.remind.features.alarms.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.border
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import vn.io.litever.remind.core.designsystem.components.*
import androidx.hilt.navigation.compose.hiltViewModel
import vn.io.litever.remind.features.alarms.R
import vn.io.litever.remind.features.alarms.viewmodel.AlarmRingingViewModel
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.Alarm

@Composable
fun AlarmRingingRoute(
    alarmId: Long,
    onFinish: () -> Unit,
    onStartMission: (Long) -> Unit,
    onDismissSuccess: (Long) -> Unit,
    navController: androidx.navigation.NavController,
    modifier: Modifier = Modifier,
    viewModel: AlarmRingingViewModel = hiltViewModel()
) {
    val is24HourFormat by viewModel.is24HourFormat.collectAsState()
    val alarm by viewModel.alarm.collectAsState()
    val autoSilenceCountdown by viewModel.autoSilenceCountdown.collectAsState()
    
    val missionResult by navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<String?>("mission_result", null)
        ?.collectAsState() ?: remember { mutableStateOf(null) }

    LaunchedEffect(missionResult) {
        when (missionResult) {
            "abandoned" -> {
                viewModel.onAbandonMission()
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("mission_result")
            }
            "success" -> {
                viewModel.dismissAlarm()
                onDismissSuccess(alarmId)
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("mission_result")
            }
        }
    }

    DisposableEffect(Unit) {
        viewModel.setRingingScreenVisible(true)
        onDispose {
            viewModel.setRingingScreenVisible(false)
        }
    }

    AlarmRingingScreen(
        onDismiss = {
            viewModel.dismissAlarm()
            onDismissSuccess(alarmId)
        },
        onSnooze = {
            viewModel.snoozeAlarm()
        },
        onStartMission = {
            viewModel.startMission()
            onStartMission(alarmId)
        },
        modifier = modifier,
        alarm = alarm,
        is24HourFormat = is24HourFormat,
        autoSilenceCountdown = autoSilenceCountdown
    )
}

@Composable
fun AlarmRingingScreen(
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
    onStartMission: () -> Unit,
    modifier: Modifier = Modifier,
    alarm: Alarm? = null,
    is24HourFormat: Boolean = false,
    autoSilenceCountdown: Int? = null,
) {
    AlarmRingingContent(
        onDismiss = onDismiss,
        onSnooze = onSnooze,
        onStartMission = onStartMission,
        modifier = modifier,
        alarm = alarm,
        is24HourFormat = is24HourFormat,
        autoSilenceCountdown = autoSilenceCountdown,
        isPreview = false
    )
}

@Composable
fun AlarmRingingContent(
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
    onStartMission: () -> Unit,
    modifier: Modifier = Modifier,
    alarm: Alarm? = null,
    is24HourFormat: Boolean = false,
    autoSilenceCountdown: Int? = null,
    isPreview: Boolean = false,
    onExitPreview: () -> Unit = {}
) {
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    var remainingSnoozeSeconds by remember { mutableLongStateOf(0L) }
 
    if (alarm == null) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }
 
    // Update current time every second
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            kotlinx.coroutines.delay(1000L)
        }
    }

    // Intercept back button to prevent escaping the ringing/locking screen
    // Unless in preview mode
    BackHandler { 
        if (isPreview) onExitPreview()
    }

    LaunchedEffect(alarm) {
        if (alarm != null) {
            val triggerTime = alarm.snoozeNextTriggerTime
            if (triggerTime != null) {
                while (true) {
                    val now = System.currentTimeMillis()
                    val diff = (triggerTime - now) / 1000
                    if (diff >= 0) {
                        remainingSnoozeSeconds = diff
                    } else {
                        remainingSnoozeSeconds = 0
                    }
                    kotlinx.coroutines.delay(1000L)
                }
            }
        }
    }

    // Shake animation for the dismiss button
    val shakeTransition = rememberInfiniteTransition(label = "shake")
    val shakeOffset by shakeTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeOffset"
    )

    // Gentle shake for the snooze button
    val snoozeShakeOffset by shakeTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "snoozeShakeOffset"
    )

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val topAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "topAlpha"
    )
    val topOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else (-50).dp,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "topOffset"
    )

    val bottomAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 300, easing = FastOutSlowInEasing),
        label = "bottomAlpha"
    )
    val bottomOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 50.dp,
        animationSpec = tween(durationMillis = 1000, delayMillis = 300, easing = FastOutSlowInEasing),
        label = "bottomOffset"
    )

    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Subtle gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                        )
                    )
                )
        )

        // Exit Preview Button
        if (isPreview) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                ReMindOutlinedButton(
                    onClick = onExitPreview,
                    modifier = Modifier.wrapContentSize()
                ) {
                    Text(
                        text = stringResource(R.string.action_exit_preview),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Time & Date
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 100.dp)
                    .graphicsLayer {
                        translationY = topOffset.toPx()
                        alpha = topAlpha
                    }
            ) {
                // Show ACTUAL current time when ringing
                val (timeStr, amPm) = TimeFormatUtils.formatTimeParts(currentTime.toLocalTime(), is24HourFormat)
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 96.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (amPm != null) {
                        Text(
                            text = amPm.uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
                        )
                    }
                }
                Text(
                    text = currentTime.format(dateFormatter).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                if (alarm != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                        shape = CircleShape
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(
                                    vn.io.litever.remind.features.alarms.R.string.scheduled_time_format,
                                    TimeFormatUtils.formatTime(alarm.time, is24HourFormat)
                                ),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                if (!alarm?.label.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                if (!alarm?.message.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = alarm.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                        textAlign = TextAlign.Center
                    )
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = alarm != null && alarm.snoozeNextTriggerTime != null,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        val minutes = remainingSnoozeSeconds / 60
                        val seconds = remainingSnoozeSeconds % 60
                        val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                        Text(
                            text = stringResource(R.string.snooze_countdown, formattedTime),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    shape = MaterialTheme.shapes.medium
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
                
                androidx.compose.animation.AnimatedVisibility(
                    visible = (alarm == null || alarm.snoozeNextTriggerTime == null) && autoSilenceCountdown != null,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        val safeCountdown = autoSilenceCountdown ?: 0
                        val minutes = safeCountdown / 60
                        val seconds = safeCountdown % 60
                        val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                        Text(
                        text = stringResource(R.string.auto_silence_countdown, formattedTime),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                shape = MaterialTheme.shapes.medium
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Middle: Empty space for future custom background
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            // Bottom: Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
                    .graphicsLayer {
                        translationY = bottomOffset.toPx()
                        alpha = bottomAlpha
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val isNotSnoozing = alarm?.snoozeNextTriggerTime == null
                
                androidx.compose.animation.AnimatedVisibility(
                    visible = isNotSnoozing && (alarm == null || (alarm.snoozeEnabled && alarm.currentSnoozeCount < alarm.snoozeRepeatCount)),
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
                ) {
                    val remainingSnoozes = if (alarm != null) alarm.snoozeRepeatCount - alarm.currentSnoozeCount else 0
                    val snoozeText = if (alarm != null && remainingSnoozes > 0) {
                        stringResource(vn.io.litever.remind.features.alarms.R.string.snooze_limit_format, remainingSnoozes)
                    } else {
                        stringResource(vn.io.litever.remind.core.designsystem.R.string.snooze)
                    }

                    ReMindOutlinedButton(
                        onClick = onSnooze,
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset { androidx.compose.ui.unit.IntOffset(x = snoozeShakeOffset.dp.roundToPx(), y = 0) } // Áp dụng rung nhẹ
                    ) {
                        Text(
                            text = snoozeText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                ReMindButton(
                    onClick = {
                        val hasMission = (alarm?.missions?.isNotEmpty() == true)
                        if (hasMission) {
                            onStartMission()
                        } else {
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { androidx.compose.ui.unit.IntOffset(x = shakeOffset.dp.roundToPx(), y = 0) } // Áp dụng hiệu ứng rung
                ) {
                    val hasMission = (alarm?.missions?.isNotEmpty() == true)
                    val dismissText = if (hasMission) {
                        stringResource(vn.io.litever.remind.core.designsystem.R.string.mission_start)
                    } else {
                        stringResource(vn.io.litever.remind.core.designsystem.R.string.dismiss)
                    }
                    Text(
                        text = dismissText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmRingingScreenPreview() {
    ReMindTheme {
        AlarmRingingScreen(
            onDismiss = {},
            onSnooze = {},
            onStartMission = {},
            alarm = Alarm(
                id = 1,
                time = LocalTime.of(7, 30),
                label = "Wake up!",
                isEnabled = true,
                snoozeEnabled = true,
                snoozeRepeatCount = 3,
                currentSnoozeCount = 1
            ),
            is24HourFormat = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmPreviewContentPreview() {
    ReMindTheme {
        AlarmRingingContent(
            onDismiss = {},
            onSnooze = {},
            onStartMission = {},
            alarm = Alarm(
                id = 1,
                time = LocalTime.of(7, 30),
                label = "Wake up!",
                isEnabled = true,
                snoozeEnabled = true,
                snoozeRepeatCount = 3,
                currentSnoozeCount = 1
            ),
            is24HourFormat = false,
            isPreview = true,
            onExitPreview = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmRingingScreenNoSnoozePreview() {
    ReMindTheme(darkTheme = true) {
        AlarmRingingScreen(
            onDismiss = {},
            onSnooze = {},
            onStartMission = {},
            alarm = Alarm(
                id = 2,
                time = LocalTime.of(8, 0),
                label = "Important Meeting",
                isEnabled = true,
                snoozeEnabled = true,
                snoozeRepeatCount = 3,
                currentSnoozeCount = 3 // Limit reached
            ),
            is24HourFormat = true
        )
    }
}










