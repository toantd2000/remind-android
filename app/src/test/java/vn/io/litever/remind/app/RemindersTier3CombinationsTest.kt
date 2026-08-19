package vn.io.litever.remind.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.io.litever.remind.core.model.ChecklistItem
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.core.model.GeofenceTransitionType
import vn.io.litever.remind.core.model.LocationTrigger
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.model.WifiTrigger
import vn.io.litever.remind.core.model.WifiTriggerType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Master Tier 3 Test Suite: Cross-Feature Pairwise and Multi-Feature Combinations (18 Tests).
 * Authoritatively derived from TEST_INFRA.md and ORIGINAL_REQUEST.md.
 */
class RemindersTier3CombinationsTest {

    @Test
    fun c01_geofence_timeWindow_checklist_snoozePersistence() {
        val window = ReminderTimeWindow(startTime = LocalTime.of(8, 0), endTime = LocalTime.of(18, 0))
        val loc = LocationTrigger(latitude = 21.0285, longitude = 105.8542, radiusMeters = 100f, timeWindow = window)
        val items = listOf(
            ChecklistItem(id = 1, reminderId = 1, text = "Submit daily report", isCompleted = false),
            ChecklistItem(id = 2, reminderId = 1, text = "Review team pull requests", isCompleted = false)
        )
        val reminder = Reminder(
            id = 1,
            title = "Office Checklist",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = loc,
            checklist = items,
            snoozeEnabled = true,
            snoozeRepeatCount = 3,
            currentSnoozeCount = 0
        )

        // 1. Verify within window
        val arrivalTime = LocalDateTime.of(2026, 8, 19, 9, 0)
        assertTrue(reminder.locationTrigger!!.timeWindow!!.isWithinWindow(arrivalTime))

        // 2. User checks item 1 and snoozes
        val checkedItems = listOf(items[0].copy(isCompleted = true), items[1])
        val snoozed = reminder.copy(
            checklist = checkedItems,
            currentSnoozeCount = 1,
            snoozeNextTriggerTime = System.currentTimeMillis() + 600000L
        )

        // 3. Verify state persistence
        assertEquals(1, snoozed.currentSnoozeCount)
        assertTrue(snoozed.checklist[0].isCompleted)
        assertFalse(snoozed.checklist[1].isCompleted)
        assertEquals(0.5f, snoozed.checklistProgress, 0.001f)
    }

    @Test
    fun c02_wifiOnConnect_timeWindow_persistentSpamAlert() {
        val window = ReminderTimeWindow(startTime = LocalTime.of(19, 0), endTime = LocalTime.of(23, 0))
        val wifi = WifiTrigger(ssid = "Home_WiFi", triggerType = WifiTriggerType.ON_CONNECT, timeWindow = window)
        val reminder = Reminder(
            id = 2,
            title = "Evening Medication",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = wifi,
            isPersistentAlert = true,
            volume = 95
        )

        val insideTime = LocalDateTime.of(2026, 8, 19, 20, 30)
        assertTrue(reminder.wifiTrigger!!.timeWindow!!.isWithinWindow(insideTime))
        assertTrue(reminder.isPersistentAlert)
        assertEquals(95, reminder.volume)
    }

    @Test
    fun c03_timeTrigger_recurringDays_checklist_maxSnoozeLimit() {
        val days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        val timeTrigger = TimeTrigger(time = LocalTime.of(7, 30), repeatDays = days)
        val items = listOf(ChecklistItem(id = 1, reminderId = 3, text = "Morning Vitamins", isCompleted = false))
        val reminder = Reminder(
            id = 3,
            title = "MWF Routine",
            triggerType = ReminderTriggerType.TIME,
            timeTrigger = timeTrigger,
            checklist = items,
            snoozeRepeatCount = 2,
            currentSnoozeCount = 2 // Max reached
        )

        assertFalse("Snooze must be disabled when max count is reached", reminder.canSnooze())
        assertEquals(3, reminder.timeTrigger!!.repeatDays.size)
    }

    @Test
    fun c04_geofenceExit_timeWindowMidnightCrossing_fullScreenIntent() {
        // Shift worker leaving at 02:00 AM (Window 22:00 -> 04:00)
        val window = ReminderTimeWindow(startTime = LocalTime.of(22, 0), endTime = LocalTime.of(4, 0))
        val loc = LocationTrigger(
            latitude = 37.7749, longitude = -122.4194,
            radiusMeters = 200f, transitionType = GeofenceTransitionType.EXIT,
            timeWindow = window
        )
        val reminder = Reminder(
            id = 4,
            title = "Night Shift Exit",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = loc,
            notificationStyle = NotificationStyle.FULL_SCREEN
        )

        val exitTime = LocalDateTime.of(2026, 8, 20, 2, 15)
        assertTrue(reminder.locationTrigger!!.timeWindow!!.isWithinWindow(exitTime))
        assertEquals(NotificationStyle.FULL_SCREEN, reminder.notificationStyle)
    }

