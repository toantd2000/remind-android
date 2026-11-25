package vn.io.litever.remind.features.alarms.ui.components

import android.content.Context
import android.content.res.Configuration
import android.media.AudioManager
import android.media.RingtoneManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.brand.tokens.LiteverTokens
import vn.io.litever.remind.core.designsystem.components.ReMindBottomSheetContent
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsDivider
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.features.alarms.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAlarmBottomSheet(
    onDismissRequest: () -> Unit,
    onSave: (minutes: Int, label: String, ringtoneUri: String?, volume: Int, vibrationEnabled: Boolean) -> Unit,
    is24HourFormat: Boolean,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        ReMindBottomSheetContent(
            title = stringResource(R.string.quick_alarm),
        ) {
            QuickAlarmBottomSheetContent(
                onDismiss = onDismissRequest,
                onSave = onSave,
                is24HourFormat = is24HourFormat
            )
        }
    }
}

@Composable
fun QuickAlarmBottomSheetContent(
    onDismiss: () -> Unit,
    onSave: (minutes: Int, label: String, ringtoneUri: String?, volume: Int, vibrationEnabled: Boolean) -> Unit,
    is24HourFormat: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) }

    var totalMinutes by remember { mutableIntStateOf(0) }
    var label by remember { mutableStateOf("") }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var volume by remember { mutableIntStateOf(maxVolume.coerceAtLeast(1)) }
    var ringtoneUri by remember { mutableStateOf<String?>(null) }
    var ringtoneTitle by remember { mutableStateOf<String?>(null) }
    val defaultRingtoneTitle = stringResource(R.string.default_ringtone)
    val displayRingtoneTitle = ringtoneTitle ?: defaultRingtoneTitle
    var showRingtoneDialog by remember { mutableStateOf(false) }

    val ringAtTime = remember(totalMinutes) {
        LocalDateTime.now().plusMinutes(totalMinutes.toLong())
    }
    val pattern = if (is24HourFormat) "HH:mm" else "h:mm a"
    val timeFormatter = remember(pattern) { DateTimeFormatter.ofPattern(pattern) }
    val formattedRingTime = ringAtTime.format(timeFormatter)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium)
            .padding(bottom = MaterialTheme.spacing.medium)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Increment Display (+ 0h / + 0m) and Reset button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            val displayText = when {
                hours > 0 && minutes > 0 -> "+ ${hours}h ${minutes}m"
                hours > 0 -> "+ ${hours}h"
                else -> "+ ${totalMinutes}m"
            }

            Text(
                text = displayText,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

            IconButton(
                onClick = { totalMinutes = 0 },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = ReMindIcons.Refresh,
                    contentDescription = stringResource(R.string.reset),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Subtitle: Target ring time calculation
        Text(
            text = stringResource(R.string.quick_alarm_ring_at, formattedRingTime),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium)
        )

        // 2. Preset Time Grid (2 rows x 3 buttons)
        val presetsRow1 = listOf(1 to R.string.quick_alarm_1_min, 5 to R.string.quick_alarm_5_min, 10 to R.string.quick_alarm_10_min)
        val presetsRow2 = listOf(15 to R.string.quick_alarm_15_min, 30 to R.string.quick_alarm_30_min, 60 to R.string.quick_alarm_1_hour)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            presetsRow1.forEach { (mins, resId) ->
                FilledTonalButton(
                    onClick = { totalMinutes += mins },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(resId))
                }
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            presetsRow2.forEach { (mins, resId) ->
                FilledTonalButton(
                    onClick = { totalMinutes += mins },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(resId))
                }
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // 3. Optional Label input field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(
                    horizontal = MaterialTheme.spacing.medium,
                    vertical = MaterialTheme.spacing.smallMedium
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = ReMindIcons.NewLabel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(MaterialTheme.spacing.mediumLarge)
                )

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

                val labelState = rememberTextFieldState(label)
                LaunchedEffect(label) {
                    if (labelState.text.toString() != label) {
                        labelState.setTextAndPlaceCursorAtEnd(label)
                    }
                }
                LaunchedEffect(labelState) {
                    snapshotFlow { labelState.text.toString() }.collect {
                        if (it != label) {
                            label = it
                        }
                    }
                }
                BasicTextField(
                    state = labelState,
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    lineLimits = TextFieldLineLimits.SingleLine,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    decorator = { innerTextField ->
                        if (labelState.text.isEmpty()) {
                            Text(
                                text = stringResource(R.string.quick_alarm_desc_placeholder),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    }
                )

                if (labelState.text.isNotEmpty()) {
                    IconButton(
                        onClick = { labelState.clearText() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = ReMindIcons.Close,
                            contentDescription = stringResource(R.string.close_text),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(MaterialTheme.spacing.medium)
                        )
                    }
                }
            }
        }

        // 4. Sound & Vibration settings group
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.smallMedium))
        ReMindSettingsGroup {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showRingtoneDialog = true }
                    .padding(
                        horizontal = MaterialTheme.spacing.medium,
                        vertical = MaterialTheme.spacing.smallMedium
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.alarm_sound),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = displayRingtoneTitle,
                    style = LiteverTokens.typography.bodyMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))

                Icon(
                    imageVector = ReMindIcons.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(MaterialTheme.spacing.mediumLarge)
                )
            }

            ReMindSettingsDivider(inset = false)

            // Row: Volume slider & Vibration toggle switch
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
                    contentDescription = stringResource(R.string.volume),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(MaterialTheme.spacing.large)
                )

                val volumeSliderState = remember(maxVolume) {
                    SliderState(
                        value = volume.toFloat(),
                        trackRange = 0f..maxVolume.toFloat(),
                        steps = if (maxVolume > 1) maxVolume - 1 else 0
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
                        volume = it.roundToInt()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = MaterialTheme.spacing.small),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                // Small vertical separator
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width(0.5.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                )

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.smallMedium))

                Icon(
                    imageVector = ReMindIcons.Vibration,
                    contentDescription = stringResource(R.string.vibration),
                    tint = if (vibrationEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(MaterialTheme.spacing.large)
                )

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

                Switch(
                    checked = vibrationEnabled,
                    onCheckedChange = { vibrationEnabled = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // 5. Action Buttons (Cancel & Set Alarm)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            OutlinedButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }

            Button(
                onClick = {
                    if (totalMinutes > 0) {
                        onSave(totalMinutes, label, ringtoneUri, volume, vibrationEnabled)
                    }
                },
                enabled = totalMinutes > 0,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(R.string.set_alarm))
            }
        }
    }

    if (showRingtoneDialog) {
        QuickRingtoneDialog(
            currentUri = ringtoneUri,
            onDismiss = { showRingtoneDialog = false },
            onSelect = { uri, title ->
                ringtoneUri = uri
                ringtoneTitle = title
                showRingtoneDialog = false
            }
        )
    }
}

