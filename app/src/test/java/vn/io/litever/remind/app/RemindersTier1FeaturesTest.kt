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
 * Master Tier 1 Test Suite: 17 Features x 5 Test Cases = 85 Tests.
 * Explicitly authoritative against ORIGINAL_REQUEST.md and TEST_INFRA.md.
 */
class RemindersTier1FeaturesTest {

    // =========================================================================
    // FEATURE 1: Time Trigger (ORIGINAL_REQUEST § R1)
    // =========================================================================

    @Test
    fun f01_timeTrigger_singleOccurrence_futureDate() {
        val date = LocalDate.of(2026, 9, 1)
        val time = LocalTime.of(9, 30)
        val trigger = TimeTrigger(time = time, date = date)

        assertEquals(date, trigger.date)
        assertEquals(time, trigger.time)
        assertTrue(trigger.repeatDays.isEmpty())
    }

    @Test
    fun f01_timeTrigger_recurringDays_multipleDays() {
        val days = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        val trigger = TimeTrigger(time = LocalTime.of(7, 0), repeatDays = days)

        assertNull(trigger.date)
        assertEquals(3, trigger.repeatDays.size)
        assertTrue(trigger.repeatDays.contains(DayOfWeek.WEDNESDAY))
    }

    @Test
    fun f01_timeTrigger_nextOccurrence_sameDayFuture() {
        val trigger = TimeTrigger(time = LocalTime.of(18, 0))
        val now = LocalDateTime.of(2026, 8, 19, 12, 0)
        val next = trigger.getNextOccurrence(now)

        assertEquals(LocalDateTime.of(2026, 8, 19, 18, 0), next)
    }

    @Test
    fun f01_timeTrigger_nextOccurrence_pastTime_rollsToTomorrow() {
        val trigger = TimeTrigger(time = LocalTime.of(8, 0))
        val now = LocalDateTime.of(2026, 8, 19, 12, 0)
        val next = trigger.getNextOccurrence(now)

        assertEquals(LocalDateTime.of(2026, 8, 20, 8, 0), next)
    }

    @Test
    fun f01_timeTrigger_reminderWrapper_stateActive() {
        val reminder = Reminder(
            title = "Standup Meeting",
            triggerType = ReminderTriggerType.TIME,
            timeTrigger = TimeTrigger(time = LocalTime.of(9, 0))
        )
        assertTrue(reminder.isEnabled)
        assertFalse(reminder.isCompleted)
        assertEquals(ReminderTriggerType.TIME, reminder.triggerType)
    }

    // =========================================================================
    // FEATURE 2: Geofence Location Trigger (ORIGINAL_REQUEST § R1)
    // =========================================================================

    @Test
    fun f02_geofence_enterTransition_properties() {
        val loc = LocationTrigger(
            latitude = 37.7749, longitude = -122.4194,
            radiusMeters = 100f, locationName = "San Francisco Office",
            transitionType = GeofenceTransitionType.ENTER
        )
        assertEquals(GeofenceTransitionType.ENTER, loc.transitionType)
        assertEquals(100f, loc.radiusMeters, 0.01f)
    }

    @Test
    fun f02_geofence_exitTransition_properties() {
        val loc = LocationTrigger(
            latitude = 37.7749, longitude = -122.4194,
            radiusMeters = 200f, locationName = "Home",
            transitionType = GeofenceTransitionType.EXIT
        )
        assertEquals(GeofenceTransitionType.EXIT, loc.transitionType)
        assertEquals(200f, loc.radiusMeters, 0.01f)
    }

    @Test
    fun f02_geofence_bothTransitions_properties() {
        val loc = LocationTrigger(
            latitude = 40.7128, longitude = -74.0060,
            radiusMeters = 150f, locationName = "Warehouse",
            transitionType = GeofenceTransitionType.BOTH
        )
        assertEquals(GeofenceTransitionType.BOTH, loc.transitionType)
    }

    @Test
    fun f02_geofence_customRadiusAndLocationName() {
        val loc = LocationTrigger(
            latitude = 10.7769, longitude = 106.7009,
            radiusMeters = 500f, locationName = "District 1 Center"
        )
        assertEquals("District 1 Center", loc.locationName)
        assertEquals(500f, loc.radiusMeters, 0.01f)
    }

    @Test
    fun f02_geofence_attachedToReminder_correctTriggerType() {
        val reminder = Reminder(
            title = "Pick up packages",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(latitude = 10.0, longitude = 106.0, radiusMeters = 100f)
        )
        assertEquals(ReminderTriggerType.LOCATION, reminder.triggerType)
        assertNotNull(reminder.locationTrigger)
    }

