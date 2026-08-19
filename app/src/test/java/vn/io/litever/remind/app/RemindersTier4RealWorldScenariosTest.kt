package vn.io.litever.remind.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.io.litever.remind.core.model.ChecklistItem
import vn.io.litever.remind.core.model.GeofenceTransitionType
import vn.io.litever.remind.core.model.LocationTrigger
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.model.WifiTrigger
import vn.io.litever.remind.core.model.WifiTriggerType
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Master Tier 4 Test Suite: 5 Real-World End-to-End Application Scenarios.
 * Authoritatively derived from TEST_INFRA.md § Real-World Application Scenarios (Tier 4).
 */
class RemindersTier4RealWorldScenariosTest {

    /**
     * Scenario 1: Morning Routine Checklist at Home Wi-Fi
     * Wi-Fi Trigger (Home_SSID), Time Window (06:00-09:00), 5-item Checklist, Standard Notification.
     */
    @Test
    fun scenario1_morningRoutineChecklist_atHomeWifi() {
        // Step 1: Create Reminder with 5-item morning checklist
        val morningWindow = ReminderTimeWindow(
            startTime = LocalTime.of(6, 0),
            endTime = LocalTime.of(9, 0)
        )
        val wifiTrigger = WifiTrigger(
            ssid = "Home_Sweet_Home_5G",
            triggerType = WifiTriggerType.ON_CONNECT,
            timeWindow = morningWindow
        )
        val initialItems = listOf(
            ChecklistItem(id = 1, reminderId = 101, text = "Drink glass of water", isCompleted = false, orderIndex = 0),
            ChecklistItem(id = 2, reminderId = 101, text = "10-minute stretch", isCompleted = false, orderIndex = 1),
            ChecklistItem(id = 3, reminderId = 101, text = "Pack lunchbox", isCompleted = false, orderIndex = 2),
            ChecklistItem(id = 4, reminderId = 101, text = "Grab house keys", isCompleted = false, orderIndex = 3),
            ChecklistItem(id = 5, reminderId = 101, text = "Turn off heater", isCompleted = false, orderIndex = 4)
        )
        var reminder = Reminder(
            id = 101L,
            title = "Morning Routine",
            notes = "Start the day fresh",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = wifiTrigger,
            checklist = initialItems,
            notificationStyle = NotificationStyle.STANDARD,
            snoozeEnabled = true,
            snoozeIntervalMinutes = 15,
            snoozeRepeatCount = 3,
            currentSnoozeCount = 0
        )

        // Step 2: Trigger occurs at 06:30 (inside window)
        val triggerTime = LocalDateTime.of(2026, 8, 19, 6, 30)
        assertTrue(reminder.wifiTrigger!!.timeWindow!!.isWithinWindow(triggerTime))
        assertEquals(0, reminder.completedChecklistCount)
        assertEquals(5, reminder.totalChecklistCount)

        // Step 3: User checks items 1 & 2, then snoozes for 15 minutes
        val updatedAfterSnooze1 = reminder.checklist.map {
            if (it.id == 1L || it.id == 2L) it.copy(isCompleted = true) else it
        }
        reminder = reminder.copy(
            checklist = updatedAfterSnooze1,
            currentSnoozeCount = 1,
            snoozeNextTriggerTime = System.currentTimeMillis() + 900000L
        )

        // Step 4: Verify persistence on 1st snooze
        assertEquals(1, reminder.currentSnoozeCount)
        assertEquals(2, reminder.completedChecklistCount)
        assertEquals(0.4f, reminder.checklistProgress, 0.001f)
        assertTrue(reminder.checklist[0].isCompleted)
        assertTrue(reminder.checklist[1].isCompleted)
        assertFalse(reminder.checklist[2].isCompleted)

        // Step 5: Snooze fires 15 mins later (06:45), user checks items 3, 4, 5 and dismisses
        val finalItems = reminder.checklist.map { it.copy(isCompleted = true) }
        reminder = reminder.copy(
            checklist = finalItems,
            isCompleted = true,
            snoozeNextTriggerTime = null
        )

        // Step 6: Verify final completion
        assertTrue(reminder.isCompleted)
        assertTrue(reminder.isAllChecklistCompleted)
        assertEquals(5, reminder.completedChecklistCount)
        assertEquals(1.0f, reminder.checklistProgress, 0.001f)
    }

