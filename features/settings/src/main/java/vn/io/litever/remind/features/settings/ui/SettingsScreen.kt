package vn.io.litever.remind.features.settings.ui

import vn.io.litever.brand.tokens.LiteverTokens
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import vn.io.litever.remind.core.designsystem.components.ReMindLogo
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsItem
import vn.io.litever.remind.core.designsystem.components.rememberReMindProGradient
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.semanticColors
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.features.settings.BuildConfig
import vn.io.litever.remind.features.settings.R

@Composable
fun SettingsRoute(
    onNavigateToGeneralSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToPaywall: (Int) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    SettingsScreen(
        uiState = uiState,
        onRefreshPermissions = viewModel::refreshPermissions,
        onNavigateToGeneralSettings = onNavigateToGeneralSettings,
        onNavigateToPermissions = onNavigateToPermissions,
        onNavigateToAbout = onNavigateToAbout,
        onNavigateToPaywall = onNavigateToPaywall
    )
}

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onRefreshPermissions: () -> Unit = {},
    onNavigateToGeneralSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToPaywall: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onRefreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val packageInfo = try {
        context.packageManager.getPackageInfo(context.packageName, 0)
    } catch (_: Exception) {
        null
    }
    val versionName = packageInfo?.versionName ?: "1.0"
    val versionCode = packageInfo?.longVersionCode ?: 1L

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ProSettingsTopBar(
                isPro = uiState.isPro,
                onClick = { onNavigateToPaywall(0) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = MaterialTheme.spacing.medium,
                    vertical = MaterialTheme.spacing.medium
                ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium)
        ) {
            // Group 1: Core App Settings (Permissions prioritized first for alarm reliability)
            ReMindSettingsGroup(title = stringResource(R.string.category_app_settings)) {
                ReMindSettingsItem(
                    title = stringResource(R.string.setting_permissions_title),
                    subtitle = stringResource(R.string.setting_permissions_subtitle),
                    icon = ReMindIcons.Security,
                    onClick = onNavigateToPermissions,
                    titleBadge = {
                        PermissionStatusPill(health = uiState.permissionHealth)
                    }
                )

                ReMindSettingsItem(
                    title = stringResource(R.string.setting_general_title),
                    subtitle = stringResource(R.string.setting_general_subtitle),
                    icon = ReMindIcons.Settings,
                    onClick = onNavigateToGeneralSettings
                )
            }

            // Group 2: Support & Community (Unified single-line M3 list items)
            ReMindSettingsGroup(title = stringResource(R.string.category_support)) {
                ReMindSettingsItem(
                    title = stringResource(R.string.setting_feedback_title),
                    icon = ReMindIcons.QuestionAnswer,
                    onClick = { sendFeedbackEmail(context, versionName, versionCode) }
                )

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

            // Group 3: About
            ReMindSettingsGroup {
                ReMindSettingsItem(
                    title = stringResource(R.string.category_about),
                    subtitle = stringResource(R.string.setting_about_subtitle),
                    icon = ReMindIcons.Info,
                    onClick = onNavigateToAbout,
                    titleBadge = {
                        VersionPill(versionName = versionName)
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f, fill = true))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = MaterialTheme.spacing.small),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReMindLogo(
                    showIcon = false,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = " • ${stringResource(vn.io.litever.remind.core.designsystem.R.string.app_slogan)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ProSettingsTopBar(
    isPro: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (isPro) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val badgeBgColor = MaterialTheme.colorScheme.primary
    val badgeIconColor = MaterialTheme.colorScheme.onPrimary

    val headerBackgroundModifier = if (isPro) {
        Modifier.background(
            brush = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.colorScheme.tertiaryContainer
                )
            )
        )
    } else {
        Modifier.background(
            color = MaterialTheme.colorScheme.surfaceContainer
        )
    }

    val proGradient = rememberReMindProGradient()
    val iconBadgeModifier = if (isPro) {
        Modifier
            .size(44.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(brush = proGradient)
    } else {
        Modifier
            .size(44.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(badgeBgColor)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(headerBackgroundModifier)
                .clickable(onClick = onClick)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(
                        horizontal = MaterialTheme.spacing.medium,
                        vertical = MaterialTheme.spacing.medium
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = iconBadgeModifier,
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = ReMindIcons.WorkspacePremium,
                        contentDescription = null,
                        tint = badgeIconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.smallMedium))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(
                            if (isPro) R.string.pro_banner_active_title else R.string.pro_banner_title
                        ),
                        style = LiteverTokens.typography.titleMediumEmphasized,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.tiny))
                    Text(
                        text = stringResource(
                            if (isPro) R.string.pro_banner_active_subtitle else R.string.pro_banner_subtitle
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Surface(
                    onClick = onClick,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = stringResource(
                            if (isPro) R.string.pro_action_manage else R.string.pro_action_upgrade
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.spacing.medium,
                            vertical = MaterialTheme.spacing.small
                        )
                    )
                }
            }
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        )
    }
}

