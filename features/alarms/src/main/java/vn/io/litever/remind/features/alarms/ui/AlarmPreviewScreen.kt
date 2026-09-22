package vn.io.litever.remind.features.alarms.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.button.LvButtonType
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.features.alarms.R
import vn.io.litever.remind.features.alarms.viewmodel.AlarmPreviewViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AlarmPreviewRoute(
    alarmId: Long,
    onExit: () -> Unit,
    onStartMissionPreview: (Long) -> Unit,
    navController: androidx.navigation.NavController,
    modifier: Modifier = Modifier,
    viewModel: AlarmPreviewViewModel = hiltViewModel()
) {
    val alarm by viewModel.alarm.collectAsState()
    val is24HourFormat by viewModel.is24HourFormat.collectAsState()
    val autoSilenceCountdown by viewModel.autoSilenceCountdown.collectAsState()

    val missionResult by navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<String?>("mission_result", null)
        ?.collectAsState() ?: remember { mutableStateOf(null) }

    LaunchedEffect(missionResult) {
        when (missionResult) {
            "success" -> {
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("mission_result")
                onExit()
            }
            "abandoned" -> {
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("mission_result")
                // Do not exit, stay on preview screen so the alarm resumes ringing
            }
        }
    }

    AlarmPreviewContent(
        onExit = onExit,
        onStartMission = { 
            viewModel.startMissionPreview()
            onStartMissionPreview(alarmId) 
        },
        modifier = modifier,
        alarm = alarm,
        is24HourFormat = is24HourFormat,
        autoSilenceCountdown = autoSilenceCountdown
    )
}

@Composable
fun AlarmPreviewContent(
    onExit: () -> Unit,
    onStartMission: () -> Unit,
    modifier: Modifier = Modifier,
    alarm: Alarm? = null,
    is24HourFormat: Boolean = false,
    autoSilenceCountdown: Int? = null
) {
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    
    if (alarm == null) {
        Box(modifier = Modifier.fillMaxSize().background(LiteverTheme.colors.background))
        return
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            kotlinx.coroutines.delay(1000L)
        }
    }

    // In preview mode, back button always exits
    BackHandler { 
        onExit()
    }

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
        // Exit Preview Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(LiteverTheme.spacing.medium),
            contentAlignment = Alignment.TopEnd
        ) {
            LvButton(
                onClick = onExit,
                type = LvButtonType.Outlined,
                semantic = LvSemantic.Secondary,
                modifier = Modifier.wrapContentSize()
            ) {
                Text(text = stringResource(R.string.action_exit_preview))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(LiteverTheme.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                val (timeStr, amPm) = TimeFormatUtils.formatTimeParts(currentTime.toLocalTime(), is24HourFormat)
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = timeStr,
                        style = LiteverTheme.typography.displayLarge.copy(
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = LiteverTheme.colors.primary
                    )
                    if (amPm != null) {
                        Text(
                            text = amPm.uppercase(Locale.getDefault()),
                            style = LiteverTheme.typography.displayLarge.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            ),
                            color = LiteverTheme.colors.primary,
                        )
                    }
                }
                Text(
                    text = currentTime.format(dateFormatter).replaceFirstChar { it.uppercase() },
                    style = LiteverTheme.typography.titleLarge,
                    color = LiteverTheme.colors.secondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(LiteverTheme.spacing.small))
                Surface(
                    color = LiteverTheme.colors.tertiaryContainer,
                    shape = CircleShape
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(
                            horizontal = LiteverTheme.spacing.smallMedium,
                            vertical = LiteverTheme.spacing.extraSmall
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = LiteverTheme.colors.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(LiteverTheme.spacing.small))
                        Text(
                            text = stringResource(
                                R.string.scheduled_time_format,
                                TimeFormatUtils.formatTime(alarm.time, is24HourFormat)
                            ),
                            style = LiteverTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                            color = LiteverTheme.colors.onTertiaryContainer
                        )
                    }
                }

                if (alarm.label.isNotBlank()) {
                    Spacer(modifier = Modifier.height(LiteverTheme.spacing.medium))
                    Text(
                        text = alarm.label,
                        style = LiteverTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = LiteverTheme.spacing.medium),
                        textAlign = TextAlign.Center
                    )
                }

                if (alarm.message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(LiteverTheme.spacing.small))
                    Text(
                        text = alarm.message,
                        style = LiteverTheme.typography.bodyMedium,
                        color = LiteverTheme.colors.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = LiteverTheme.spacing.extraLarge),
                        textAlign = TextAlign.Center
                    )
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = autoSilenceCountdown != null,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(LiteverTheme.spacing.medium))
                        val safeCountdown = autoSilenceCountdown ?: 0
                        val minutes = safeCountdown / 60
                        val seconds = safeCountdown % 60
                        val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                        Text(
                            text = stringResource(R.string.auto_silence_countdown, formattedTime),
                            style = LiteverTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = LiteverTheme.colors.error,
                            modifier = Modifier
                                .background(
                                    color = LiteverTheme.colors.errorContainer.copy(alpha = 0.3f),
                                    shape = LiteverTheme.shapes.medium
                                )
                                .border(
                                    width = 1.dp,
                                    color = LiteverTheme.colors.error.copy(alpha = 0.1f),
                                    shape = LiteverTheme.shapes.medium
                                )
                                .padding(horizontal = LiteverTheme.spacing.medium, vertical = LiteverTheme.spacing.small)
                        )
                    }
                }
            }

            // Bottom: Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = LiteverTheme.spacing.medium)
                    .graphicsLayer {
                        translationY = bottomOffset.toPx()
                        alpha = bottomAlpha
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
            ) {
                LvButton(
                    onClick = {
                        val hasMission = (alarm.missions.isNotEmpty())
                        if (hasMission) {
                            onStartMission()
                        } else {
                            onExit()
                        }
                    },
                    shape = LiteverTheme.shapes.large,
                    type = LvButtonType.Filled,
                    semantic = LvSemantic.Primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .offset { androidx.compose.ui.unit.IntOffset(x = shakeOffset.dp.roundToPx(), y = 0) }
                ) {
                    val hasMission = (alarm.missions.isNotEmpty())
                    val dismissText = if (hasMission) {
                        stringResource(vn.io.litever.remind.core.designsystem.R.string.mission_start)
                    } else {
                        stringResource(vn.io.litever.remind.core.designsystem.R.string.dismiss)
                    }
                    Text(
                        text = dismissText,
                        style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AlarmPreviewContentPreview() {
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme {
        AlarmPreviewContent(
            onExit = {},
            onStartMission = {},
            alarm = Alarm(
                id = 1,
                time = java.time.LocalTime.of(7, 30),
                label = "Wake up!",
                message = "Have a great day ahead!",
                isEnabled = true,
                snoozeEnabled = true,
                snoozeRepeatCount = 3,
                currentSnoozeCount = 1
            ),
            is24HourFormat = false,
            autoSilenceCountdown = 60
        )
    }
}
