package vn.io.litever.remind.features.settings.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.io.litever.designsystem.components.LiteverAlertDialog
import vn.io.litever.designsystem.components.LiteverButton
import vn.io.litever.designsystem.components.LiteverDialog
import vn.io.litever.designsystem.components.LiteverOutlinedButton
import vn.io.litever.remind.core.ads.api.AdPlacement
import vn.io.litever.remind.core.ads.api.AdState
import vn.io.litever.remind.core.common.util.DeviceUtils
import vn.io.litever.remind.core.ads.api.LocalAdManager
import vn.io.litever.remind.core.designsystem.components.*
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
    val context = LocalContext.current
    SettingsScreen(
        uiState = uiState,
        onNavigateToGeneralSettings = onNavigateToGeneralSettings,
        onNavigateToQA = onNavigateToQA,
        onNavigateToPermissions = onNavigateToPermissions,
        onNavigateToAlarmSettings = onNavigateToAlarmSettings,
        onNavigateToLicenses = onNavigateToLicenses,
        onNavigateToUpdateHistory = onNavigateToUpdateHistory,
        onNavigateToAttributions = onNavigateToAttributions,
        onRewardGranted = { viewModel.disableAdsFor24Hours(context) }
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
    onNavigateToAttributions: () -> Unit,
    onRewardGranted: () -> Unit
) {
    val context = LocalContext.current
    val adManager = LocalAdManager.current
    val notReadyMessage = stringResource(R.string.rewarded_ad_not_ready)

    val adState by adManager.adState.collectAsState()
    var showDonateDialog by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }
    var showRewardedAdSimulator by remember { mutableStateOf(false) }
    var showThankYouDialog by remember { mutableStateOf(false) }
    var showSupportDeveloperDialog by remember { mutableStateOf(false) }
    var showAdLoading by remember { mutableStateOf(false) }


    LaunchedEffect(adState, showAdLoading) {
        if (showAdLoading) {
            when (adState) {
                is AdState.Loaded -> {
                    showAdLoading = false
                    val activity = context.findActivity()
                    if (activity != null) {
                        adManager.showAd(activity, AdPlacement.SUPPORT_REWARDED) {
                            showThankYouDialog = true
                        }
                    }
                }
                is AdState.Failed -> {
                    showAdLoading = false
                    if (DeviceUtils.isEmulator()) {
                        showRewardedAdSimulator = true
                    } else {
                        android.widget.Toast.makeText(
                            context,
                            notReadyMessage,
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                else -> {}
            }
        }
    }

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
            // Group 1: App Settings
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
                    
                    // Alarm Settings is kept hidden (tạm ẩn) since it is not yet fully implemented
                }
            }

            // Group 2: Support & Community
            item {
                ReMindSettingsGroup(title = stringResource(R.string.category_support)) {
                    // SettingsItem(
                    //     title = stringResource(R.string.setting_qa),
                    //     icon = Icons.Rounded.QuestionAnswer,
                    //     onClick = { showFaqDialog = true }
                    // )

                    SettingsItem(
                        title = stringResource(R.string.setting_rate),
                        icon = Icons.Rounded.Star,
                        onClick = { rateApp(context) }
                    )

                    SettingsItem(
                        title = stringResource(R.string.setting_share),
                        icon = Icons.Rounded.Share,
                        onClick = { shareApp(context) }
                    )

                    SettingsItem(
                        title = stringResource(R.string.setting_support_dev_title),
                        subtitle = stringResource(R.string.setting_support_dev_desc),
                        icon = Icons.Rounded.Favorite,
                        onClick = { showSupportDeveloperDialog = true }
                    )
                }
            }

            // Group 3: About & Legal
            item {
                ReMindSettingsGroup(title = stringResource(R.string.category_about)) {
                    SettingsItem(
                        title = stringResource(R.string.setting_story),
                        icon = Icons.Rounded.History,
                        onClick = { launchCustomTab(context, BuildConfig.URL_AUTHOR) }
                    )

                    SettingsItem(
                        title = stringResource(R.string.setting_history),
                        icon = Icons.Rounded.Code,
                        onClick = onNavigateToUpdateHistory
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
                        title = stringResource(R.string.setting_attributions_title),
                        subtitle = stringResource(R.string.setting_attributions_subtitle),
                        icon = Icons.Rounded.Description,
                        onClick = onNavigateToAttributions
                    )
                }
            }

            // Group 4: App Info
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

    // Support Developer Dialog
    if (showSupportDeveloperDialog) {
        val watchAdAction = stringResource(R.string.watch_ad_action)
        val directSupportAction = stringResource(R.string.direct_support_action)
        val supportDevTitle = stringResource(R.string.setting_support_dev_title)
        val supportDevDesc = stringResource(R.string.support_dev_dialog_desc)

        LiteverDialog(
            onDismissRequest = { showSupportDeveloperDialog = false },
            confirmButton = {
                // Watch Ad Button (Primary-colored Solid Button)
                LiteverButton(
                    onClick = {
                        showSupportDeveloperDialog = false
                        val activity = context.findActivity()
                        if (activity != null && adManager.isAdLoaded(AdPlacement.SUPPORT_REWARDED)) {
                            adManager.showAd(activity, AdPlacement.SUPPORT_REWARDED) {
                                showThankYouDialog = true
                            }
                        } else {
                            if (DeviceUtils.isEmulator()) {
                                showRewardedAdSimulator = true
                            } else {
                                adManager.loadAd(AdPlacement.SUPPORT_REWARDED)
                                showAdLoading = true
                            }
                        }
                    },
                ) {
                    Text(text = watchAdAction)
                }
            },
            dismissButton = {
                // Direct Donate Button (Tertiary-colored Outlined Button)
                LiteverOutlinedButton(
                    onClick = {
                        showSupportDeveloperDialog = false
                        showDonateDialog = true
                    },
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text(text = directSupportAction)
                }
            },
            title = {
                Text(
                    text = supportDevTitle,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column {
                    Text(
                        text = supportDevDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }

    // Direct Donation Dialog
    if (showDonateDialog) {
        LiteverAlertDialog(
            onDismissRequest = { showDonateDialog = false },
            confirmButtonText = stringResource(R.string.close_text),
            onConfirmClick = { showDonateDialog = false },
            title = stringResource(R.string.support_dev_dialog_title)
        ) {
            Text(
                text = stringResource(R.string.support_dev_dialog_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // FAQ / Q&A Upcoming Dialog
    if (showFaqDialog) {
        LiteverAlertDialog(
            onDismissRequest = { showFaqDialog = false },
            confirmButtonText = stringResource(R.string.close_text),
            onConfirmClick = { showFaqDialog = false },
            title = stringResource(R.string.faq_upcoming_title)
        ) {
            Text(
                text = stringResource(R.string.faq_upcoming_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Rewarded Ad Simulator
    if (showRewardedAdSimulator) {
        RewardedAdSimulatorDialog(
            onDismiss = { showRewardedAdSimulator = false },
            onRewardEarned = {
                showRewardedAdSimulator = false
                onRewardGranted()
                showThankYouDialog = true
            }
        )
    }

    // Ad Loading Dialog Overlay
    if (showAdLoading) {
        val loadingAdMessage = stringResource(R.string.loading_ad_message)
        LiteverDialog(
            onDismissRequest = { showAdLoading = false },
            confirmButton = {},
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = loadingAdMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        )
    }

    // Ad-Free Granted Thank You Dialog
    if (showThankYouDialog) {
        LiteverAlertDialog(
            onDismissRequest = { showThankYouDialog = false },
            confirmButtonText = stringResource(R.string.close_text),
            onConfirmClick = { showThankYouDialog = false },
            title = stringResource(R.string.watch_ad_dialog_title)
        ) {
            Text(
                text = stringResource(R.string.ads_disabled_reward_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}



@Composable
fun RewardedAdSimulatorDialog(
    onDismiss: () -> Unit,
    onRewardEarned: () -> Unit
) {
    var countdown by remember { mutableStateOf(5) }
    val rewardAdLoadingText = stringResource(R.string.reward_ad_loading, countdown)
    val watchAdDialogMessage = stringResource(R.string.watch_ad_dialog_message)
    
    LaunchedEffect(Unit) {
        while (countdown > 0) {
            kotlinx.coroutines.delay(1000L)
            countdown--
        }
        onRewardEarned()
    }
    
    androidx.compose.ui.window.Dialog(onDismissRequest = {}) {
        androidx.compose.material3.Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = rewardAdLoadingText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = watchAdDialogMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
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
        Uri.parse("market://details?id=$appId")
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    }
    try {
        context.startActivity(playStoreIntent)
    } catch (e: android.content.ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appId")))
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
    try {
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(context, Uri.parse(url))
    } catch (e: Exception) {
        // Fallback to regular browser if Custom Tabs fails
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}
