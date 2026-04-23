package vn.io.litever.remind.features.settings.ui

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import vn.io.litever.remind.features.settings.R
import vn.io.litever.remind.core.designsystem.components.*

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

    ReMindScaffold(
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top // Căn Top để nếu tiêu đề dài xuống dòng trông vẫn đẹp
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Badge nằm ngay dưới tiêu đề nếu màn hình hẹp, hoặc có thể tùy biến
                    StatusBadge(isGranted = isGranted)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!isGranted && isCritical) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.permission_warning_critical),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (!isGranted) {
                Spacer(modifier = Modifier.height(16.dp))
                ReMindButton(
                    onClick = onRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.permission_request_action),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
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
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f)
        ),
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.SettingsSuggest,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.permission_manufacturer_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.permission_manufacturer_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            ReMindOutlinedButton(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.permission_request_action),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun StatusBadge(isGranted: Boolean) {
    Surface(
        color = if (isGranted) Color(0xFF4CAF50).copy(alpha = 0.1f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
        shape = MaterialTheme.shapes.small,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isGranted) Color(0xFF4CAF50).copy(alpha = 0.2f) else MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
        )
    ) {
        Text(
            text = if (isGranted) stringResource(R.string.permission_granted) else stringResource(R.string.permission_denied),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (isGranted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
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
        Uri.parse("package:${context.packageName}")
    )
    context.startActivity(intent)
}

private fun requestIgnoreBatteryOptimization(context: Context) {
    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
        data = Uri.parse("package:${context.packageName}")
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
        data = Uri.parse("package:$packageName")
    }
    context.startActivity(detailIntent)
}

private fun isIntentAvailable(context: Context, intent: Intent): Boolean {
    return context.packageManager.queryIntentActivities(intent, 0).isNotEmpty()
}
