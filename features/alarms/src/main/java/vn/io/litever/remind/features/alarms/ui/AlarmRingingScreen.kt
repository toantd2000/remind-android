package vn.io.litever.remind.features.alarms.ui

import vn.io.litever.brand.tokens.LiteverTokens
import androidx.compose.material3.MaterialTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.features.alarms.R
import vn.io.litever.remind.features.alarms.viewmodel.AlarmRingingViewModel
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun AlarmRingingRoute(
    alarmId: Long,
    onFinish: () -> Unit,
    onStartMission: (Long) -> Unit,
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
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("mission_result")
            }
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_START) {
                viewModel.setRingingScreenVisible(true)
            } else if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                viewModel.setRingingScreenVisible(false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.setRingingScreenVisible(false)
        }
    }

    AlarmRingingScreen(
        onDismiss = {
            viewModel.dismissAlarm()
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
        autoSilenceCountdown = autoSilenceCountdown
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
    autoSilenceCountdown: Int? = null
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
            kotlinx.coroutines.delay(1000L.milliseconds)
        }
    }

    // Intercept back button to prevent escaping the ringing/locking screen
    BackHandler { 
        // Do nothing to prevent escaping
    }

    LaunchedEffect(alarm) {
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
                kotlinx.coroutines.delay(1000L.milliseconds)
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(MaterialTheme.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Content: Centered in remaining space
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .graphicsLayer {
                        translationY = topOffset.toPx()
                        alpha = topAlpha
                    }
            ) {
                // Show ACTUAL current time when ringing
                val (timeStr, amPm) = TimeFormatUtils.formatTimeParts(currentTime.toLocalTime(), is24HourFormat)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = timeStr,
                        style = LiteverTokens.typography.displayLargeEmphasized.copy(
                            fontSize = 76.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.alignByBaseline()
                    )
                    if (amPm != null) {
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                        Text(
                            text = amPm.uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.alignByBaseline()
                        )
                    }
                }
                Text(
                    text = currentTime.format(dateFormatter).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = CircleShape
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.spacing.smallMedium,
                            vertical = MaterialTheme.spacing.extraSmall
                        )
                    ) {
                        Icon(
                            imageVector = ReMindIcons.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                        Text(
                            text = stringResource(
                                R.string.scheduled_time_format,
                                TimeFormatUtils.formatTime(alarm.time, is24HourFormat)
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                if (alarm.label.isNotBlank()) {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.spacing.medium),
                        textAlign = TextAlign.Center
                    )
                }

                if (alarm.message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                    Text(
                        text = alarm.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.spacing.extraLarge),
                        textAlign = TextAlign.Center
                    )
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = alarm.snoozeNextTriggerTime != null,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                        val minutes = remainingSnoozeSeconds / 60
                        val seconds = remainingSnoozeSeconds % 60
                        val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                        Text(
                            text = stringResource(R.string.snooze_countdown, formattedTime),
                            style = LiteverTokens.typography.bodyLargeEmphasized.copy(
                                fontFeatureSettings = "tnum"
                            ),
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
                                .padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small)
                        )
                    }
                }
                
                androidx.compose.animation.AnimatedVisibility(
                    visible = alarm.snoozeNextTriggerTime == null && autoSilenceCountdown != null,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                        val safeCountdown = autoSilenceCountdown ?: 0
                        val minutes = safeCountdown / 60
                        val seconds = safeCountdown % 60
                        val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                        Text(
                            text = stringResource(R.string.auto_silence_countdown, formattedTime),
                            style = LiteverTokens.typography.bodyLargeEmphasized.copy(
                                fontFeatureSettings = "tnum"
                            ),
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
                                .padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small)
                        )
                    }
                }
            }

            // Bottom: Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = MaterialTheme.spacing.medium)
                    .graphicsLayer {
                        translationY = bottomOffset.toPx()
                        alpha = bottomAlpha
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
            ) {
                val isNotSnoozing = alarm.snoozeNextTriggerTime == null
                
                androidx.compose.animation.AnimatedVisibility(
                    visible = isNotSnoozing && (alarm.snoozeEnabled && alarm.currentSnoozeCount < alarm.snoozeRepeatCount),
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
                ) {
                    val remainingSnoozes = alarm.snoozeRepeatCount - alarm.currentSnoozeCount
                    val snoozeText = if (remainingSnoozes > 0) {
                        stringResource(vn.io.litever.remind.features.alarms.R.string.snooze_limit_format, remainingSnoozes)
                    } else {
                        stringResource(vn.io.litever.remind.core.designsystem.R.string.snooze)
                    }

                    OutlinedButton(
                        onClick = onSnooze,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .offset { androidx.compose.ui.unit.IntOffset(x = snoozeShakeOffset.dp.roundToPx(), y = 0) } // Áp dụng rung nhẹ
                    ) {
                        Text(
                            text = snoozeText,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Button(
                    onClick = {
                        val hasMission = (alarm.missions.isNotEmpty())
                        if (hasMission) {
                            onStartMission()
                        } else {
                            onDismiss()
                        }
                    },
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .offset { androidx.compose.ui.unit.IntOffset(x = shakeOffset.dp.roundToPx(), y = 0) } // Áp dụng hiệu ứng rung
                ) {
                    val hasMission = (alarm.missions.isNotEmpty())
                    val dismissText = if (hasMission) {
                        stringResource(vn.io.litever.remind.core.designsystem.R.string.mission_start)
                    } else {
                        stringResource(vn.io.litever.remind.core.designsystem.R.string.dismiss)
                    }
                    Text(
                        text = dismissText,
                        style = LiteverTokens.typography.titleMediumEmphasized
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










