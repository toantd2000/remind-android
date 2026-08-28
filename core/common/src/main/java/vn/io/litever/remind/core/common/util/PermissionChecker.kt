package vn.io.litever.remind.core.common.util

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import javax.inject.Inject
import javax.inject.Singleton

enum class ReminderPermissionType {
    NOTIFICATION,
    EXACT_ALARM,
    LOCATION,
    BACKGROUND_LOCATION,
    WIFI,
    FULL_SCREEN_INTENT,
    OVERLAY
}

@Singleton
class PermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Checks if notifications are enabled (runtime POST_NOTIFICATIONS on API 33+ or app-level).
     */
    fun hasNotificationPermission(): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.areNotificationsEnabled()
    }

    /**
     * Checks if exact alarms can be scheduled (API 31+ SCHEDULE_EXACT_ALARM / USE_EXACT_ALARM).
     */
    fun hasExactAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * Checks if either FINE or COARSE location permission is granted.
     */
    fun hasLocationPermission(): Boolean {
        val hasFine = context.checkSelfPermission(
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = context.checkSelfPermission(
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return hasFine || hasCoarse
    }

    /**
     * Checks if FINE location permission is granted (required for high-accuracy geofencing).
     */
    fun hasFineLocationPermission(): Boolean {
        return context.checkSelfPermission(
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if BACKGROUND location permission is granted (API 29+).
     */
    fun hasBackgroundLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.checkSelfPermission(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Checks if Wi-Fi state permission is granted.
     */
    fun hasWifiStatePermission(): Boolean {
        return context.checkSelfPermission(
            Manifest.permission.ACCESS_WIFI_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if Network state permission is granted.
     */
    fun hasNetworkStatePermission(): Boolean {
        return context.checkSelfPermission(
            Manifest.permission.ACCESS_NETWORK_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if permissions required for Wi-Fi SSID identification are granted.
     * On Android 10+ (API 29+), ACCESS_FINE_LOCATION is required alongside ACCESS_WIFI_STATE.
     */
    fun hasWifiTriggerPermissions(): Boolean {
        return hasWifiStatePermission() && hasLocationPermission()
    }

    /**
     * Checks if Full-Screen Intent permission is granted (API 34+).
     */
    fun hasFullScreenIntentPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.canUseFullScreenIntent()
        } else {
            true
        }
    }

    /**
     * Checks if SYSTEM_ALERT_WINDOW (overlay) permission is granted.
     */
    fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    /**
     * Checks if battery optimization is ignored for background persistence.
     */
    fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun hasNotificationPolicyAccess(): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }

    /**
     * Checks if critical alarm permissions (Notification + Exact Alarm) are granted.
     */
    fun hasCriticalPermissions(): Boolean {
        return hasNotificationPermission() && hasExactAlarmPermission()
    }

    /**
     * Checks if all required permissions for a specific Reminder instance are granted.
     */
    fun hasPermissionsForReminder(reminder: Reminder): Boolean {
        if (!hasNotificationPermission()) return false

        return when (reminder.triggerType) {
            ReminderTriggerType.TIME -> hasExactAlarmPermission()
            ReminderTriggerType.LOCATION -> hasLocationPermission()
            ReminderTriggerType.WIFI -> hasWifiTriggerPermissions()
        }
    }

    /**
     * Returns a list of missing permissions for a specific Reminder.
     */
    fun getMissingPermissions(reminder: Reminder): List<ReminderPermissionType> {
        val missing = mutableListOf<ReminderPermissionType>()
        if (!hasNotificationPermission()) {
            missing.add(ReminderPermissionType.NOTIFICATION)
        }

        when (reminder.triggerType) {
            ReminderTriggerType.TIME -> {
                if (!hasExactAlarmPermission()) {
                    missing.add(ReminderPermissionType.EXACT_ALARM)
                }
            }
            ReminderTriggerType.LOCATION -> {
                if (!hasLocationPermission()) {
                    missing.add(ReminderPermissionType.LOCATION)
                }
            }
            ReminderTriggerType.WIFI -> {
                if (!hasWifiStatePermission()) {
                    missing.add(ReminderPermissionType.WIFI)
                }
                if (!hasLocationPermission()) {
                    missing.add(ReminderPermissionType.LOCATION)
                }
            }
        }

        if (reminder.notificationStyle == NotificationStyle.FULL_SCREEN && !hasFullScreenIntentPermission()) {
            missing.add(ReminderPermissionType.FULL_SCREEN_INTENT)
        }

        return missing
    }
}