@Composable
private fun QuickRingtoneDialog(
    currentUri: String?,
    onDismiss: () -> Unit,
    onSelect: (uri: String?, title: String) -> Unit
) {
    val context = LocalContext.current
    val defaultRingtoneTitle = stringResource(R.string.default_ringtone)
    val ringtones = remember(defaultRingtoneTitle) {
        val list = mutableListOf<Pair<String?, String>>()
        list.add(null to defaultRingtoneTitle)
        try {
            val manager = RingtoneManager(context)
            manager.setType(RingtoneManager.TYPE_ALARM or RingtoneManager.TYPE_RINGTONE)
            val cursor = manager.cursor
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                    val uri = manager.getRingtoneUri(cursor.position)?.toString()
                    if (title != null && uri != null) {
                        list.add(uri to title)
                    }
                }
            }
        } catch (_: Exception) { }
        list
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.ringtone_selection_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                LazyColumn {
                    items(ringtones.size) { index ->
                        val (uri, title) = ringtones[index]
                        val isSelected = (uri == currentUri)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(uri, title) }
                                .padding(vertical = MaterialTheme.spacing.smallMedium),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = title,
                                style = if (isSelected) {
                                    LiteverTokens.typography.bodyLargeEmphasized
                                } else {
                                    MaterialTheme.typography.bodyLarge
                                },
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = ReMindIcons.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(MaterialTheme.spacing.mediumLarge)
                                )
                            }
                        }
                        if (index < ringtones.size - 1) {
                            ReMindSettingsDivider(inset = false)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.close_text))
            }
        }
    )
}

@Preview(name = "Quick Alarm - Light", showBackground = true)
@Composable
fun QuickAlarmBottomSheetPreview() {
    ReMindTheme {
        QuickAlarmBottomSheetContent(
            onDismiss = {},
            onSave = { _, _, _, _, _ -> },
            is24HourFormat = false
        )
    }
}

@Preview(
    name = "Quick Alarm - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0xFF19120CL
)
@Composable
fun QuickAlarmBottomSheetDarkPreview() {
    ReMindTheme(darkTheme = true) {
        QuickAlarmBottomSheetContent(
            onDismiss = {},
            onSave = { _, _, _, _, _ -> },
            is24HourFormat = true
        )
    }
}
