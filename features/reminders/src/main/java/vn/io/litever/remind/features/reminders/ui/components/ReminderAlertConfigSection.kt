package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverOutlinedCard
import vn.io.litever.designsystem.components.LiteverSwitch
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.ReMindSettingIcon
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.features.reminders.R

@Composable
fun ReminderAlertConfigSection(
    notificationStyle: NotificationStyle,
    onNotificationStyleChange: (NotificationStyle) -> Unit,
    ringtoneTitle: String,
    onRingtoneClick: () -> Unit,
    volume: Int,
    onVolumeChange: (Int) -> Unit,
    vibrationEnabled: Boolean,
    onVibrationChange: (Boolean) -> Unit,
    isPersistentAlert: Boolean,
    onPersistentAlertChange: (Boolean) -> Unit,
    snoozeEnabled: Boolean,
    snoozeIntervalMinutes: Int,
    snoozeRepeatCount: Int,
    onSnoozeSettingsClick: () -> Unit,
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

            // Notification Style Selector (Standard Heads-Up vs Full-Screen Alarm)
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

            // Sound & Ringtone Settings
            Text(
                text = stringResource(R.string.sound_settings_title),
                style = LiteverTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = LiteverTheme.colors.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Ringtone Selection Row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LiteverTheme.shapes.medium)
                    .clickable(onClick = onRingtoneClick),
                color = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.4f),
                shape = LiteverTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ReMindSettingIcon(
                            imageVector = Icons.Rounded.MusicNote
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.ringtone_title),
                                style = LiteverTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = LiteverTheme.colors.onSurface
                            )
                            Text(
                                text = ringtoneTitle.ifBlank { stringResource(R.string.default_ringtone) },
                                style = LiteverTheme.typography.bodySmall,
                                color = LiteverTheme.colors.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = LiteverTheme.colors.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Volume Slider
            Text(
                text = stringResource(R.string.volume_label, volume),
                style = LiteverTheme.typography.labelMedium,
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

            Spacer(modifier = Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            // Alert & Snooze Behavior Section
            Text(
                text = stringResource(R.string.alert_behavior_snooze_title),
                style = LiteverTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = LiteverTheme.colors.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // If Full-Screen Alarm: Show Persistent Alert Loop toggle
            if (notificationStyle == NotificationStyle.FULL_SCREEN) {
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

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Snooze Settings Row (Click opens shared SnoozeSettingsScreen)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LiteverTheme.shapes.medium)
                    .clickable(onClick = onSnoozeSettingsClick),
                color = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.4f),
                shape = LiteverTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ReMindSettingIcon(
                            imageVector = Icons.Rounded.Snooze
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.snooze_settings_title),
                                style = LiteverTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = LiteverTheme.colors.onSurface
                            )

                            val repeatText = when (snoozeRepeatCount) {
                                -1 -> stringResource(R.string.snooze_unlimited)
                                1 -> stringResource(R.string.times_format, 1)
                                else -> stringResource(R.string.times_format, snoozeRepeatCount)
                            }
                            val summary = if (snoozeEnabled) {
                                "${stringResource(R.string.minutes_format, snoozeIntervalMinutes)}, $repeatText"
                            } else {
                                stringResource(R.string.off)
                            }

                            Text(
                                text = summary,
                                style = LiteverTheme.typography.bodySmall,
                                color = LiteverTheme.colors.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = LiteverTheme.colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

