package vn.io.litever.remind.core.model.reminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
import java.time.LocalTime

/**
 * Comprehensive Unit Tests for Reminder, ChecklistItem, and related Domain Models (Tiers 1 & 2).
 */
class ReminderTest {

    @Test
    fun reminder_defaultValues_areCorrect() {
        val reminder = Reminder(
            title = "Test Reminder",
            triggerType = ReminderTriggerType.TIME
        )

        assertEquals(0L, reminder.id)
        assertEquals("Test Reminder", reminder.title)
        assertEquals("", reminder.notes)
        assertTrue(reminder.isEnabled)
        assertEquals(ReminderTriggerType.TIME, reminder.triggerType)
        assertNull(reminder.timeTrigger)
        assertNull(reminder.locationTrigger)
        assertNull(reminder.wifiTrigger)
        assertTrue(reminder.checklist.isEmpty())
        assertEquals(NotificationStyle.STANDARD, reminder.notificationStyle)
        assertFalse(reminder.isPersistentAlert)
        assertNull(reminder.ringtoneUri)
        assertTrue(reminder.vibrationEnabled)
        assertEquals(100, reminder.volume)
        assertTrue(reminder.snoozeEnabled)
        assertEquals(5, reminder.snoozeIntervalMinutes)
        assertEquals(3, reminder.snoozeRepeatCount)
        assertEquals(0, reminder.currentSnoozeCount)
        assertNull(reminder.snoozeNextTriggerTime)
        assertNull(reminder.lastTriggeredTime)
        assertFalse(reminder.isCompleted)
    }

    @Test
    fun checklistItem_properties_areCorrect() {
        val item = ChecklistItem(
            id = 10L,
            reminderId = 1L,
            text = "Buy milk",
            isCompleted = false,
            orderIndex = 0
        )

        assertEquals(10L, item.id)
        assertEquals(1L, item.reminderId)
        assertEquals("Buy milk", item.text)
        assertFalse(item.isCompleted)
        assertEquals(0, item.orderIndex)
    }

    @Test
    fun checklistItem_toggleCompletion_updatesState() {
        val item = ChecklistItem(
            id = 1L,
            reminderId = 1L,
            text = "Clean room",
            isCompleted = false
        )
        val completedItem = item.copy(isCompleted = true)

        assertTrue(completedItem.isCompleted)
        assertEquals("Clean room", completedItem.text)
    }

    @Test
    fun checklist_progressCalculations_emptyChecklist() {
        val reminder = Reminder(
            title = "No checklist",
            triggerType = ReminderTriggerType.TIME,
            checklist = emptyList()
        )

        assertEquals(0, reminder.totalChecklistCount)
        assertEquals(0, reminder.completedChecklistCount)
        assertEquals(0f, reminder.checklistProgress, 0.001f)
        assertFalse(reminder.isAllChecklistCompleted)
    }

    @Test
    fun checklist_progressCalculations_partiallyCompleted() {
        val items = listOf(
            ChecklistItem(id = 1, reminderId = 1, text = "Task 1", isCompleted = true, orderIndex = 0),
            ChecklistItem(id = 2, reminderId = 1, text = "Task 2", isCompleted = false, orderIndex = 1),
            ChecklistItem(id = 3, reminderId = 1, text = "Task 3", isCompleted = true, orderIndex = 2),
            ChecklistItem(id = 4, reminderId = 1, text = "Task 4", isCompleted = false, orderIndex = 3)
        )
        val reminder = Reminder(
            title = "Partial checklist",
            triggerType = ReminderTriggerType.TIME,
            checklist = items
        )

        assertEquals(4, reminder.totalChecklistCount)
        assertEquals(2, reminder.completedChecklistCount)
        assertEquals(0.5f, reminder.checklistProgress, 0.001f)
        assertFalse(reminder.isAllChecklistCompleted)
    }

