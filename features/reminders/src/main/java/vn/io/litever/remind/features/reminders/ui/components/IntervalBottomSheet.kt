package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.button.LvButtonType
import vn.io.litever.designsystem.components.button.LvIconButton
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.ReMindBottomSheetContent
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.features.reminders.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntervalBottomSheet(
    currentIntervalMinutes: Int,
    onDismissRequest: () -> Unit,
    onConfirm: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = LiteverTheme.colors.surface,
        modifier = modifier
    ) {
        ReMindBottomSheetContent(
            title = stringResource(R.string.interval_section_title)
        ) {
            IntervalBottomSheetContent(
                currentIntervalMinutes = currentIntervalMinutes,
                onDismiss = onDismissRequest,
                onConfirm = onConfirm
            )
        }
    }
}

@Composable
fun IntervalBottomSheetContent(
    currentIntervalMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMinutes by remember { mutableIntStateOf(currentIntervalMinutes) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = LiteverTheme.spacing.mediumLarge)
            .padding(bottom = LiteverTheme.spacing.large)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display selected time (similar to QuickAlarmBottomSheet)
        val hours = selectedMinutes / 60
        val minutes = selectedMinutes % 60
        val displayDuration = when {
            selectedMinutes <= 0 -> "0m"
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = displayDuration,
                style = LiteverTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = LiteverTheme.colors.onSurface
            )

            if (selectedMinutes > 0) {
                Spacer(modifier = Modifier.padding(start = LiteverTheme.spacing.small))
                LvIconButton(
                    onClick = { selectedMinutes = 0 },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = ReMindIcons.Refresh,
                        contentDescription = stringResource(R.string.reset),
                        tint = LiteverTheme.colors.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(LiteverTheme.spacing.mediumLarge))

        // Preset grid (chunked by 3)
        val presets = listOf(
            15 to stringResource(R.string.interval_preset_15m),
            30 to stringResource(R.string.interval_preset_30m),
            60 to stringResource(R.string.interval_preset_1h),
            120 to stringResource(R.string.interval_preset_2h),
            180 to stringResource(R.string.interval_preset_3h),
            240 to stringResource(R.string.interval_preset_4h)
        )

        val chunkedPresets = presets.chunked(3)
        chunkedPresets.forEach { rowPresets ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = LiteverTheme.spacing.extraSmall),
                horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
            ) {
                rowPresets.forEach { (mins, label) ->
                    val isSelected = selectedMinutes == mins
                    LvButton(
                        onClick = { selectedMinutes = mins },
                        modifier = Modifier.weight(1f),
                        type = if (isSelected) LvButtonType.Tonal else LvButtonType.Outlined,
                        semantic = if (isSelected) LvSemantic.Primary else LvSemantic.Secondary
                    ) {
                        Text(
                            text = label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
                repeat(3 - rowPresets.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(LiteverTheme.spacing.large))

        // Action Buttons (Cancel & Save / Set Interval)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
        ) {
            LvButton(
                onClick = onDismiss,
                type = LvButtonType.Outlined,
                semantic = LvSemantic.Secondary
            ) {
                Text(text = stringResource(R.string.action_cancel))
            }

            LvButton(
                onClick = { onConfirm(selectedMinutes) },
                semantic = LvSemantic.Primary,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(R.string.action_save))
            }
        }
    }
}
