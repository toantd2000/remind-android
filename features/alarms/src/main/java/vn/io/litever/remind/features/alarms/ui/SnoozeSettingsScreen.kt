package vn.io.litever.remind.features.alarms.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.CardDefaults
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
import vn.io.litever.designsystem.components.LiteverButton
import vn.io.litever.designsystem.components.LiteverCard
import vn.io.litever.designsystem.components.LiteverScaffold
import vn.io.litever.designsystem.components.LiteverSwitch
import vn.io.litever.designsystem.components.LiteverTopAppBar
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.ReMindBottomBar
import vn.io.litever.remind.features.alarms.R

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

    LiteverScaffold(
        topBar = {
            LiteverTopAppBar(
                title = stringResource(R.string.snooze_settings),
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            ReMindBottomBar {
                LiteverButton(
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Enable Toggle Card
            item {
                LiteverCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    border = BorderStroke(1.dp, LiteverTheme.colors.outlineVariant.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEnabledChange(!enabled) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.enable_snooze),
                            style = LiteverTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        LiteverSwitch(
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
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        LiteverCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            border = BorderStroke(1.dp, LiteverTheme.colors.outlineVariant.copy(alpha = 0.2f))
                        ) {
                            Column {
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

                // Repeat Selection Card
                item {
                    Column {
                        Text(
                            text = stringResource(R.string.snooze_repeat),
                            style = LiteverTheme.typography.titleSmall,
                            color = LiteverTheme.colors.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        LiteverCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            shape = LiteverTheme.shapes.medium,
                            border = BorderStroke(1.dp, LiteverTheme.colors.outlineVariant.copy(alpha = 0.2f))
                        ) {
                            Column {
                                repeatOptions.forEach { option ->
                                    val label = if (option == -1) {
                                        stringResource(R.string.forever)
                                    } else if (option == 1) {
                                        stringResource(R.string.one_time)
                                    } else {
                                        stringResource(R.string.times_unit, option)
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
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        vn.io.litever.designsystem.components.LiteverRadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(16.dp))
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
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme {
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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SnoozeSettingsScreenDarkPreview() {
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme(darkTheme = true) {
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










