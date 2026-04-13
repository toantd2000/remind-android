package vn.io.litever.remind.features.settings.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.io.litever.remind.core.designsystem.components.SettingsCategory
import vn.io.litever.remind.core.designsystem.components.SettingsItem
import vn.io.litever.remind.features.settings.R

@Composable
fun SettingsRoute(
    onNavigateToGeneralSettings: () -> Unit,
    onNavigateToQA: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToAlarmSettings: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    SettingsScreen(
        uiState = uiState,
        onNavigateToGeneralSettings = onNavigateToGeneralSettings,
        onNavigateToQA = onNavigateToQA,
        onNavigateToPermissions = onNavigateToPermissions,
        onNavigateToAlarmSettings = onNavigateToAlarmSettings
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onNavigateToGeneralSettings: () -> Unit,
    onNavigateToQA: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToAlarmSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings_title)) })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Group: App Settings
            item {
                SettingsCategory(title = stringResource(R.string.category_app_settings))
            }

            item {
                SettingsItem(
                    title = stringResource(R.string.setting_general_title),
                    subtitle = stringResource(R.string.setting_general_subtitle),
                    icon = Icons.Default.Language,
                    onClick = onNavigateToGeneralSettings
                )
            }

            item {
                SettingsItem(
                    title = stringResource(R.string.setting_permissions_title),
                    subtitle = stringResource(R.string.setting_permissions_subtitle),
                    icon = Icons.Default.Security,
                    onClick = onNavigateToPermissions
                )
            }

            item {
                SettingsItem(
                    title = stringResource(R.string.setting_alarm_title),
                    subtitle = stringResource(R.string.setting_alarm_subtitle),
                    icon = Icons.Default.NotificationsActive,
                    onClick = onNavigateToAlarmSettings
                )
            }

            // Group: Support
            item {
                SettingsCategory(title = stringResource(R.string.category_support))
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.setting_qa),
                    icon = Icons.Default.QuestionAnswer,
                    onClick = onNavigateToQA
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.setting_rate),
                    icon = Icons.Default.Star,
                    onClick = { /* TODO */ }
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.setting_share),
                    icon = Icons.Default.Share,
                    onClick = { /* TODO */ }
                )
            }

            // Group: About
            item {
                SettingsCategory(title = stringResource(R.string.category_about))
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.setting_story),
                    icon = Icons.Default.History,
                    onClick = { /* TODO */ }
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.setting_terms),
                    icon = Icons.Default.Description,
                    onClick = { /* TODO */ }
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.setting_history),
                    icon = Icons.Default.Code,
                    onClick = { /* TODO */ }
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.setting_licenses),
                    icon = Icons.Default.Description,
                    onClick = { /* TODO */ }
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // TODO: Get real version dynamically
                    Text(
                        text = stringResource(R.string.app_version_format, "1.0", 1),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme {
        SettingsScreen(
            uiState = SettingsUiState(is24HourFormat = true, timeFormat = "SYSTEM", themeMode = "SYSTEM", colorPalette = "DEFAULT"),
            onNavigateToGeneralSettings = {},
            onNavigateToQA = {},
            onNavigateToPermissions = {},
            onNavigateToAlarmSettings = {}
        )
    }
}
