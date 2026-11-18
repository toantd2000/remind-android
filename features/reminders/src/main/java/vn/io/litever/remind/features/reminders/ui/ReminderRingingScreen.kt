package vn.io.litever.remind.features.reminders.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.delay
import vn.io.litever.designsystem.components.LiteverCircularProgressIndicator
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.button.LvButtonType
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.components.dialog.LvAlertDialog
import vn.io.litever.designsystem.components.textfield.LvTextField
import vn.io.litever.designsystem.components.textfield.LvTextFieldType
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderAction
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.RepeatMode as ReminderRepeatMode
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.model.UrgencyLevel
import vn.io.litever.remind.features.reminders.R
import vn.io.litever.remind.features.reminders.ui.components.ReminderCardFormatter
import vn.io.litever.remind.features.reminders.viewmodel.ReminderRingingUiState
import vn.io.litever.remind.features.reminders.viewmodel.ReminderRingingViewModel
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ReminderRingingRoute(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReminderRingingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Intercept back press
    BackHandler {
        // Do nothing to prevent accidental dismissal during lock-screen alarm
    }

    if (uiState.reminder == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LiteverTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            LiteverCircularProgressIndicator(color = LiteverTheme.colors.primary)
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

    // Shake animation for dismiss and snooze buttons, aligned with AlarmRingingScreen
    val shakeTransition = rememberInfiniteTransition(label = "shake")
    val shakeOffsetState = shakeTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeOffset"
    )
    val snoozeShakeOffsetState = shakeTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "snoozeShakeOffset"
    )

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

    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(LiteverTheme.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Section: Clock & Date anchored at top, matching AlarmRingingScreen typography & colors
            RingingClockHeader(
                currentDateTime = currentDateTime,
                is24HourFormat = uiState.is24HourFormat,
                modifier = Modifier
                    .padding(top = LiteverTheme.spacing.large)
                    .graphicsLayer {
                        translationY = topOffset.toPx()
                        alpha = topAlpha
                    }
            )

            // Center Section: Reminder schedule pills, title, notes & auto-silence countdown centered in remaining space
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

            // Bottom Section: Snooze & Dismiss Action Buttons aligned with AlarmRingingScreen
            RingingBottomActions(
                reminder = reminder,
                showQuickSnoozeOptions = showQuickSnoozeOptions,
                snoozeShakeOffsetProvider = { snoozeShakeOffsetState.value },
                dismissShakeOffsetProvider = { shakeOffsetState.value },
                onToggleQuickSnoozeOptions = { showQuickSnoozeOptions = !showQuickSnoozeOptions },
                onSnoozeClick = onSnoozeClick,
                onOpenCustomSnooze = {
                    customMinutesText = ""
                    showCustomSnoozeDialog = true
                },
                onDismissClick = onDismissClick,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = LiteverTheme.spacing.medium)
                    .graphicsLayer {
                        translationY = bottomOffset.toPx()
                        alpha = bottomAlpha
                    }
            )
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
private fun RingingClockHeader(
    currentDateTime: LocalDateTime,
    is24HourFormat: Boolean,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()) }
    val (timeStr, amPm) = TimeFormatUtils.formatTimeParts(currentDateTime.toLocalTime(), is24HourFormat)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                Spacer(modifier = Modifier.width(LiteverTheme.spacing.extraSmall))
                Text(
                    text = amPm.uppercase(Locale.getDefault()),
                    style = LiteverTheme.typography.displayLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = LiteverTheme.colors.primary
                )
            }
        }

        Text(
            text = currentDateTime.format(dateFormatter).replaceFirstChar { it.uppercase() },
            style = LiteverTheme.typography.titleLarge,
            color = LiteverTheme.colors.secondary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RingingReminderContent(
    reminder: Reminder,
    is24HourFormat: Boolean,
    autoSilenceCountdown: Int?,
    modifier: Modifier = Modifier
) {
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

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (scheduleSummary.isNotBlank() || isHardUrgency) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(
                    LiteverTheme.spacing.small,
                    Alignment.CenterHorizontally
                ),
                verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.extraSmall),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (scheduleSummary.isNotBlank()) {
                    RingingSchedulePill(
                        icon = if (reminder.triggerType == ReminderTriggerType.INTERVAL) {
                            ReMindIcons.Timer
                        } else {
                            ReMindIcons.Notifications
                        },
                        text = scheduleSummary,
                        containerColor = LiteverTheme.colors.tertiaryContainer,
                        contentColor = LiteverTheme.colors.onTertiaryContainer
                    )
                }

                if (isHardUrgency) {
                    RingingSchedulePill(
                        icon = ReMindIcons.NotificationImportant,
                        text = stringResource(R.string.urgency_hard),
                        containerColor = LiteverTheme.colors.errorContainer,
                        contentColor = LiteverTheme.colors.onErrorContainer
                    )
                }
            }
        }

        if (reminder.title.isNotBlank()) {
            Spacer(modifier = Modifier.height(LiteverTheme.spacing.medium))
            Text(
                text = reminder.title,
                style = LiteverTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LiteverTheme.spacing.medium),
                textAlign = TextAlign.Center
            )
        }

        if (reminder.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(LiteverTheme.spacing.small))
            Text(
                text = reminder.notes,
                style = LiteverTheme.typography.bodyMedium,
                color = LiteverTheme.colors.onBackground.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LiteverTheme.spacing.extraLarge),
                textAlign = TextAlign.Center
            )
        }

        AnimatedVisibility(
            visible = autoSilenceCountdown != null && autoSilenceCountdown > 0,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(LiteverTheme.spacing.medium))
                val safeCountdown = (autoSilenceCountdown ?: 0).coerceAtLeast(0)
                val minutes = safeCountdown / 60
                val seconds = safeCountdown % 60
                val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

                Text(
                    text = stringResource(R.string.auto_silence_countdown_mm_ss, formattedTime),
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
                        .padding(
                            horizontal = LiteverTheme.spacing.medium,
                            vertical = LiteverTheme.spacing.small
                        )
                )
            }
        }
    }
}

