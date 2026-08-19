package vn.io.litever.remind.core.common.util

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import vn.io.litever.remind.core.model.LocationTrigger
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.model.WifiTrigger
import java.time.LocalTime

class PermissionCheckerTest {

    private val context = mockk<Context>(relaxed = true)
    private val notificationManager = mockk<NotificationManager>(relaxed = true)
    private val alarmManager = mockk<AlarmManager>(relaxed = true)
    private val powerManager = mockk<PowerManager>(relaxed = true)

    private lateinit var permissionChecker: PermissionChecker

    @Before
    fun setup() {
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
        every { context.getSystemService(Context.ALARM_SERVICE) } returns alarmManager
        every { context.getSystemService(Context.POWER_SERVICE) } returns powerManager
        every { context.packageName } returns "vn.io.litever.remind"

        permissionChecker = PermissionChecker(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun hasNotificationPermission_reflectsNotificationManager() {
        every { notificationManager.areNotificationsEnabled() } returns true
        assertTrue(permissionChecker.hasNotificationPermission())

        every { notificationManager.areNotificationsEnabled() } returns false
        assertFalse(permissionChecker.hasNotificationPermission())
    }

    @Test
    fun isIgnoringBatteryOptimizations_reflectsPowerManager() {
        every { powerManager.isIgnoringBatteryOptimizations("vn.io.litever.remind") } returns true
        assertTrue(permissionChecker.isIgnoringBatteryOptimizations())

        every { powerManager.isIgnoringBatteryOptimizations("vn.io.litever.remind") } returns false
        assertFalse(permissionChecker.isIgnoringBatteryOptimizations())
    }

    @Test
    fun hasPermissionsForReminder_timeReminder_checksNotificationsAndAlarms() {
        val reminder = Reminder(
            title = "Medicine",
            triggerType = ReminderTriggerType.TIME,
            timeTrigger = TimeTrigger(time = LocalTime.of(8, 0))
        )

        every { notificationManager.areNotificationsEnabled() } returns true
        // On pre-S or exact alarms enabled:
        assertTrue(permissionChecker.hasPermissionsForReminder(reminder))

        every { notificationManager.areNotificationsEnabled() } returns false
        assertFalse(permissionChecker.hasPermissionsForReminder(reminder))
    }

    @Test
    fun getMissingPermissions_returnsExpectedListForLocation() {
        val reminder = Reminder(
            title = "Arrive home",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(latitude = 21.0, longitude = 105.0, radiusMeters = 100f),
            notificationStyle = NotificationStyle.FULL_SCREEN
        )

        every { notificationManager.areNotificationsEnabled() } returns false
        every { context.checkPermission(any(), any(), any()) } returns PackageManager.PERMISSION_DENIED

        val missing = permissionChecker.getMissingPermissions(reminder)
        assertTrue(missing.contains(ReminderPermissionType.NOTIFICATION))
        assertTrue(missing.contains(ReminderPermissionType.LOCATION))
    }

    @Test
    fun getMissingPermissions_returnsExpectedListForWifi() {
        val reminder = Reminder(
            title = "Connect Wifi",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = WifiTrigger(ssid = "Office_WiFi")
        )

        every { notificationManager.areNotificationsEnabled() } returns false
        every { context.checkPermission(any(), any(), any()) } returns PackageManager.PERMISSION_DENIED

        val missing = permissionChecker.getMissingPermissions(reminder)
        assertTrue(missing.contains(ReminderPermissionType.NOTIFICATION))
        assertTrue(missing.contains(ReminderPermissionType.WIFI))
        assertTrue(missing.contains(ReminderPermissionType.LOCATION))
    }
}