    @Test
    fun checklist_progressCalculations_allCompleted() {
        val items = listOf(
            ChecklistItem(id = 1, reminderId = 1, text = "Task 1", isCompleted = true, orderIndex = 0),
            ChecklistItem(id = 2, reminderId = 1, text = "Task 2", isCompleted = true, orderIndex = 1)
        )
        val reminder = Reminder(
            title = "All done",
            triggerType = ReminderTriggerType.TIME,
            checklist = items
        )

        assertEquals(2, reminder.totalChecklistCount)
        assertEquals(2, reminder.completedChecklistCount)
        assertEquals(1.0f, reminder.checklistProgress, 0.001f)
        assertTrue(reminder.isAllChecklistCompleted)
    }

    @Test
    fun canSnooze_whenEnabledAndCountBelowRepeatCount_returnsTrue() {
        val reminder = Reminder(
            title = "Snooze test",
            triggerType = ReminderTriggerType.TIME,
            snoozeEnabled = true,
            snoozeRepeatCount = 3,
            currentSnoozeCount = 0
        )
        assertTrue(reminder.canSnooze())

        val snoozedTwice = reminder.copy(currentSnoozeCount = 2)
        assertTrue(snoozedTwice.canSnooze())
    }

    @Test
    fun canSnooze_whenCountReachesRepeatCount_returnsFalse() {
        val reminder = Reminder(
            title = "Snooze limit",
            triggerType = ReminderTriggerType.TIME,
            snoozeEnabled = true,
            snoozeRepeatCount = 3,
            currentSnoozeCount = 3
        )
        assertFalse(reminder.canSnooze())
    }

    @Test
    fun canSnooze_whenSnoozeDisabled_returnsFalse() {
        val reminder = Reminder(
            title = "No snooze",
            triggerType = ReminderTriggerType.TIME,
            snoozeEnabled = false,
            snoozeRepeatCount = 3,
            currentSnoozeCount = 0
        )
        assertFalse(reminder.canSnooze())
    }

    @Test
    fun timeTrigger_oneTime_properties() {
        val time = LocalTime.of(14, 30)
        val date = LocalDate.of(2026, 8, 20)
        val trigger = TimeTrigger(
            time = time,
            date = date,
            repeatDays = emptyList()
        )

        assertEquals(time, trigger.time)
        assertEquals(date, trigger.date)
        assertTrue(trigger.repeatDays.isEmpty())
    }

    @Test
    fun timeTrigger_recurring_properties() {
        val time = LocalTime.of(8, 0)
        val repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        val trigger = TimeTrigger(
            time = time,
            date = null,
            repeatDays = repeatDays
        )

        assertEquals(time, trigger.time)
        assertNull(trigger.date)
        assertEquals(3, trigger.repeatDays.size)
        assertTrue(trigger.repeatDays.contains(DayOfWeek.MONDAY))
        assertTrue(trigger.repeatDays.contains(DayOfWeek.WEDNESDAY))
        assertTrue(trigger.repeatDays.contains(DayOfWeek.FRIDAY))
    }