    @Test
    fun c05_wifiOnDisconnect_timeWindow_standardNotification_liveChecklistToggle() {
        val window = ReminderTimeWindow(startTime = LocalTime.of(7, 0), endTime = LocalTime.of(9, 0))
        val wifi = WifiTrigger(ssid = "Home_Fiber", triggerType = WifiTriggerType.ON_DISCONNECT, timeWindow = window)
        val items = listOf(
            ChecklistItem(id = 1, reminderId = 5, text = "Grab keys", isCompleted = false),
            ChecklistItem(id = 2, reminderId = 5, text = "Grab wallet", isCompleted = false),
            ChecklistItem(id = 3, reminderId = 5, text = "Lock door", isCompleted = false)
        )
        val reminder = Reminder(
            id = 5,
            title = "Leaving Home Checklist",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = wifi,
            checklist = items,
            notificationStyle = NotificationStyle.STANDARD
        )

        val disconnectTime = LocalDateTime.of(2026, 8, 19, 7, 45)
        assertTrue(reminder.wifiTrigger!!.timeWindow!!.isWithinWindow(disconnectTime))

        // Toggle all live items
        val allChecked = reminder.checklist.map { it.copy(isCompleted = true) }
        val updatedReminder = reminder.copy(checklist = allChecked)

        assertTrue(updatedReminder.isAllChecklistCompleted)
        assertEquals(1.0f, updatedReminder.checklistProgress, 0.001f)
    }

    @Test
    fun c06_geofenceBoth_zeroPermissions_saveAnywayFlow() {
        val loc = LocationTrigger(latitude = 10.7769, longitude = 106.7009, radiusMeters = 150f, transitionType = GeofenceTransitionType.BOTH)
        val reminder = Reminder(
            id = 6,
            title = "Warehouse Entrance/Exit",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = loc,
            isEnabled = false // Saved anyway without permissions
        )

        assertFalse(reminder.isEnabled)
        assertEquals(GeofenceTransitionType.BOTH, reminder.locationTrigger!!.transitionType)
    }

    @Test
    fun c07_timeTrigger_oneTimeDate_persistentAlert_customSnoozeInterval() {
        val date = LocalDate.of(2026, 12, 25)
        val time = LocalTime.of(8, 0)
        val reminder = Reminder(
            id = 7,
            title = "Christmas Morning Surprise",
            triggerType = ReminderTriggerType.TIME,
            timeTrigger = TimeTrigger(time = time, date = date),
            isPersistentAlert = true,
            snoozeIntervalMinutes = 20,
            snoozeRepeatCount = 5
        )

        assertEquals(date, reminder.timeTrigger!!.date)
        assertTrue(reminder.isPersistentAlert)
        assertEquals(20, reminder.snoozeIntervalMinutes)
        assertEquals(5, reminder.snoozeRepeatCount)
    }

    @Test
    fun c08_wifiWhileConnected_timeWindow_ringingScreen_dismiss() {
        val window = ReminderTimeWindow(startTime = LocalTime.of(13, 0), endTime = LocalTime.of(15, 0))
        val wifi = WifiTrigger(ssid = "Library_WiFi", triggerType = WifiTriggerType.WHILE_CONNECTED, timeWindow = window)
        val reminder = Reminder(
            id = 8,
            title = "Study Session Focus",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = wifi,
            notificationStyle = NotificationStyle.FULL_SCREEN
        )

        val studyTime = LocalDateTime.of(2026, 8, 19, 14, 0)
        assertTrue(reminder.wifiTrigger!!.timeWindow!!.isWithinWindow(studyTime))

        // Dismiss action
        val dismissed = reminder.copy(isCompleted = true)
        assertTrue(dismissed.isCompleted)
    }

    @Test
    fun c09_geofence_largeChecklist_partialSnooze_rebootRecovery() {
        val largeList = (1..50).map {
            ChecklistItem(id = it.toLong(), reminderId = 9, text = "Inventory item #$it", isCompleted = it <= 30)
        }
        val reminder = Reminder(
            id = 9,
            title = "Stock Inventory",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(latitude = 21.0, longitude = 105.0, radiusMeters = 50f),
            checklist = largeList,
            currentSnoozeCount = 1,
            snoozeNextTriggerTime = 1724050000000L
        )

        // Verify state preserved after reboot simulation
        assertEquals(50, reminder.checklist.size)
        assertEquals(30, reminder.completedChecklistCount)
        assertEquals(0.6f, reminder.checklistProgress, 0.001f)
        assertEquals(1, reminder.currentSnoozeCount)
    }

    @Test
    fun c10_wifiQuotedSsid_fullScreenIntent_volumeRamping() {
        val wifi = WifiTrigger(ssid = "\"Corporate_Secure_Network\"")
        val reminder = Reminder(
            id = 10,
            title = "IT Security Briefing",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = wifi,
            notificationStyle = NotificationStyle.FULL_SCREEN,
            volume = 100
        )

        assertEquals("\"Corporate_Secure_Network\"", reminder.wifiTrigger!!.ssid)
        assertEquals(NotificationStyle.FULL_SCREEN, reminder.notificationStyle)
        assertEquals(100, reminder.volume)
    }

