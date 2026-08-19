package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverOutlinedCard
import vn.io.litever.designsystem.components.LiteverSwitch
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.features.reminders.R

@Composable
fun ReminderAlertConfigSection(
    notificationStyle: NotificationStyle,
    onNotificationStyleChange: (NotificationStyle) -> Unit,
    isPersistentAlert: Boolean,
    onPersistentAlertChange: (Boolean) -> Unit,
    vibrationEnabled: Boolean,
    onVibrationChange: (Boolean) -> Unit,
    volume: Int,
    onVolumeChange: (Int) -> Unit,
    snoozeEnabled: Boolean,
    onSnoozeEnabledChange: (Boolean) -> Unit,
    snoozeIntervalMinutes: Int,
    onSnoozeIntervalChange: (Int) -> Unit,
    snoozeRepeatCount: Int,
    onSnoozeRepeatCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LiteverOutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.section_alert_settings),
                style = LiteverTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LiteverTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notification Style Selector
            Text(
                text = stringResource(R.string.notification_style_label),
                style = LiteverTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = LiteverTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                NotificationStyle.entries.forEachIndexed { index, style ->
                    val labelRes = when (style) {
                        NotificationStyle.STANDARD -> R.string.style_standard
                        NotificationStyle.FULL_SCREEN -> R.string.style_full_screen
                    }
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = NotificationStyle.entries.size
                        ),
                        onClick = { onNotificationStyleChange(style) },
                        selected = style == notificationStyle,
                        label = {
                            Text(
                                text = stringResource(labelRes),
                                style = LiteverTheme.typography.labelMedium
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Persistent Alert Loop
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = stringResource(R.string.persistent_alert_title),
                        style = LiteverTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = LiteverTheme.colors.onSurface
                    )
                    Text(
                        text = stringResource(R.string.persistent_alert_desc),
                        style = LiteverTheme.typography.bodySmall,
                        color = LiteverTheme.colors.onSurfaceVariant
                    )
                }
                LiteverSwitch(
                    checked = isPersistentAlert,
                    onCheckedChange = onPersistentAlertChange
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vibration Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.vibration_label),
                    style = LiteverTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = LiteverTheme.colors.onSurface
                )
                LiteverSwitch(
                    checked = vibrationEnabled,
                    onCheckedChange = onVibrationChange
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Volume Slider
            Text(
                text = stringResource(R.string.volume_label, volume),
                style = LiteverTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = LiteverTheme.colors.onSurface
            )
            Slider(
                value = volume.toFloat(),
                onValueChange = { onVolumeChange(it.toInt()) },
                valueRange = 0f..100f,
                steps = 9,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Snooze Settings
            Text(
                text = stringResource(R.string.snooze_settings_title),
                style = LiteverTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = LiteverTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.snooze_enabled_label),
                    style = LiteverTheme.typography.bodyMedium,
                    color = LiteverTheme.colors.onSurface
                )
                LiteverSwitch(
                    checked = snoozeEnabled,
                    onCheckedChange = onSnoozeEnabledChange
                )
            }

            if (snoozeEnabled) {
                Spacer(modifier = Modifier.height(12.dp))

                // Snooze Interval (1, 5, 10, 15, 30 min)
                Text(
                    text = stringResource(R.string.snooze_interval_label),
                    style = LiteverTheme.typography.labelMedium,
                    color = LiteverTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(1, 5, 10, 15, 30).forEach { mins ->
                        FilterChip(
                            selected = snoozeIntervalMinutes == mins,
                            onClick = { onSnoozeIntervalChange(mins) },
                            label = { Text(stringResource(R.string.minutes_format, mins)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LiteverTheme.colors.primary,
                                selectedLabelColor = LiteverTheme.colors.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Snooze Repeat Count (1, 2, 3, 5, 0 for unlimited)
                Text(
                    text = stringResource(R.string.snooze_repeat_count_label),
                    style = LiteverTheme.typography.labelMedium,
                    color = LiteverTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(1, 2, 3, 5, 0).forEach { count ->
                        val label = if (count == 0) {
                            stringResource(R.string.snooze_unlimited)
                        } else {
                            stringResource(R.string.times_format, count)
                        }
                        FilterChip(
                            selected = snoozeRepeatCount == count,
                            onClick = { onSnoozeRepeatCountChange(count) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LiteverTheme.colors.primary,
                                selectedLabelColor = LiteverTheme.colors.onPrimary
                            )
                        )
                    }
                }
            }
        }
    }
}
