package vn.io.litever.remind.features.alarms.ui.components

import androidx.compose.material3.MaterialTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.remind.core.designsystem.components.ReMindBottomSheetContent
import vn.io.litever.remind.core.designsystem.components.ReMindProDiamondTag
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.features.alarms.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GentleAlarmBottomSheet(
    currentDuration: Int,
    extraLoudEnabled: Boolean = false,
    announceTimeTts: Boolean = false,
    isPro: Boolean = false,
    onPreviewGentle: (Int) -> Unit = {},
    onPreviewExtraLoud: () -> Unit = {},
    onPreviewTimeTts: () -> Unit = {},
    onStopPreview: () -> Unit = {},
    onSave: (durationSeconds: Int, extraLoud: Boolean, announceTime: Boolean) -> Unit,
    onNavigateToPaywall: (Int) -> Unit = {},
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
) {
    var draftDuration by remember(currentDuration) { mutableIntStateOf(currentDuration) }
    var draftExtraLoud by remember(extraLoudEnabled) { mutableStateOf(extraLoudEnabled) }
    var draftAnnounceTime by remember(announceTimeTts) { mutableStateOf(announceTimeTts) }

    val requiresPro = !isPro && (draftExtraLoud || draftAnnounceTime)

    DisposableEffect(Unit) {
        onDispose { onStopPreview() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        ReMindBottomSheetContent(
            title = stringResource(R.string.volume_boost_sheet_title)
        ) {
            GentleAlarmBottomSheetContent(
                durationSeconds = draftDuration,
                onDurationChange = { draftDuration = it },
                extraLoudEnabled = draftExtraLoud,
                onExtraLoudToggle = { draftExtraLoud = it },
                announceTimeTts = draftAnnounceTime,
                onAnnounceTimeToggle = { draftAnnounceTime = it },
                requiresPro = requiresPro,
                onPreviewGentle = { onPreviewGentle(draftDuration) },
                onPreviewExtraLoud = onPreviewExtraLoud,
                onPreviewTimeTts = onPreviewTimeTts,
                onSaveClick = {
                    if (requiresPro) {
                        val targetSlide = if (draftExtraLoud) 4 else 2
                        onDismissRequest()
                        onNavigateToPaywall(targetSlide)
                    } else {
                        onSave(draftDuration, draftExtraLoud, draftAnnounceTime)
                        onDismissRequest()
                    }
                }
            )
        }
    }
}

@Composable
fun GentleAlarmBottomSheetContent(
    durationSeconds: Int,
    onDurationChange: (Int) -> Unit,
    extraLoudEnabled: Boolean,
    onExtraLoudToggle: (Boolean) -> Unit,
    announceTimeTts: Boolean,
    onAnnounceTimeToggle: (Boolean) -> Unit,
    requiresPro: Boolean,
    onPreviewGentle: () -> Unit,
    onPreviewExtraLoud: () -> Unit,
    onPreviewTimeTts: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val durationOptions = listOf(5, 10, 20)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium)
            .padding(bottom = MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium)
    ) {
        VolumeBoostOptionRow(
            icon = ReMindIcons.GraphicEq,
            title = stringResource(R.string.gentle_alarm_title),
            description = stringResource(R.string.gentle_alarm_description),
            isProFeature = false,
            checked = durationSeconds > 0,
            onCheckedChange = { enabled -> onDurationChange(if (enabled) 10 else 0) },
            onPreviewClick = onPreviewGentle,
            extraContent = {
                if (durationSeconds > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 34.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        durationOptions.forEach { seconds ->
                            FilterChip(
                                selected = durationSeconds == seconds,
                                onClick = { onDurationChange(seconds) },
                                label = {
                                    Text(
                                        text = stringResource(R.string.seconds_unit_short, seconds),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            )
                        }
                    }
                }
            }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        VolumeBoostOptionRow(
            icon = ReMindIcons.VolumeUp,
            title = stringResource(R.string.extra_loud_title),
            description = stringResource(R.string.extra_loud_desc),
            isProFeature = true,
            checked = extraLoudEnabled,
            onCheckedChange = onExtraLoudToggle,
            onPreviewClick = onPreviewExtraLoud
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        VolumeBoostOptionRow(
            icon = ReMindIcons.AccessTime,
            title = stringResource(R.string.time_announcement_title),
            description = stringResource(R.string.time_announcement_desc),
            isProFeature = true,
            checked = announceTimeTts,
            onCheckedChange = onAnnounceTimeToggle,
            onPreviewClick = onPreviewTimeTts
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (requiresPro) {
                Icon(
                    imageVector = ReMindIcons.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = stringResource(R.string.save),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun VolumeBoostOptionRow(
    icon: ImageVector,
    title: String,
    description: String,
    isProFeature: Boolean,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onPreviewClick: () -> Unit,
    extraContent: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isProFeature) {
                        ReMindProDiamondTag()
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }

        extraContent?.invoke()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            FilledTonalButton(
                onClick = onPreviewClick
            ) {
                Icon(
                    imageVector = ReMindIcons.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.action_preview),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GentleAlarmBottomSheetContentPreview() {
    ReMindTheme {
        GentleAlarmBottomSheetContent(
            durationSeconds = 10,
            onDurationChange = {},
            extraLoudEnabled = true,
            onExtraLoudToggle = {},
            announceTimeTts = false,
            onAnnounceTimeToggle = {},
            requiresPro = true,
            onPreviewGentle = {},
            onPreviewExtraLoud = {},
            onPreviewTimeTts = {},
            onSaveClick = {}
        )
    }
}
