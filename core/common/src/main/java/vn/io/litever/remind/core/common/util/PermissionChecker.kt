package vn.io.litever.remind.core.common.util

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import javax.inject.Inject
import javax.inject.Singleton

enum class ReminderPermissionType {
    NOTIFICATION,
    EXACT_ALARM,
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
     * Checks if all non-critical alarm permissions (Overlay, Battery Optimization, DND policy, Full-Screen Intent) are granted.
     */
    fun hasNonCriticalPermissions(): Boolean {
        return hasOverlayPermission() &&
                isIgnoringBatteryOptimizations() &&
                hasNotificationPolicyAccess() &&
                hasFullScreenIntentPermission()
    }

    /**
     * Checks if all required permissions for a specific Reminder instance are granted.
     */
    fun hasPermissionsForReminder(reminder: Reminder): Boolean {
        if (!hasNotificationPermission()) return false

        return when (reminder.triggerType) {
            ReminderTriggerType.FIXED_TIME,
            ReminderTriggerType.INTERVAL -> hasExactAlarmPermission()
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
            ReminderTriggerType.FIXED_TIME,
            ReminderTriggerType.INTERVAL -> {
                if (!hasExactAlarmPermission()) {
                    missing.add(ReminderPermissionType.EXACT_ALARM)
                }
            }
        }

        if (reminder.action.notificationStyle == NotificationStyle.FULL_SCREEN && !hasFullScreenIntentPermission()) {
            missing.add(ReminderPermissionType.FULL_SCREEN_INTENT)
        }

        return missing
    }
}
