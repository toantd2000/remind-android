package vn.io.litever.remind.features.settings.ui

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import vn.io.litever.remind.core.designsystem.components.LiteVerLogo
import vn.io.litever.remind.core.designsystem.components.LvTopAppBar
import vn.io.litever.remind.core.designsystem.components.ReMindLogo
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsItem
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.features.settings.BuildConfig
import vn.io.litever.remind.features.settings.R

@Composable
fun AboutRoute(
    onNavigateToLicenses: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    AboutScreen(
        isPro = uiState.isPro,
        onDebugProToggle = viewModel::setDebugProEnabled,
        onNavigateToLicenses = onNavigateToLicenses,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    isPro: Boolean,
    onDebugProToggle: (Boolean) -> Unit,
    onNavigateToLicenses: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val packageInfo = try {
        context.packageManager.getPackageInfo(context.packageName, 0)
    } catch (_: Exception) {
        null
    }
    val versionName = packageInfo?.versionName ?: "1.0"
    val versionCode = packageInfo?.longVersionCode ?: 1L
    val isDebugBuild =
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    Scaffold(
        topBar = {
            LvTopAppBar(
                title = stringResource(R.string.category_about),
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(MaterialTheme.spacing.medium),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main Content (Top Brand + Legal/Info + Attributions + Debug)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ReMind App Logo & Version Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = MaterialTheme.spacing.small),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ReMindLogo(
                            showIcon = true,
                            iconSize = 68.dp,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shape = CircleShape
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.app_version_format,
                                    versionName,
                                    versionCode
                                ),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(
                                    horizontal = MaterialTheme.spacing.smallMedium,
                                    vertical = MaterialTheme.spacing.extraSmall
                                )
                            )
                        }
                    }

                    // Story, Changelog & Legal
                    ReMindSettingsGroup {
                        ReMindSettingsItem(
                            title = stringResource(R.string.setting_story),
                            icon = ReMindIcons.History,
                            onClick = { launchAboutCustomTab(context, BuildConfig.URL_AUTHOR) }
                        )
                        ReMindSettingsItem(
                            title = stringResource(R.string.setting_history),
                            icon = ReMindIcons.Code,
                            onClick = { launchAboutCustomTab(context, BuildConfig.URL_CHANGELOG) }
                        )
                        ReMindSettingsItem(
                            title = stringResource(R.string.setting_terms),
                            icon = ReMindIcons.Description,
                            onClick = { launchAboutCustomTab(context, BuildConfig.URL_TERMS) }
                        )
                        ReMindSettingsItem(
                            title = stringResource(R.string.setting_privacy),
                            icon = ReMindIcons.PrivacyTip,
                            onClick = { launchAboutCustomTab(context, BuildConfig.URL_PRIVACY) }
                        )
                    }

                    // Attributions & Credits (merged directly into About)
                    ReMindSettingsGroup(title = stringResource(R.string.setting_attributions_title)) {
                        ReMindSettingsItem(
                            title = stringResource(R.string.attribution_oss_title),
                            subtitle = stringResource(R.string.attribution_oss_desc),
                            icon = ReMindIcons.Code,
                            onClick = onNavigateToLicenses
                        )
                        ReMindSettingsItem(
                            title = stringResource(R.string.attribution_storyset_title),
                            subtitle = stringResource(R.string.attribution_storyset_desc),
                            icon = ReMindIcons.Palette,
                            onClick = { launchAboutCustomTab(context, "https://storyset.com") }
                        )
                    }

                    // Debug-only Pro Override
                    if (isDebugBuild) {
                        ReMindSettingsGroup {
                            ReMindSettingsItem(
                                title = "Debug: Lifetime Pro",
                                subtitle = if (isPro) "Active (Debug override)" else "Free mode (Tap to unlock)",
                                icon = ReMindIcons.Diamond,
                                onClick = { onDebugProToggle(!isPro) },
                                trailingContent = {
                                    Switch(
                                        checked = isPro,
                                        onCheckedChange = onDebugProToggle
                                    )
                                }
                            )
                        }
                    }
                }

                // Bottom LiteVer Studio Brand Footer (pushed to bottom of screen)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = MaterialTheme.spacing.large,
                            bottom = MaterialTheme.spacing.small
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LiteVerLogo(
                        fontSize = 18.sp,
                        showIcon = true,
                        iconSize = 22.dp
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
                    Text(
                        text = stringResource(R.string.author_info_slogan),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.tiny))
                    Text(
                        text = stringResource(R.string.author_info_developed_by),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun launchAboutCustomTab(context: Context, url: String) {
    val lang = java.util.Locale.getDefault().language
    val finalUrl = if (url.contains("?")) "$url&lang=$lang" else "$url?lang=$lang"
    try {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(context, finalUrl.toUri())
    } catch (_: Exception) {
        val intent = Intent(Intent.ACTION_VIEW, finalUrl.toUri())
        context.startActivity(intent)
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutScreenPreview() {
    ReMindTheme {
        AboutScreen(
            isPro = false,
            onDebugProToggle = {},
            onNavigateToLicenses = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF19120C)
@Composable
private fun AboutScreenDarkPreview() {
    ReMindTheme(darkTheme = true) {
        AboutScreen(
            isPro = true,
            onDebugProToggle = {},
            onNavigateToLicenses = {},
            onNavigateBack = {}
        )
    }
}
