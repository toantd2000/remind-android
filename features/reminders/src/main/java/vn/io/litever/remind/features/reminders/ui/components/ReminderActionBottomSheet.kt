package vn.io.litever.remind.features.reminders.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.brand.tokens.LiteverTokens
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderAction
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.RepeatMode
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.model.UrgencyLevel
import vn.io.litever.remind.features.reminders.R
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderActionBottomSheet(
    reminder: Reminder,
    onDismissRequest: () -> Unit,
    onEditClick: () -> Unit,
    onDuplicateClick: () -> Unit,
    onToggleEnabledClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    is24HourFormat: Boolean = true,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        ReminderActionBottomSheetContent(
            reminder = reminder,
            is24HourFormat = is24HourFormat,
            onToggleEnabledClick = {
                onDismissRequest()
                onToggleEnabledClick()
            },
            onEditClick = {
                onDismissRequest()
                onEditClick()
            },
            onDuplicateClick = {
                onDismissRequest()
                onDuplicateClick()
            },
            onDeleteClick = {
                onDismissRequest()
                onDeleteClick()
            },
            modifier = Modifier.navigationBarsPadding()
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ReminderActionBottomSheetContent(
    reminder: Reminder,
    is24HourFormat: Boolean,
    onToggleEnabledClick: () -> Unit,
    onEditClick: () -> Unit,
    onDuplicateClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSnoozing = reminder.isEnabled && reminder.snoozeNextTriggerTime != null
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
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = MaterialTheme.spacing.small)
    ) {
        // Header Summary (Aligned with AlarmActionBottomSheetContent)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium)
        ) {
            if (scheduleSummary.isNotBlank() || isSnoozing || isHardUrgency || !reminder.isEnabled) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
                    itemVerticalAlignment = Alignment.CenterVertically
                ) {
                    if (scheduleSummary.isNotBlank()) {
                        Text(
                            text = scheduleSummary,
                            style = LiteverTokens.typography.labelSmallEmphasized,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (!reminder.isEnabled) {
                        SheetStatusPill(
                            text = stringResource(R.string.reminder_status_disabled),
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (isSnoozing) {
                        SheetStatusPill(
                            text = stringResource(R.string.reminder_status_snoozing),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    if (isHardUrgency) {
                        SheetStatusPill(
                            text = stringResource(R.string.urgency_hard),
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
            }

            Text(
                text = reminder.title,
                style = LiteverTokens.typography.titleLargeEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (reminder.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.tiny))
                Text(
                    text = reminder.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
        )

        // Safe Actions Group
        val toggleText = if (reminder.isEnabled) {
            stringResource(R.string.action_turn_off)
        } else {
            stringResource(R.string.action_turn_on)
        }
        val toggleIcon = if (reminder.isEnabled) {
            ReMindIcons.NotificationsPaused
        } else {
            ReMindIcons.NotificationsActive
        }

        ListItem(
            headlineContent = { Text(toggleText) },
            supportingContent = if (isSnoozing) {
                {
                    Text(
                        text = stringResource(R.string.action_turn_off_snoozing_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                null
            },
            leadingContent = {
                Icon(
                    imageVector = toggleIcon,
                    contentDescription = null,
                    tint = if (reminder.isEnabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleEnabledClick)
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.action_edit)) },
            leadingContent = {
                Icon(
                    imageVector = ReMindIcons.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onEditClick)
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.action_duplicate)) },
            leadingContent = {
                Icon(
                    imageVector = ReMindIcons.ContentCopy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onDuplicateClick)
        )

        // Destructive Action Group
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
        )

        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(R.string.action_delete),
                    color = MaterialTheme.colorScheme.error
                )
            },
            leadingContent = {
                Icon(
                    imageVector = ReMindIcons.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onDeleteClick)
        )
    }
}

@Composable
private fun SheetStatusPill(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = CircleShape
    ) {
        Text(
            text = text,
            style = LiteverTokens.typography.labelSmallEmphasized,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Preview(showBackground = true, name = "Reminder Action Sheet - Active")
@Composable
private fun ReminderActionBottomSheetPreview() {
    ReMindTheme {
        ReminderActionBottomSheetContent(
            reminder = Reminder(
                id = 1L,
                title = "Drink Water & Stretch",
                notes = "Take a 5-minute walk away from desk.",
                isEnabled = true,
                triggerType = ReminderTriggerType.FIXED_TIME,
                timeTrigger = TimeTrigger(
                    time = listOf(LocalTime.of(10, 0), LocalTime.of(15, 0)),
                    repeatMode = RepeatMode.DAILY
                ),
                action = ReminderAction()
            ),
            is24HourFormat = true,
            onToggleEnabledClick = {},
            onEditClick = {},
            onDuplicateClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Reminder Action Sheet - Snoozing Hard")
@Composable
private fun ReminderActionBottomSheetSnoozingPreview() {
    ReMindTheme(darkTheme = true) {
        ReminderActionBottomSheetContent(
            reminder = Reminder(
                id = 2L,
                title = "Take Heart Medication",
                notes = "1 pill after dinner with water.",
                isEnabled = true,
                snoozeNextTriggerTime = System.currentTimeMillis() + 600_000L,
                triggerType = ReminderTriggerType.FIXED_TIME,
                timeTrigger = TimeTrigger(
                    time = listOf(LocalTime.of(20, 0)),
                    repeatMode = RepeatMode.DAILY
                ),
                action = ReminderAction(urgencyLevel = UrgencyLevel.HARD)
            ),
            is24HourFormat = false,
            onToggleEnabledClick = {},
            onEditClick = {},
            onDuplicateClick = {},
            onDeleteClick = {}
        )
    }
}
