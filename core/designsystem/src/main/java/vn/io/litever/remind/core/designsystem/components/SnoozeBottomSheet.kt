package vn.io.litever.remind.core.designsystem.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.remind.core.designsystem.R
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnoozeBottomSheet(
    enabled: Boolean,
    interval: Int,
    onEnabledChange: (Boolean) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    repeatCount: Int? = null,
    onRepeatCountChange: ((Int) -> Unit)? = null,
    sheetState: SheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        ReMindBottomSheetContent(
            title = stringResource(R.string.snooze_settings)
        ) {
            SnoozeBottomSheetContent(
                enabled = enabled,
                interval = interval,
                onEnabledChange = onEnabledChange,
                onIntervalChange = onIntervalChange,
                repeatCount = repeatCount,
                onRepeatCountChange = onRepeatCountChange
            )
        }
    }
}

@Composable
fun SnoozeBottomSheetContent(
    enabled: Boolean,
    interval: Int,
    onEnabledChange: (Boolean) -> Unit,
    onIntervalChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    repeatCount: Int? = null,
    onRepeatCountChange: ((Int) -> Unit)? = null
) {
    val intervalOptions = listOf(1, 3, 5, 10, 30)
    val repeatOptions = listOf(-1, 1, 3, 5, 10)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.small, horizontal = MaterialTheme.spacing.medium)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
    ) {
        ReMindGroupCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEnabledChange(!enabled) }
                    .padding(MaterialTheme.spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.enable_snooze),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange
                )
            }
        }

        ReMindSettingsGroup(
            title = stringResource(R.string.snooze_interval)
        ) {
            val chunkedIntervals = intervalOptions.chunked(3)
            chunkedIntervals.forEach { rowOptions ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    rowOptions.forEach { option ->
                        val isSelected = interval == option
                        val buttonContent: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
                            Text(stringResource(R.string.minutes_unit, option))
                        }
                        if (isSelected) {
                            FilledTonalButton(
                                onClick = { if (enabled) onIntervalChange(option) },
                                modifier = Modifier.weight(1f),
                                content = buttonContent
                            )
                        } else {
                            OutlinedButton(
                                onClick = { if (enabled) onIntervalChange(option) },
                                modifier = Modifier.weight(1f),
                                content = buttonContent
                            )
                        }
                    }
                    repeat(3 - rowOptions.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        }

        if (repeatCount != null && onRepeatCountChange != null) {
            ReMindSettingsGroup(
                title = stringResource(R.string.snooze_repeat)
            ) {
                val chunkedRepeats = repeatOptions.chunked(3)
                chunkedRepeats.forEach { rowOptions ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MaterialTheme.spacing.medium),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        rowOptions.forEach { option ->
                            val isSelected = repeatCount == option
                            val label = when (option) {
                                -1 -> stringResource(R.string.forever)
                                1 -> stringResource(R.string.one_time)
                                else -> stringResource(
                                    R.string.times_unit,
                                    option
                                )
                            }
                            val buttonContent: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                )
                            }
                            if (isSelected) {
                                FilledTonalButton(
                                    onClick = { if (enabled) onRepeatCountChange(option) },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                                    content = buttonContent
                                )
                            } else {
                                OutlinedButton(
                                    onClick = { if (enabled) onRepeatCountChange(option) },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                                    content = buttonContent
                                )
                            }
                        }
                        repeat(3 - rowOptions.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SnoozeBottomSheetAlarmPreview() {
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

@Preview(showBackground = true)
@Composable
private fun SnoozeBottomSheetReminderPreview() {
    ReMindTheme {
        SnoozeBottomSheetContent(
            enabled = true,
            interval = 5,
            onEnabledChange = {},
            onIntervalChange = {}
        )
    }
}
