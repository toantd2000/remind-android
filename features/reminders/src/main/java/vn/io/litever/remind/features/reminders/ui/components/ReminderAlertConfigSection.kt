package vn.io.litever.remind.features.reminders.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverFilterChip
import vn.io.litever.designsystem.components.LiteverSwitch
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.AutoSilenceBottomSheet
import vn.io.litever.remind.core.designsystem.components.ReMindSettingIcon
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsCategory
import vn.io.litever.remind.core.designsystem.components.SnoozeBottomSheet
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
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
    onSnoozeEnabledChange: (Boolean) -> Unit,
    snoozeIntervalMinutes: Int,
    onSnoozeIntervalChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    onTogglePlay: () -> Unit = {},
    extraLoudEnabled: Boolean = false,
    onExtraLoudChange: (Boolean) -> Unit = {},
    announceTitleTts: Boolean = false,
    onAnnounceTitleTtsChange: (Boolean) -> Unit = {},
    isPro: Boolean = false,
    onNavigateToPaywall: (Int) -> Unit = {}
) {
    var showAutoSilenceSheet by remember { mutableStateOf(false) }
    var showSnoozeSheet by remember { mutableStateOf(false) }

    if (showAutoSilenceSheet) {
        AutoSilenceBottomSheet(
            currentMinutes = autoSilenceMinutes,
            onMinutesSelect = { minutes ->
                onAutoSilenceMinutesChange(minutes)
                showAutoSilenceSheet = false
            },
            onDismissRequest = { showAutoSilenceSheet = false },
            options = listOf(1, 3, 5, 10, 15, 30, 0)
        )
    }

    if (showSnoozeSheet) {
        SnoozeBottomSheet(
            enabled = snoozeEnabled,
            interval = snoozeIntervalMinutes,
            onEnabledChange = onSnoozeEnabledChange,
            onIntervalChange = onSnoozeIntervalChange,
            onDismissRequest = { showSnoozeSheet = false }
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = LiteverTheme.shapes.large,
        color = LiteverTheme.colors.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LiteverTheme.spacing.medium)
        ) {

            ReMindSettingsCategory(
                title = stringResource(R.string.section_alert_settings)
            )

            // 1. Notification Style Selector (Đổ chuông vs Thông báo thường)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotificationStyle.entries.forEach { style ->
                    val labelRes = when (style) {
                        NotificationStyle.FULL_SCREEN -> R.string.style_full_screen
                        NotificationStyle.STANDARD -> R.string.style_standard
                    }
                    val isSelected = notificationStyle == style
                    LiteverFilterChip(
                        selected = isSelected,
                        onClick = { onNotificationStyleChange(style) },
                        label = {
                            Text(
                                text = stringResource(labelRes),
                                style = LiteverTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = LiteverTheme.colors.surfaceVariant,
                            labelColor = LiteverTheme.colors.onSurfaceVariant,
                            selectedContainerColor = LiteverTheme.colors.primary,
                            selectedLabelColor = LiteverTheme.colors.onPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = LiteverTheme.colors.outlineVariant,
                            selectedBorderColor = LiteverTheme.colors.outline
                        ),
                        shape = LiteverTheme.shapes.medium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            val styleDesc = when (notificationStyle) {
                NotificationStyle.FULL_SCREEN -> stringResource(R.string.style_full_screen_desc)
                NotificationStyle.STANDARD -> stringResource(R.string.style_standard_desc)
            }
            Text(
                text = styleDesc,
                style = LiteverTheme.typography.bodySmall,
                color = LiteverTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
            )

            // 2. Sound & Vibration Settings (Applies to both ringing and standard)
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
                            imageVector = ReMindIcons.MusicNote
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
                        imageVector = ReMindIcons.KeyboardArrowRight,
                        contentDescription = null,
                        tint = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (notificationStyle == NotificationStyle.FULL_SCREEN) {
                // Volume Slider Row (Full Screen Ringing Mode)
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
                            imageVector = if (volume == 0) ReMindIcons.VolumeOff else ReMindIcons.VolumeUp,
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
                            imageVector = if (isPlaying) ReMindIcons.Pause else ReMindIcons.PlayArrow,
                            selected = isPlaying,
                            onClick = onTogglePlay
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Vibration Switch Row (Both FULL_SCREEN and STANDARD modes)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LiteverTheme.shapes.medium)
                    .clickable { onVibrationChange(!vibrationEnabled) },
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
                            imageVector = ReMindIcons.Vibration,
                            selected = vibrationEnabled
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.vibration_label),
                                style = LiteverTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = LiteverTheme.colors.onSurface
                            )
                            Text(
                                text = if (vibrationEnabled) {
                                    stringResource(R.string.vibration_enabled_desc)
                                } else {
                                    stringResource(R.string.off)
                                },
                                style = LiteverTheme.typography.bodySmall,
                                color = LiteverTheme.colors.onSurfaceVariant
                            )
                        }
                    }

                    LiteverSwitch(
                        checked = vibrationEnabled,
                        onCheckedChange = onVibrationChange
                    )
                }
            }

            // 3. Ringing-only Settings (Auto-Silence & Alarm Stream)
            if (notificationStyle == NotificationStyle.FULL_SCREEN) {
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
                                imageVector = ReMindIcons.Timer,
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
                            imageVector = ReMindIcons.KeyboardArrowRight,
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
                                imageVector = ReMindIcons.AlarmOn,
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

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Snooze Row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LiteverTheme.shapes.medium)
                    .clickable { showSnoozeSheet = true },
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
                            imageVector = ReMindIcons.Snooze,
                            selected = snoozeEnabled
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.snooze_action),
                                style = LiteverTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = LiteverTheme.colors.onSurface
                            )
                            val snoozeSummary = if (snoozeEnabled) {
                                stringResource(R.string.minutes_format, snoozeIntervalMinutes)
                            } else {
                                stringResource(R.string.off)
                            }
                            Text(
                                text = snoozeSummary,
                                style = LiteverTheme.typography.bodySmall,
                                color = LiteverTheme.colors.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = ReMindIcons.KeyboardArrowRight,
                        contentDescription = null,
                        tint = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Voice Announcement TTS (Pro)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LiteverTheme.shapes.medium)
                    .clickable {
                        if (!isPro && !announceTitleTts) onNavigateToPaywall(2)
                        else onAnnounceTitleTtsChange(!announceTitleTts)
                    },
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
                            imageVector = ReMindIcons.Speaker,
                            selected = announceTitleTts
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.announce_title_tts_title),
                                    style = LiteverTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LiteverTheme.colors.onSurface
                                )
                                vn.io.litever.remind.core.designsystem.components.ReMindProDiamondTag()
                            }
                            Text(
                                text = stringResource(R.string.announce_title_tts_desc),
                                style = LiteverTheme.typography.bodySmall,
                                color = LiteverTheme.colors.onSurfaceVariant
                            )
                        }
                    }

                    LiteverSwitch(
                        checked = announceTitleTts,
                        onCheckedChange = { enabled ->
                            if (!isPro && enabled) onNavigateToPaywall(2)
                            else onAnnounceTitleTtsChange(enabled)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6. Extra Loud (Pro)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LiteverTheme.shapes.medium)
                    .clickable {
                        if (!isPro && !extraLoudEnabled) onNavigateToPaywall(4)
                        else onExtraLoudChange(!extraLoudEnabled)
                    },
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
                            imageVector = ReMindIcons.VolumeUp,
                            selected = extraLoudEnabled
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.extra_loud_title),
                                    style = LiteverTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LiteverTheme.colors.onSurface
                                )
                                vn.io.litever.remind.core.designsystem.components.ReMindProDiamondTag()
                            }
                            Text(
                                text = stringResource(R.string.extra_loud_desc),
                                style = LiteverTheme.typography.bodySmall,
                                color = LiteverTheme.colors.onSurfaceVariant
                            )
                        }
                    }

                    LiteverSwitch(
                        checked = extraLoudEnabled,
                        onCheckedChange = { enabled ->
                            if (!isPro && enabled) onNavigateToPaywall(4)
                            else onExtraLoudChange(enabled)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderAlertConfigSectionPreview() {
    ReMindTheme {
        ReminderAlertConfigSection(
            notificationStyle = NotificationStyle.FULL_SCREEN,
            onNotificationStyleChange = {},
            ringtoneTitle = "Default Ringtone",
            onRingtoneClick = {},
            volume = 80,
            onVolumeChange = {},
            vibrationEnabled = true,
            onVibrationChange = {},
            autoSilenceMinutes = 5,
            onAutoSilenceMinutesChange = {},
            useAlarmStream = true,
            onUseAlarmStreamChange = {},
            snoozeEnabled = true,
            onSnoozeEnabledChange = {},
            snoozeIntervalMinutes = 10,
            onSnoozeIntervalChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderAlertConfigSectionStandardPreview() {
    ReMindTheme {
        ReminderAlertConfigSection(
            notificationStyle = NotificationStyle.STANDARD,
            onNotificationStyleChange = {},
            ringtoneTitle = "Default Ringtone",
            onRingtoneClick = {},
            volume = 80,
            onVolumeChange = {},
            vibrationEnabled = true,
            onVibrationChange = {},
            autoSilenceMinutes = 5,
            onAutoSilenceMinutesChange = {},
            useAlarmStream = true,
            onUseAlarmStreamChange = {},
            snoozeEnabled = false,
            onSnoozeEnabledChange = {},
            snoozeIntervalMinutes = 5,
            onSnoozeIntervalChange = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReminderAlertConfigSectionDarkPreview() {
    ReMindTheme(darkTheme = true) {
        ReminderAlertConfigSection(
            notificationStyle = NotificationStyle.FULL_SCREEN,
            onNotificationStyleChange = {},
            ringtoneTitle = "Default Ringtone",
            onRingtoneClick = {},
            volume = 80,
            onVolumeChange = {},
            vibrationEnabled = true,
            onVibrationChange = {},
            autoSilenceMinutes = 5,
            onAutoSilenceMinutesChange = {},
            useAlarmStream = true,
            onUseAlarmStreamChange = {},
            snoozeEnabled = true,
            onSnoozeEnabledChange = {},
            snoozeIntervalMinutes = 10,
            onSnoozeIntervalChange = {}
        )
    }
}