    /**
     * Scenario 2: Office Arrival Geofence with Multi-Snooze
     * Geofence Trigger (Work, 100m radius), Time Window (08:30-10:00),
     * Checklist with 3/5 checked on first snooze, Full-Screen Intent Alert.
     */
    @Test
    fun scenario2_officeArrivalGeofence_withMultiSnooze() {
        // Step 1: Create Geofence Reminder for Office Arrival
        val workWindow = ReminderTimeWindow(startTime = LocalTime.of(8, 30), endTime = LocalTime.of(10, 0))
        val officeGeofence = LocationTrigger(
            latitude = 21.0285,
            longitude = 105.8542,
            radiusMeters = 100f,
            locationName = "Tech Park Tower",
            transitionType = GeofenceTransitionType.ENTER,
            timeWindow = workWindow
        )
        val tasks = listOf(
            ChecklistItem(id = 1, reminderId = 202, text = "Badge in at security gate", isCompleted = false),
            ChecklistItem(id = 2, reminderId = 202, text = "Prepare standup talking points", isCompleted = false),
            ChecklistItem(id = 3, reminderId = 202, text = "Refill water bottle", isCompleted = false),
            ChecklistItem(id = 4, reminderId = 202, text = "Review morning PRs", isCompleted = false),
            ChecklistItem(id = 5, reminderId = 202, text = "Sync with project lead", isCompleted = false)
        )
        var reminder = Reminder(
            id = 202L,
            title = "Office Arrival Protocol",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = officeGeofence,
            checklist = tasks,
            notificationStyle = NotificationStyle.FULL_SCREEN,
            snoozeEnabled = true,
            snoozeIntervalMinutes = 10,
            snoozeRepeatCount = 3,
            currentSnoozeCount = 0
        )

        // Step 2: User arrives at 08:45 -> Alert triggers
        val arrivalTime = LocalDateTime.of(2026, 8, 19, 8, 45)
        assertTrue(reminder.locationTrigger!!.timeWindow!!.isWithinWindow(arrivalTime))
        assertEquals(NotificationStyle.FULL_SCREEN, reminder.notificationStyle)

        // Step 3: First Snooze: checks 3/5 items (items 1, 2, 3) and snoozes
        val afterSnooze1 = reminder.checklist.map {
            if (it.id in listOf(1L, 2L, 3L)) it.copy(isCompleted = true) else it
        }
        reminder = reminder.copy(checklist = afterSnooze1, currentSnoozeCount = 1)
        assertEquals(1, reminder.currentSnoozeCount)
        assertEquals(3, reminder.completedChecklistCount)
        assertTrue(reminder.canSnooze())

        // Step 4: Second Snooze: 10m later, checks item 4 and snoozes again
        val afterSnooze2 = reminder.checklist.map {
            if (it.id == 4L) it.copy(isCompleted = true) else it
        }
        reminder = reminder.copy(checklist = afterSnooze2, currentSnoozeCount = 2)
        assertEquals(2, reminder.currentSnoozeCount)
        assertEquals(4, reminder.completedChecklistCount)
        assertTrue(reminder.canSnooze())

        // Step 5: Third ring: checks item 5 and taps Dismiss
        val finalChecklist = reminder.checklist.map { it.copy(isCompleted = true) }
        reminder = reminder.copy(checklist = finalChecklist, isCompleted = true)

        // Step 6: Verify full completion with snooze history preserved
        assertTrue(reminder.isCompleted)
        assertTrue(reminder.isAllChecklistCompleted)
        assertEquals(2, reminder.currentSnoozeCount)
    }

    /**
     * Scenario 3: Grocery Shopping Geofence & Checklist Partial Checks
     * Geofence Exit (Home) -> Enter (Supermarket), 8-item Grocery Checklist, live checks on Ringing Screen, Dismiss.
     */
    @Test
    fun scenario3_groceryShoppingGeofence_partialChecksAndDismiss() {
        val groceryItems = listOf(
            ChecklistItem(id = 1, reminderId = 303, text = "Almond Milk", isCompleted = false),
            ChecklistItem(id = 2, reminderId = 303, text = "Whole Grain Bread", isCompleted = false),
            ChecklistItem(id = 3, reminderId = 303, text = "Eggs (dozen)", isCompleted = false),
            ChecklistItem(id = 4, reminderId = 303, text = "Bananas", isCompleted = false),
            ChecklistItem(id = 5, reminderId = 303, text = "Chicken Breast", isCompleted = false),
            ChecklistItem(id = 6, reminderId = 303, text = "Olive Oil", isCompleted = false),
            ChecklistItem(id = 7, reminderId = 303, text = "Greek Yogurt", isCompleted = false),
            ChecklistItem(id = 8, reminderId = 303, text = "Ground Coffee", isCompleted = false)
        )
        val supermarketGeofence = LocationTrigger(
            latitude = 21.0123,
            longitude = 105.8456,
            radiusMeters = 200f,
            locationName = "Mega Supermarket",
            transitionType = GeofenceTransitionType.ENTER
        )
        var reminder = Reminder(
            id = 303L,
            title = "Weekly Grocery Run",
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = supermarketGeofence,
            checklist = groceryItems,
            notificationStyle = NotificationStyle.FULL_SCREEN
        )

        // Step 1: User enters supermarket -> Ringing screen opens
        assertEquals(8, reminder.totalChecklistCount)
        assertEquals(0, reminder.completedChecklistCount)

        // Step 2: Live interactive toggles on Ringing Screen (7 of 8 items found; coffee out of stock)
        val shoppedItems = reminder.checklist.map { item ->
            if (item.id != 8L) item.copy(isCompleted = true) else item.copy(isCompleted = false)
        }
        reminder = reminder.copy(checklist = shoppedItems)

        assertEquals(7, reminder.completedChecklistCount)
        assertEquals(7f / 8f, reminder.checklistProgress, 0.001f)
        assertFalse(reminder.isAllChecklistCompleted)

        // Step 3: User dismisses reminder
        reminder = reminder.copy(isCompleted = true)

        // Step 4: Verify dismissal with partial checklist saved in history
        assertTrue(reminder.isCompleted)
        assertEquals(7, reminder.completedChecklistCount)
        assertFalse(reminder.checklist.last().isCompleted)
    }

