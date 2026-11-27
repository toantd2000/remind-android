package vn.io.litever.remind.features.settings.ui

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.res.Configuration
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import vn.io.litever.remind.core.designsystem.components.LiteVerLogo
import vn.io.litever.remind.core.designsystem.components.LvTopAppBar
import vn.io.litever.remind.core.designsystem.components.ReMindGroupCard
import vn.io.litever.remind.core.designsystem.components.ReMindLogo
import vn.io.litever.remind.core.designsystem.components.ReMindPlanBadge
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsDivider
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsItem
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.features.settings.BuildConfig
import vn.io.litever.remind.features.settings.R
import java.util.Locale

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(MaterialTheme.spacing.medium)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium)
        ) {
            // Group 1: Application (ReMind logo, version, plan badge, changelog & legal)
            ReMindGroupCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = MaterialTheme.spacing.medium,
                                vertical = MaterialTheme.spacing.large
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ReMindLogo(
                            showIcon = true,
                            iconSize = 64.dp,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.smallMedium))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                        ) {
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
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(
                                        horizontal = MaterialTheme.spacing.smallMedium,
                                        vertical = MaterialTheme.spacing.extraSmall
                                    )
                                )
                            }
                            ReMindPlanBadge(isPro = isPro)
                        }
                    }

                    ReMindSettingsDivider(inset = false)

                    ReMindSettingsItem(
                        title = stringResource(R.string.setting_history),
                        icon = ReMindIcons.Code,
                        showChevron = true,
                        onClick = { launchAboutCustomTab(context, BuildConfig.URL_CHANGELOG) }
                    )
                    ReMindSettingsDivider()
                    ReMindSettingsItem(
                        title = stringResource(R.string.setting_terms),
                        icon = ReMindIcons.Description,
                        showChevron = true,
                        onClick = { launchAboutCustomTab(context, BuildConfig.URL_TERMS) }
                    )
                    ReMindSettingsDivider()
                    ReMindSettingsItem(
                        title = stringResource(R.string.setting_privacy),
                        icon = ReMindIcons.PrivacyTip,
                        showChevron = true,
                        onClick = { launchAboutCustomTab(context, BuildConfig.URL_PRIVACY) }
                    )
                }
            }

            // Group 2: Developer / Studio (LiteVer Studio identity & story)
            ReMindGroupCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = MaterialTheme.spacing.medium,
                                vertical = MaterialTheme.spacing.mediumLarge
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LiteVerLogo(
                            fontSize = 20.sp,
                            showIcon = true,
                            iconSize = 26.dp
                        )
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                        Text(
                            text = stringResource(R.string.author_info_slogan),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.tiny))
                        Text(
                            text = stringResource(R.string.author_info_developed_by),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center
                        )
                    }

                    ReMindSettingsDivider(inset = false)

                    ReMindSettingsItem(
                        title = stringResource(R.string.setting_story),
                        icon = ReMindIcons.History,
                        showChevron = true,
                        onClick = { launchAboutCustomTab(context, BuildConfig.URL_AUTHOR) }
                    )
                }
            }

            // Group 3: Attributions & Credits
            ReMindSettingsGroup(title = stringResource(R.string.setting_attributions_title)) {
                ReMindSettingsItem(
                    title = stringResource(R.string.attribution_oss_title),
                    subtitle = stringResource(R.string.attribution_oss_desc),
                    icon = ReMindIcons.Code,
                    showChevron = true,
                    onClick = onNavigateToLicenses
                )
            }

            // Debug-only Pro Override
            if (isDebugBuild) {
                ReMindSettingsGroup {
                    ReMindSettingsItem(
                        title = "Debug: Lifetime Pro",
                        subtitle = if (isPro) "Active (Debug override)" else "Free mode (Tap to unlock)",
                        icon = ReMindIcons.Diamond,
                        iconTint = if (isPro) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
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
    }
}

private fun launchAboutCustomTab(context: Context, url: String) {
    val lang = Locale.getDefault().language
    val finalUrl = if (url.contains("?")) "$url&lang=$lang" else "$url?lang=$lang"
    try {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(context, finalUrl.toUri())
    } catch (_: Exception) {
        val intent = Intent(Intent.ACTION_VIEW, finalUrl.toUri())
        context.startActivity(intent)
    }
}

@Preview(name = "About - Light", showBackground = true)
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

@Preview(
    name = "About - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0xFF19120CL
)
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
