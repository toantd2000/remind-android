package vn.io.litever.remind.features.alarms.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import vn.io.litever.brand.tokens.LiteverTokens
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.features.alarms.R
import vn.io.litever.remind.features.alarms.viewmodel.AlarmRingingViewModel
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun AlarmRingingRoute(
    alarmId: Long,
    onFinish: () -> Unit,
    onStartMission: (Long) -> Unit,
    navController: NavController,
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

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.setRingingScreenVisible(true)
            } else if (event == Lifecycle.Event.ON_STOP) {
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
    autoSilenceCountdown: Int? = null,
    isPreview: Boolean = false,
    onExitPreview: (() -> Unit)? = null,
) {
    val isInspectionMode = LocalInspectionMode.current
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    var remainingSnoozeSeconds by remember { mutableLongStateOf(0L) }

    if (alarm == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
        return
    }

    val isRinging = alarm.snoozeNextTriggerTime == null

    // Update current time every second
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            delay(1000L.milliseconds)
        }
    }

    // Intercept back button: exit in preview mode, block escape when ringing for real
    BackHandler {
        if (isPreview) {
            onExitPreview?.invoke()
        }
    }

    LaunchedEffect(alarm) {
        val triggerTime = alarm.snoozeNextTriggerTime
        if (triggerTime != null) {
            while (true) {
                val now = System.currentTimeMillis()
                val diff = (triggerTime - now) / 1000
                remainingSnoozeSeconds = if (diff >= 0) diff else 0
                delay(1000L.milliseconds)
            }
        }
    }

    // Ambient breathing glow & periodic burst shake only when actively ringing
    val ringingTransition = rememberInfiniteTransition(label = "ringingTransition")
    val pulseScale by ringingTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by ringingTransition.animateFloat(
        initialValue = 0.14f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    val burstShakeOffset by ringingTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2200
                0f at 0
                -3.5f at 60
                3.5f at 120
                -2.5f at 180
                2.5f at 240
                -1.5f at 300
                0f at 360
                0f at 2200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "burstShakeOffset"
    )

    val activeGlowAlpha = if (isRinging && !isInspectionMode) pulseAlpha else if (isRinging) 0.22f else 0.08f
    val activeGlowScale = if (isRinging && !isInspectionMode) pulseScale else 0.95f
    val activeShakeOffset = if (isRinging && !isInspectionMode) burstShakeOffset else 0f

    var isVisible by remember { mutableStateOf(isInspectionMode) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val topAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "topAlpha"
    )
    val topOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else (-32).dp,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "topOffset"
    )

    val bottomAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 700, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "bottomAlpha"
    )
    val bottomOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 32.dp,
        animationSpec = tween(durationMillis = 700, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "bottomOffset"
    )

    val currentLocale = LocalLocale.current.platformLocale
    val dateFormatter = remember(currentLocale) {
        DateTimeFormatter.ofPattern("EEEE, d MMMM", currentLocale)
    }
    val glowColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = activeGlowAlpha),
                                Color.Transparent
                            ),
                            center = Offset(x = size.width / 2f, y = size.height * 0.36f),
                            radius = size.minDimension * 0.72f * activeGlowScale
                        )
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = MaterialTheme.spacing.medium),
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
                    val (timeStr, amPm) = TimeFormatUtils.formatTimeParts(
                        currentTime.toLocalTime(),
                        is24HourFormat
                    )
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
                                text = amPm.uppercase(currentLocale),
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

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.smallMedium))

                    RingingStatusPill(
                        icon = ReMindIcons.NotificationsActive,
                        text = stringResource(
                            R.string.scheduled_time_format,
                            TimeFormatUtils.formatTime(alarm.time, is24HourFormat)
                        ),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )

                    if (alarm.label.isNotBlank()) {
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
                        Text(
                            text = alarm.label,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = MaterialTheme.spacing.medium),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (alarm.message.isNotBlank()) {
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                        Text(
                            text = alarm.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = MaterialTheme.spacing.large),
                            textAlign = TextAlign.Center
                        )
                    }

                    AnimatedVisibility(
                        visible = !isRinging,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        val minutes = remainingSnoozeSeconds / 60
                        val seconds = remainingSnoozeSeconds % 60
                        val formattedTime = String.format(currentLocale, "%02d:%02d", minutes, seconds)
                        Box(
                            modifier = Modifier.padding(top = MaterialTheme.spacing.medium)
                        ) {
                            RingingStatusPill(
                                icon = ReMindIcons.Snooze,
                                text = stringResource(R.string.snooze_countdown, formattedTime),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                tabularNumbers = true
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = isRinging && autoSilenceCountdown != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        val safeCountdown = autoSilenceCountdown ?: 0
                        val minutes = safeCountdown / 60
                        val seconds = safeCountdown % 60
                        val formattedTime = String.format(currentLocale, "%02d:%02d", minutes, seconds)
                        Box(
                            modifier = Modifier.padding(top = MaterialTheme.spacing.medium)
                        ) {
                            RingingStatusPill(
                                icon = ReMindIcons.VolumeOff,
                                text = stringResource(R.string.auto_silence_countdown, formattedTime),
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                tabularNumbers = true
                            )
                        }
                    }
                }

                // Bottom: Ergonomic Action Buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = MaterialTheme.spacing.large)
                        .graphicsLayer {
                            translationY = bottomOffset.toPx()
                            alpha = bottomAlpha
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium)
                ) {
                    val canSnooze = !isPreview &&
                        isRinging &&
                        alarm.snoozeEnabled &&
                        alarm.currentSnoozeCount < alarm.snoozeRepeatCount

                    AnimatedVisibility(
                        visible = canSnooze,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        val remainingSnoozes = alarm.snoozeRepeatCount - alarm.currentSnoozeCount
                        val snoozeText = if (remainingSnoozes > 0) {
                            stringResource(R.string.snooze_limit_format, remainingSnoozes)
                        } else {
                            stringResource(vn.io.litever.remind.core.designsystem.R.string.snooze)
                        }

                        FilledTonalButton(
                            onClick = onSnooze,
                            shape = CircleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Icon(
                                imageVector = ReMindIcons.Snooze,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                            Text(
                                text = snoozeText,
                                style = LiteverTokens.typography.titleMediumEmphasized.copy(
                                    fontSize = 17.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    val hasMission = alarm.missions.isNotEmpty()
                    val dismissText = if (hasMission) {
                        stringResource(vn.io.litever.remind.core.designsystem.R.string.mission_start)
                    } else {
                        stringResource(vn.io.litever.remind.core.designsystem.R.string.dismiss)
                    }

                    Button(
                        onClick = {
                            if (hasMission) {
                                onStartMission()
                            } else {
                                onDismiss()
                            }
                        },
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(62.dp)
                            .offset { IntOffset(x = activeShakeOffset.dp.roundToPx(), y = 0) }
                    ) {
                        Icon(
                            imageVector = if (hasMission) ReMindIcons.Extension else ReMindIcons.AlarmOff,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                        Text(
                            text = dismissText,
                            style = LiteverTokens.typography.titleLargeEmphasized.copy(
                                fontSize = 20.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (isPreview && onExitPreview != null) {
                Surface(
                    onClick = onExitPreview,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(MaterialTheme.spacing.medium)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.spacing.smallMedium,
                            vertical = MaterialTheme.spacing.small
                        )
                    ) {
                        Icon(
                            imageVector = ReMindIcons.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                        Text(
                            text = stringResource(R.string.action_exit_preview),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RingingStatusPill(
    icon: ImageVector,
    text: String,
    containerColor: Color,
    contentColor: Color,
    tabularNumbers: Boolean = false,
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = CircleShape
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.smallMedium,
                vertical = MaterialTheme.spacing.extraSmall + 2.dp
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            Text(
                text = text,
                style = if (tabularNumbers) {
                    LiteverTokens.typography.labelLargeEmphasized.copy(fontFeatureSettings = "tnum")
                } else {
                    MaterialTheme.typography.labelLarge
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
                message = "Have a great day ahead!",
                isEnabled = true,
                snoozeEnabled = true,
                snoozeRepeatCount = 3,
                currentSnoozeCount = 1
            ),
            is24HourFormat = false,
            autoSilenceCountdown = 58
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
                currentSnoozeCount = 3
            ),
            is24HourFormat = true
        )
    }
}
