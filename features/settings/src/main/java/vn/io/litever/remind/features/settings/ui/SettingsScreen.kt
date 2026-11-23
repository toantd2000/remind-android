package vn.io.litever.remind.features.settings.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.ads.api.AdManager
import vn.io.litever.remind.core.ads.api.AdPlacement
import vn.io.litever.remind.core.ads.api.AdState
import vn.io.litever.remind.core.ads.api.LocalAdManager
import vn.io.litever.remind.core.designsystem.components.LvTopAppBar
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsItem
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.features.settings.BuildConfig
import vn.io.litever.remind.features.settings.R

@Composable
fun SettingsRoute(
    onNavigateToGeneralSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToAttributions: () -> Unit,
    onNavigateToPaywall: (Int) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    SettingsScreen(
        uiState = uiState,
        onNavigateToGeneralSettings = onNavigateToGeneralSettings,
        onNavigateToPermissions = onNavigateToPermissions,
        onNavigateToAttributions = onNavigateToAttributions,
        onNavigateToPaywall = onNavigateToPaywall,
        onDebugProToggle = viewModel::setDebugProEnabled
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onNavigateToGeneralSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToAttributions: () -> Unit,
    onNavigateToPaywall: (Int) -> Unit = {},
    onDebugProToggle: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var showFaqDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LvTopAppBar(
                title = stringResource(R.string.settings_title)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small),
            contentPadding = PaddingValues(LiteverTheme.spacing.medium)
        ) {
            // Pro Upgrade Banner
            item {
                ProSettingsBanner(
                    isPro = uiState.isPro,
                    onClick = { onNavigateToPaywall(0) }
                )
            }

            // Group 1: App Settings
            item {
                ReMindSettingsGroup(title = stringResource(R.string.category_app_settings)) {
                    ReMindSettingsItem(
                        title = stringResource(R.string.setting_general_title),
                        subtitle = stringResource(R.string.setting_general_subtitle),
                        icon = ReMindIcons.Language,
                        onClick = onNavigateToGeneralSettings
                    )

                    ReMindSettingsItem(
                        title = stringResource(R.string.setting_permissions_title),
                        subtitle = stringResource(R.string.setting_permissions_subtitle),
                        icon = ReMindIcons.Security,
                        onClick = onNavigateToPermissions
                    )

                    // Alarm Settings is kept hidden (tạm ẩn) since it is not yet fully implemented
                }
            }

            // Group 2: Support & Community
            item {
                ReMindSettingsGroup(title = stringResource(R.string.category_support)) {
                    // SettingsItem(
                    //     title = stringResource(R.string.setting_qa),
                    //     icon = ReMindIcons.QuestionAnswer,
                    //     onClick = { showFaqDialog = true }
                    // )

                    ReMindSettingsItem(
                        title = stringResource(R.string.setting_rate),
                        icon = ReMindIcons.Star,
                        onClick = { rateApp(context) }
                    )

                    ReMindSettingsItem(
                        title = stringResource(R.string.setting_share),
                        icon = ReMindIcons.Share,
                        onClick = { shareApp(context) }
                    )
                }
            }

            // Group 3: About & Legal
            item {
                ReMindSettingsGroup(title = stringResource(R.string.category_about)) {
                    ReMindSettingsItem(
                        title = stringResource(R.string.setting_story),
                        icon = ReMindIcons.History,
                        onClick = { launchCustomTab(context, BuildConfig.URL_AUTHOR) }
                    )

                    ReMindSettingsItem(
                        title = stringResource(R.string.setting_history),
                        icon = ReMindIcons.Code,
                        onClick = { launchCustomTab(context, BuildConfig.URL_CHANGELOG) }
                    )

                    ReMindSettingsItem(
                        title = stringResource(R.string.setting_terms),
                        icon = ReMindIcons.Description,
                        onClick = { launchCustomTab(context, BuildConfig.URL_TERMS) }
                    )

                    ReMindSettingsItem(
                        title = stringResource(R.string.setting_privacy),
                        icon = ReMindIcons.PrivacyTip,
                        onClick = { launchCustomTab(context, BuildConfig.URL_PRIVACY) }
                    )

                    ReMindSettingsItem(
                        title = stringResource(R.string.setting_attributions_title),
                        subtitle = stringResource(R.string.setting_attributions_subtitle),
                        icon = ReMindIcons.Description,
                        onClick = onNavigateToAttributions
                    )
                }
            }

            // Group 4: App Info
            item {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val versionName = packageInfo.versionName ?: "1.0"
                val versionCode =
                    packageInfo.longVersionCode
                val isDebugBuild = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0

                ReMindSettingsGroup {
                    if (isDebugBuild) {
                        ReMindSettingsItem(
                            title = "Debug: Lifetime Pro",
                            subtitle = if (uiState.isPro) "Active (Debug override)" else "Free mode (Tap to unlock)",
                            icon = ReMindIcons.Diamond,
                            onClick = { onDebugProToggle(!uiState.isPro) },
                            trailingContent = {
                                Switch(
                                    checked = uiState.isPro,
                                    onCheckedChange = onDebugProToggle
                                )
                            }
                        )
                    }
                    ReMindSettingsItem(
                        title = stringResource(R.string.setting_version_title),
                        subtitle = stringResource(
                            R.string.app_version_format,
                            versionName,
                            versionCode
                        ),
                        icon = ReMindIcons.Info
                    )
                }
            }

        }
    }

    // FAQ / Q&A Upcoming Dialog
    if (showFaqDialog) {
        AlertDialog(
            onDismissRequest = { showFaqDialog = false },
            title = { Text(stringResource(R.string.faq_upcoming_title)) },
            text = { Text(stringResource(R.string.faq_upcoming_message)) },
            confirmButton = {
                Button(onClick = { showFaqDialog = false }) {
                    Text(stringResource(R.string.close_text))
                }
            }
        )
    }
}

