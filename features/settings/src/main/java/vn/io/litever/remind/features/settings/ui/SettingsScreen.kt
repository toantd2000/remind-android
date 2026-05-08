package vn.io.litever.remind.features.settings.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.rounded.PrivacyTip
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import vn.io.litever.remind.core.designsystem.components.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

import vn.io.litever.remind.features.settings.BuildConfig
import vn.io.litever.remind.features.settings.R

@Composable
fun SettingsRoute(
    onNavigateToGeneralSettings: () -> Unit,
    onNavigateToQA: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToAlarmSettings: () -> Unit,
    onNavigateToLicenses: () -> Unit,
    onNavigateToUpdateHistory: () -> Unit,
    onNavigateToAttributions: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    SettingsScreen(
        uiState = uiState,
        onNavigateToGeneralSettings = onNavigateToGeneralSettings,
        onNavigateToQA = onNavigateToQA,
        onNavigateToPermissions = onNavigateToPermissions,
        onNavigateToAlarmSettings = onNavigateToAlarmSettings,
        onNavigateToLicenses = onNavigateToLicenses,
        onNavigateToUpdateHistory = onNavigateToUpdateHistory,
        onNavigateToAttributions = onNavigateToAttributions
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
    onNavigateToLicenses: () -> Unit,
    onNavigateToUpdateHistory: () -> Unit,
    onNavigateToAttributions: () -> Unit
) {
    val context = LocalContext.current

    ReMindScaffold(
        topBar = {
            ReMindTopAppBar(title = stringResource(R.string.settings_title))
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Group: App Settings
            item {
                ReMindSettingsGroup(title = stringResource(R.string.category_app_settings)) {
                    SettingsItem(
                        title = stringResource(R.string.setting_general_title),
                        subtitle = stringResource(R.string.setting_general_subtitle),
                        icon = Icons.Rounded.Language,
                        onClick = onNavigateToGeneralSettings
                    )
                    
                    SettingsItem(
                        title = stringResource(R.string.setting_permissions_title),
                        subtitle = stringResource(R.string.setting_permissions_subtitle),
                        icon = Icons.Rounded.Security,
                        onClick = onNavigateToPermissions
                    )
                }
            }

            // Group: About
            item {
                ReMindSettingsGroup(title = stringResource(R.string.category_about)) {
                    SettingsItem(
                        title = stringResource(R.string.setting_story),
                        icon = Icons.Rounded.History,
                        onClick = { launchCustomTab(context, BuildConfig.URL_AUTHOR) }
                    )
                    
                    SettingsItem(
                        title = stringResource(R.string.setting_terms),
                        icon = Icons.Rounded.Description,
                        onClick = { launchCustomTab(context, BuildConfig.URL_TERMS) }
                    )
                    
                    SettingsItem(
                        title = stringResource(R.string.setting_privacy),
                        icon = Icons.Rounded.PrivacyTip,
                        onClick = { launchCustomTab(context, BuildConfig.URL_PRIVACY) }
                    )
                    
                    SettingsItem(
                        title = stringResource(R.string.setting_history),
                        icon = Icons.Rounded.Code,
                        onClick = onNavigateToUpdateHistory
                    )
                    
                    SettingsItem(
                        title = stringResource(R.string.setting_attributions_title),
                        subtitle = stringResource(R.string.setting_attributions_subtitle),
                        icon = Icons.Rounded.Description,
                        onClick = onNavigateToAttributions
                    )
                }
            }

            // Group: App Info
            item {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val versionName = packageInfo.versionName ?: "1.0"
                val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode.toLong()
                }

                ReMindSettingsGroup {
                    SettingsItem(
                        title = stringResource(R.string.setting_version_title),
                        subtitle = stringResource(R.string.app_version_format, versionName, versionCode),
                        icon = Icons.Rounded.Info
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
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
            onNavigateToLicenses = {},
            onNavigateToUpdateHistory = {},
            onNavigateToAttributions = {}
        )
    }
}

private fun launchCustomTab(context: Context, url: String) {
    try {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    } catch (e: Exception) {
        // Fallback to regular browser if Custom Tabs fails
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}