@Composable
private fun PermissionStatusPill(health: PermissionHealthSummary) {
    val isAllGranted = health.grantedCount >= health.totalCount
    val isMissingCritical = !health.hasCriticalPermissions

    val containerColor = when {
        isMissingCritical -> MaterialTheme.colorScheme.errorContainer
        !isAllGranted -> MaterialTheme.semanticColors.warningContainer
        else -> MaterialTheme.semanticColors.success.copy(alpha = 0.14f)
    }
    val textColor = when {
        isMissingCritical -> MaterialTheme.colorScheme.onErrorContainer
        !isAllGranted -> MaterialTheme.semanticColors.onWarningContainer
        else -> MaterialTheme.semanticColors.success
    }
    val label = when {
        isMissingCritical -> stringResource(R.string.permission_badge_critical)
        !isAllGranted -> stringResource(
            R.string.permission_badge_partial,
            health.grantedCount,
            health.totalCount
        )
        else -> stringResource(R.string.permission_badge_ready)
    }

    Surface(
        color = containerColor,
        shape = CircleShape
    ) {
        Text(
            text = label,
            style = LiteverTokens.typography.labelSmallEmphasized,
            color = textColor,
            maxLines = 1,
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.small,
                vertical = MaterialTheme.spacing.tiny
            )
        )
    }
}

@Composable
private fun VersionPill(versionName: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = CircleShape
    ) {
        Text(
            text = "v$versionName",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.small,
                vertical = MaterialTheme.spacing.tiny
            )
        )
    }
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

private fun sendFeedbackEmail(context: Context, versionName: String, versionCode: Long) {
    val email = BuildConfig.SUPPORT_EMAIL
    val subject = context.getString(R.string.feedback_email_subject)
    val deviceInfo = buildString {
        appendLine()
        appendLine("---")
        appendLine("App: ReMind v$versionName ($versionCode)")
        appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
    }
    val mailtoUri = Uri.parse(
        "mailto:${Uri.encode(email)}?subject=${Uri.encode(subject)}&body=${Uri.encode(deviceInfo)}"
    )
    val emailIntent = Intent(Intent.ACTION_SENDTO, mailtoUri)
    try {
        context.startActivity(emailIntent)
    } catch (_: android.content.ActivityNotFoundException) {
        Toast.makeText(
            context,
            context.getString(R.string.feedback_no_email_app, email),
            Toast.LENGTH_LONG
        ).show()
    }
}

@Preview(showBackground = true, name = "Free - Light")
@Composable
fun SettingsScreenPreview() {
    ReMindTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                isPro = false,
                permissionHealth = PermissionHealthSummary(
                    hasCriticalPermissions = false,
                    grantedCount = 3,
                    totalCount = 6
                )
            ),
            onNavigateToGeneralSettings = {},
            onNavigateToPermissions = {},
            onNavigateToAbout = {},
            onNavigateToPaywall = {}
        )
    }
}

@Preview(showBackground = true, name = "Pro - Light")
@Composable
fun SettingsScreenProLightPreview() {
    ReMindTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                isPro = true,
                permissionHealth = PermissionHealthSummary(
                    hasCriticalPermissions = true,
                    grantedCount = 6,
                    totalCount = 6
                )
            ),
            onNavigateToGeneralSettings = {},
            onNavigateToPermissions = {},
            onNavigateToAbout = {},
            onNavigateToPaywall = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF19120C, name = "Free - Dark")
@Composable
fun SettingsScreenFreeDarkPreview() {
    ReMindTheme(darkTheme = true) {
        SettingsScreen(
            uiState = SettingsUiState(
                isPro = false,
                permissionHealth = PermissionHealthSummary(
                    hasCriticalPermissions = true,
                    grantedCount = 4,
                    totalCount = 6
                )
            ),
            onNavigateToGeneralSettings = {},
            onNavigateToPermissions = {},
            onNavigateToAbout = {},
            onNavigateToPaywall = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF19120C, name = "Pro - Dark")
@Composable
fun SettingsScreenDarkPreview() {
    ReMindTheme(darkTheme = true) {
        SettingsScreen(
            uiState = SettingsUiState(
                isPro = true,
                permissionHealth = PermissionHealthSummary(
                    hasCriticalPermissions = true,
                    grantedCount = 6,
                    totalCount = 6
                )
            ),
            onNavigateToGeneralSettings = {},
            onNavigateToPermissions = {},
            onNavigateToAbout = {},
            onNavigateToPaywall = {}
        )
    }
}
