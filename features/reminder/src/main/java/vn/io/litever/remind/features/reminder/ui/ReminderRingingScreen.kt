package vn.io.litever.remind.features.reminder.ui

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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    modifier: Modifier = Modifier,
    viewModel: ReminderRingingViewModel = hiltViewModel()
) {
    val is24HourFormat by viewModel.is24HourFormat.collectAsState()
    val reminder by viewModel.reminder.collectAsState()
    val autoSilenceCountdown by viewModel.autoSilenceCountdown.collectAsState()

    ReminderRingingScreen(
        reminder = reminder,
        is24HourFormat = is24HourFormat,
        autoSilenceCountdown = autoSilenceCountdown,
        onDismiss = {
            viewModel.dismissReminder()
            onFinish()
        },
        onSnooze = {
            viewModel.snoozeReminder()
            onFinish()
        },
        modifier = modifier
    )
}

@Composable
fun ReminderRingingScreen(
    reminder: Reminder?,
    is24HourFormat: Boolean,
    autoSilenceCountdown: Int? = null,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
    modifier: Modifier = Modifier
) {
    var remainingSnoozeSeconds by remember { mutableLongStateOf(0L) }

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

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())
    val currentTime = LocalDateTime.now()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
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
                modifier = Modifier.padding(top = 100.dp)
            ) {
                val (timeStr, amPm) = TimeFormatUtils.formatTimeParts(currentTime.toLocalTime(), is24HourFormat)
                Row(verticalAlignment = Alignment.Bottom) {
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
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                if (!reminder?.label.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = reminder.label,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                if (reminder?.isMissed == true) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.missed_alarm_title),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                } else if (reminder != null && reminder.snoozeNextTriggerTime != null) {
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
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                } else if (autoSilenceCountdown != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    val minutes = autoSilenceCountdown / 60
                    val seconds = autoSilenceCountdown % 60
                    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                    Text(
                        text = stringResource(R.string.auto_silence_countdown, formattedTime),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            // Middle: Icon or Pulse Effect
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Bottom: Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val isNotMissedNorSnoozing = reminder?.isMissed != true && reminder?.snoozeNextTriggerTime == null
                if (isNotMissedNorSnoozing && (reminder == null || (reminder.snoozeEnabled && reminder.currentSnoozeCount < reminder.snoozeRepeatCount))) {
                    val remainingSnoozes = if (reminder != null) reminder.snoozeRepeatCount - reminder.currentSnoozeCount else 0
                    val snoozeText = if (reminder != null && remainingSnoozes > 0) {
                        stringResource(R.string.snooze_limit_format, remainingSnoozes)
                    } else {
                        stringResource(R.string.snooze)
                    }

                    OutlinedButton(
                        onClick = onSnooze,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = snoozeText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text(
                        text = stringResource(R.string.dismiss),
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
            reminder = Reminder(
                id = 1,
                time = LocalTime.of(7, 30),
                label = "Wake up!",
                isEnabled = true,
                snoozeEnabled = true,
                snoozeRepeatCount = 3,
                currentSnoozeCount = 1
            ),
            is24HourFormat = false,
            onDismiss = {},
            onSnooze = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReminderRingingScreenNoSnoozePreview() {
    ReMindTheme(darkTheme = true) {
        ReminderRingingScreen(
            reminder = Reminder(
                id = 2,
                time = LocalTime.of(8, 0),
                label = "Important Meeting",
                isEnabled = true,
                snoozeEnabled = true,
                snoozeRepeatCount = 3,
                currentSnoozeCount = 3 // Limit reached
            ),
            is24HourFormat = true,
            onDismiss = {},
            onSnooze = {}
        )
    }
}
