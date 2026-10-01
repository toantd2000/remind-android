package vn.io.litever.remind.core.common.util

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.os.PowerManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderAction
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.TimeTrigger
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
    fun hasCriticalPermissions_requiresBothNotificationAndExactAlarm() {
        every { notificationManager.areNotificationsEnabled() } returns true
        assertTrue(permissionChecker.hasCriticalPermissions())

        every { notificationManager.areNotificationsEnabled() } returns false
        assertFalse(permissionChecker.hasCriticalPermissions())
    }

    @Test
    fun hasNonCriticalPermissions_requiresOverlayBatteryDndFullScreen() {
        io.mockk.mockkStatic(android.provider.Settings::class)
        try {
            every { android.provider.Settings.canDrawOverlays(any()) } returns true
            every { powerManager.isIgnoringBatteryOptimizations("vn.io.litever.remind") } returns true
            every { notificationManager.isNotificationPolicyAccessGranted } returns true
            assertTrue(permissionChecker.hasNonCriticalPermissions())

            every { powerManager.isIgnoringBatteryOptimizations("vn.io.litever.remind") } returns false
            assertFalse(permissionChecker.hasNonCriticalPermissions())
        } finally {
            io.mockk.unmockkStatic(android.provider.Settings::class)
        }
    }

    @Test
    fun hasPermissionsForReminder_fixedTimeReminder_checksNotificationsAndAlarms() {
        val reminder = Reminder(
            title = "Medicine",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(time = listOf(LocalTime.of(8, 0)))
        )

        every { notificationManager.areNotificationsEnabled() } returns true
        assertTrue(permissionChecker.hasPermissionsForReminder(reminder))

        every { notificationManager.areNotificationsEnabled() } returns false
        assertFalse(permissionChecker.hasPermissionsForReminder(reminder))
    }

    @Test
    fun hasPermissionsForReminder_intervalReminder_checksNotificationsAndAlarms() {
        val reminder = Reminder(
            title = "Drink Water",
            triggerType = ReminderTriggerType.INTERVAL,
            timeTrigger = TimeTrigger(intervalMinutes = 60)
        )

        every { notificationManager.areNotificationsEnabled() } returns true
        assertTrue(permissionChecker.hasPermissionsForReminder(reminder))

        every { notificationManager.areNotificationsEnabled() } returns false
        assertFalse(permissionChecker.hasPermissionsForReminder(reminder))
    }

    @Test
    fun getMissingPermissions_returnsMissingNotificationAndExactAlarm() {
        val reminder = Reminder(
            title = "Morning Alarm",
            triggerType = ReminderTriggerType.FIXED_TIME,
            timeTrigger = TimeTrigger(time = listOf(LocalTime.of(7, 0))),
            action = ReminderAction(notificationStyle = NotificationStyle.FULL_SCREEN)
        )

        every { notificationManager.areNotificationsEnabled() } returns false

        val missing = permissionChecker.getMissingPermissions(reminder)
        assertTrue(missing.contains(ReminderPermissionType.NOTIFICATION))
    }
}
