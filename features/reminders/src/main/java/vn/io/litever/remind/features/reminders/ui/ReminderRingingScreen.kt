package vn.io.litever.remind.features.reminders.ui

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.delay
import vn.io.litever.brand.tokens.LiteverTokens
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderAction
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.model.UrgencyLevel
import vn.io.litever.remind.features.reminders.R
import vn.io.litever.remind.features.reminders.ui.components.ReminderCardFormatter
import vn.io.litever.remind.features.reminders.viewmodel.ReminderRingingUiState
import vn.io.litever.remind.features.reminders.viewmodel.ReminderRingingViewModel
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import vn.io.litever.remind.core.model.RepeatMode as ReminderRepeatMode

@Composable
fun ReminderRingingRoute(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReminderRingingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Intercept back press
    BackHandler {
        // Do nothing to prevent accidental dismissal during lock-screen alert
    }

    if (uiState.reminder == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    ReminderRingingScreen(
        uiState = uiState,
        onSnoozeClick = { mins -> viewModel.snooze(mins, onFinish) },
        onDismissClick = { viewModel.dismiss(onFinish) },
        modifier = modifier
    )
}

@Composable
fun ReminderRingingScreen(
    uiState: ReminderRingingUiState,
    onSnoozeClick: (Int) -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reminder = uiState.reminder ?: return
    val isInspectionMode = LocalInspectionMode.current
    val currentLocale = LocalLocale.current.platformLocale
    val isHardUrgency = reminder.action.urgencyLevel == UrgencyLevel.HARD

    var isVisible by remember { mutableStateOf(isInspectionMode) }
    var currentDateTime by remember { mutableStateOf(LocalDateTime.now()) }
    var showQuickSnoozeOptions by remember { mutableStateOf(false) }
    var showCustomSnoozeDialog by remember { mutableStateOf(false) }
    var customMinutesText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        isVisible = true
        while (true) {
            currentDateTime = LocalDateTime.now()
            delay(1000L)
        }
    }

    // Ambient breathing glow & periodic burst shake aligned with AlarmRingingScreen
    val ringingTransition = rememberInfiniteTransition(label = "reminderRingingTransition")
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
        targetValue = 0.26f,
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

    val activeGlowAlpha = if (uiState.isRinging && !isInspectionMode) pulseAlpha else 0.18f
    val activeGlowScale = if (uiState.isRinging && !isInspectionMode) pulseScale else 0.96f
    val activeShakeOffset = if (uiState.isRinging && !isInspectionMode) burstShakeOffset else 0f

    val topAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "topAlpha"
    )
    val topOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else (-28).dp,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "topOffset"
    )

    val bottomAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 700, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "bottomAlpha"
    )
    val bottomOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 28.dp,
        animationSpec = tween(durationMillis = 700, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "bottomOffset"
    )

    val glowColor = if (isHardUrgency) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

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
                // Top Section: Compact contextual time & date strip
                RingingTimeContextStrip(
                    currentDateTime = currentDateTime,
                    is24HourFormat = uiState.is24HourFormat,
                    modifier = Modifier
                        .padding(top = MaterialTheme.spacing.medium)
                        .graphicsLayer {
                            translationY = topOffset.toPx()
                            alpha = topAlpha
                        }
                )

                // Center Hero: Reminder Title, Notes, Urgency & Schedule as primary focal point
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .graphicsLayer {
                            translationY = topOffset.toPx()
                            alpha = topAlpha
                        },
                    contentAlignment = Alignment.Center
                ) {
                    RingingReminderContent(
                        reminder = reminder,
                        is24HourFormat = uiState.is24HourFormat,
                        autoSilenceCountdown = uiState.autoSilenceCountdown,
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    )
                }

                // Bottom Section: Ergonomic Snooze & Dismiss Action Buttons
                RingingBottomActions(
                    reminder = reminder,
                    showQuickSnoozeOptions = showQuickSnoozeOptions,
                    dismissShakeOffsetProvider = { activeShakeOffset },
                    onToggleQuickSnoozeOptions = { showQuickSnoozeOptions = !showQuickSnoozeOptions },
                    onSnoozeClick = onSnoozeClick,
                    onOpenCustomSnooze = {
                        customMinutesText = ""
                        showCustomSnoozeDialog = true
                    },
                    onDismissClick = onDismissClick,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = MaterialTheme.spacing.large)
                        .graphicsLayer {
                            translationY = bottomOffset.toPx()
                            alpha = bottomAlpha
                        }
                )
            }
        }

        if (showCustomSnoozeDialog) {
            CustomSnoozeDialog(
                minutesText = customMinutesText,
                onMinutesChange = { input ->
                    customMinutesText = input.filter { it.isDigit() }.take(4)
                },
                onDismiss = { showCustomSnoozeDialog = false },
                onConfirm = { minutes ->
                    showCustomSnoozeDialog = false
                    onSnoozeClick(minutes)
                }
            )
        }
    }
}

