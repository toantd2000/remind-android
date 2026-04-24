package vn.io.litever.remind.features.reminder.ui

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
import vn.io.litever.remind.features.reminder.R
import vn.io.litever.remind.features.reminder.viewmodel.ReminderRingingViewModel
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.Reminder

@Composable
fun ReminderRingingRoute(
    reminderId: Long,
    onFinish: () -> Unit,
    onStartMission: (Long) -> Unit,
    onDismissSuccess: (Long) -> Unit,
    navController: androidx.navigation.NavController,
    modifier: Modifier = Modifier,
    viewModel: ReminderRingingViewModel = hiltViewModel()
) {
    val is24HourFormat by viewModel.is24HourFormat.collectAsState()
    val reminder by viewModel.reminder.collectAsState()
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
                viewModel.dismissReminder()
                onDismissSuccess(reminderId)
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("mission_result")
            }
        }
    }

    LaunchedEffect(Unit) {
        // Stop sound if we are returning from mission success (handled by startMission earlier)
    }

    ReminderRingingScreen(
        onDismiss = {
            viewModel.dismissReminder()
            onDismissSuccess(reminderId)
        },
        onSnooze = {
            viewModel.snoozeReminder()
        },
        onStartMission = {
            viewModel.startMission()
            onStartMission(reminderId)
        },
        modifier = modifier,
        reminder = reminder,
        is24HourFormat = is24HourFormat,
        autoSilenceCountdown = autoSilenceCountdown
    )
}

@Composable
fun ReminderRingingScreen(
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
    onStartMission: () -> Unit,
    modifier: Modifier = Modifier,
    reminder: Reminder? = null,
    is24HourFormat: Boolean = false,
    autoSilenceCountdown: Int? = null,
) {
    var remainingSnoozeSeconds by remember { mutableLongStateOf(0L) }

    // Intercept back button to prevent escaping the ringing/locking screen
    BackHandler { }

    LaunchedEffect(reminder) {
        if (reminder != null) {
            val triggerTime = reminder.snoozeNextTriggerTime
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

    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())
    val currentTime = LocalDateTime.now()

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
                val displayTime = reminder?.time ?: currentTime.toLocalTime()
                val (timeStr, amPm) = TimeFormatUtils.formatTimeParts(displayTime, is24HourFormat)
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
                            text = " $amPm",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 16.dp)
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

                if (!reminder?.label.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = reminder.label,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                if (!reminder?.message.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = reminder.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                        textAlign = TextAlign.Center
                    )
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = reminder != null && reminder.snoozeNextTriggerTime != null,
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
                    visible = (reminder == null || reminder.snoozeNextTriggerTime == null) && autoSilenceCountdown != null,
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
                val isNotSnoozing = reminder?.snoozeNextTriggerTime == null
                
                androidx.compose.animation.AnimatedVisibility(
                    visible = isNotSnoozing && (reminder == null || (reminder.snoozeEnabled && reminder.currentSnoozeCount < reminder.snoozeRepeatCount)),
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
                ) {
                    val remainingSnoozes = if (reminder != null) reminder.snoozeRepeatCount - reminder.currentSnoozeCount else 0
                    val snoozeText = if (reminder != null && remainingSnoozes > 0) {
                        stringResource(vn.io.litever.remind.features.reminder.R.string.snooze_limit_format, remainingSnoozes)
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
                        val hasMission = (reminder?.missions?.isNotEmpty() == true)
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
                    val hasMission = (reminder?.missions?.isNotEmpty() == true)
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
fun ReminderRingingScreenPreview() {
    ReMindTheme {
        ReminderRingingScreen(
            onDismiss = {},
            onSnooze = {},
            onStartMission = {},
            reminder = Reminder(
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
fun ReminderRingingScreenNoSnoozePreview() {
    ReMindTheme(darkTheme = true) {
        ReminderRingingScreen(
            onDismiss = {},
            onSnooze = {},
            onStartMission = {},
            reminder = Reminder(
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
