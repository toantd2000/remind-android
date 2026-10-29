package vn.io.litever.remind.features.reminders.ui

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import vn.io.litever.designsystem.components.LiteverButton
import vn.io.litever.designsystem.components.LiteverOutlinedButton
import vn.io.litever.designsystem.components.LiteverTextField
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.button.LvButtonType
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.components.dialog.LvAlertDialog
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.features.reminders.R
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderAction
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.features.reminders.viewmodel.ReminderRingingUiState
import vn.io.litever.remind.features.reminders.viewmodel.ReminderRingingViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun ReminderRingingRoute(
    reminderId: Long,
    onFinish: () -> Unit,
    navController: NavController,
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
            CircularProgressIndicator(color = LiteverTheme.colors.primary)
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
    uiState: vn.io.litever.remind.features.reminders.viewmodel.ReminderRingingUiState,
    onSnoozeClick: (Int) -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reminder = uiState.reminder ?: return
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    var showCustomSnoozeDialog by remember { mutableStateOf(false) }
    var customMinutesText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(1000L)
        }
    }

    val timeString = TimeFormatUtils.formatTime(currentTime, uiState.is24HourFormat)
    val dateString = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))

    Surface(
        modifier = modifier.fillMaxSize(),
        color = LiteverTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, bottom = 36.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Time, Date & Auto-silence
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Icon(
                    imageVector = ReMindIcons.NotificationsActive,
                    contentDescription = null,
                    tint = LiteverTheme.colors.primary,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = timeString,
                    style = LiteverTheme.typography.displayLarge.copy(
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = LiteverTheme.colors.onBackground
                )

                Text(
                    text = dateString,
                    style = LiteverTheme.typography.bodyLarge,
                    color = LiteverTheme.colors.onSurfaceVariant
                )

                if (uiState.autoSilenceCountdown != null && uiState.autoSilenceCountdown > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = LiteverTheme.colors.errorContainer,
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            text = stringResource(
                                R.string.auto_silence_countdown_format,
                                uiState.autoSilenceCountdown
                            ),
                            style = LiteverTheme.typography.labelSmall,
                            color = LiteverTheme.colors.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Middle Section: Reminder Title & Notes
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Text(
                    text = reminder.title,
                    style = LiteverTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = LiteverTheme.colors.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                if (reminder.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = reminder.notes,
                        style = LiteverTheme.typography.bodyLarge,
                        color = LiteverTheme.colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

                        // Bottom Section: Snooze & Dismiss Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Snooze Action (if enabled)
                if (reminder.action.snoozeEnabled) {
                    val canSnooze = reminder.action.canSnooze()
                    if (canSnooze) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val options = listOf(5, 10, 15)
                            options.forEach { mins ->
                                LiteverOutlinedButton(
                                    onClick = { onSnoozeClick(mins) },
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Text(
                                        text = "+${mins}m",
                                        style = LiteverTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                            LiteverOutlinedButton(
                                onClick = { showCustomSnoozeDialog = true },
                                modifier = Modifier.weight(1.3f).height(48.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.snooze_custom),
                                    style = LiteverTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    } else {
                        LiteverOutlinedButton(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.snooze_limit_reached),
                                style = LiteverTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }

                if (showCustomSnoozeDialog) {
                    LvAlertDialog(
                        onDismissRequest = { showCustomSnoozeDialog = false },
                        title = { Text(stringResource(R.string.snooze_custom_dialog_title)) },
                        text = {
                            Column {
                                LiteverTextField(
                                    value = customMinutesText,
                                    onValueChange = {
                                        customMinutesText = it.filter { char -> char.isDigit() }.take(4)
                                    },
                                    placeholder = stringResource(R.string.snooze_custom_minutes_hint),
                                    label = stringResource(R.string.snooze_custom_minutes_hint),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        },
                        confirmButton = {
                            LvButton(
                                onClick = {
                                    val minutes = customMinutesText.toIntOrNull()
                                    if (minutes != null && minutes > 0) {
                                        showCustomSnoozeDialog = false
                                        onSnoozeClick(minutes)
                                    }
                                },
                                enabled = (customMinutesText.toIntOrNull() ?: 0) > 0
                            ) {
                                Text(stringResource(R.string.snooze_action))
                            }
                        },
                        dismissButton = {
                            LvButton(
                                onClick = { showCustomSnoozeDialog = false },
                                type = LvButtonType.Outlined,
                                semantic = LvSemantic.Secondary
                            ) {
                                Text(stringResource(R.string.action_cancel))
                            }
                        }
                    )
                }

                // Dismiss Action
                LiteverButton(
                    onClick = onDismissClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(
                        imageVector = ReMindIcons.Check,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.dismiss_action),
                        style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
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
                    notes = "Take a short break, stand up and walk around.",
                    triggerType = ReminderTriggerType.FIXED_TIME,
                    action = ReminderAction(
                        snoozeEnabled = true,
                        snoozeIntervalMinutes = 10
                    )
                ),
                is24HourFormat = true,
                autoSilenceCountdown = 300,
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
                    id = 1L,
                    title = "Drink Water & Stretch",
                    notes = "Take a short break, stand up and walk around.",
                    triggerType = ReminderTriggerType.FIXED_TIME,
                    action = ReminderAction(
                        snoozeEnabled = true,
                        snoozeIntervalMinutes = 10
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