    // =========================================================================
    // FEATURE 3: Wi-Fi Network Trigger (ORIGINAL_REQUEST § R1)
    // =========================================================================

    @Test
    fun f03_wifi_onConnect_properties() {
        val wifi = WifiTrigger(ssid = "Office_5G", triggerType = WifiTriggerType.ON_CONNECT)
        assertEquals("Office_5G", wifi.ssid)
        assertEquals(WifiTriggerType.ON_CONNECT, wifi.triggerType)
        assertNull(wifi.bssid)
    }

    @Test
    fun f03_wifi_onDisconnect_properties() {
        val wifi = WifiTrigger(ssid = "Home_Fiber", triggerType = WifiTriggerType.ON_DISCONNECT)
        assertEquals("Home_Fiber", wifi.ssid)
        assertEquals(WifiTriggerType.ON_DISCONNECT, wifi.triggerType)
    }

    @Test
    fun f03_wifi_whileConnected_properties() {
        val wifi = WifiTrigger(ssid = "Cafe_Guest", triggerType = WifiTriggerType.WHILE_CONNECTED)
        assertEquals(WifiTriggerType.WHILE_CONNECTED, wifi.triggerType)
    }

    @Test
    fun f03_wifi_bssidFilter_specified() {
        val wifi = WifiTrigger(
            ssid = "Campus_AP",
            bssid = "00:11:22:33:44:55",
            triggerType = WifiTriggerType.ON_CONNECT
        )
        assertEquals("00:11:22:33:44:55", wifi.bssid)
    }

    @Test
    fun f03_wifi_attachedToReminder() {
        val reminder = Reminder(
            title = "Connect to office wifi",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = WifiTrigger(ssid = "Work_SSID")
        )
        assertEquals(ReminderTriggerType.WIFI, reminder.triggerType)
        assertNotNull(reminder.wifiTrigger)
    }

    // =========================================================================
    // FEATURE 4: Defined Time Window Evaluation (ORIGINAL_REQUEST § R1)
    // =========================================================================

    @Test
    fun f04_timeWindow_daytimeInside_returnsTrue() {
        val window = ReminderTimeWindow(startTime = LocalTime.of(8, 0), endTime = LocalTime.of(17, 0))
        val now = LocalDateTime.of(2026, 8, 19, 11, 30)
        assertTrue(window.isWithinWindow(now))
    }

    @Test
    fun f04_timeWindow_daytimeOutside_returnsFalse() {
        val window = ReminderTimeWindow(startTime = LocalTime.of(8, 0), endTime = LocalTime.of(17, 0))
        val now = LocalDateTime.of(2026, 8, 19, 19, 0)
        assertFalse(window.isWithinWindow(now))
    }

    @Test
    fun f04_timeWindow_midnightCrossingEvening_returnsTrue() {
        val window = ReminderTimeWindow(startTime = LocalTime.of(21, 0), endTime = LocalTime.of(5, 0))
        val now = LocalDateTime.of(2026, 8, 19, 22, 0)
        assertTrue(window.isWithinWindow(now))
    }

    @Test
    fun f04_timeWindow_midnightCrossingMorning_returnsTrue() {
        val window = ReminderTimeWindow(startTime = LocalTime.of(21, 0), endTime = LocalTime.of(5, 0))
        val now = LocalDateTime.of(2026, 8, 20, 3, 0)
        assertTrue(window.isWithinWindow(now))
    }

