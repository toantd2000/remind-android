package vn.io.litever.remind.features.reminder.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.remind.core.designsystem.components.ReMindSwitch
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.common.util.TimeFormatUtils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import vn.io.litever.remind.features.reminder.R

import androidx.compose.material.icons.rounded.NotificationsPaused

import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReminderCard(
    reminder: Reminder,
    is24HourFormat: Boolean,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onSkipOnce: () -> Unit,
    onCancelSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSkipped = reminder.skippedAt != null && reminder.isEnabled
    val alpha = if (reminder.isEnabled && !isSkipped) 1f else 0.5f
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            ),
        colors = if (isSkipped) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        } else {
            CardDefaults.cardColors()
        },
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer { this.alpha = alpha }
                ) {
                    // Top: Repeat Info
                    Text(
                        text = getRepeatText(reminder),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (reminder.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Middle: Time
                    val (timeStr, amPm) = TimeFormatUtils.formatTimeParts(reminder.time, is24HourFormat)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (amPm != null) {
                            Text(
                                text = " $amPm",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    
                    // Bottom: Label
                    Text(
                        text = reminder.label.ifEmpty { " " },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Skipped Info Text
                    val skippedAt = reminder.skippedAt
                    if (isSkipped && skippedAt != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM") }
                        Text(
                            text = stringResource(R.string.skipped_next_format, skippedAt.format(dateFormatter)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                ReMindSwitch(
                    checked = reminder.isEnabled && !isSkipped,
                    onCheckedChange = { checked ->
                        if (isSkipped) {
                            onCancelSkip()
                        } else {
                            onToggle(checked)
                        }
                    }
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                // Skip Once (only for repeating reminders that are currently enabled)
                if (reminder.repeatDays.isNotEmpty() && reminder.isEnabled) {
                    val isSkipped = reminder.skippedAt != null
                    DropdownMenuItem(
                        text = { 
                            Text(
                                if (isSkipped) stringResource(R.string.action_cancel_skip)
                                else stringResource(R.string.action_skip_once)
                            ) 
                        },
                        leadingIcon = { Icon(Icons.Rounded.SkipNext, contentDescription = null) },
                        onClick = {
                            if (isSkipped) onCancelSkip() else onSkipOnce()
                            showMenu = false
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_duplicate)) },
                    leadingIcon = { Icon(Icons.Rounded.ContentCopy, contentDescription = null) },
                    onClick = {
                        onDuplicate()
                        showMenu = false
                    }
                )
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = stringResource(R.string.action_delete),
                            color = MaterialTheme.colorScheme.error
                        ) 
                    },
                    leadingIcon = { 
                        Icon(
                            Icons.Rounded.Delete, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        ) 
                    },
                    onClick = {
                        onDelete()
                        showMenu = false
                    }
                )
            }
        }
    }
}

@Composable
private fun getRepeatText(reminder: Reminder): String {
    return getRepeatSummaryText(reminder.repeatDays, reminder.time, reminder.date)
}
