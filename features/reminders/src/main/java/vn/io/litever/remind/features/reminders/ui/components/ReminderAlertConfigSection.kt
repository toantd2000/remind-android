package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AlarmOn
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverListItem
import vn.io.litever.designsystem.components.LiteverOutlinedCard
import vn.io.litever.designsystem.components.LiteverRadioButton
import vn.io.litever.designsystem.components.LiteverSegmentedButton
import vn.io.litever.designsystem.components.LiteverSingleChoiceSegmentedButtonRow
import vn.io.litever.designsystem.components.LiteverSwitch
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.ReMindSettingIcon
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.features.reminders.R
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
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
    autoSilenceMinutes: Int,
    onAutoSilenceMinutesChange: (Int) -> Unit,
    useAlarmStream: Boolean,
    onUseAlarmStreamChange: (Boolean) -> Unit,
    snoozeEnabled: Boolean,
    snoozeIntervalMinutes: Int,
    snoozeRepeatCount: Int,
    onSnoozeSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAutoSilenceSheet by remember { mutableStateOf(false) }

    if (showAutoSilenceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAutoSilenceSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            ReminderAutoSilenceBottomSheetContent(
                currentMinutes = autoSilenceMinutes,
                onMinutesSelect = { minutes ->
                    onAutoSilenceMinutesChange(minutes)
                    showAutoSilenceSheet = false
                }
            )
        }
    }

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

            LiteverSingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                NotificationStyle.entries.forEachIndexed { index, style ->
                    val labelRes = when (style) {
                        NotificationStyle.STANDARD -> R.string.style_standard
                        NotificationStyle.FULL_SCREEN -> R.string.style_full_screen
                    }
                    LiteverSegmentedButton(
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

            // Sound & Ringtone Settings (Only shown for Alarm Ringing mode)
            if (notificationStyle == NotificationStyle.FULL_SCREEN) {
                Spacer(modifier = Modifier.height(16.dp))

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
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
                            tint = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Grouped Volume & Vibration Row
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(LiteverTheme.shapes.medium),
                    color = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.4f),
                    shape = LiteverTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReMindSettingIcon(
                            imageVector = if (volume == 0) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                            selected = false
                        )

                        Slider(
                            value = volume.toFloat(),
                            onValueChange = { onVolumeChange(it.roundToInt()) },
                            valueRange = 0f..100f,
                            steps = 9,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )

                        ReMindSettingIcon(
                            imageVector = Icons.Rounded.Vibration,
                            selected = vibrationEnabled,
                            onClick = { onVibrationChange(!vibrationEnabled) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Auto-Silence Row
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(LiteverTheme.shapes.medium)
                        .clickable { showAutoSilenceSheet = true },
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            ReMindSettingIcon(
                                imageVector = Icons.Rounded.Timer,
                                selected = autoSilenceMinutes > 0
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.auto_silence_title),
                                    style = LiteverTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LiteverTheme.colors.onSurface
                                )
                                val autoSilenceSummary = if (autoSilenceMinutes <= 0) {
                                    stringResource(R.string.auto_silence_never)
                                } else {
                                    stringResource(R.string.minutes_format, autoSilenceMinutes)
                                }
                                Text(
                                    text = autoSilenceSummary,
                                    style = LiteverTheme.typography.bodySmall,
                                    color = LiteverTheme.colors.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            tint = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Use Alarm Stream (STREAM_ALARM) Switch Row
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(LiteverTheme.shapes.medium)
                        .clickable { onUseAlarmStreamChange(!useAlarmStream) },
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            ReMindSettingIcon(
                                imageVector = Icons.Rounded.AlarmOn,
                                selected = useAlarmStream
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.padding(end = 8.dp)) {
                                Text(
                                    text = stringResource(R.string.use_alarm_stream_title),
                                    style = LiteverTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LiteverTheme.colors.onSurface
                                )
                                Text(
                                    text = if (useAlarmStream) {
                                        stringResource(R.string.use_alarm_stream_desc)
                                    } else {
                                        stringResource(R.string.use_notification_stream_desc)
                                    },
                                    style = LiteverTheme.typography.bodySmall,
                                    color = LiteverTheme.colors.onSurfaceVariant
                                )
                            }
                        }

                        LiteverSwitch(
                            checked = useAlarmStream,
                            onCheckedChange = onUseAlarmStreamChange
                        )
                    }
                }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        ReMindSettingIcon(
                            imageVector = Icons.Rounded.Snooze,
                            selected = snoozeEnabled
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
                                "${
                                    stringResource(
                                        R.string.minutes_format,
                                        snoozeIntervalMinutes
                                    )
                                }, $repeatText"
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
                        tint = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun ReminderAutoSilenceBottomSheetContent(
    currentMinutes: Int,
    onMinutesSelect: (Int) -> Unit
) {
    val options = listOf(1, 5, 10, 15, 0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.auto_silence_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        options.forEach { option ->
            val label = if (option == 0) {
                stringResource(R.string.auto_silence_never)
            } else {
                stringResource(R.string.minutes_format, option)
            }

            LiteverListItem(
                headlineContent = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                },
                leadingContent = {
                    LiteverRadioButton(
                        selected = currentMinutes == option,
                        onClick = { onMinutesSelect(option) }
                    )
                },
                modifier = Modifier.clickable { onMinutesSelect(option) }
            )
        }
    }
}