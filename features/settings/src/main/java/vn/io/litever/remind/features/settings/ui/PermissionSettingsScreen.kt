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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import vn.io.litever.remind.core.designsystem.components.ReMindScaffold
import vn.io.litever.remind.features.settings.R

@Composable
fun PermissionSettingsRoute(
    onNavigateBack: () -> Unit
) {
    PermissionSettingsScreen(onNavigateBack = onNavigateBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Status states
    var isNotificationGranted by remember { mutableStateOf(checkNotificationPermission(context)) }
    var isExactAlarmGranted by remember { mutableStateOf(checkExactAlarmPermission(context)) }
    var isOverlayGranted by remember { mutableStateOf(checkOverlayPermission(context)) }
    var isBatteryOptIgnored by remember { mutableStateOf(checkBatteryOptimization(context)) }

    fun refreshPermissions() {
        isNotificationGranted = checkNotificationPermission(context)
        isExactAlarmGranted = checkExactAlarmPermission(context)
        isOverlayGranted = checkOverlayPermission(context)
        isBatteryOptIgnored = checkBatteryOptimization(context)
    }

    // Observe lifecycle events to refresh when user returns to app
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    ReMindScaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.setting_permissions_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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
                        isGranted = isExactAlarmGranted,
                        icon = Icons.Default.Alarm,
                        isCritical = true,
                        onRequest = { requestExactAlarmPermission(context) }
                    )
                }
            }

            // 2. Notifications (High Priority, critical warning)
            item {
                PermissionTile(
                    title = stringResource(R.string.permission_notification_title),
                    description = stringResource(R.string.permission_notification_desc),
                    isGranted = isNotificationGranted,
                    icon = Icons.Default.Notifications,
                    isCritical = true,
                    onRequest = { requestNotificationPermission(context) }
                )
            }

            // 3. Overlay
            item {
                PermissionTile(
                    title = stringResource(R.string.permission_overlay_title),
                    description = stringResource(R.string.permission_overlay_desc),
                    isGranted = isOverlayGranted,
                    icon = Icons.Default.Layers,
                    onRequest = { requestOverlayPermission(context) }
                )
            }

            // 4. Battery Optimization
            item {
                PermissionTile(
                    title = stringResource(R.string.permission_battery_title),
                    description = stringResource(R.string.permission_battery_desc),
                    isGranted = isBatteryOptIgnored,
                    icon = Icons.Default.BatteryChargingFull,
                    onRequest = { requestIgnoreBatteryOptimization(context) }
                )
            }

            // 5. Manufacturer Specific (No status)
            item {
                ManufacturerSettingsTile(context)
            }
        }
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                
                StatusBadge(isGranted = isGranted)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
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
                        imageVector = Icons.Outlined.ErrorOutline,
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
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRequest,
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text(stringResource(R.string.permission_request_action))
                }
            }
        }
    }
}

@Composable
fun ManufacturerSettingsTile(context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SettingsSuggest,
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

            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = { openManufacturerSettings(context) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.permission_request_action))
            }
        }
    }
}

@Composable
fun StatusBadge(isGranted: Boolean) {
    Surface(
        color = if (isGranted) Color(0xFF4CAF50).copy(alpha = 0.1f) else MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.small
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

// Permission Helpers

private fun checkNotificationPermission(context: Context): Boolean {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return notificationManager.areNotificationsEnabled()
}

private fun checkExactAlarmPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }
}

private fun checkOverlayPermission(context: Context): Boolean {
    return Settings.canDrawOverlays(context)
}

private fun checkBatteryOptimization(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
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
