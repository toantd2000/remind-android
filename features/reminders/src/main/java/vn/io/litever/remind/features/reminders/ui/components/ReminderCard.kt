package vn.io.litever.remind.features.reminders.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.io.litever.designsystem.components.LiteverCard
import vn.io.litever.designsystem.components.LiteverIconButton
import vn.io.litever.designsystem.components.LiteverSwitch
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.Reminder
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
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSnoozed = reminder.snoozeNextTriggerTime != null
    val isCardActive = reminder.isEnabled && !reminder.isCompleted
    val alpha = if (isCardActive) 1f else 0.6f
    val containerColor = if (isCardActive) {
        LiteverTheme.colors.surface
    } else {
        LiteverTheme.colors.surface.copy(alpha = 0.6f)
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

    LiteverCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(LiteverTheme.shapes.large)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = LiteverTheme.shapes.large,
        border = BorderStroke(1.dp, LiteverTheme.colors.outlineVariant.copy(alpha = 0.3f))
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
                    start = 16.dp,
                    end = 0.dp,
                    top = 12.dp,
                    bottom = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer { this.alpha = alpha }
            ) {
                // Top Row: Trigger badge & badges
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    TriggerBadge(
                        reminder = reminder,
                        is24HourFormat = is24HourFormat,
                        containerColor = accentContainerColor.copy(alpha = 0.7f),
                        contentColor = onAccentContainerColor
                    )

                    if (reminder.triggerType == ReminderTriggerType.INTERVAL) {
                        val activeWindows = reminder.timeTrigger?.timeWindows ?: emptyList()
                        if (activeWindows.isNotEmpty()) {
                            val firstWindow = activeWindows.first()
                            TimeWindowBadge(
                                startTime = firstWindow.startTime.toString(),
                                endTime = firstWindow.endTime.toString()
                            )
                        }
                    }

                    if (isSnoozed) {
                        SnoozingBadge(
                            snoozeTimeMillis = reminder.snoozeNextTriggerTime!!,
                            is24HourFormat = is24HourFormat
                        )
                    }

                    if (reminder.action.urgencyLevel == UrgencyLevel.HARD) {
                        BadgeContainer(
                            icon = ReMindIcons.NotificationImportant,
                            text = stringResource(R.string.urgency_hard),
                            containerColor = LiteverTheme.colors.errorContainer,
                            contentColor = LiteverTheme.colors.onErrorContainer
                        )
                    }

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
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null
                    ),
                    color = when {
                        reminder.isCompleted -> LiteverTheme.colors.onSurface.copy(alpha = 0.5f)
                        isSnoozed -> LiteverTheme.colors.secondary
                        else -> LiteverTheme.colors.onSurface
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Notes snippet
                if (reminder.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = reminder.notes,
                        style = LiteverTheme.typography.bodySmall,
                        color = LiteverTheme.colors.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Options menu button (3-dots) positioned immediately to the left of the Switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {

                MaterialTheme(
                    colorScheme = if (isSnoozed) {
                        MaterialTheme.colorScheme.copy(
                            primary = LiteverTheme.colors.secondary,
                            primaryContainer = LiteverTheme.colors.secondaryContainer
                        )
                    } else {
                        MaterialTheme.colorScheme
                    }
                ) {
                    LiteverSwitch(
                        checked = reminder.isEnabled,
                        onCheckedChange = onToggle
                    )
                }

                LiteverIconButton(
                    onClick = onMoreClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = ReMindIcons.MoreVert,
                        contentDescription = stringResource(R.string.action_more),
                        modifier = Modifier.size(20.dp),
                        tint = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TriggerBadge(
    reminder: Reminder,
    is24HourFormat: Boolean,
    containerColor: Color,
    contentColor: Color
) {
    val timeTrigger = reminder.timeTrigger
    val pair: Pair<ImageVector, String> = when (reminder.triggerType) {
        ReminderTriggerType.INTERVAL -> {
            val interval = timeTrigger?.intervalMinutes ?: 60
            Pair(ReMindIcons.Schedule, stringResource(R.string.badge_interval_format, interval))
        }
        ReminderTriggerType.FIXED_TIME -> {
            val times = timeTrigger?.time ?: emptyList()
            val timesText = if (times.isEmpty()) {
                stringResource(R.string.trigger_time)
            } else if (times.size == 1) {
                TimeFormatUtils.formatTime(times.first(), is24HourFormat)
            } else if (times.size <= 2) {
                times.joinToString(", ") { TimeFormatUtils.formatTime(it, is24HourFormat) }
            } else {
                "${TimeFormatUtils.formatTime(times[0], is24HourFormat)}, ${TimeFormatUtils.formatTime(times[1], is24HourFormat)} +${times.size - 2}"
            }

            val repeatSuffix = when {
                timeTrigger?.repeatMode == RepeatMode.DAILY -> " • Daily"
                timeTrigger?.repeatMode == RepeatMode.WEEKLY && timeTrigger.repeatValues.isNotEmpty() -> " • Weekly"
                timeTrigger?.repeatMode == RepeatMode.MONTHLY && timeTrigger.monthlyDays.isNotEmpty() -> {
                    " • Days: ${timeTrigger.monthlyDays.take(3).joinToString(",")}${if (timeTrigger.monthlyDays.size > 3) "…" else ""}"
                }
                timeTrigger?.repeatMode == RepeatMode.YEARLY && timeTrigger.yearlyDates.isNotEmpty() -> {
                    " • Yearly"
                }
                else -> ""
            }

            Pair(ReMindIcons.AccessTime, timesText + repeatSuffix)
        }
    }
    val icon = pair.first
    val text = pair.second

    BadgeContainer(
        icon = icon,
        text = text,
        containerColor = containerColor,
        contentColor = contentColor
    )
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
private fun TimeWindowBadge(startTime: String, endTime: String) {
    Surface(
        color = LiteverTheme.colors.secondaryContainer,
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
                text = "$startTime - $endTime",
                style = LiteverTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = LiteverTheme.colors.onSecondaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
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
private fun ReminderCardPreview() {
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
                    repeatMode = RepeatMode.DAILY
                )
            ),
            is24HourFormat = false,
            onToggle = {},
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
            onToggle = {},
            onClick = {},
            onMoreClick = {}
        )
    }
}