@Composable
private fun RingingTimeContextStrip(
    currentDateTime: LocalDateTime,
    is24HourFormat: Boolean,
    modifier: Modifier = Modifier
) {
    val currentLocale = LocalLocale.current.platformLocale
    val dateFormatter = remember(currentLocale) {
        DateTimeFormatter.ofPattern("EEE, d MMM", currentLocale)
    }
    val formattedTime = TimeFormatUtils.formatTime(currentDateTime.toLocalTime(), is24HourFormat)
        .uppercase(currentLocale)
    val formattedDate = currentDateTime.format(dateFormatter)
        .replaceFirstChar { it.uppercase(currentLocale) }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = CircleShape
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.medium,
                vertical = MaterialTheme.spacing.small
            )
        ) {
            Icon(
                imageVector = ReMindIcons.AccessTime,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            Text(
                text = "$formattedTime • $formattedDate",
                style = LiteverTokens.typography.labelLargeEmphasized.copy(
                    fontFeatureSettings = "tnum"
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RingingReminderContent(
    reminder: Reminder,
    is24HourFormat: Boolean,
    autoSilenceCountdown: Int?,
    modifier: Modifier = Modifier
) {
    val currentLocale = LocalLocale.current.platformLocale
    val isHardUrgency = reminder.action.urgencyLevel == UrgencyLevel.HARD
    val recurrenceText = ReminderCardFormatter.formatRecurrenceSchedule(reminder.timeTrigger)
    val timeDetailText = when (reminder.triggerType) {
        ReminderTriggerType.INTERVAL -> ReminderCardFormatter.formatIntervalFrequency(
            reminder.timeTrigger?.intervalMinutes
        )
        ReminderTriggerType.FIXED_TIME -> ReminderCardFormatter.formatFixedTimes(
            reminder.timeTrigger?.time ?: emptyList(),
            is24HourFormat
        )
    }
    val scheduleSummary = listOf(recurrenceText, timeDetailText)
        .filter { it.isNotBlank() }
        .joinToString(" • ")

    val badgeContainerColor = if (isHardUrgency) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val badgeIconColor = if (isHardUrgency) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Column(
        modifier = modifier.padding(vertical = MaterialTheme.spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Hero Reminder Icon Badge
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(badgeContainerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isHardUrgency) {
                    ReMindIcons.NotificationImportant
                } else {
                    ReMindIcons.NotificationsActive
                },
                contentDescription = null,
                tint = badgeIconColor,
                modifier = Modifier.size(32.dp)
            )
        }

        if (reminder.title.isNotBlank()) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
            Text(
                text = reminder.title,
                style = LiteverTokens.typography.headlineLargeEmphasized,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.small),
                textAlign = TextAlign.Center
            )
        }

        if (reminder.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = reminder.notes,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = MaterialTheme.spacing.medium,
                            vertical = MaterialTheme.spacing.smallMedium
                        )
                )
            }
        }

        val hasMetaPills = scheduleSummary.isNotBlank() ||
            isHardUrgency ||
            (autoSilenceCountdown != null && autoSilenceCountdown > 0)

        if (hasMetaPills) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(
                    MaterialTheme.spacing.small,
                    Alignment.CenterHorizontally
                ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (scheduleSummary.isNotBlank()) {
                    RingingSchedulePill(
                        icon = if (reminder.triggerType == ReminderTriggerType.INTERVAL) {
                            ReMindIcons.Timer
                        } else {
                            ReMindIcons.NotificationsActive
                        },
                        text = scheduleSummary,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                if (isHardUrgency) {
                    RingingSchedulePill(
                        icon = ReMindIcons.NotificationImportant,
                        text = stringResource(R.string.urgency_hard),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                if (autoSilenceCountdown != null && autoSilenceCountdown > 0) {
                    val safeCountdown = autoSilenceCountdown.coerceAtLeast(0)
                    val minutes = safeCountdown / 60
                    val seconds = safeCountdown % 60
                    val formattedTime = String.format(currentLocale, "%02d:%02d", minutes, seconds)
                    RingingSchedulePill(
                        icon = ReMindIcons.VolumeOff,
                        text = stringResource(R.string.auto_silence_countdown_mm_ss, formattedTime),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        tabularNumbers = true
                    )
                }
            }
        }
    }
}

@Composable
private fun RingingSchedulePill(
    icon: ImageVector,
    text: String,
    containerColor: Color,
    contentColor: Color,
    tabularNumbers: Boolean = false
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

@Composable
private fun RingingBottomActions(
    reminder: Reminder,
    showQuickSnoozeOptions: Boolean,
    dismissShakeOffsetProvider: () -> Float,
    onToggleQuickSnoozeOptions: () -> Unit,
    onSnoozeClick: (Int) -> Unit,
    onOpenCustomSnooze: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (showQuickSnoozeOptions) 180f else 0f,
        label = "snoozeArrowRotation"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium)
    ) {
        if (reminder.action.snoozeEnabled) {
            if (reminder.action.canSnooze()) {
                AnimatedVisibility(
                    visible = showQuickSnoozeOptions,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        val quickOptions = listOf(5, 10, 15)
                        quickOptions.forEach { mins ->
                            FilledTonalButton(
                                onClick = { onSnoozeClick(mins) },
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.extraSmall),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Text(
                                    text = "+${mins}m",
                                    style = LiteverTokens.typography.labelLargeEmphasized,
                                    maxLines = 1
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = onOpenCustomSnooze,
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.small),
                            modifier = Modifier
                                .weight(1.4f)
                                .height(46.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.snooze_custom),
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    FilledTonalButton(
                        onClick = { onSnoozeClick(reminder.action.snoozeIntervalMinutes) },
                        shape = CircleShape,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Icon(
                            imageVector = ReMindIcons.Snooze,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                        Text(
                            text = stringResource(
                                R.string.snooze_default_format,
                                reminder.action.snoozeIntervalMinutes
                            ),
                            style = LiteverTokens.typography.titleMediumEmphasized.copy(
                                fontSize = 17.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    FilledTonalButton(
                        onClick = onToggleQuickSnoozeOptions,
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.snooze_more_options),
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                        Icon(
                            imageVector = ReMindIcons.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer { rotationZ = arrowRotation }
                        )
                    }
                }
            } else {
                FilledTonalButton(
                    onClick = {},
                    enabled = false,
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = stringResource(R.string.snooze_limit_reached),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        Button(
            onClick = onDismissClick,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .offset { IntOffset(x = dismissShakeOffsetProvider().dp.roundToPx(), y = 0) }
        ) {
            Icon(
                imageVector = ReMindIcons.Check,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            Text(
                text = stringResource(R.string.dismiss_action),
                style = LiteverTokens.typography.titleLargeEmphasized.copy(
                    fontSize = 20.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CustomSnoozeDialog(
    minutesText: String,
    onMinutesChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val parsedMinutes = minutesText.toIntOrNull() ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.snooze_custom_dialog_title),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            OutlinedTextField(
                value = minutesText,
                onValueChange = onMinutesChange,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                label = { Text(stringResource(R.string.snooze_custom_minutes_hint)) },
                placeholder = { Text(stringResource(R.string.snooze_custom_minutes_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (parsedMinutes > 0) {
                        onConfirm(parsedMinutes)
                    }
                },
                enabled = parsedMinutes > 0
            ) {
                Text(stringResource(R.string.snooze_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun ReminderRingingScreenPreview() {
    ReMindTheme {
        ReminderRingingScreen(
            uiState = ReminderRingingUiState(
                reminder = Reminder(
                    id = 1L,
                    title = "Drink Water & Stretch",
                    notes = "Take a short break, stand up and walk around for 5 minutes.",
                    triggerType = ReminderTriggerType.FIXED_TIME,
                    timeTrigger = TimeTrigger(
                        time = listOf(LocalTime.of(9, 0)),
                        repeatMode = ReminderRepeatMode.WEEKLY,
                        repeatValues = listOf(1, 2, 3, 4, 5)
                    ),
                    action = ReminderAction(
                        snoozeEnabled = true,
                        snoozeIntervalMinutes = 10
                    )
                ),
                is24HourFormat = true,
                autoSilenceCountdown = 285,
                isRinging = true
            ),
            onSnoozeClick = {},
            onDismissClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReminderRingingScreenDarkPreview() {
    ReMindTheme(darkTheme = true) {
        ReminderRingingScreen(
            uiState = ReminderRingingUiState(
                reminder = Reminder(
                    id = 2L,
                    title = "Take Heart Medication",
                    notes = "Take 1 pill after dinner with a full glass of water.",
                    triggerType = ReminderTriggerType.INTERVAL,
                    timeTrigger = TimeTrigger(
                        intervalMinutes = 120,
                        repeatMode = ReminderRepeatMode.DAILY
                    ),
                    action = ReminderAction(
                        urgencyLevel = UrgencyLevel.HARD,
                        snoozeEnabled = true,
                        snoozeIntervalMinutes = 15
                    )
                ),
                is24HourFormat = false,
                autoSilenceCountdown = null,
                isRinging = true
            ),
            onSnoozeClick = {},
            onDismissClick = {}
        )
    }
}
