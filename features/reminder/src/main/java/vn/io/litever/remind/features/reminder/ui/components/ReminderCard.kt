package vn.io.litever.remind.features.reminder.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import vn.io.litever.remind.core.designsystem.components.ReMindSwitch
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.common.util.TimeFormatUtils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import vn.io.litever.remind.features.reminder.R

import androidx.compose.material.icons.rounded.NotificationsPaused
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import vn.io.litever.remind.core.model.Mission
import vn.io.litever.remind.core.model.MissionType

import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReminderCard(
    reminder: Reminder,
    is24HourFormat: Boolean,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSkipped = reminder.skippedAt != null
    val alpha = if (reminder.isEnabled && !isSkipped) 1f else 0.6f

    val containerColor = if (reminder.isEnabled && !isSkipped) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(MaterialTheme.shapes.large)
            .clickable(
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Left Accent Bar - Thinner and more subtle
            if (reminder.isEnabled && !isSkipped) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(3.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
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
                    // Top Row: Repeat Info
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = getRepeatText(reminder),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (reminder.isEnabled && !isSkipped) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        
                        if (isSkipped) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.NotificationsPaused,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Middle Row: Time - Lighter font weight
                    val (timeStr, amPm) = TimeFormatUtils.formatTimeParts(reminder.time, is24HourFormat)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (amPm != null) {
                            Text(
                                text = amPm.uppercase(),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                            )
                        }
                        
                        // Mission Icons
                        MissionIcons(
                            missions = reminder.missions,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )
                    }

                    // Bottom Row: Label - Italics if empty
                    val labelText = reminder.label.ifEmpty { stringResource(R.string.no_label) }
                    Text(
                        text = labelText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = if (reminder.label.isEmpty()) FontStyle.Italic else FontStyle.Normal,
                            color = if (reminder.label.isEmpty()) 
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )

                    // Skipped Info Text
                    val skippedAt = reminder.skippedAt
                    if (isSkipped && skippedAt != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM") }
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = stringResource(R.string.skipped_next_format, skippedAt.format(dateFormatter)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                ReMindSwitch(
                    checked = reminder.isEnabled && !isSkipped,
                    onCheckedChange = onToggle
                )

                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = stringResource(R.string.action_more),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MissionIcons(missions: List<Mission>, modifier: Modifier = Modifier) {
    if (missions.isEmpty()) return
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (missions.size <= 2) {
            missions.forEach { mission ->
                Icon(
                    imageVector = getMissionIcon(mission.type),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp).padding(horizontal = 2.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
        } else {
            Icon(
                imageVector = getMissionIcon(missions[0].type),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                shape = CircleShape
            ) {
                Text(
                    text = "+${missions.size - 1}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun getMissionIcon(type: MissionType): ImageVector {
    return when (type) {
        MissionType.TYPING -> Icons.Rounded.Keyboard
        MissionType.MATH -> Icons.Rounded.Calculate
        MissionType.QR_CODE -> Icons.Rounded.QrCodeScanner
        MissionType.SHAKE -> Icons.Rounded.Smartphone
        MissionType.STEP -> Icons.Rounded.DirectionsWalk
        MissionType.COLOR_MATCH -> Icons.Rounded.Palette
        MissionType.TAP_CHALLENGE -> Icons.Rounded.TouchApp
        MissionType.FIND_ITEM -> Icons.Rounded.Search
    }
}

@Composable
private fun getRepeatText(reminder: Reminder): String {
    return getRepeatSummaryText(reminder.repeatDays, reminder.time, reminder.date)
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ReminderCardPreview() {
    val reminder = Reminder(
        id = 1L,
        time = java.time.LocalTime.of(7, 30),
        isEnabled = true,
        label = "Morning Workout",
        repeatDays = listOf(vn.io.litever.remind.core.model.DayOfWeek.MONDAY, vn.io.litever.remind.core.model.DayOfWeek.WEDNESDAY, vn.io.litever.remind.core.model.DayOfWeek.FRIDAY)
    )
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ReminderCard(
                reminder = reminder,
                is24HourFormat = false,
                onToggle = {},
                onClick = {},
                onMoreClick = {}
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ReminderCardDisabledPreview() {
    val reminder = Reminder(
        id = 1L,
        time = java.time.LocalTime.of(8, 0),
        isEnabled = false,
        label = "Weekend Alarm",
        repeatDays = listOf(vn.io.litever.remind.core.model.DayOfWeek.SATURDAY, vn.io.litever.remind.core.model.DayOfWeek.SUNDAY)
    )
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ReminderCard(
                reminder = reminder,
                is24HourFormat = true,
                onToggle = {},
                onClick = {},
                onMoreClick = {}
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ReminderCardSkippedPreview() {
    val reminder = Reminder(
        id = 1L,
        time = java.time.LocalTime.of(9, 15),
        isEnabled = true,
        label = "Office Meeting",
        repeatDays = listOf(vn.io.litever.remind.core.model.DayOfWeek.MONDAY),
        skippedAt = java.time.LocalDateTime.now()
    )
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ReminderCard(
                reminder = reminder,
                is24HourFormat = false,
                onToggle = {},
                onClick = {},
                onMoreClick = {}
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ReminderCardNoLabelPreview() {
    val reminder = Reminder(
        id = 1L,
        time = java.time.LocalTime.of(6, 0),
        isEnabled = true,
        label = "",
        repeatDays = listOf(vn.io.litever.remind.core.model.DayOfWeek.MONDAY, vn.io.litever.remind.core.model.DayOfWeek.TUESDAY)
    )
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ReminderCard(
                reminder = reminder,
                is24HourFormat = false,
                onToggle = {},
                onClick = {},
                onMoreClick = {}
            )
        }
    }
}