    @Test
    fun locationTrigger_properties() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(18, 0),
            activeDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)
        )
        val trigger = LocationTrigger(
            latitude = 21.0285,
            longitude = 105.8542,
            radiusMeters = 150f,
            locationName = "Office Tower",
            transitionType = GeofenceTransitionType.ENTER,
            timeWindow = window
        )

        assertEquals(21.0285, trigger.latitude, 0.0001)
        assertEquals(105.8542, trigger.longitude, 0.0001)
        assertEquals(150f, trigger.radiusMeters, 0.01f)
        assertEquals("Office Tower", trigger.locationName)
        assertEquals(GeofenceTransitionType.ENTER, trigger.transitionType)
        assertEquals(window, trigger.timeWindow)
    }

    @Test
    fun wifiTrigger_properties() {
        val window = ReminderTimeWindow(
            startTime = LocalTime.of(8, 0),
            endTime = LocalTime.of(20, 0)
        )
        val trigger = WifiTrigger(
            ssid = "Company_Guest_WiFi",
            bssid = "00:14:22:01:23:45",
            triggerType = WifiTriggerType.ON_CONNECT,
            timeWindow = window
        )

        assertEquals("Company_Guest_WiFi", trigger.ssid)
        assertEquals("00:14:22:01:23:45", trigger.bssid)
        assertEquals(WifiTriggerType.ON_CONNECT, trigger.triggerType)
        assertEquals(window, trigger.timeWindow)
    }

    @Test
    fun reminder_fullScreenNotificationAndPersistentAlert_properties() {
        val reminder = Reminder(
            title = "Urgent Medication",
            triggerType = ReminderTriggerType.TIME,
            notificationStyle = NotificationStyle.FULL_SCREEN,
            isPersistentAlert = true,
            volume = 80,
            vibrationEnabled = true,
            ringtoneUri = "content://media/internal/audio/media/1"
        )

        assertEquals(NotificationStyle.FULL_SCREEN, reminder.notificationStyle)
        assertTrue(reminder.isPersistentAlert)
        assertEquals(80, reminder.volume)
        assertTrue(reminder.vibrationEnabled)
        assertEquals("content://media/internal/audio/media/1", reminder.ringtoneUri)
    }

    @Test
    fun reminder_copyImmutability_preservesUnchangedFields() {
        val original = Reminder(
            id = 5L,
            title = "Original Title",
            notes = "Original Notes",
            triggerType = ReminderTriggerType.LOCATION,
            currentSnoozeCount = 1
        )

        val updated = original.copy(title = "Updated Title", currentSnoozeCount = 2)

        assertEquals(5L, updated.id)
        assertEquals("Updated Title", updated.title)
        assertEquals("Original Notes", updated.notes)
        assertEquals(ReminderTriggerType.LOCATION, updated.triggerType)
        assertEquals(2, updated.currentSnoozeCount)
    }

    @Test
    fun boundary_largeChecklist_calculatesProgressAccurately() {
        val largeList = (1..100).map { index ->
            ChecklistItem(
                id = index.toLong(),
                reminderId = 1L,
                text = "Item #$index",
                isCompleted = index <= 75,
                orderIndex = index - 1
            )
        }
        val reminder = Reminder(
            title = "Massive checklist",
            triggerType = ReminderTriggerType.TIME,
            checklist = largeList
        )

        assertEquals(100, reminder.totalChecklistCount)
        assertEquals(75, reminder.completedChecklistCount)
        assertEquals(0.75f, reminder.checklistProgress, 0.001f)
        assertFalse(reminder.isAllChecklistCompleted)
    }

    @Test
    fun boundary_extremeCoordinates_locationTrigger() {
        val northPole = LocationTrigger(latitude = 90.0, longitude = 0.0, radiusMeters = 50f)
        val southPole = LocationTrigger(latitude = -90.0, longitude = 0.0, radiusMeters = 50f)
        val datelineWest = LocationTrigger(latitude = 0.0, longitude = -180.0, radiusMeters = 50f)
        val datelineEast = LocationTrigger(latitude = 0.0, longitude = 180.0, radiusMeters = 50f)

        assertEquals(90.0, northPole.latitude, 0.0001)
        assertEquals(-90.0, southPole.latitude, 0.0001)
        assertEquals(-180.0, datelineWest.longitude, 0.0001)
        assertEquals(180.0, datelineEast.longitude, 0.0001)
    }

    @Test
    fun boundary_specialCharactersInTitleAndChecklist() {
        val specialTitle = "🎉 Special Rem!nder @ #1 & (Test) / [Unicode: 🚀 ✓ 🔔]"
        val specialItem = ChecklistItem(
            id = 1L,
            reminderId = 1L,
            text = "Buy \"Almond\" Milk & <Organic> Apples 😊 \n Multi-line text"
        )
        val reminder = Reminder(
            title = specialTitle,
            triggerType = ReminderTriggerType.TIME,
            checklist = listOf(specialItem)
        )

        assertEquals(specialTitle, reminder.title)
        assertEquals(specialItem.text, reminder.checklist.first().text)
    }
}
