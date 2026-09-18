package vn.io.litever.remind.features.alarms.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.button.LvButtonType
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.ReMindBottomSheetContent
import vn.io.litever.remind.core.designsystem.components.ReMindGroupCard
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnoozeBottomSheet(
    enabled: Boolean,
    interval: Int,
    repeatCount: Int,
    onEnabledChange: (Boolean) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onRepeatCountChange: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = LiteverTheme.colors.surface,
        modifier = modifier
    ) {
        ReMindBottomSheetContent(
            title = stringResource(vn.io.litever.remind.core.designsystem.R.string.snooze_settings)
        ) {
            SnoozeBottomSheetContent(
                enabled = enabled,
                interval = interval,
                repeatCount = repeatCount,
                onEnabledChange = onEnabledChange,
                onIntervalChange = onIntervalChange,
                onRepeatCountChange = onRepeatCountChange
            )
        }
    }
}

@Composable
fun SnoozeBottomSheetContent(
    enabled: Boolean,
    interval: Int,
    repeatCount: Int,
    onEnabledChange: (Boolean) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onRepeatCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val intervalOptions = listOf(1, 3, 5, 10, 30)
    val repeatOptions = listOf(-1, 1, 3, 5, 10)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = LiteverTheme.spacing.small)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
    ) {
        ReMindGroupCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEnabledChange(!enabled) }
                    .padding(LiteverTheme.spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(vn.io.litever.remind.core.designsystem.R.string.enable_snooze),
                    style = LiteverTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange
                )
            }
        }

        ReMindSettingsGroup(
            title = stringResource(vn.io.litever.remind.core.designsystem.R.string.snooze_interval)
        ) {
            val chunkedIntervals = intervalOptions.chunked(3)
            chunkedIntervals.forEach { rowOptions ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LiteverTheme.spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
                ) {
                    rowOptions.forEach { option ->
                        val isSelected = interval == option
                        LvButton(
                            onClick = { if (enabled) onIntervalChange(option) },
                            modifier = Modifier.weight(1f),
                            type = if (isSelected) LvButtonType.Tonal else LvButtonType.Outlined,
                            semantic = if (isSelected) LvSemantic.Primary else LvSemantic.Neutral,
                        ) {
                            Text(
                                stringResource(
                                    vn.io.litever.remind.core.designsystem.R.string.minutes_unit,
                                    option
                                ),
                            )
                        }
                    }
                    repeat(3 - rowOptions.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(LiteverTheme.spacing.medium))
        }

        ReMindSettingsGroup(
            title = stringResource(vn.io.litever.remind.core.designsystem.R.string.snooze_repeat)
        ) {
            val chunkedRepeats = repeatOptions.chunked(3)
            chunkedRepeats.forEach { rowOptions ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LiteverTheme.spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
                ) {
                    rowOptions.forEach { option ->
                        val isSelected = repeatCount == option
                        val label = when (option) {
                            -1 -> stringResource(vn.io.litever.remind.core.designsystem.R.string.forever)
                            1 -> stringResource(vn.io.litever.remind.core.designsystem.R.string.one_time)
                            else -> stringResource(
                                vn.io.litever.remind.core.designsystem.R.string.times_unit,
                                option
                            )
                        }
                        LvButton(
                            onClick = { if (enabled) onRepeatCountChange(option) },
                            modifier = Modifier.weight(1f),
                            contentPadding = ButtonDefaults.TextButtonContentPadding,
                            type = if (isSelected) LvButtonType.Tonal else LvButtonType.Outlined,
                            semantic = if (isSelected) LvSemantic.Primary else LvSemantic.Neutral,
                        ) {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                            )
                        }
                    }
                    repeat(3 - rowOptions.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(LiteverTheme.spacing.medium))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SnoozeBottomSheetPreview() {
    ReMindTheme {
        SnoozeBottomSheetContent(
            enabled = true,
            interval = 5,
            repeatCount = 3,
            onEnabledChange = {},
            onIntervalChange = {},
            onRepeatCountChange = {}
        )
    }
}
