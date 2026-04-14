package vn.io.litever.remind.features.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.io.litever.remind.core.designsystem.components.ReMindScaffold
import vn.io.litever.remind.features.settings.R

@Composable
fun AlarmSettingsRoute(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    AlarmSettingsScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onSnoozeDurationChange = viewModel::setSnoozeDuration,
        onSilenceDurationChange = viewModel::setSilenceDuration,
        onIncreasingVolumeChange = viewModel::setIncreasingVolume,
        onBuiltInSpeakerChange = viewModel::setBuiltInSpeaker,
        onPreNotificationChange = viewModel::setPreNotificationEnabled
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmSettingsScreen(
    uiState: SettingsUiState,
    onNavigateBack: () -> Unit,
    onSnoozeDurationChange: (Int) -> Unit,
    onSilenceDurationChange: (Int) -> Unit,
    onIncreasingVolumeChange: (Boolean) -> Unit,
    onBuiltInSpeakerChange: (Boolean) -> Unit,
    onPreNotificationChange: (Boolean) -> Unit
) {
    var showSnoozeDialog by remember { mutableStateOf(false) }
    var showSilenceDialog by remember { mutableStateOf(false) }

    ReMindScaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.setting_alarm_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                SettingsSwitchTile(
                    title = stringResource(R.string.increasing_volume_title),
                    subtitle = stringResource(R.string.increasing_volume_desc),
                    checked = uiState.isIncreasingVolume,
                    onCheckedChange = onIncreasingVolumeChange,
                    icon = Icons.Default.VolumeUp
                )
            }
            
            item {
                SettingsSwitchTile(
                    title = stringResource(R.string.built_in_speaker_title),
                    subtitle = stringResource(R.string.built_in_speaker_desc),
                    checked = uiState.useBuiltInSpeaker,
                    onCheckedChange = onBuiltInSpeakerChange,
                    icon = Icons.Default.Speaker
                )
            }

            item {
                SettingsSwitchTile(
                    title = stringResource(R.string.pre_notification_title),
                    subtitle = stringResource(R.string.pre_notification_desc),
                    checked = uiState.isPreNotificationEnabled,
                    onCheckedChange = onPreNotificationChange,
                    icon = Icons.Default.NotificationImportant
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                SettingsClickableTile(
                    title = stringResource(R.string.snooze_duration_title),
                    subtitle = stringResource(R.string.minute_format, uiState.snoozeDuration),
                    onClick = { showSnoozeDialog = true },
                    icon = Icons.Default.Snooze
                )
            }

            item {
                SettingsClickableTile(
                    title = stringResource(R.string.silence_duration_title),
                    subtitle = stringResource(R.string.minute_format, uiState.silenceDuration),
                    onClick = { showSilenceDialog = true },
                    icon = Icons.Default.TimerOff
                )
            }
        }
    }

    if (showSnoozeDialog) {
        DurationSelectionDialog(
            title = stringResource(R.string.snooze_duration_title),
            currentValue = uiState.snoozeDuration,
            options = listOf(5, 10, 15, 20, 30),
            onDismiss = { showSnoozeDialog = false },
            onSelect = {
                onSnoozeDurationChange(it)
                showSnoozeDialog = false
            }
        )
    }

    if (showSilenceDialog) {
        DurationSelectionDialog(
            title = stringResource(R.string.silence_duration_title),
            currentValue = uiState.silenceDuration,
            options = listOf(1, 5, 10, 20, 30, 60),
            onDismiss = { showSilenceDialog = false },
            onSelect = {
                onSilenceDurationChange(it)
                showSilenceDialog = false
            }
        )
    }
}

@Composable
fun SettingsSwitchTile(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        modifier = Modifier.clickable { onCheckedChange(!checked) }
    )
}

@Composable
fun SettingsClickableTile(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun DurationSelectionDialog(
    title: String,
    currentValue: Int,
    options: List<Int>,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == currentValue,
                            onClick = { onSelect(option) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.minute_format, option))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