@Composable
private fun RingingSchedulePill(
    icon: ImageVector,
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
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
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(LiteverTheme.spacing.small))
            Text(
                text = text,
                style = LiteverTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = contentColor,
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
    snoozeShakeOffsetProvider: () -> Float,
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
        verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
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
                        horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
                    ) {
                        val quickOptions = listOf(5, 10, 15)
                        quickOptions.forEach { mins ->
                            LvButton(
                                onClick = { onSnoozeClick(mins) },
                                type = LvButtonType.Outlined,
                                semantic = LvSemantic.Secondary,
                                shape = LiteverTheme.shapes.large,
                                contentPadding = PaddingValues(horizontal = LiteverTheme.spacing.extraSmall),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = "+${mins}m",
                                    style = LiteverTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1
                                )
                            }
                        }

                        LvButton(
                            onClick = onOpenCustomSnooze,
                            type = LvButtonType.Outlined,
                            semantic = LvSemantic.Secondary,
                            shape = LiteverTheme.shapes.large,
                            contentPadding = PaddingValues(horizontal = LiteverTheme.spacing.small),
                            modifier = Modifier
                                .weight(1.4f)
                                .height(48.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.snooze_custom),
                                style = LiteverTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = snoozeShakeOffsetProvider().dp.roundToPx(), y = 0) },
                    horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
                ) {
                    LvButton(
                        onClick = { onSnoozeClick(reminder.action.snoozeIntervalMinutes) },
                        type = LvButtonType.Outlined,
                        semantic = LvSemantic.Secondary,
                        shape = LiteverTheme.shapes.large,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.snooze_default_format,
                                reminder.action.snoozeIntervalMinutes
                            ),
                            style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    LvButton(
                        onClick = onToggleQuickSnoozeOptions,
                        type = LvButtonType.Outlined,
                        semantic = LvSemantic.Secondary,
                        shape = LiteverTheme.shapes.large,
                        contentPadding = PaddingValues(horizontal = LiteverTheme.spacing.medium),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.snooze_more_options),
                            style = LiteverTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(LiteverTheme.spacing.extraSmall))
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
                LvButton(
                    onClick = {},
                    enabled = false,
                    type = LvButtonType.Outlined,
                    semantic = LvSemantic.Secondary,
                    shape = LiteverTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = stringResource(R.string.snooze_limit_reached),
                        style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        LvButton(
            onClick = onDismissClick,
            type = LvButtonType.Filled,
            semantic = LvSemantic.Primary,
            shape = LiteverTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .offset { IntOffset(x = dismissShakeOffsetProvider().dp.roundToPx(), y = 0) }
        ) {
            Text(
                text = stringResource(R.string.dismiss_action),
                style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
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

    LvAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.snooze_custom_dialog_title),
                style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            LvTextField(
                value = minutesText,
                onValueChange = onMinutesChange,
                modifier = Modifier.fillMaxWidth(),
                type = LvTextFieldType.Outlined,
                label = stringResource(R.string.snooze_custom_minutes_hint),
                placeholder = stringResource(R.string.snooze_custom_minutes_hint),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            LvButton(
                onClick = {
                    if (parsedMinutes > 0) {
                        onConfirm(parsedMinutes)
                    }
                },
                enabled = parsedMinutes > 0,
                semantic = LvSemantic.Primary
            ) {
                Text(stringResource(R.string.snooze_action))
            }
        },
        dismissButton = {
            LvButton(
                onClick = onDismiss,
                type = LvButtonType.Outlined,
                semantic = LvSemantic.Secondary
            ) {
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
