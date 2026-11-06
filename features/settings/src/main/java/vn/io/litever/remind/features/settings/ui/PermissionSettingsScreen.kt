package vn.io.litever.remind.features.settings.ui

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.button.LvButtonType
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.LvTopAppBar
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.features.settings.R

@Composable
fun PermissionSettingsRoute(
    onNavigateBack: () -> Unit,
    viewModel: PermissionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    PermissionSettingsScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRefreshPermissions = viewModel::refreshPermissions,
        onRequestExactAlarm = { requestExactAlarmPermission(context) },
        onRequestNotification = { requestNotificationPermission(context) },
        onRequestFullScreenIntent = { requestFullScreenIntentPermission(context) },
        onRequestOverlay = { requestOverlayPermission(context) },
        onRequestBatteryOptimization = { requestIgnoreBatteryOptimization(context) },
        onRequestDndPolicy = { requestDndPolicyAccess(context) },
        onOpenManufacturerSettings = { openManufacturerSettings(context) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionSettingsScreen(
    uiState: PermissionUiState,
    onNavigateBack: () -> Unit,
    onRefreshPermissions: () -> Unit,
    onRequestExactAlarm: () -> Unit,
    onRequestNotification: () -> Unit,
    onRequestFullScreenIntent: () -> Unit = {},
    onRequestOverlay: () -> Unit,
    onRequestBatteryOptimization: () -> Unit,
    onRequestDndPolicy: () -> Unit,
    onOpenManufacturerSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // Observe lifecycle events to refresh when user returns to app
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

    Scaffold(
        modifier = modifier,
        topBar = {
            LvTopAppBar(
                title = stringResource(R.string.setting_permissions_title),
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(LiteverTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
        ) {
            // 1. Exact Alarm (Top Priority, critical)
            item {
                PermissionTile(
                    title = stringResource(R.string.permission_exact_alarm_title),
                    description = stringResource(R.string.permission_exact_alarm_desc),
                    isGranted = uiState.isExactAlarmGranted,
                    icon = ReMindIcons.Alarm,
                    isCritical = true,
                    onRequest = onRequestExactAlarm
                )
            }

            // 2. Notifications (High Priority, critical warning)
            item {
                PermissionTile(
                    title = stringResource(R.string.permission_notification_title),
                    description = stringResource(R.string.permission_notification_desc),
                    isGranted = uiState.isNotificationGranted,
                    icon = ReMindIcons.Notifications,
                    isCritical = true,
                    onRequest = onRequestNotification
                )
            }

            // 3. Full-Screen Intent (Android 14+ Lock Screen Alert)
            item {
                PermissionTile(
                    title = stringResource(R.string.permission_full_screen_intent_title),
                    description = stringResource(R.string.permission_full_screen_intent_desc),
                    isGranted = uiState.isFullScreenIntentGranted,
                    icon = ReMindIcons.NotificationImportant,
                    onRequest = onRequestFullScreenIntent
                )
            }

            // 4. Overlay
            item {
                PermissionTile(
                    title = stringResource(R.string.permission_overlay_title),
                    description = stringResource(R.string.permission_overlay_desc),
                    isGranted = uiState.isOverlayGranted,
                    icon = ReMindIcons.Layers,
                    onRequest = onRequestOverlay
                )
            }

            // 5. Battery Optimization
            item {
                PermissionTile(
                    title = stringResource(R.string.permission_battery_title),
                    description = stringResource(R.string.permission_battery_desc),
                    isGranted = uiState.isBatteryOptIgnored,
                    icon = ReMindIcons.BatteryChargingFull,
                    onRequest = onRequestBatteryOptimization
                )
            }

            // 6. Do Not Disturb Access
            item {
                PermissionTile(
                    title = stringResource(R.string.permission_dnd_title),
                    description = stringResource(R.string.permission_dnd_desc),
                    isGranted = uiState.isDndPolicyGranted,
                    icon = ReMindIcons.DoNotDisturbOff,
                    onRequest = onRequestDndPolicy
                )
            }

            // 7. Manufacturer Specific (Auto-start & background limits)
            item {
                ManufacturerSettingsTile(onOpen = onOpenManufacturerSettings)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionSettingsScreenPreview() {
    ReMindTheme {
        PermissionSettingsScreen(
            uiState = PermissionUiState(
                isExactAlarmGranted = true,
                isNotificationGranted = false,
                isFullScreenIntentGranted = false,
                isOverlayGranted = false,
                isBatteryOptIgnored = true
            ),
            onNavigateBack = {},
            onRefreshPermissions = {},
            onRequestExactAlarm = {},
            onRequestNotification = {},
            onRequestFullScreenIntent = {},
            onRequestOverlay = {},
            onRequestBatteryOptimization = {},
            onRequestDndPolicy = {},
            onOpenManufacturerSettings = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionSettingsScreenDarkPreview() {
    ReMindTheme(darkTheme = true) {
        PermissionSettingsScreen(
            uiState = PermissionUiState(
                isExactAlarmGranted = true,
                isNotificationGranted = true,
                isFullScreenIntentGranted = true,
                isOverlayGranted = false,
                isBatteryOptIgnored = false
            ),
            onNavigateBack = {},
            onRefreshPermissions = {},
            onRequestExactAlarm = {},
            onRequestNotification = {},
            onRequestFullScreenIntent = {},
            onRequestOverlay = {},
            onRequestBatteryOptimization = {},
            onRequestDndPolicy = {},
            onOpenManufacturerSettings = {}
        )
    }
}

@Composable
fun PermissionTile(
    title: String,
    description: String,
    isGranted: Boolean,
    icon: ImageVector,
    isCritical: Boolean = false,
    onRequest: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = LiteverTheme.colors.surfaceContainerLow,
        shape = LiteverTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(LiteverTheme.spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = LiteverTheme.colors.primary,
                    modifier = Modifier.size(LiteverTheme.spacing.large)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = LiteverTheme.spacing.smallMedium)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(LiteverTheme.spacing.extraSmall))

                    StatusBadge(isGranted = isGranted)
                }
            }

            Spacer(modifier = Modifier.height(LiteverTheme.spacing.smallMedium))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = LiteverTheme.colors.onSurfaceVariant
            )

            if (!isGranted && isCritical) {
                Spacer(modifier = Modifier.height(LiteverTheme.spacing.smallMedium))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = ReMindIcons.ErrorOutline,
                        contentDescription = null,
                        tint = LiteverTheme.colors.error,
                        modifier = Modifier.size(LiteverTheme.spacing.medium)
                    )
                    Spacer(modifier = Modifier.width(LiteverTheme.spacing.small))
                    Text(
                        text = stringResource(R.string.permission_warning_critical),
                        style = MaterialTheme.typography.labelSmall,
                        color = LiteverTheme.colors.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!isGranted) {
                Spacer(modifier = Modifier.height(LiteverTheme.spacing.medium))
                LvButton(
                    onClick = onRequest,
                    modifier = Modifier.fillMaxWidth(),
                    semantic = LvSemantic.Primary
                ) {
                    Text(
                        stringResource(R.string.permission_request_action),
                    )
                }
            }
        }
    }
}

@Composable
fun ManufacturerSettingsTile(onOpen: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = LiteverTheme.colors.secondaryContainer.copy(alpha = 0.1f),
        shape = LiteverTheme.shapes.large,
        border = BorderStroke(1.dp, LiteverTheme.colors.secondary.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(LiteverTheme.spacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = ReMindIcons.SettingsSuggest,
                    contentDescription = null,
                    tint = LiteverTheme.colors.secondary
                )
                Spacer(modifier = Modifier.width(LiteverTheme.spacing.smallMedium))
                Text(
                    text = stringResource(R.string.permission_manufacturer_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(LiteverTheme.spacing.small))

            Text(
                text = stringResource(R.string.permission_manufacturer_desc),
                style = MaterialTheme.typography.bodySmall,
                color = LiteverTheme.colors.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(LiteverTheme.spacing.medium))

            LvButton(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth(),
                type = LvButtonType.Outlined,
                semantic = LvSemantic.Secondary
            ) {
                Text(
                    stringResource(R.string.permission_request_action),
                )
            }
        }
    }
}

@Composable
fun StatusBadge(isGranted: Boolean) {
    val successColor = LiteverTheme.colors.success
    val errorColor = LiteverTheme.colors.error
    val containerColor = if (isGranted) successColor.copy(alpha = 0.12f) else LiteverTheme.colors.errorContainer.copy(alpha = 0.4f)
    val borderColor = if (isGranted) successColor.copy(alpha = 0.3f) else errorColor.copy(alpha = 0.2f)
    val textColor = if (isGranted) successColor else errorColor

    Surface(
        color = containerColor,
        shape = LiteverTheme.shapes.small,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = if (isGranted) stringResource(R.string.permission_granted) else stringResource(R.string.permission_denied),
            modifier = Modifier.padding(
                horizontal = LiteverTheme.spacing.small,
                vertical = LiteverTheme.spacing.extraSmall
            ),
            style = LiteverTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

// Request Helpers

private fun requestNotificationPermission(context: Context) {
    val intent = Intent().apply {
        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    }
    context.startActivity(intent)
}

private fun requestExactAlarmPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val intent = Intent().apply {
            action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }
}

private fun requestFullScreenIntentPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val intent = Intent(
            Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
            "package:${context.packageName}".toUri()
        )
        try {
            context.startActivity(intent)
            return
        } catch (_: Exception) {
            // Fallback to notification settings
        }
    }
    requestNotificationPermission(context)
}

private fun requestOverlayPermission(context: Context) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        "package:${context.packageName}".toUri()
    )
    context.startActivity(intent)
}

private fun requestDndPolicyAccess(context: Context) {
    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
    context.startActivity(intent)
}

@SuppressLint("BatteryLife")
private fun requestIgnoreBatteryOptimization(context: Context) {
    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
        data = "package:${context.packageName}".toUri()
    }
    context.startActivity(intent)
}

private fun openManufacturerSettings(context: Context) {
    val manufacturer = Build.MANUFACTURER.lowercase()
    val packageName = context.packageName

    val candidateIntents = buildList {
        when {
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") -> {
                add(
                    Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                        setClassName(
                            "com.miui.securitycenter",
                            "com.miui.permcenter.permissions.PermissionsEditorActivity"
                        )
                        putExtra("extra_pkgname", packageName)
                    }
                )
                add(
                    Intent().apply {
                        component = ComponentName(
                            "com.miui.securitycenter",
                            "com.miui.permcenter.autostart.AutoStartManagementActivity"
                        )
                    }
                )
            }

            manufacturer.contains("oppo") || manufacturer.contains("realme") || manufacturer.contains("oneplus") -> {
                add(
                    Intent().apply {
                        component = ComponentName(
                            "com.coloros.safecenter",
                            "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                        )
                    }
                )
                add(
                    Intent().apply {
                        component = ComponentName(
                            "com.oplus.safecenter",
                            "com.oplus.safecenter.permission.startup.StartupAppListActivity"
                        )
                    }
                )
                add(
                    Intent().apply {
                        component = ComponentName(
                            "com.coloros.oppoguardelf",
                            "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity"
                        )
                    }
                )
            }

            manufacturer.contains("vivo") || manufacturer.contains("iqoo") -> {
                add(
                    Intent().apply {
                        component = ComponentName(
                            "com.vivo.permissionmanager",
                            "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                        )
                    }
                )
                add(
                    Intent().apply {
                        component = ComponentName(
                            "com.iqoo.secure",
                            "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
                        )
                    }
                )
            }

            manufacturer.contains("huawei") || manufacturer.contains("honor") -> {
                add(
                    Intent().apply {
                        component = ComponentName(
                            "com.huawei.systemmanager",
                            "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                        )
                    }
                )
                add(
                    Intent().apply {
                        component = ComponentName(
                            "com.hihonor.systemmanager",
                            "com.hihonor.systemmanager.optimize.process.ProtectActivity"
                        )
                    }
                )
            }

            manufacturer.contains("samsung") -> {
                add(
                    Intent().apply {
                        component = ComponentName(
                            "com.samsung.android.lool",
                            "com.samsung.android.sm.battery.ui.BatteryActivity"
                        )
                    }
                )
                add(
                    Intent().apply {
                        component = ComponentName(
                            "com.samsung.android.sm",
                            "com.samsung.android.sm.ui.battery.BatteryActivity"
                        )
                    }
                )
            }

            manufacturer.contains("asus") -> {
                add(
                    Intent().apply {
                        component = ComponentName(
                            "com.asus.mobilemanager",
                            "com.asus.mobilemanager.autostart.AutoStartActivity"
                        )
                    }
                )
            }
        }
    }

    for (intent in candidateIntents) {
        if (isIntentAvailable(context, intent)) {
            try {
                context.startActivity(intent)
                return
            } catch (_: Exception) {
                // Try next candidate
            }
        }
    }

    // Default fallback: App Details page
    val detailIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = "package:$packageName".toUri()
    }
    context.startActivity(detailIntent)
}

private fun isIntentAvailable(context: Context, intent: Intent): Boolean {
    return context.packageManager.queryIntentActivities(intent, 0).isNotEmpty()
}
