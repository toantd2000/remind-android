package vn.io.litever.remind.features.settings.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import vn.io.litever.remind.core.designsystem.components.ReMindTopAppBar
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
        onRequestOverlay = { requestOverlayPermission(context) },
        onRequestBatteryOptimization = { requestIgnoreBatteryOptimization(context) },
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
    onRequestOverlay: () -> Unit,
    onRequestBatteryOptimization: () -> Unit,
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
            ReMindTopAppBar(
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
            verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
        ) {
            // 1. Exact Alarm (Top Priority, conditional)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                item {
                    PermissionTile(
                        title = stringResource(R.string.permission_exact_alarm_title),
                        description = stringResource(R.string.permission_exact_alarm_desc),
                        isGranted = uiState.isExactAlarmGranted,
                        icon = Icons.Rounded.Alarm,
                        isCritical = true,
                        onRequest = onRequestExactAlarm
                    )
                }
            }

            // 2. Notifications (High Priority, critical warning)
            item {
                PermissionTile(
                    title = stringResource(R.string.permission_notification_title),
                    description = stringResource(R.string.permission_notification_desc),
                    isGranted = uiState.isNotificationGranted,
                    icon = Icons.Rounded.Notifications,
                    isCritical = true,
                    onRequest = onRequestNotification
                )
            }

            // 3. Overlay
            item {
                PermissionTile(
                    title = stringResource(R.string.permission_overlay_title),
                    description = stringResource(R.string.permission_overlay_desc),
                    isGranted = uiState.isOverlayGranted,
                    icon = Icons.Rounded.Layers,
                    onRequest = onRequestOverlay
                )
            }

            // 4. Battery Optimization
            item {
                PermissionTile(
                    title = stringResource(R.string.permission_battery_title),
                    description = stringResource(R.string.permission_battery_desc),
                    isGranted = uiState.isBatteryOptIgnored,
                    icon = Icons.Rounded.BatteryChargingFull,
                    onRequest = onRequestBatteryOptimization
                )
            }

            // 5. Manufacturer Specific (No status)
            item {
                ManufacturerSettingsTile(onOpen = onOpenManufacturerSettings)
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun PermissionSettingsScreenPreview() {
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme {
        PermissionSettingsScreen(
            uiState = PermissionUiState(
                isExactAlarmGranted = true,
                isNotificationGranted = false,
                isOverlayGranted = false,
                isBatteryOptIgnored = true
            ),
            onNavigateBack = {},
            onRefreshPermissions = {},
            onRequestExactAlarm = {},
            onRequestNotification = {},
            onRequestOverlay = {},
            onRequestBatteryOptimization = {},
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = LiteverTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, LiteverTheme.colors.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(LiteverTheme.spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top // Căn Top để nếu tiêu đề dài xuống dòng trông vẫn đẹp
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
                    
                    // Badge nằm ngay dưới tiêu đề nếu màn hình hẹp, hoặc có thể tùy biến
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
                        imageVector = Icons.Rounded.ErrorOutline,
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = LiteverTheme.colors.secondaryContainer.copy(alpha = 0.1f)
        ),
        shape = LiteverTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, LiteverTheme.colors.secondary.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(LiteverTheme.spacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.SettingsSuggest,
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
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
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

private fun requestOverlayPermission(context: Context) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        "package:${context.packageName}".toUri()
    )
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
    
    val intent = when {
        manufacturer.contains("xiaomi") -> {
            Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
                putExtra("extra_pkgname", packageName)
            }
        }
        else -> null
    }

    if (intent != null && isIntentAvailable(context, intent)) {
        try {
            context.startActivity(intent)
            return
        } catch (e: Exception) {
            // Fallback
        }
    }

    // Default fallback: App Details page (users can find "Other permissions" here)
    val detailIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = "package:$packageName".toUri()
    }
    context.startActivity(detailIntent)
}

private fun isIntentAvailable(context: Context, intent: Intent): Boolean {
    return context.packageManager.queryIntentActivities(intent, 0).isNotEmpty()
}










