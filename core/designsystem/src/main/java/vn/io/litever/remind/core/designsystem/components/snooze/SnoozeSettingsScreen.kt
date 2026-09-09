package vn.io.litever.remind.core.designsystem.components.snooze

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteVerButtonDefaults
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.R
import vn.io.litever.remind.core.designsystem.components.ReMindBottomBar
import vn.io.litever.remind.core.designsystem.components.ReMindTopAppBar
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme

@Composable
fun SnoozeSettingsRoute(
    initialEnabled: Boolean,
    initialInterval: Int,
    initialRepeatCount: Int,
    onBackClick: () -> Unit,
    onSave: (Boolean, Int, Int) -> Unit
) {
    var enabled by remember { mutableStateOf(initialEnabled) }
    var interval by remember { mutableIntStateOf(initialInterval) }
    var repeatCount by remember { mutableIntStateOf(initialRepeatCount) }

    BackHandler {
        onSave(enabled, interval, repeatCount)
    }

    SnoozeSettingsScreen(
        enabled = enabled,
        interval = interval,
        repeatCount = repeatCount,
        onEnabledChange = { enabled = it },
        onIntervalChange = { interval = it },
        onRepeatCountChange = { repeatCount = it },
        onBackClick = {
            onSave(enabled, interval, repeatCount)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnoozeSettingsScreen(
    enabled: Boolean,
    interval: Int,
    repeatCount: Int,
    onEnabledChange: (Boolean) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onRepeatCountChange: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val intervalOptions = listOf(1, 3, 5, 10, 30)
    val repeatOptions = listOf(-1, 1, 3, 5, 10)

    Scaffold(
        topBar = {
            ReMindTopAppBar(
                title = stringResource(R.string.snooze_settings),
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            ReMindBottomBar {
                Button(
                    onClick = onBackClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(LiteverTheme.spacing.doubleLarge),
                    shape = LiteVerButtonDefaults.shape,
                    colors = LiteVerButtonDefaults.primaryColors()
                ) {
                    Text(
                        stringResource(R.string.save),
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = LiteverTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
        ) {
            item { Spacer(modifier = Modifier.height(LiteverTheme.spacing.small)) }

            // Enable Toggle Card
            item {
                OutlinedCard(
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
                            text = stringResource(R.string.enable_snooze),
                            style = LiteverTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = enabled,
                            onCheckedChange = onEnabledChange
                        )
                    }
                }
            }

            if (enabled) {
                // Interval Selection Card
                item {
                    Column {
                        Text(
                            text = stringResource(R.string.snooze_interval),
                            style = LiteverTheme.typography.titleSmall,
                            color = LiteverTheme.colors.primary,
                            modifier = Modifier.padding(bottom = LiteverTheme.spacing.smallMedium)
                        )
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(
                                    vertical = LiteverTheme.spacing.smallMedium,
                                    horizontal = LiteverTheme.spacing.small
                                )
                            ) {
                                intervalOptions.forEach { option ->
                                    SnoozeOptionRow(
                                        label = stringResource(R.string.minutes_unit, option),
                                        isSelected = interval == option,
                                        onClick = { onIntervalChange(option) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Repeat Selection Card (Maximum Repeat Count)
                item {
                    Column {
                        Text(
                            text = stringResource(R.string.snooze_repeat),
                            style = LiteverTheme.typography.titleSmall,
                            color = LiteverTheme.colors.primary,
                            modifier = Modifier.padding(bottom = LiteverTheme.spacing.smallMedium)
                        )
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(
                                    vertical = LiteverTheme.spacing.smallMedium,
                                    horizontal = LiteverTheme.spacing.small
                                )
                            ) {
                                repeatOptions.forEach { option ->
                                    val label = when (option) {
                                        -1 -> stringResource(R.string.forever)
                                        1 -> stringResource(R.string.one_time)
                                        else -> stringResource(R.string.times_unit, option)
                                    }

                                    SnoozeOptionRow(
                                        label = label,
                                        isSelected = repeatCount == option,
                                        onClick = { onRepeatCountChange(option) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(LiteverTheme.spacing.extraLarge)) }
        }
    }
}

@Composable
fun SnoozeOptionRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = LiteverTheme.spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(LiteverTheme.spacing.medium))
        Text(
            text = label,
            style = LiteverTheme.typography.bodyLarge,
            color = LiteverTheme.colors.onSurface
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun SnoozeSettingsScreenPreview() {
    ReMindTheme {
        SnoozeSettingsScreen(
            enabled = true,
            interval = 5,
            repeatCount = 3,
            onEnabledChange = {},
            onIntervalChange = {},
            onRepeatCountChange = {},
            onBackClick = {}
        )
    }
}
