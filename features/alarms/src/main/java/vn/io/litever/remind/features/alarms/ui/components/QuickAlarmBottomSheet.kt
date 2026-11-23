package vn.io.litever.remind.features.alarms.ui.components

import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.ReMindBottomSheetContent
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = LiteverTheme.colors.surface,
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
            .padding(horizontal = LiteverTheme.spacing.mediumLarge)
            .padding(bottom = LiteverTheme.spacing.large)
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
                style = LiteverTheme.typography.displayMedium.copy(
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                ),
                color = LiteverTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.width(LiteverTheme.spacing.small))

            IconButton(
                onClick = { totalMinutes = 0 },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = ReMindIcons.Refresh,
                    contentDescription = stringResource(R.string.reset),
                    tint = LiteverTheme.colors.onSurfaceVariant
                )
            }
        }

        // Subtitle: Target ring time calculation
        Text(
            text = stringResource(R.string.quick_alarm_ring_at, formattedRingTime),
            style = LiteverTheme.typography.labelMedium,
            color = LiteverTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(bottom = LiteverTheme.spacing.mediumLarge)
        )

        // 2. Preset Time Grid (2 rows x 3 buttons)
        val presetsRow1 = listOf(1 to R.string.quick_alarm_1_min, 5 to R.string.quick_alarm_5_min, 10 to R.string.quick_alarm_10_min)
        val presetsRow2 = listOf(15 to R.string.quick_alarm_15_min, 30 to R.string.quick_alarm_30_min, 60 to R.string.quick_alarm_1_hour)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
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

        Spacer(modifier = Modifier.height(LiteverTheme.spacing.medium))

        // 3. Optional Label (description) input field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(LiteverTheme.colors.surfaceContainer)
                .padding(
                    horizontal = LiteverTheme.spacing.medium,
                    vertical = LiteverTheme.spacing.smallMedium
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = ReMindIcons.NewLabel,
                    contentDescription = null,
                    tint = LiteverTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(LiteverTheme.spacing.small))

                BasicTextField(
                    value = label,
                    onValueChange = { label = it },
                    modifier = Modifier.weight(1f),
                    textStyle = LiteverTheme.typography.bodyLarge.copy(color = LiteverTheme.colors.onSurface),
                    singleLine = true,
                    cursorBrush = SolidColor(LiteverTheme.colors.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    decorationBox = { innerTextField ->
                        if (label.isEmpty()) {
                            Text(
                                text = stringResource(R.string.quick_alarm_desc_placeholder),
                                style = LiteverTheme.typography.bodyLarge,
                                color = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    }
                )

                if (label.isNotEmpty()) {
                    IconButton(
                        onClick = { label = "" },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = ReMindIcons.Close,
                            contentDescription = stringResource(R.string.close_text),
                            tint = LiteverTheme.colors.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 4. Sound & Vibration settings group
        Spacer(modifier = Modifier.height(LiteverTheme.spacing.small))
        ReMindSettingsGroup {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showRingtoneDialog = true }
                    .padding(top = LiteverTheme.spacing.medium)
                    .padding(horizontal = LiteverTheme.spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.alarm_sound),
                    style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = LiteverTheme.colors.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = displayRingtoneTitle,
                    style = LiteverTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = LiteverTheme.colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(LiteverTheme.spacing.extraSmall))

                Icon(
                    imageVector = ReMindIcons.ChevronRight,
                    contentDescription = null,
                    tint = LiteverTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Row: Volume slider & Vibration toggle switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LiteverTheme.spacing.medium)
                    .padding(top = LiteverTheme.spacing.small, bottom = LiteverTheme.spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (volume == 0) ReMindIcons.VolumeOff else ReMindIcons.VolumeUp,
                    contentDescription = stringResource(R.string.volume),
                    tint = LiteverTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )

                Slider(
                    value = volume.toFloat(),
                    onValueChange = { volume = it.roundToInt() },
                    valueRange = 0f..maxVolume.toFloat(),
                    steps = if (maxVolume > 1) maxVolume - 1 else 0,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = LiteverTheme.spacing.small),
                    colors = SliderDefaults.colors(
                        thumbColor = LiteverTheme.colors.onSurface,
                        activeTrackColor = LiteverTheme.colors.onSurface,
                        inactiveTrackColor = LiteverTheme.colors.surfaceContainerHighest
                    )
                )

                // Small vertical separator
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .width(1.dp)
                        .background(LiteverTheme.colors.outlineVariant.copy(alpha = 0.5f))
                )

                Spacer(modifier = Modifier.width(LiteverTheme.spacing.smallMedium))

                Icon(
                    imageVector = ReMindIcons.Vibration,
                    contentDescription = stringResource(R.string.vibration),
                    tint = if (vibrationEnabled) LiteverTheme.colors.primary else LiteverTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(LiteverTheme.spacing.small))

                Switch(
                    checked = vibrationEnabled,
                    onCheckedChange = { vibrationEnabled = it }
                )
            }
        }



        Spacer(modifier = Modifier.height(LiteverTheme.spacing.medium))

        // 5. Action Buttons (Cancel & Set Alarm)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
        ) {
            OutlinedButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.action_cancel),
                )
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
                Text(
                    text = stringResource(R.string.set_alarm),
                )
            }
        }
    }

    // Simple Ringtone Selection Dialog
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
                androidx.compose.foundation.lazy.LazyColumn {
                    items(ringtones.size) { index ->
                        val (uri, title) = ringtones[index]
                        val isSelected = (uri == currentUri)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(uri, title) }
                                .padding(vertical = LiteverTheme.spacing.smallMedium),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = title,
                                style = LiteverTheme.typography.bodyLarge,
                                color = if (isSelected) LiteverTheme.colors.primary else LiteverTheme.colors.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = ReMindIcons.Check,
                                    contentDescription = null,
                                    tint = LiteverTheme.colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        if (index < ringtones.size - 1) {
                            HorizontalDivider(color = LiteverTheme.colors.outlineVariant.copy(alpha = 0.2f))
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

@Preview(showBackground = true)
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
