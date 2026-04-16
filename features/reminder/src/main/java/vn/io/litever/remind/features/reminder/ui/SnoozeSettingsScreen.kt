package vn.io.litever.remind.features.reminder.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.remind.core.designsystem.components.ReMindScaffold
import vn.io.litever.remind.core.designsystem.components.ReMindTopAppBar
import vn.io.litever.remind.features.reminder.R

@Composable
fun SnoozeSettingsRoute(
    initialEnabled: Boolean,
    initialInterval: Int,
    initialRepeatCount: Int,
    onBackClick: () -> Unit,
    onSave: (Boolean, Int, Int) -> Unit
) {
    var enabled by remember { mutableStateOf(initialEnabled) }
    var interval by remember { mutableStateOf(initialInterval) }
    var repeatCount by remember { mutableStateOf(initialRepeatCount) }

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

    ReMindScaffold(
        topBar = {
            ReMindTopAppBar(
                title = stringResource(R.string.snooze_settings),
                onBackClick = onBackClick
            )
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = MaterialTheme.shapes.large
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
                            style = MaterialTheme.typography.bodyLarge,
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
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Column {
                                intervalOptions.forEachIndexed { index, option ->
                                    SnoozeOptionRow(
                                        label = stringResource(R.string.minutes_unit, option),
                                        isSelected = interval == option,
                                        onClick = { onIntervalChange(option) }
                                    )
                                    if (index < intervalOptions.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                                        )
                                    }
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
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Column {
                                repeatOptions.forEachIndexed { index, option ->
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
                                    if (index < repeatOptions.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                                        )
                                    }
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
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
