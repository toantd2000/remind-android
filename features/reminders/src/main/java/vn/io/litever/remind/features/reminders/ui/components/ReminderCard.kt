package vn.io.litever.remind.features.reminders.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.io.litever.designsystem.components.LiteverIconButton
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.Reminder
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
    val isSnoozed = reminder.snoozeNextTriggerTime != null
    val isCardActive = reminder.isEnabled
    val alpha = if (isCardActive) 1f else 0.6f
    val containerColor = if (isCardActive) {
        LiteverTheme.colors.surfaceContainer
    } else {
        LiteverTheme.colors.surfaceContainerLow
    }

    val accentColor = if (isSnoozed) {
        LiteverTheme.colors.secondary
    } else {
        LiteverTheme.colors.primary
    }

    val accentContainerColor = if (isSnoozed) {
        LiteverTheme.colors.secondaryContainer
    } else {
        LiteverTheme.colors.primaryContainer
    }

    val onAccentContainerColor = if (isSnoozed) {
        LiteverTheme.colors.onSecondaryContainer
    } else {
        LiteverTheme.colors.onPrimaryContainer
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        color = containerColor,
        shape = LiteverTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    if (isCardActive) {
                        drawRect(
                            color = accentColor.copy(alpha = 0.8f),
                            size = Size(3.dp.toPx(), size.height)
                        )
                    }
                }
                .padding(
                    start = LiteverTheme.spacing.medium,
                    end = LiteverTheme.spacing.small,
                    top = LiteverTheme.spacing.smallMedium,
                    bottom = LiteverTheme.spacing.smallMedium
                ),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer { this.alpha = alpha }
            ) {
                // Top Row: Separated Time badge, Recurrence badge, Time Windows, Snooze, Urgency badges
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    // 1. Time / Interval frequency badge
                    TimeBadge(
                        reminder = reminder,
                        is24HourFormat = is24HourFormat,
                        containerColor = accentContainerColor.copy(alpha = 0.7f),
                        contentColor = onAccentContainerColor
                    )

                    // 2. Separate Recurrence / Repeat Schedule badge
                    RecurrenceBadge(
                        timeTrigger = reminder.timeTrigger
                    )

                    // 3. Time Window Badge (for Interval)
                    if (reminder.triggerType == ReminderTriggerType.INTERVAL) {
                        val activeWindows = reminder.timeTrigger?.timeWindows ?: emptyList()
                        if (activeWindows.isNotEmpty()) {
                            TimeWindowBadge(
                                timeWindows = activeWindows,
                                is24HourFormat = is24HourFormat
                            )
                        }
                    }

                    // 4. Snoozing Badge (if snoozed)
                    if (isSnoozed) {
                        SnoozingBadge(
                            snoozeTimeMillis = reminder.snoozeNextTriggerTime!!,
                            is24HourFormat = is24HourFormat
                        )
                    }

                    // 5. Hard Urgency Badge
                    if (reminder.action.urgencyLevel == UrgencyLevel.HARD) {
                        BadgeContainer(
                            icon = ReMindIcons.NotificationImportant,
                            text = stringResource(R.string.urgency_hard),
                            containerColor = LiteverTheme.colors.errorContainer,
                            contentColor = LiteverTheme.colors.onErrorContainer
                        )
                    }

                    // 6. Persistent Alert Badge
                    if (reminder.action.isPersistentAlert) {
                        BadgeContainer(
                            icon = ReMindIcons.Alarm,
                            text = stringResource(R.string.badge_persistent_alert),
                            containerColor = LiteverTheme.colors.primaryContainer.copy(alpha = 0.8f),
                            contentColor = LiteverTheme.colors.onPrimaryContainer
                        )
                    }
                }

                // Title
                Text(
                    text = reminder.title,
                    style = LiteverTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = when {
                        !reminder.isEnabled -> LiteverTheme.colors.onSurface.copy(alpha = 0.5f)
                        isSnoozed -> LiteverTheme.colors.secondary
                        else -> LiteverTheme.colors.onSurface
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Notes snippet
                if (reminder.notes.isNotBlank()) {
                    Text(
                        text = reminder.notes,
                        style = LiteverTheme.typography.bodySmall,
                        color = LiteverTheme.colors.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = LiteverTheme.spacing.tiny)
                    )
                }

                // Footer: Next Trigger / Relative Countdown / Inactive status
                NextTriggerFooter(
                    reminder = reminder,
                    is24HourFormat = is24HourFormat,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Options menu button (3-dots)
            LiteverIconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = ReMindIcons.MoreVert,
                    contentDescription = stringResource(R.string.action_more),
                    modifier = Modifier.size(20.dp),
                    tint = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun TimeBadge(
    reminder: Reminder,
    is24HourFormat: Boolean,
    containerColor: Color,
    contentColor: Color
) {
    val timeTrigger = reminder.timeTrigger
    val (icon, text) = when (reminder.triggerType) {
        ReminderTriggerType.INTERVAL -> {
            val freq = ReminderCardFormatter.formatIntervalFrequency(timeTrigger?.intervalMinutes)
            Pair(ReMindIcons.Timer, freq)
        }
        ReminderTriggerType.FIXED_TIME -> {
            val timesText = ReminderCardFormatter.formatFixedTimes(timeTrigger?.time ?: emptyList(), is24HourFormat)
                .ifEmpty { stringResource(R.string.trigger_time) }
            Pair(ReMindIcons.AccessTime, timesText)
        }
    }

    BadgeContainer(
        icon = icon,
        text = text,
        containerColor = containerColor,
        contentColor = contentColor
    )
}

@Composable
private fun RecurrenceBadge(
    timeTrigger: TimeTrigger?
) {
    val text = ReminderCardFormatter.formatRecurrenceSchedule(timeTrigger)
    if (text.isBlank()) return

    BadgeContainer(
        icon = ReMindIcons.CalendarMonth,
        text = text,
        containerColor = LiteverTheme.colors.surfaceContainerHighest.copy(alpha = 0.8f),
        contentColor = LiteverTheme.colors.onSurfaceVariant
    )
}

@Composable
private fun TimeWindowBadge(
    timeWindows: List<ReminderTimeWindow>,
    is24HourFormat: Boolean
) {
    val text = ReminderCardFormatter.formatTimeWindows(timeWindows, is24HourFormat)
    if (text.isBlank()) return

    Surface(
        color = LiteverTheme.colors.secondaryContainer.copy(alpha = 0.6f),
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = ReMindIcons.DateRange,
                contentDescription = null,
                tint = LiteverTheme.colors.onSecondaryContainer,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = LiteverTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = LiteverTheme.colors.onSecondaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SnoozingBadge(
    snoozeTimeMillis: Long,
    is24HourFormat: Boolean
) {
    val snoozeTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(snoozeTimeMillis),
        ZoneId.systemDefault()
    ).toLocalTime()
    val formattedTime = TimeFormatUtils.formatTime(snoozeTime, is24HourFormat)

    Surface(
        color = LiteverTheme.colors.secondaryContainer,
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = ReMindIcons.Snooze,
                contentDescription = null,
                tint = LiteverTheme.colors.onSecondaryContainer,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.snoozing_badge_format, formattedTime),
                style = LiteverTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = LiteverTheme.colors.onSecondaryContainer,
                fontWeight = FontWeight.Medium
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
        displayInfo.isInactive -> LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
        displayInfo.isSnoozed -> LiteverTheme.colors.secondary
        else -> LiteverTheme.colors.primary
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
            style = LiteverTheme.typography.labelSmall.copy(
                fontSize = 11.5.sp,
                fontWeight = if (displayInfo.isInactive) FontWeight.Normal else FontWeight.Medium
            ),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BadgeContainer(
    icon: ImageVector,
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = LiteverTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = contentColor,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
            onMoreClick = {}
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
                        ReminderTimeWindow(startTime = LocalTime.of(8, 0), endTime = LocalTime.of(17, 30))
                    )
                )
            ),
            is24HourFormat = true,
            onClick = {},
            onMoreClick = {}
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
                isEnabled = true,
                triggerType = ReminderTriggerType.FIXED_TIME,
                timeTrigger = TimeTrigger(
                    time = listOf(LocalTime.of(9, 0), LocalTime.of(14, 0)),
                    repeatMode = RepeatMode.DAILY
                )
            ),
            is24HourFormat = false,
            onClick = {},
            onMoreClick = {}
        )
    }
}
