package vn.io.litever.remind.core.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.io.litever.remind.core.database.entity.ChecklistItemEntity
import vn.io.litever.remind.core.database.entity.ReminderEntity
import vn.io.litever.remind.core.database.model.PopulatedReminder
import java.time.DayOfWeek
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

class ReminderMapperTest {

    @Test
    fun `toModel correctly maps TimeTrigger and its recurrence days`() {
        val entity = ReminderEntity(
            id = 1L,
            title = "Morning Medicine",
            notes = "Take with water",
            isEnabled = true,
            triggerType = "TIME",
            timeTriggerHour = 8,
            timeTriggerMinute = 30,
            timeTriggerDate = "2026-08-25",
            timeTriggerRepeatDays = "MONDAY,WEDNESDAY,FRIDAY",
            notificationStyle = "FULL_SCREEN",
            isPersistentAlert = true,
            snoozeIntervalMinutes = 10,
            snoozeRepeatCount = 5
        )

        val model = entity.toModel()

        assertEquals(1L, model.id)
        assertEquals("Morning Medicine", model.title)
        assertEquals(ReminderTriggerType.TIME, model.triggerType)
        assertNotNull(model.timeTrigger)
        assertEquals(LocalTime.of(8, 30), model.timeTrigger?.time)
        assertEquals(LocalDate.of(2026, 8, 25), model.timeTrigger?.date)
        assertEquals(listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), model.timeTrigger?.repeatDays)
        assertEquals(NotificationStyle.FULL_SCREEN, model.notificationStyle)
        assertTrue(model.isPersistentAlert)
        assertEquals(10, model.snoozeIntervalMinutes)
        assertEquals(5, model.snoozeRepeatCount)
    }

    @Test
    fun `toModel correctly maps LocationTrigger and its time window`() {
        val entity = ReminderEntity(
            id = 2L,
            title = "Office Arrival",
            isEnabled = true,
            triggerType = "LOCATION",
            locationLatitude = 37.7749,
            locationLongitude = -122.4194,
            locationRadiusMeters = 200f,
            locationName = "Headquarters",
            locationTransitionType = "ENTER",
            locationWindowStartTime = "08:30",
            locationWindowEndTime = "10:00",
            locationWindowDays = "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY"
        )

        val model = entity.toModel()

        assertEquals(ReminderTriggerType.LOCATION, model.triggerType)
        assertNotNull(model.locationTrigger)
        assertEquals(37.7749, model.locationTrigger?.latitude ?: 0.0, 0.0001)
        assertEquals(-122.4194, model.locationTrigger?.longitude ?: 0.0, 0.0001)
        assertEquals(200f, model.locationTrigger?.radiusMeters ?: 0f, 0.01f)
        assertEquals("Headquarters", model.locationTrigger?.locationName)
        assertEquals(GeofenceTransitionType.ENTER, model.locationTrigger?.transitionType)
        assertNotNull(model.locationTrigger?.timeWindow)
        assertEquals(LocalTime.of(8, 30), model.locationTrigger?.timeWindow?.startTime)
        assertEquals(LocalTime.of(10, 0), model.locationTrigger?.timeWindow?.endTime)
        assertEquals(5, model.locationTrigger?.timeWindow?.activeDays?.size)
    }

    @Test
    fun `toModel correctly maps WifiTrigger and its time window`() {
        val entity = ReminderEntity(
            id = 3L,
            title = "Home Wi-Fi Sync",
            isEnabled = true,
            triggerType = "WIFI",
            wifiSsid = "Home_5G",
            wifiBssid = "00:11:22:33:44:55",
            wifiTriggerType = "WHILE_CONNECTED",
            wifiWindowStartTime = "20:00",
            wifiWindowEndTime = "23:00",
            wifiWindowDays = "SATURDAY,SUNDAY"
        )

        val model = entity.toModel()

        assertEquals(ReminderTriggerType.WIFI, model.triggerType)
        assertNotNull(model.wifiTrigger)
        assertEquals("Home_5G", model.wifiTrigger?.ssid)
        assertEquals("00:11:22:33:44:55", model.wifiTrigger?.bssid)
        assertEquals(WifiTriggerType.WHILE_CONNECTED, model.wifiTrigger?.triggerType)
        assertNotNull(model.wifiTrigger?.timeWindow)
        assertEquals(LocalTime.of(20, 0), model.wifiTrigger?.timeWindow?.startTime)
        assertEquals(LocalTime.of(23, 0), model.wifiTrigger?.timeWindow?.endTime)
    }

    @Test
    fun `toModel handles corrupted or unknown enum strings gracefully`() {
        val entity = ReminderEntity(
            id = 4L,
            title = "Corrupted Enum Test",
            triggerType = "UNKNOWN_TRIGGER",
            notificationStyle = "INVALID_STYLE",
            locationTransitionType = "INVALID_TRANSITION"
        )

        val model = entity.toModel()

        assertEquals(ReminderTriggerType.TIME, model.triggerType)
        assertEquals(NotificationStyle.STANDARD, model.notificationStyle)
    }

    @Test
    fun `PopulatedReminder maps and sorts checklist items by orderIndex`() {
        val reminderEntity = ReminderEntity(id = 5L, title = "Grocery Shopping", triggerType = "LOCATION")
        val checklistEntities = listOf(
            ChecklistItemEntity(id = 103, reminderId = 5L, text = "Bread", orderIndex = 2),
            ChecklistItemEntity(id = 101, reminderId = 5L, text = "Milk", orderIndex = 0),
            ChecklistItemEntity(id = 102, reminderId = 5L, text = "Eggs", orderIndex = 1)
        )
        val populated = PopulatedReminder(reminder = reminderEntity, checklist = checklistEntities)

        val model = populated.toModel()

        assertEquals(3, model.checklist.size)
        assertEquals("Milk", model.checklist[0].text)
        assertEquals("Eggs", model.checklist[1].text)
        assertEquals("Bread", model.checklist[2].text)
    }

    @Test
    fun `toEntity faithfully serializes domain model properties`() {
        val domainReminder = Reminder(
            id = 10L,
            title = "Gym Session",
            notes = "Leg day",
            isEnabled = true,
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(
                latitude = 10.762622,
                longitude = 106.660172,
                radiusMeters = 150f,
                locationName = "Fitness Center",
                transitionType = GeofenceTransitionType.BOTH,
                timeWindow = ReminderTimeWindow(
                    startTime = LocalTime.of(17, 0),
                    endTime = LocalTime.of(20, 0),
                    activeDays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
                )
            ),
            notificationStyle = NotificationStyle.FULL_SCREEN,
            isPersistentAlert = true,
            snoozeIntervalMinutes = 15,
            snoozeRepeatCount = 2,
            currentSnoozeCount = 1
        )

        val entity = domainReminder.toEntity()

        assertEquals(10L, entity.id)
        assertEquals("Gym Session", entity.title)
        assertEquals("LOCATION", entity.triggerType)
        assertEquals(10.762622, entity.locationLatitude ?: 0.0, 0.000001)
        assertEquals(106.660172, entity.locationLongitude ?: 0.0, 0.000001)
        assertEquals(150f, entity.locationRadiusMeters ?: 0f, 0.01f)
        assertEquals("Fitness Center", entity.locationName)
        assertEquals("BOTH", entity.locationTransitionType)
        assertEquals("17:00", entity.locationWindowStartTime)
        assertEquals("20:00", entity.locationWindowEndTime)
        assertEquals("MONDAY,WEDNESDAY,FRIDAY", entity.locationWindowDays)
        assertEquals("FULL_SCREEN", entity.notificationStyle)
        assertTrue(entity.isPersistentAlert)
        assertEquals(15, entity.snoozeIntervalMinutes)
        assertEquals(2, entity.snoozeRepeatCount)
        assertEquals(1, entity.currentSnoozeCount)
    }
}