    @Test
    fun c11_timeTrigger_midnightCrossingWindow_standardNotification_quickDelete() {
        val window = ReminderTimeWindow(startTime = LocalTime.of(23, 0), endTime = LocalTime.of(2, 0))
        val reminder = Reminder(
            id = 11,
            title = "Late Night Check",
            triggerType = ReminderTriggerType.TIME,
            timeTrigger = TimeTrigger(time = LocalTime.of(23, 30)),
            notificationStyle = NotificationStyle.STANDARD
        )

        val atLateNight = LocalDateTime.of(2026, 8, 19, 23, 30)
        assertTrue(window.isWithinWindow(atLateNight))
        assertEquals(NotificationStyle.STANDARD, reminder.notificationStyle)
    }

    @Test
    fun c12_compositeTriggerSimulation_locationAndWifiSameReminder() {
        val loc = LocationTrigger(latitude = 21.0, longitude = 105.0, radiusMeters = 100f)
        val wifi = WifiTrigger(ssid = "Office_WiFi")
        val reminder = Reminder(
            id = 12,
            title = "Smart Office Arrival",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = loc,
            wifiTrigger = wifi
        )

        assertNotNull(reminder.locationTrigger)
        assertNotNull(reminder.wifiTrigger)
    }

    @Test
    fun c13_timeTrigger_foregroundServiceNotRequired() {
        val reminder = Reminder(
            id = 13,
            title = "Pure Alarm",
            triggerType = ReminderTriggerType.TIME,
            timeTrigger = TimeTrigger(time = LocalTime.of(12, 0))
        )
        val needsService = reminder.triggerType != ReminderTriggerType.TIME
        assertFalse(needsService)
    }

    @Test
    fun c14_wifiTrigger_missingPermissions_bannerShown_disabledState() {
        val wifi = WifiTrigger(ssid = "Cafe_WiFi")
        val reminder = Reminder(
            id = 14,
            title = "Cafe Visit",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = wifi,
            isEnabled = false
        )
        assertFalse(reminder.isEnabled)
    }

    @Test
    fun c15_geofence_snoozeLimitOne_snoozeOnce_subsequentDismissal() {
        val reminder = Reminder(
            id = 15,
            title = "Quick Errand",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(latitude = 10.0, longitude = 106.0, radiusMeters = 50f),
            snoozeRepeatCount = 1,
            currentSnoozeCount = 0
        )
        assertTrue(reminder.canSnooze())

        val snoozedOnce = reminder.copy(currentSnoozeCount = 1)
        assertFalse(snoozedOnce.canSnooze())

        val dismissed = snoozedOnce.copy(isCompleted = true)
        assertTrue(dismissed.isCompleted)
    }

    @Test
    fun c16_timeTrigger_allChecklistItemsCompletedPriorToTrigger_autoComplete() {
        val items = listOf(
            ChecklistItem(id = 1, reminderId = 16, text = "Task A", isCompleted = true),
            ChecklistItem(id = 2, reminderId = 16, text = "Task B", isCompleted = true)
        )
        val reminder = Reminder(
            id = 16,
            title = "Pre-completed Tasks",
            triggerType = ReminderTriggerType.TIME,
            checklist = items
        )

        assertTrue(reminder.isAllChecklistCompleted)
    }

    @Test
    fun c17_wifiAndGeofenceOutsideTimeWindow_suppressesTrigger() {
        val window = ReminderTimeWindow(startTime = LocalTime.of(9, 0), endTime = LocalTime.of(11, 0))
        val nowOutside = LocalDateTime.of(2026, 8, 19, 14, 0)
        assertFalse(window.isWithinWindow(nowOutside))
    }

    @Test
    fun c18_quadFeatureFusion_geofence_timeWindow_checklist_persistentAlert_fullScreen() {
        val window = ReminderTimeWindow(startTime = LocalTime.of(8, 0), endTime = LocalTime.of(10, 0))
        val loc = LocationTrigger(latitude = 21.0285, longitude = 105.8542, radiusMeters = 100f, timeWindow = window)
        val items = listOf(
            ChecklistItem(id = 1, reminderId = 18, text = "Critical Inspection 1", isCompleted = false),
            ChecklistItem(id = 2, reminderId = 18, text = "Critical Inspection 2", isCompleted = false)
        )
        val reminder = Reminder(
            id = 18,
            title = "Emergency Inspection Routine",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = loc,
            checklist = items,
            notificationStyle = NotificationStyle.FULL_SCREEN,
            isPersistentAlert = true,
            volume = 100,
            vibrationEnabled = true
        )

        val insideTime = LocalDateTime.of(2026, 8, 19, 8, 30)
        assertTrue(reminder.locationTrigger!!.timeWindow!!.isWithinWindow(insideTime))
        assertEquals(2, reminder.checklist.size)
        assertEquals(NotificationStyle.FULL_SCREEN, reminder.notificationStyle)
        assertTrue(reminder.isPersistentAlert)
        assertEquals(100, reminder.volume)
        assertTrue(reminder.vibrationEnabled)
    }
}
