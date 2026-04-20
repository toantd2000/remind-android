package vn.io.litever.remind.features.settings.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    onNavigateToLicenses: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    SettingsScreen(
        uiState = uiState,
        onNavigateToGeneralSettings = onNavigateToGeneralSettings,
        onNavigateToQA = onNavigateToQA,
        onNavigateToPermissions = onNavigateToPermissions,
        onNavigateToAlarmSettings = onNavigateToAlarmSettings,
        onNavigateToLicenses = onNavigateToLicenses
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onNavigateToGeneralSettings: () -> Unit,
    onNavigateToQA: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToAlarmSettings: () -> Unit,
    onNavigateToLicenses: () -> Unit
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
                    icon = Icons.Rounded.Language,
                    onClick = onNavigateToGeneralSettings
                )
            }

            item {
                SettingsItem(
                    title = stringResource(R.string.setting_permissions_title),
                    subtitle = stringResource(R.string.setting_permissions_subtitle),
                    icon = Icons.Rounded.Security,
                    onClick = onNavigateToPermissions
                )
            }

/*
            item {
                SettingsItem(
                    title = stringResource(R.string.setting_alarm_title),
                    subtitle = stringResource(R.string.setting_alarm_subtitle),
                    icon = Icons.Rounded.NotificationsActive,
                    onClick = onNavigateToAlarmSettings
                )
            }
*/

/*
            // Group: Support
            item {
                SettingsCategory(title = stringResource(R.string.category_support))
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.setting_qa),
                    icon = Icons.Rounded.QuestionAnswer,
                    onClick = onNavigateToQA
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.setting_rate),
                    icon = Icons.Rounded.Star,
                    onClick = { /* TODO */ }
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.setting_share),
                    icon = Icons.Rounded.Share,
                    onClick = { /* TODO */ }
                )
            }
*/

            // Group: About
            item {
                SettingsCategory(title = stringResource(R.string.category_about))
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.setting_story),
                    icon = Icons.Rounded.History,
                    onClick = { /* TODO */ }
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.setting_terms),
                    icon = Icons.Rounded.Description,
                    onClick = { /* TODO */ }
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.setting_history),
                    icon = Icons.Rounded.Code,
                    onClick = { /* TODO */ }
                )
            }
            item {
                SettingsItem(
                    title = stringResource(R.string.setting_licenses),
                    icon = Icons.Rounded.Description,
                    onClick = onNavigateToLicenses
                )
            }

            item {
                val context = LocalContext.current
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val versionName = packageInfo.versionName ?: "1.0"
                val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode.toLong()
                }

                SettingsItem(
                    title = stringResource(R.string.setting_version_title),
                    subtitle = stringResource(R.string.app_version_format, versionName, versionCode),
                    icon = Icons.Rounded.Info
                )
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
            onNavigateToAlarmSettings = {},
            onNavigateToLicenses = {}
        )
    }
}
