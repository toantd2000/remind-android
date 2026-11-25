package vn.io.litever.remind.core.designsystem.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.remind.core.designsystem.R
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing

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
            .padding(
                horizontal = MaterialTheme.spacing.medium,
                vertical = MaterialTheme.spacing.small
            )
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium)
    ) {
        ReMindGroupCard(modifier = Modifier.fillMaxWidth()) {
            ReMindSettingsItem(
                title = stringResource(R.string.enable_snooze),
                icon = ReMindIcons.Snooze,
                iconTint = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                onClick = { onEnabledChange(!enabled) },
                trailingContent = {
                    Switch(
                        checked = enabled,
                        onCheckedChange = onEnabledChange
                    )
                }
            )
        }

        AnimatedVisibility(visible = enabled) {
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium)
            ) {
                ReMindSettingsGroup(
                    title = stringResource(R.string.snooze_interval)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = MaterialTheme.spacing.medium,
                                vertical = MaterialTheme.spacing.smallMedium
                            ),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        intervalOptions.chunked(3).forEach { rowOptions ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                            ) {
                                rowOptions.forEach { option ->
                                    SnoozeOptionButton(
                                        label = stringResource(R.string.minutes_unit, option),
                                        selected = interval == option,
                                        onClick = { onIntervalChange(option) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(3 - rowOptions.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                if (repeatCount != null && onRepeatCountChange != null) {
                    ReMindSettingsGroup(
                        title = stringResource(R.string.snooze_repeat)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = MaterialTheme.spacing.medium,
                                    vertical = MaterialTheme.spacing.smallMedium
                                ),
                            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                        ) {
                            repeatOptions.chunked(3).forEach { rowOptions ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                                ) {
                                    rowOptions.forEach { option ->
                                        val label = when (option) {
                                            -1 -> stringResource(R.string.forever)
                                            1 -> stringResource(R.string.one_time)
                                            else -> stringResource(R.string.times_unit, option)
                                        }
                                        SnoozeOptionButton(
                                            label = label,
                                            selected = repeatCount == option,
                                            onClick = { onRepeatCountChange(option) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    repeat(3 - rowOptions.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
    }
}

@Composable
private fun SnoozeOptionButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val content: @Composable RowScope.() -> Unit = {
        Text(text = label, maxLines = 1)
    }
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier,
            contentPadding = ButtonDefaults.TextButtonContentPadding,
            content = content
        )
    } else {
        FilledTonalButton(
            onClick = onClick,
            modifier = modifier,
            contentPadding = ButtonDefaults.TextButtonContentPadding,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            content = content
        )
    }
}

@Preview(name = "Snooze Alarm - Light", showBackground = true)
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

@Preview(
    name = "Snooze Alarm - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0xFF19120CL
)
@Composable
private fun SnoozeBottomSheetAlarmDarkPreview() {
    ReMindTheme(darkTheme = true) {
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

@Preview(name = "Snooze Disabled - Light", showBackground = true)
@Composable
private fun SnoozeBottomSheetDisabledPreview() {
    ReMindTheme {
        SnoozeBottomSheetContent(
            enabled = false,
            interval = 5,
            onEnabledChange = {},
            onIntervalChange = {}
        )
    }
}