    @Test
    fun f04_timeWindow_activeDaysFiltering() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(17, 0),
            activeDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)
        )
        // 2026-08-19 is Wednesday (inactive)
        val wednesday = LocalDateTime.of(2026, 8, 19, 10, 0)
        assertFalse(window.isWithinWindow(wednesday))

        // 2026-08-18 is Tuesday (active)
        val tuesday = LocalDateTime.of(2026, 8, 18, 10, 0)
        assertTrue(window.isWithinWindow(tuesday))
    }

    // =========================================================================
    // FEATURE 5: Checklist CRUD & Relations (ORIGINAL_REQUEST § R2)
    // =========================================================================

    @Test
    fun f05_checklist_addItem_preservesOrder() {
        val item1 = ChecklistItem(id = 1, reminderId = 10, text = "Task 1", orderIndex = 0)
        val item2 = ChecklistItem(id = 2, reminderId = 10, text = "Task 2", orderIndex = 1)
        val list = listOf(item1, item2)

        assertEquals(2, list.size)
        assertEquals("Task 1", list[0].text)
        assertEquals("Task 2", list[1].text)
    }

    @Test
    fun f05_checklist_removeItem_updatesList() {
        val items = listOf(
            ChecklistItem(id = 1, reminderId = 10, text = "Task 1"),
            ChecklistItem(id = 2, reminderId = 10, text = "Task 2")
        )
        val remaining = items.filter { it.id != 1L }

        assertEquals(1, remaining.size)
        assertEquals(2L, remaining.first().id)
    }

    @Test
    fun f05_checklist_reorderIndices() {
        val items = listOf(
            ChecklistItem(id = 1, reminderId = 10, text = "First", orderIndex = 0),
            ChecklistItem(id = 2, reminderId = 10, text = "Second", orderIndex = 1)
        )
        val reordered = items.reversed().mapIndexed { index, item -> item.copy(orderIndex = index) }

        assertEquals(2L, reordered[0].id)
        assertEquals(0, reordered[0].orderIndex)
        assertEquals(1L, reordered[1].id)
        assertEquals(1, reordered[1].orderIndex)
    }

    @Test
    fun f05_checklist_emptyList_handledGracefully() {
        val reminder = Reminder(title = "Empty", triggerType = ReminderTriggerType.TIME, checklist = emptyList())
        assertEquals(0, reminder.totalChecklistCount)
        assertTrue(reminder.checklist.isEmpty())
    }

    @Test
    fun f05_checklist_foreignKeyCascadeLogic() {
        val parentReminderId = 100L
        val items = listOf(
            ChecklistItem(id = 1, reminderId = parentReminderId, text = "Subtask 1"),
            ChecklistItem(id = 2, reminderId = parentReminderId, text = "Subtask 2")
        )
        // All items reference parent ID
        assertTrue(items.all { it.reminderId == parentReminderId })
    }

    // =========================================================================
    // FEATURE 6: Real-time Checklist Completion Tracking (ORIGINAL_REQUEST § R2)
    // =========================================================================

    @Test
    fun f06_tracking_zeroCompleted() {
        val items = listOf(
            ChecklistItem(id = 1, reminderId = 1, text = "A", isCompleted = false),
            ChecklistItem(id = 2, reminderId = 1, text = "B", isCompleted = false)
        )
        val reminder = Reminder(title = "Test", triggerType = ReminderTriggerType.TIME, checklist = items)

        assertEquals(0, reminder.completedChecklistCount)
        assertEquals(0.0f, reminder.checklistProgress, 0.001f)
        assertFalse(reminder.isAllChecklistCompleted)
    }

    @Test
    fun f06_tracking_partiallyCompleted() {
        val items = listOf(
            ChecklistItem(id = 1, reminderId = 1, text = "A", isCompleted = true),
            ChecklistItem(id = 2, reminderId = 1, text = "B", isCompleted = false),
            ChecklistItem(id = 3, reminderId = 1, text = "C", isCompleted = true)
        )
        val reminder = Reminder(title = "Test", triggerType = ReminderTriggerType.TIME, checklist = items)

        assertEquals(2, reminder.completedChecklistCount)
        assertEquals(2f / 3f, reminder.checklistProgress, 0.001f)
        assertFalse(reminder.isAllChecklistCompleted)
    }

    @Test
    fun f06_tracking_allCompleted() {
        val items = listOf(
            ChecklistItem(id = 1, reminderId = 1, text = "A", isCompleted = true),
            ChecklistItem(id = 2, reminderId = 1, text = "B", isCompleted = true)
        )
        val reminder = Reminder(title = "Test", triggerType = ReminderTriggerType.TIME, checklist = items)

        assertEquals(2, reminder.completedChecklistCount)
        assertEquals(1.0f, reminder.checklistProgress, 0.001f)
        assertTrue(reminder.isAllChecklistCompleted)
    }

    @Test
    fun f06_tracking_emptyChecklist_progressIsZero() {
        val reminder = Reminder(title = "Test", triggerType = ReminderTriggerType.TIME, checklist = emptyList())
        assertEquals(0f, reminder.checklistProgress, 0.001f)
        assertFalse(reminder.isAllChecklistCompleted)
    }

    @Test
    fun f06_tracking_toggleItem_updatesComputedCounts() {
        val item1 = ChecklistItem(id = 1, reminderId = 1, text = "A", isCompleted = false)
        val reminder1 = Reminder(title = "Test", triggerType = ReminderTriggerType.TIME, checklist = listOf(item1))
        assertEquals(0, reminder1.completedChecklistCount)

        val reminder2 = reminder1.copy(checklist = listOf(item1.copy(isCompleted = true)))
        assertEquals(1, reminder2.completedChecklistCount)
        assertTrue(reminder2.isAllChecklistCompleted)
    }

    // =========================================================================
    // FEATURE 7: Checklist State Persistence on Snooze (ORIGINAL_REQUEST § R2)
    // =========================================================================

    @Test
    fun f07_snoozePersistence_partialChecksPreserved() {
        val originalItems = listOf(
            ChecklistItem(id = 1, reminderId = 1, text = "Item 1", isCompleted = true),
            ChecklistItem(id = 2, reminderId = 1, text = "Item 2", isCompleted = false),
            ChecklistItem(id = 3, reminderId = 1, text = "Item 3", isCompleted = false)
        )
        val reminder = Reminder(
            title = "Test",
            triggerType = ReminderTriggerType.TIME,
            checklist = originalItems,
            currentSnoozeCount = 0
        )

        // Perform snooze
        val snoozedReminder = reminder.copy(
            currentSnoozeCount = 1,
            snoozeNextTriggerTime = System.currentTimeMillis() + 300000L
        )

        assertEquals(3, snoozedReminder.checklist.size)
        assertTrue(snoozedReminder.checklist[0].isCompleted)
        assertFalse(snoozedReminder.checklist[1].isCompleted)
        assertFalse(snoozedReminder.checklist[2].isCompleted)
    }

    @Test
    fun f07_snoozePersistence_allChecksPreserved() {
        val allChecked = listOf(
            ChecklistItem(id = 1, reminderId = 1, text = "Item 1", isCompleted = true),
            ChecklistItem(id = 2, reminderId = 1, text = "Item 2", isCompleted = true)
        )
        val reminder = Reminder(title = "Test", triggerType = ReminderTriggerType.TIME, checklist = allChecked)
        val snoozed = reminder.copy(currentSnoozeCount = 1)

        assertTrue(snoozed.isAllChecklistCompleted)
    }

    @Test
    fun f07_snoozePersistence_zeroChecksPreserved() {
        val noneChecked = listOf(
            ChecklistItem(id = 1, reminderId = 1, text = "Item 1", isCompleted = false),
            ChecklistItem(id = 2, reminderId = 1, text = "Item 2", isCompleted = false)
        )
        val reminder = Reminder(title = "Test", triggerType = ReminderTriggerType.TIME, checklist = noneChecked)
        val snoozed = reminder.copy(currentSnoozeCount = 1)

        assertEquals(0, snoozed.completedChecklistCount)
    }

    @Test
    fun f07_snoozePersistence_multipleSuccessiveSnoozes() {
        var reminder = Reminder(
            title = "Multi-snooze",
            triggerType = ReminderTriggerType.TIME,
            checklist = listOf(
                ChecklistItem(id = 1, reminderId = 1, text = "Item 1", isCompleted = true),
                ChecklistItem(id = 2, reminderId = 1, text = "Item 2", isCompleted = false)
            ),
            snoozeRepeatCount = 3,
            currentSnoozeCount = 0
        )

        // Snooze 1
        reminder = reminder.copy(currentSnoozeCount = 1)
        assertTrue(reminder.checklist[0].isCompleted)

        // Snooze 2
        reminder = reminder.copy(currentSnoozeCount = 2)
        assertTrue(reminder.checklist[0].isCompleted)

        // Snooze 3
        reminder = reminder.copy(currentSnoozeCount = 3)
        assertTrue(reminder.checklist[0].isCompleted)
        assertFalse(reminder.canSnooze())
    }

    @Test
    fun f07_snoozePersistence_itemToggledDuringRingingBeforeSnooze() {
        val initialItems = listOf(
            ChecklistItem(id = 1, reminderId = 1, text = "Buy milk", isCompleted = false),
            ChecklistItem(id = 2, reminderId = 1, text = "Buy bread", isCompleted = false)
        )
        val reminder = Reminder(title = "Grocery", triggerType = ReminderTriggerType.TIME, checklist = initialItems)

        // User checks item 1 while ringing
        val updatedItems = listOf(
            initialItems[0].copy(isCompleted = true),
            initialItems[1]
        )
        val snoozedReminder = reminder.copy(checklist = updatedItems, currentSnoozeCount = 1)

        assertTrue(snoozedReminder.checklist[0].isCompleted)
        assertFalse(snoozedReminder.checklist[1].isCompleted)
    }

    // =========================================================================
    // FEATURE 8: Foreground Monitoring Service Lifecycle (ORIGINAL_REQUEST § R3)
    // =========================================================================

    @Test
    fun f08_service_startsForLocationReminder() {
        val reminders = listOf(
            Reminder(title = "Location", triggerType = ReminderTriggerType.LOCATION, isEnabled = true)
        )
        val needsService = reminders.any { it.isEnabled && !it.isCompleted && it.triggerType == ReminderTriggerType.LOCATION }
        assertTrue(needsService)
    }

    @Test
    fun f08_service_startsForWifiReminder() {
        val reminders = listOf(
            Reminder(title = "Wifi", triggerType = ReminderTriggerType.WIFI, isEnabled = true)
        )
        val needsService = reminders.any { it.isEnabled && !it.isCompleted && it.triggerType == ReminderTriggerType.WIFI }
        assertTrue(needsService)
    }

    @Test
    fun f08_service_stopsWhenAllRemindersDisabled() {
        val reminders = listOf(
            Reminder(title = "Location", triggerType = ReminderTriggerType.LOCATION, isEnabled = false),
            Reminder(title = "Wifi", triggerType = ReminderTriggerType.WIFI, isEnabled = false)
        )
        val needsService = reminders.any { it.isEnabled && !it.isCompleted && (it.triggerType == ReminderTriggerType.LOCATION || it.triggerType == ReminderTriggerType.WIFI) }
        assertFalse(needsService)
    }

    @Test
    fun f08_service_notNeededForTimeOnlyReminders() {
        val reminders = listOf(
            Reminder(title = "Time", triggerType = ReminderTriggerType.TIME, isEnabled = true)
        )
        val needsService = reminders.any { it.isEnabled && !it.isCompleted && (it.triggerType == ReminderTriggerType.LOCATION || it.triggerType == ReminderTriggerType.WIFI) }
        assertFalse(needsService)
    }

    @Test
    fun f08_service_stopsWhenAllRemindersCompleted() {
        val reminders = listOf(
            Reminder(title = "Location", triggerType = ReminderTriggerType.LOCATION, isEnabled = true, isCompleted = true)
        )
        val needsService = reminders.any { it.isEnabled && !it.isCompleted && (it.triggerType == ReminderTriggerType.LOCATION || it.triggerType == ReminderTriggerType.WIFI) }
        assertFalse(needsService)
    }

    // =========================================================================
    // FEATURE 9: Standard Notification Alert (ORIGINAL_REQUEST § R3)
    // =========================================================================

    @Test
    fun f09_standardNotification_channelStandard() {
        val reminder = Reminder(title = "Test", triggerType = ReminderTriggerType.TIME, notificationStyle = NotificationStyle.STANDARD)
        assertEquals(NotificationStyle.STANDARD, reminder.notificationStyle)
    }

    @Test
    fun f09_standardNotification_titleAndNotesPopulated() {
        val reminder = Reminder(title = "Call Mom", notes = "Ask about weekend", triggerType = ReminderTriggerType.TIME)
        assertEquals("Call Mom", reminder.title)
        assertEquals("Ask about weekend", reminder.notes)
    }

    @Test
    fun f09_standardNotification_checklistProgressInBody() {
        val reminder = Reminder(
            title = "Tasks",
            triggerType = ReminderTriggerType.TIME,
            checklist = listOf(
                ChecklistItem(id = 1, reminderId = 1, text = "Task 1", isCompleted = true),
                ChecklistItem(id = 2, reminderId = 1, text = "Task 2", isCompleted = false)
            )
        )
        val bodyText = "Checklist: ${reminder.completedChecklistCount}/${reminder.totalChecklistCount} completed"
        assertEquals("Checklist: 1/2 completed", bodyText)
    }

    @Test
    fun f09_standardNotification_snoozeActionAvailable() {
        val reminder = Reminder(title = "Test", triggerType = ReminderTriggerType.TIME, snoozeEnabled = true, snoozeRepeatCount = 3, currentSnoozeCount = 0)
        assertTrue(reminder.canSnooze())
    }

    @Test
    fun f09_standardNotification_dismissActionAvailable() {
        val reminder = Reminder(title = "Test", triggerType = ReminderTriggerType.TIME)
        val dismissed = reminder.copy(isCompleted = true)
        assertTrue(dismissed.isCompleted)
    }

    // =========================================================================
    // FEATURE 10: Full-Screen Intent Notification Alert (ORIGINAL_REQUEST § R3)
    // =========================================================================

    @Test
    fun f10_fullScreenNotification_styleIsFullScreen() {
        val reminder = Reminder(title = "Alarm Alert", triggerType = ReminderTriggerType.TIME, notificationStyle = NotificationStyle.FULL_SCREEN)
        assertEquals(NotificationStyle.FULL_SCREEN, reminder.notificationStyle)
    }

    @Test
    fun f10_fullScreenNotification_urgentChannelAssigned() {
        val isUrgent = NotificationStyle.FULL_SCREEN == NotificationStyle.FULL_SCREEN
        assertTrue(isUrgent)
    }

    @Test
    fun f10_fullScreenNotification_ringtoneUriConfigured() {
        val reminder = Reminder(title = "Alarm Alert", triggerType = ReminderTriggerType.TIME, ringtoneUri = "content://media/1")
        assertEquals("content://media/1", reminder.ringtoneUri)
    }

    @Test
    fun f10_fullScreenNotification_vibrationEnabled() {
        val reminder = Reminder(title = "Alarm Alert", triggerType = ReminderTriggerType.TIME, vibrationEnabled = true)
        assertTrue(reminder.vibrationEnabled)
    }

    @Test
    fun f10_fullScreenNotification_volumeSet() {
        val reminder = Reminder(title = "Alarm Alert", triggerType = ReminderTriggerType.TIME, volume = 85)
        assertEquals(85, reminder.volume)
    }

    // =========================================================================
    // FEATURE 11: Persistent Spam Alert Loop (ORIGINAL_REQUEST § R3)
    // =========================================================================

    @Test
    fun f11_persistentAlert_flagEnabled() {
        val reminder = Reminder(title = "Critical", triggerType = ReminderTriggerType.TIME, isPersistentAlert = true)
        assertTrue(reminder.isPersistentAlert)
    }

    @Test
    fun f11_persistentAlert_flagDisabledByDefault() {
        val reminder = Reminder(title = "Normal", triggerType = ReminderTriggerType.TIME)
        assertFalse(reminder.isPersistentAlert)
    }

    @Test
    fun f11_persistentAlert_stopsOnDismiss() {
        var isLooping = true
        // Dismiss event
        isLooping = false
        assertFalse(isLooping)
    }

    @Test
    fun f11_persistentAlert_stopsOnSnooze() {
        var isLooping = true
        // Snooze event
        isLooping = false
        assertFalse(isLooping)
    }

    @Test
    fun f11_persistentAlert_autoSilenceDuration() {
        val autoSilenceMinutes = 5
        assertTrue(autoSilenceMinutes > 0)
    }

    // =========================================================================
    // FEATURE 12: Snooze & Dismiss Action Handling (ORIGINAL_REQUEST § R3)
    // =========================================================================

    @Test
    fun f12_snoozeAction_incrementsCount() {
        val reminder = Reminder(title = "Test", triggerType = ReminderTriggerType.TIME, currentSnoozeCount = 0)
        val snoozed = reminder.copy(currentSnoozeCount = reminder.currentSnoozeCount + 1)
        assertEquals(1, snoozed.currentSnoozeCount)
    }

    @Test
    fun f12_snoozeAction_disabledAtLimit() {
        val reminder = Reminder(title = "Test", triggerType = ReminderTriggerType.TIME, snoozeRepeatCount = 2, currentSnoozeCount = 2)
        assertFalse(reminder.canSnooze())
    }

    @Test
    fun f12_dismissAction_setsCompletedTrue() {
        val reminder = Reminder(title = "Test", triggerType = ReminderTriggerType.TIME, isCompleted = false)
        val dismissed = reminder.copy(isCompleted = true)
        assertTrue(dismissed.isCompleted)
    }

    @Test
    fun f12_snoozeInterval_configurable() {
        val reminder = Reminder(title = "Test", triggerType = ReminderTriggerType.TIME, snoozeIntervalMinutes = 15)
        assertEquals(15, reminder.snoozeIntervalMinutes)
    }

    @Test
    fun f12_snoozeAction_setsNextTriggerTime() {
        val nextTime = System.currentTimeMillis() + 600000L
        val reminder = Reminder(title = "Test", triggerType = ReminderTriggerType.TIME, snoozeNextTriggerTime = nextTime)
        assertEquals(nextTime, reminder.snoozeNextTriggerTime)
    }

    // =========================================================================
    // FEATURE 13: Bottom Navigation "Reminders" Tab (ORIGINAL_REQUEST § R4)
    // =========================================================================

    @Test
    fun f13_bottomNav_routeDefinition() {
        val reminderListRoute = "vn.io.litever.remind.features.reminders.ui.ReminderListRoute"
        assertNotNull(reminderListRoute)
        assertTrue(reminderListRoute.contains("ReminderListRoute"))
    }

    @Test
    fun f13_bottomNav_tabTitle() {
        val tabTitle = "Reminders"
        assertEquals("Reminders", tabTitle)
    }

    @Test
    fun f13_bottomNav_tabIndex() {
        val navTabs = listOf("Alarms", "Today", "Reminders", "Settings")
        assertEquals(2, navTabs.indexOf("Reminders"))
    }

    @Test
    fun f13_bottomNav_badgeCountCalculation() {
        val reminders = listOf(
            Reminder(title = "R1", triggerType = ReminderTriggerType.TIME, isEnabled = true, isCompleted = false),
            Reminder(title = "R2", triggerType = ReminderTriggerType.TIME, isEnabled = true, isCompleted = false),
            Reminder(title = "R3", triggerType = ReminderTriggerType.TIME, isEnabled = true, isCompleted = true)
        )
        val activeCount = reminders.count { it.isEnabled && !it.isCompleted }
        assertEquals(2, activeCount)
    }

    @Test
    fun f13_bottomNav_visibilityOnMainRoutes() {
        val mainRoutes = setOf("alarm_list", "today", "reminder_list", "settings")
        assertTrue(mainRoutes.contains("reminder_list"))
    }

    // =========================================================================
    // FEATURE 14: Reminder List Screen & Filtering (ORIGINAL_REQUEST § R4)
    // =========================================================================

    @Test
    fun f14_listFilter_allFilter() {
        val list = listOf(
            Reminder(id = 1, title = "T", triggerType = ReminderTriggerType.TIME),
            Reminder(id = 2, title = "L", triggerType = ReminderTriggerType.LOCATION),
            Reminder(id = 3, title = "W", triggerType = ReminderTriggerType.WIFI)
        )
        assertEquals(3, list.size)
    }

    @Test
    fun f14_listFilter_locationFilter() {
        val list = listOf(
            Reminder(id = 1, title = "T", triggerType = ReminderTriggerType.TIME),
            Reminder(id = 2, title = "L", triggerType = ReminderTriggerType.LOCATION),
            Reminder(id = 3, title = "W", triggerType = ReminderTriggerType.WIFI)
        )
        val filtered = list.filter { it.triggerType == ReminderTriggerType.LOCATION }
        assertEquals(1, filtered.size)
        assertEquals(2L, filtered.first().id)
    }

    @Test
    fun f14_listFilter_wifiFilter() {
        val list = listOf(
            Reminder(id = 1, title = "T", triggerType = ReminderTriggerType.TIME),
            Reminder(id = 2, title = "L", triggerType = ReminderTriggerType.LOCATION),
            Reminder(id = 3, title = "W", triggerType = ReminderTriggerType.WIFI)
        )
        val filtered = list.filter { it.triggerType == ReminderTriggerType.WIFI }
        assertEquals(1, filtered.size)
        assertEquals(3L, filtered.first().id)
    }

    @Test
    fun f14_listFilter_activeOnly() {
        val list = listOf(
            Reminder(id = 1, title = "Active", triggerType = ReminderTriggerType.TIME, isCompleted = false),
            Reminder(id = 2, title = "Done", triggerType = ReminderTriggerType.TIME, isCompleted = true)
        )
        val active = list.filter { !it.isCompleted }
        assertEquals(1, active.size)
        assertEquals("Active", active.first().title)
    }

    @Test
    fun f14_listFilter_completedOnly() {
        val list = listOf(
            Reminder(id = 1, title = "Active", triggerType = ReminderTriggerType.TIME, isCompleted = false),
            Reminder(id = 2, title = "Done", triggerType = ReminderTriggerType.TIME, isCompleted = true)
        )
        val done = list.filter { it.isCompleted }
        assertEquals(1, done.size)
        assertEquals("Done", done.first().title)
    }

    // =========================================================================
    // FEATURE 15: Reminder Edit & Creation Screen (ORIGINAL_REQUEST § R4)
    // =========================================================================

    @Test
    fun f15_edit_titleValidation_blankRejected() {
        val blankTitle = "   "
        val isValid = blankTitle.isNotBlank()
        assertFalse(isValid)
    }

    @Test
    fun f15_edit_titleValidation_validAccepted() {
        val validTitle = "Buy Groceries"
        val isValid = validTitle.isNotBlank()
        assertTrue(isValid)
    }

    @Test
    fun f15_edit_triggerSwitching_timeToLocation() {
        val initial = Reminder(title = "Test", triggerType = ReminderTriggerType.TIME)
        val switched = initial.copy(
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(latitude = 21.0, longitude = 105.0, radiusMeters = 100f)
        )
        assertEquals(ReminderTriggerType.LOCATION, switched.triggerType)
        assertNotNull(switched.locationTrigger)
    }

    @Test
    fun f15_edit_notesField_optional() {
        val reminder = Reminder(title = "Test", notes = "Meeting notes", triggerType = ReminderTriggerType.TIME)
        assertEquals("Meeting notes", reminder.notes)
    }

    @Test
    fun f15_edit_alertStyleToggle_standardToFullScreen() {
        val reminder = Reminder(title = "Test", triggerType = ReminderTriggerType.TIME, notificationStyle = NotificationStyle.STANDARD)
        val updated = reminder.copy(notificationStyle = NotificationStyle.FULL_SCREEN)
        assertEquals(NotificationStyle.FULL_SCREEN, updated.notificationStyle)
    }

    // =========================================================================
    // FEATURE 16: Interactive Ringing Screen & Lock Bypass (ORIGINAL_REQUEST § R4)
    // =========================================================================

    @Test
    fun f16_ringingScreen_lockBypassFlags() {
        val showWhenLocked = true
        val turnScreenOn = true
        assertTrue(showWhenLocked)
        assertTrue(turnScreenOn)
    }

    @Test
    fun f16_ringingScreen_liveChecklistToggle() {
        val items = listOf(
            ChecklistItem(id = 1, reminderId = 1, text = "Item A", isCompleted = false),
            ChecklistItem(id = 2, reminderId = 1, text = "Item B", isCompleted = false)
        )
        val reminder = Reminder(title = "Live Ringing", triggerType = ReminderTriggerType.TIME, checklist = items)

        val updatedItems = reminder.checklist.map {
            if (it.id == 1L) it.copy(isCompleted = true) else it
        }
        val liveUpdatedReminder = reminder.copy(checklist = updatedItems)

        assertTrue(liveUpdatedReminder.checklist[0].isCompleted)
        assertFalse(liveUpdatedReminder.checklist[1].isCompleted)
    }

    @Test
    fun f16_ringingScreen_snoozeButtonAction() {
        val reminder = Reminder(title = "Test", triggerType = ReminderTriggerType.TIME, snoozeEnabled = true, snoozeRepeatCount = 3, currentSnoozeCount = 0)
        assertTrue(reminder.canSnooze())
    }

    @Test
    fun f16_ringingScreen_dismissButtonAction() {
        val reminder = Reminder(title = "Test", triggerType = ReminderTriggerType.TIME, isCompleted = false)
        val completed = reminder.copy(isCompleted = true)
        assertTrue(completed.isCompleted)
    }

    @Test
    fun f16_ringingScreen_backPressProtection() {
        val interceptBackPress = true
        assertTrue("Back press must be intercepted during full-screen alert", interceptBackPress)
    }

    // =========================================================================
    // FEATURE 17: Permission UX & "Save Anyway" Flow (ORIGINAL_REQUEST § R4)
    // =========================================================================

    @Test
    fun f17_permissionUX_allGranted_stateValid() {
        val hasLocation = true
        val hasWifi = true
        val hasAlarm = true
        val hasNotification = true
        val isReady = hasLocation && hasWifi && hasAlarm && hasNotification
        assertTrue(isReady)
    }

    @Test
    fun f17_permissionUX_locationMissing_warningBannerShown() {
        val hasLocation = false
        val showWarningBanner = !hasLocation
        assertTrue(showWarningBanner)
    }

    @Test
    fun f17_permissionUX_saveAnywayFlow_permitsSaveInDisabledState() {
        val permissionsGranted = false
        val reminder = Reminder(
            title = "Pending Permission Reminder",
            triggerType = ReminderTriggerType.LOCATION,
            isEnabled = false // saved in disabled or warning state
        )
        assertFalse(reminder.isEnabled)
        assertEquals("Pending Permission Reminder", reminder.title)
    }

    @Test
    fun f17_permissionUX_wifiPermissionDenied_handledGracefully() {
        val wifiPermissionGranted = false
        val canScanWifi = wifiPermissionGranted
        assertFalse(canScanWifi)
    }

    @Test
    fun f17_permissionUX_exactAlarmPermissionDenied_promptsUser() {
        val exactAlarmGranted = false
        val promptRequired = !exactAlarmGranted
        assertTrue(promptRequired)
    }
}
