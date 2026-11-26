package vn.io.litever.remind.features.reminders.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.brand.tokens.LiteverTokens
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderAction
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.RepeatMode
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.model.UrgencyLevel
import vn.io.litever.remind.features.reminders.R
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderCard(
    reminder: Reminder,
    is24HourFormat: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSnoozed = reminder.isEnabled && reminder.snoozeNextTriggerTime != null
    val isCardActive = reminder.isEnabled

    // Match AlarmCard container color & inactive dimming language
    val containerColor = if (isCardActive) {
        MaterialTheme.colorScheme.surfaceContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    val recurrenceText = ReminderCardFormatter.formatRecurrenceSchedule(reminder.timeTrigger)
    val timeText = when (reminder.triggerType) {
        ReminderTriggerType.INTERVAL -> ReminderCardFormatter.formatIntervalFrequency(
            reminder.timeTrigger?.intervalMinutes
        )
        ReminderTriggerType.FIXED_TIME -> ReminderCardFormatter.formatFixedTimes(
            reminder.timeTrigger?.time ?: emptyList(),
            is24HourFormat
        ).ifEmpty { stringResource(R.string.trigger_time) }
    }
    val windowText = if (reminder.triggerType == ReminderTriggerType.INTERVAL) {
        ReminderCardFormatter.formatTimeWindows(
            reminder.timeTrigger?.timeWindows.orEmpty(),
            is24HourFormat
        )
    } else {
        ""
    }
    val scheduleSummary = listOf(recurrenceText, timeText, windowText)
        .filter { it.isNotBlank() }
        .joinToString(" • ")

    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        color = containerColor,
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = MaterialTheme.spacing.medium,
                    end = MaterialTheme.spacing.extraSmall,
                    top = MaterialTheme.spacing.smallMedium,
                    bottom = MaterialTheme.spacing.smallMedium
                )
        ) {
            // 1. Hero Row: Title + Notes first (Reminder-First hierarchy) + compact MoreVert
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 2.dp)
                ) {
                    Text(
                        text = reminder.title,
                        style = LiteverTokens.typography.titleMediumEmphasized,
                        color = when {
                            !isCardActive -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            isSnoozed -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (reminder.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.tiny))
                        Text(
                            text = reminder.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (isCardActive) 0.9f else 0.55f
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = ReMindIcons.MoreVert,
                        contentDescription = stringResource(R.string.action_more),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(MaterialTheme.spacing.mediumLarge),
                    )
                }
            }

            // 2. Concise Schedule & Notable Status Row (subtle tonal pills, no color overload)
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(end = MaterialTheme.spacing.smallMedium)
            ) {
                if (scheduleSummary.isNotBlank()) {
                    ReminderMetaPill(
                        icon = if (reminder.triggerType == ReminderTriggerType.INTERVAL) {
                            ReMindIcons.Timer
                        } else {
                            ReMindIcons.AccessTime
                        },
                        text = scheduleSummary,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (isCardActive) 1f else 0.6f
                        ),
                        tabularNumbers = true
                    )
                }

                if (isSnoozed) {
                    val snoozeTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(reminder.snoozeNextTriggerTime!!),
                        ZoneId.systemDefault()
                    ).toLocalTime()
                    val formattedTime = TimeFormatUtils.formatTime(snoozeTime, is24HourFormat)
                    ReminderMetaPill(
                        icon = ReMindIcons.Snooze,
                        text = stringResource(R.string.snoozing_badge_format, formattedTime),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        tabularNumbers = true
                    )
                }

                if (reminder.action.urgencyLevel == UrgencyLevel.HARD) {
                    ReminderMetaPill(
                        icon = ReMindIcons.NotificationImportant,
                        text = stringResource(R.string.urgency_hard),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                if (reminder.action.isPersistentAlert) {
                    ReminderMetaPill(
                        icon = ReMindIcons.NotificationsActive,
                        text = stringResource(R.string.badge_persistent_alert),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // 3. Footer: Next Trigger / Relative Countdown / Inactive status
            NextTriggerFooter(
                reminder = reminder,
                is24HourFormat = is24HourFormat,
                modifier = Modifier.padding(
                    top = MaterialTheme.spacing.small,
                    end = MaterialTheme.spacing.smallMedium
                )
            )
        }
    }
}

@Composable
private fun ReminderMetaPill(
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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = if (tabularNumbers) {
                    MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum")
                } else {
                    MaterialTheme.typography.labelSmall
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun NextTriggerFooter(
    reminder: Reminder,
    is24HourFormat: Boolean,
    modifier: Modifier = Modifier
) {
    val displayInfo = ReminderCardFormatter.getNextTriggerDisplayInfo(reminder, is24HourFormat)

    val contentColor = when {
        displayInfo.isInactive -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
        displayInfo.isSnoozed -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.tertiary
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = displayInfo.icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = displayInfo.text,
            style = if (displayInfo.isInactive) {
                MaterialTheme.typography.labelSmall
            } else {
                LiteverTokens.typography.labelSmallEmphasized.copy(fontFeatureSettings = "tnum")
            },
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderCardFixedTimePreview() {
    ReMindTheme {
        ReminderCard(
            reminder = Reminder(
                id = 1L,
                title = "Drink Water & Take Vitamins",
                notes = "Stay hydrated throughout the day and take daily supplements.",
                isEnabled = true,
                triggerType = ReminderTriggerType.FIXED_TIME,
                timeTrigger = TimeTrigger(
                    time = listOf(LocalTime.of(9, 0), LocalTime.of(14, 0)),
                    repeatMode = RepeatMode.WEEKLY,
                    repeatValues = listOf(1, 2, 3, 4, 5)
                )
            ),
            is24HourFormat = false,
            onClick = {},
            onMoreClick = {},
            modifier = Modifier.padding(MaterialTheme.spacing.medium)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderCardIntervalPreview() {
    ReMindTheme {
        ReminderCard(
            reminder = Reminder(
                id = 2L,
                title = "Hydration Break",
                notes = "Drink 250ml of water.",
                isEnabled = true,
                triggerType = ReminderTriggerType.INTERVAL,
                timeTrigger = TimeTrigger(
                    intervalMinutes = 120,
                    repeatMode = RepeatMode.DAILY,
                    timeWindows = listOf(
                        ReminderTimeWindow(
                            startTime = LocalTime.of(8, 0),
                            endTime = LocalTime.of(17, 30)
                        )
                    )
                ),
                action = ReminderAction(urgencyLevel = UrgencyLevel.HARD)
            ),
            is24HourFormat = true,
            onClick = {},
            onMoreClick = {},
            modifier = Modifier.padding(MaterialTheme.spacing.medium)
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReminderCardDarkPreview() {
    ReMindTheme(darkTheme = true) {
        ReminderCard(
            reminder = Reminder(
                id = 1L,
                title = "Drink Water & Take Vitamins",
                notes = "Stay hydrated throughout the day and take daily supplements.",
                isEnabled = false,
                triggerType = ReminderTriggerType.FIXED_TIME,
                timeTrigger = TimeTrigger(
                    time = listOf(LocalTime.of(9, 0), LocalTime.of(14, 0)),
                    repeatMode = RepeatMode.DAILY
                )
            ),
            is24HourFormat = false,
            onClick = {},
            onMoreClick = {},
            modifier = Modifier.padding(MaterialTheme.spacing.medium)
        )
    }
}
