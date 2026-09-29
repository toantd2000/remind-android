package vn.io.litever.remind.features.reminders.ui.components

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
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.io.litever.designsystem.components.LiteverCard
import vn.io.litever.designsystem.components.LiteverIconButton
import vn.io.litever.designsystem.components.LiteverLinearProgressIndicator
import vn.io.litever.designsystem.components.LiteverSwitch
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.common.util.TimeFormatUtils

import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType

import vn.io.litever.remind.features.reminders.R
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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

                    if (isSnoozed) {
                        SnoozingBadge(
                            snoozeTimeMillis = reminder.snoozeNextTriggerTime!!,
                            is24HourFormat = is24HourFormat
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

                // Checklist progress
                if (reminder.checklist.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = ReMindIcons.Checklist,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(
                                R.string.checklist_progress_format,
                                reminder.completedChecklistCount,
                                reminder.totalChecklistCount
                            ),
                            style = LiteverTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Medium
                        )
                        LiteverLinearProgressIndicator(
                            progress = { reminder.checklistProgress },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(MaterialTheme.shapes.small),
                            color = accentColor,
                            trackColor = accentContainerColor.copy(alpha = 0.4f),
                        )
                    }
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
    val (icon: ImageVector, text: String) = when (reminder.triggerType) {
        ReminderTriggerType.FIXED_TIME, ReminderTriggerType.INTERVAL -> {
            val timeTrigger = reminder.timeTrigger
            val timeStr = if (timeTrigger != null) {
                val formattedTime = TimeFormatUtils.formatTime(timeTrigger.time.firstOrNull() ?: java.time.LocalTime.now(), is24HourFormat)
                if (timeTrigger.repeatValues.isNotEmpty()) {
                    "$formattedTime • Repeat configured"
                } else {
                    formattedTime
                }
            } else {
                stringResource(R.string.trigger_time)
            }
            ReMindIcons.AccessTime to timeStr
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