// Helper extension to resolve Activity from Context
fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is android.content.ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

private fun rateApp(context: Context) {
    val appId = context.packageName
    val playStoreIntent = Intent(
        Intent.ACTION_VIEW,
        "market://details?id=$appId".toUri()
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    }
    try {
        context.startActivity(playStoreIntent)
    } catch (_: android.content.ActivityNotFoundException) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$appId".toUri()
            )
        )
    }
}

private fun shareApp(context: Context) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_app_text))
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, context.getString(R.string.share_title))
    context.startActivity(shareIntent)
}

private fun launchCustomTab(context: Context, url: String) {
    val lang = java.util.Locale.getDefault().language
    val finalUrl = if (url.contains("?")) "$url&lang=$lang" else "$url?lang=$lang"
    try {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(context, finalUrl.toUri())
    } catch (e: Exception) {
        // Fallback to regular browser if Custom Tabs fails
        val intent = Intent(Intent.ACTION_VIEW, finalUrl.toUri())
        context.startActivity(intent)
    }
}

@Composable
private fun ProSettingsBanner(
    isPro: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LiteverTheme.shapes.large)
            .clickable(onClick = onClick),
        shape = LiteverTheme.shapes.large,
        color = if (isPro) LiteverTheme.colors.secondaryContainer else LiteverTheme.colors.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LiteverTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isPro) LiteverTheme.colors.secondary else LiteverTheme.colors.primary,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = ReMindIcons.Diamond,
                        contentDescription = null,
                        tint = if (isPro) LiteverTheme.colors.onSecondary else LiteverTheme.colors.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(LiteverTheme.spacing.medium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        if (isPro) R.string.pro_banner_active_title else R.string.pro_banner_title
                    ),
                    style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isPro) LiteverTheme.colors.onSecondaryContainer else LiteverTheme.colors.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(
                        if (isPro) R.string.pro_banner_active_subtitle else R.string.pro_banner_subtitle
                    ),
                    style = LiteverTheme.typography.bodySmall,
                    color = (if (isPro) LiteverTheme.colors.onSecondaryContainer else LiteverTheme.colors.onPrimaryContainer).copy(alpha = 0.85f)
                )
            }
            Icon(
                imageVector = ReMindIcons.ChevronRight,
                contentDescription = null,
                tint = if (isPro) LiteverTheme.colors.onSecondaryContainer else LiteverTheme.colors.onPrimaryContainer
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    ReMindTheme {
        CompositionLocalProvider(LocalAdManager provides PreviewAdManager) {
            SettingsScreen(
                uiState = SettingsUiState(),
                onNavigateToGeneralSettings = {},
                onNavigateToPermissions = {},
                onNavigateToAttributions = {},
                onNavigateToPaywall = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenDarkPreview() {
    ReMindTheme(darkTheme = true) {
        CompositionLocalProvider(LocalAdManager provides PreviewAdManager) {
            SettingsScreen(
                uiState = SettingsUiState(isPro = true),
                onNavigateToGeneralSettings = {},
                onNavigateToPermissions = {},
                onNavigateToAttributions = {},
                onNavigateToPaywall = {}
            )
        }
    }
}

private object PreviewAdManager : AdManager {
    override val adState: StateFlow<AdState> = MutableStateFlow(AdState.Idle)
    override fun initialize() {}
    override fun loadAd(placement: AdPlacement) {}
    override fun showAd(activity: Activity, placement: AdPlacement, onAdDismissed: () -> Unit) {}
    override fun isAdLoaded(placement: AdPlacement): Boolean = false

    @Composable
    override fun NativeAdView(placement: AdPlacement, modifier: Modifier) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(LiteverTheme.colors.surfaceVariant, LiteverTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Native Ad Preview ($placement)",
                style = LiteverTheme.typography.labelLarge,
                color = LiteverTheme.colors.onSurfaceVariant
            )
        }
    }

    @Composable
    override fun MicroAdStrip(placement: AdPlacement, modifier: Modifier) {}
}