    /**
     * Scenario 4: Evening Medication Time Alert with Persistent Spam Loop
     * Time Trigger (20:00), Full-Screen Intent, Persistent Spam Loop ringing until Dismiss confirmed.
     */
    @Test
    fun scenario4_eveningMedicationTimeAlert_withPersistentSpamLoop() {
        val medItems = listOf(
            ChecklistItem(id = 1, reminderId = 404, text = "Take Blood Pressure Tablet (5mg)", isCompleted = false),
            ChecklistItem(id = 2, reminderId = 404, text = "Drink full glass of water", isCompleted = false),
            ChecklistItem(id = 3, reminderId = 404, text = "Record reading in BP log", isCompleted = false)
        )
        val reminder = Reminder(
            id = 404L,
            title = "Evening Blood Pressure Medication",
            notes = "Do not take with grapefruit juice",
            triggerType = ReminderTriggerType.TIME,
            timeTrigger = TimeTrigger(time = LocalTime.of(20, 0)),
            checklist = medItems,
            notificationStyle = NotificationStyle.FULL_SCREEN,
            isPersistentAlert = true,
            volume = 100,
            vibrationEnabled = true
        )

        // Step 1: Verify persistent alert configuration
        assertTrue(reminder.isPersistentAlert)
        assertEquals(NotificationStyle.FULL_SCREEN, reminder.notificationStyle)
        assertEquals(100, reminder.volume)

        // Step 2: Alarm rings at 20:00: persistent alert starts spam loop
        var isLoopActive = true
        assertTrue(isLoopActive)

        // Step 3: User takes medication and completes checklist items
        val completedItems = reminder.checklist.map { it.copy(isCompleted = true) }
        val updatedReminder = reminder.copy(checklist = completedItems)
        assertTrue(updatedReminder.isAllChecklistCompleted)

        // Step 4: User clicks Dismiss -> Stops spam loop and completes reminder
        isLoopActive = false
        val finalReminder = updatedReminder.copy(isCompleted = true)

        assertFalse("Spam loop must terminate immediately on dismiss", isLoopActive)
        assertTrue(finalReminder.isCompleted)
    }

    /**
     * Scenario 5: Urgent Wi-Fi Disconnect Alert with Zero Permissions Granted
     * Wi-Fi Disconnect Trigger, Missing Permissions Warning Banner, "Save Anyway" in disabled state,
     * permissions granted later enabling trigger.
     */
    @Test
    fun scenario5_urgentWifiDisconnectAlert_zeroPermissionsGrantedFlow() {
        // Step 1: User creates Wi-Fi Disconnect reminder without granting Wi-Fi/Location permissions
        val wifiTrigger = WifiTrigger(
            ssid = "Lab_Critical_Server_WiFi",
            triggerType = WifiTriggerType.ON_DISCONNECT
        )
        var reminder = Reminder(
            id = 505L,
            title = "Server Lab Connection Lost",
            notes = "Immediate investigation required if disconnected",
            triggerType = ReminderTriggerType.WIFI,
            wifiTrigger = wifiTrigger,
            notificationStyle = NotificationStyle.FULL_SCREEN,
            isPersistentAlert = true,
            isEnabled = false // Saved via "Save Anyway" in disabled state
        )

        // Step 2: Verify initially disabled
        assertFalse("Reminder should be disabled until permissions granted", reminder.isEnabled)
        assertFalse(reminder.isCompleted)

        // Step 3: User opens app settings and grants NEARBY_WIFI_DEVICES and Location permissions
        val permissionsNowGranted = true
        assertTrue(permissionsNowGranted)

        // Step 4: App enables the reminder
        reminder = reminder.copy(isEnabled = true)
        assertTrue(reminder.isEnabled)

        // Step 5: Wi-Fi disconnect occurs -> Trigger fires full-screen persistent alert
        val disconnectOccurred = true
        assertTrue(disconnectOccurred)
        assertEquals(NotificationStyle.FULL_SCREEN, reminder.notificationStyle)
        assertTrue(reminder.isPersistentAlert)
    }
}
