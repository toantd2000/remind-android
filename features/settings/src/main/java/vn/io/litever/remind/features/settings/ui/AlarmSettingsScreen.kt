package vn.io.litever.remind.features.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
        onBuiltInSpeakerChange = viewModel::setBuiltInSpeaker,
        onPreNotificationChange = viewModel::setPreNotificationEnabled
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmSettingsScreen(
    uiState: SettingsUiState,
    onNavigateBack: () -> Unit,
    onBuiltInSpeakerChange: (Boolean) -> Unit,
    onPreNotificationChange: (Boolean) -> Unit
) {

    ReMindScaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.setting_alarm_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
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
                    title = stringResource(R.string.built_in_speaker_title),
                    subtitle = stringResource(R.string.built_in_speaker_desc),
                    checked = uiState.useBuiltInSpeaker,
                    onCheckedChange = onBuiltInSpeakerChange,
                    icon = Icons.Rounded.Speaker
                )
            }

            item {
                SettingsSwitchTile(
                    title = stringResource(R.string.pre_notification_title),
                    subtitle = stringResource(R.string.pre_notification_desc),
                    checked = uiState.isPreNotificationEnabled,
                    onCheckedChange = onPreNotificationChange,
                    icon = Icons.Rounded.NotificationImportant
                )
            }
        }
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
