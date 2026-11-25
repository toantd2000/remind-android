package vn.io.litever.remind.features.reminders.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.brand.tokens.LiteverTokens
import vn.io.litever.remind.core.designsystem.components.AutoSilenceBottomSheet
import vn.io.litever.remind.core.designsystem.components.ReMindProDiamondTag
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsDivider
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsItem
import vn.io.litever.remind.core.designsystem.components.SnoozeBottomSheet
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
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

    ReMindSettingsGroup(
        title = stringResource(R.string.section_alert_settings),
        modifier = modifier
    ) {
        // 1. Notification Style Selector (Full-screen ringing vs Standard notification)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.spacing.medium,
                    vertical = MaterialTheme.spacing.small
                ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                NotificationStyle.entries.forEach { style ->
                    val labelRes = when (style) {
                        NotificationStyle.FULL_SCREEN -> R.string.style_full_screen
                        NotificationStyle.STANDARD -> R.string.style_standard
                    }
                    val isSelected = notificationStyle == style
                    FilterChip(
                        selected = isSelected,
                        onClick = { onNotificationStyleChange(style) },
                        label = {
                            Text(
                                text = stringResource(labelRes),
                                style = if (isSelected) {
                                    LiteverTokens.typography.labelMediumEmphasized
                                } else {
                                    MaterialTheme.typography.labelMedium
                                },
                                maxLines = 1
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        border = null,
                        shape = MaterialTheme.shapes.medium,
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
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ReMindSettingsDivider(inset = false)

        // 2. Ringtone
        ReMindSettingsItem(
            title = stringResource(R.string.ringtone_title),
            subtitle = ringtoneTitle.ifBlank { stringResource(R.string.default_ringtone) },
            icon = ReMindIcons.MusicNote,
            showChevron = true,
            onClick = onRingtoneClick
        )

        ReMindSettingsDivider()

        // 3. Volume Slider (Full-screen mode)
        if (notificationStyle == NotificationStyle.FULL_SCREEN) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = MaterialTheme.spacing.medium,
                        vertical = MaterialTheme.spacing.small
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (volume == 0) ReMindIcons.VolumeOff else ReMindIcons.VolumeUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(MaterialTheme.spacing.large)
                )

                val volumeSliderState = remember {
                    SliderState(
                        value = volume.toFloat(),
                        steps = 9,
                        trackRange = 0f..100f
                    )
                }
                LaunchedEffect(volume) {
                    if (volumeSliderState.value.roundToInt() != volume) {
                        volumeSliderState.value = volume.toFloat()
                    }
                }
                Slider(
                    state = volumeSliderState,
                    onValueChange = {
                        volumeSliderState.value = it
                        onVolumeChange(it.roundToInt())
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = MaterialTheme.spacing.smallMedium),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                FilledTonalIconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (isPlaying) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        },
                        contentColor = if (isPlaying) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying) ReMindIcons.Pause else ReMindIcons.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(MaterialTheme.spacing.mediumLarge)
                    )
                }
            }

            ReMindSettingsDivider()
        }

        // 4. Vibration
        ReMindSettingsItem(
            title = stringResource(R.string.vibration_label),
            subtitle = if (vibrationEnabled) {
                stringResource(R.string.vibration_enabled_desc)
            } else {
                stringResource(R.string.off)
            },
            icon = ReMindIcons.Vibration,
            iconTint = if (vibrationEnabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            onClick = { onVibrationChange(!vibrationEnabled) },
            trailingContent = {
                Switch(
                    checked = vibrationEnabled,
                    onCheckedChange = onVibrationChange
                )
            }
        )

        // 5. Ringing-only Settings (Auto-Silence & Alarm Stream)
        if (notificationStyle == NotificationStyle.FULL_SCREEN) {
            ReMindSettingsDivider()

            val autoSilenceSummary = if (autoSilenceMinutes <= 0) {
                stringResource(R.string.auto_silence_never)
            } else {
                stringResource(R.string.minutes_format, autoSilenceMinutes)
            }
            ReMindSettingsItem(
                title = stringResource(R.string.auto_silence_title),
                subtitle = autoSilenceSummary,
                icon = ReMindIcons.Timer,
                iconTint = if (autoSilenceMinutes > 0) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                showChevron = true,
                onClick = { showAutoSilenceSheet = true }
            )

            ReMindSettingsDivider()

            ReMindSettingsItem(
                title = stringResource(R.string.use_alarm_stream_title),
                subtitle = if (useAlarmStream) {
                    stringResource(R.string.use_alarm_stream_desc)
                } else {
                    stringResource(R.string.use_notification_stream_desc)
                },
                icon = ReMindIcons.AlarmOn,
                iconTint = if (useAlarmStream) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                onClick = { onUseAlarmStreamChange(!useAlarmStream) },
                trailingContent = {
                    Switch(
                        checked = useAlarmStream,
                        onCheckedChange = onUseAlarmStreamChange
                    )
                }
            )
        }

        ReMindSettingsDivider()

        // 6. Snooze
        val snoozeSummary = if (snoozeEnabled) {
            stringResource(R.string.minutes_format, snoozeIntervalMinutes)
        } else {
            stringResource(R.string.off)
        }
        ReMindSettingsItem(
            title = stringResource(R.string.snooze_action),
            subtitle = snoozeSummary,
            icon = ReMindIcons.Snooze,
            iconTint = if (snoozeEnabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            showChevron = true,
            onClick = { showSnoozeSheet = true }
        )

        ReMindSettingsDivider()

        // 7. Voice Announcement TTS (Pro)
        ReMindSettingsItem(
            title = stringResource(R.string.announce_title_tts_title),
            subtitle = stringResource(R.string.announce_title_tts_desc),
            icon = ReMindIcons.Speaker,
            iconTint = if (announceTitleTts) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            titleBadge = { ReMindProDiamondTag() },
            onClick = {
                if (!isPro && !announceTitleTts) onNavigateToPaywall(2)
                else onAnnounceTitleTtsChange(!announceTitleTts)
            },
            trailingContent = {
                Switch(
                    checked = announceTitleTts,
                    onCheckedChange = { enabled ->
                        if (!isPro && enabled) onNavigateToPaywall(2)
                        else onAnnounceTitleTtsChange(enabled)
                    }
                )
            }
        )

        ReMindSettingsDivider()

        // 8. Extra Loud (Pro)
        ReMindSettingsItem(
            title = stringResource(R.string.extra_loud_title),
            subtitle = stringResource(R.string.extra_loud_desc),
            icon = ReMindIcons.VolumeUp,
            iconTint = if (extraLoudEnabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            titleBadge = { ReMindProDiamondTag() },
            onClick = {
                if (!isPro && !extraLoudEnabled) onNavigateToPaywall(4)
                else onExtraLoudChange(!extraLoudEnabled)
            },
            trailingContent = {
                Switch(
                    checked = extraLoudEnabled,
                    onCheckedChange = { enabled ->
                        if (!isPro && enabled) onNavigateToPaywall(4)
                        else onExtraLoudChange(enabled)
                    }
                )
            }
        )
    }
}

@Preview(name = "Alert Config Full Screen - Light", showBackground = true)
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

@Preview(name = "Alert Config Standard - Light", showBackground = true)
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

@Preview(
    name = "Alert Config - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0xFF19120CL
)
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
