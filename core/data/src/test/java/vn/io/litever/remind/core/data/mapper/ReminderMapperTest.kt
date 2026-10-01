package vn.io.litever.remind.core.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.io.litever.remind.core.database.entity.ReminderEntity
import java.time.DayOfWeek
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderAction
import vn.io.litever.remind.core.model.ReminderMonthDay
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.RepeatMode
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.model.UrgencyLevel
import java.time.LocalDate
import java.time.LocalTime

class ReminderMapperTest {

    @Test
    fun `toModel correctly maps FIXED_TIME with multiple times and repeat days`() {
        val entity = ReminderEntity(
            id = 1L,
            title = "Morning Medicine",
            notes = "Take with water",
            isEnabled = true,
            triggerType = "FIXED_TIME",
            timeTriggerTimes = "08:30,20:30",
            timeTriggerExactDate = "2026-08-25",
            timeTriggerRepeatMode = "WEEKLY",
            timeTriggerRepeatValues = "1,3,5",
            urgencyLevel = "HARD",
            notificationStyle = "FULL_SCREEN",
            isPersistentAlert = true,
            snoozeIntervalMinutes = 10,
            
        )

        val model = entity.toModel()

        assertEquals(1L, model.id)
        assertEquals("Morning Medicine", model.title)
        assertEquals(ReminderTriggerType.FIXED_TIME, model.triggerType)
        assertNotNull(model.timeTrigger)
        assertEquals(2, model.timeTrigger?.time?.size)
        assertEquals(LocalTime.of(8, 30), model.timeTrigger?.time?.get(0))
        assertEquals(LocalTime.of(20, 30), model.timeTrigger?.time?.get(1))
        assertEquals(LocalDate.of(2026, 8, 25), model.timeTrigger?.exactDate)
        assertEquals(RepeatMode.WEEKLY, model.timeTrigger?.repeatMode)
        assertEquals(listOf(1, 3, 5), model.timeTrigger?.repeatValues)
        assertEquals(UrgencyLevel.HARD, model.action.urgencyLevel)
        assertEquals(NotificationStyle.FULL_SCREEN, model.action.notificationStyle)
        assertTrue(model.action.isPersistentAlert)
        assertEquals(10, model.action.snoozeIntervalMinutes)
    }

    @Test
    fun `toModel correctly maps INTERVAL with multiple time windows`() {
        val entity = ReminderEntity(
            id = 2L,
            title = "Drink Water",
            isEnabled = true,
            triggerType = "INTERVAL",
            timeTriggerIntervalMinutes = 60,
            timeTriggerTimeWindows = "08:00|11:30|MONDAY,TUESDAY;13:30|17:30|",
            urgencyLevel = "SOFT"
        )

        val model = entity.toModel()

        assertEquals(ReminderTriggerType.INTERVAL, model.triggerType)
        assertNotNull(model.timeTrigger)
        assertEquals(60, model.timeTrigger?.intervalMinutes)
        assertEquals(2, model.timeTrigger?.timeWindows?.size)

        val window1 = model.timeTrigger?.timeWindows?.get(0)
        assertEquals(LocalTime.of(8, 0), window1?.startTime)
        assertEquals(LocalTime.of(11, 30), window1?.endTime)
        assertEquals(listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY), window1?.activeDays)

        val window2 = model.timeTrigger?.timeWindows?.get(1)
        assertEquals(LocalTime.of(13, 30), window2?.startTime)
        assertEquals(LocalTime.of(17, 30), window2?.endTime)
        assertTrue(window2?.activeDays?.isEmpty() == true)

        assertEquals(UrgencyLevel.SOFT, model.action.urgencyLevel)
    }

    @Test
    fun `toModel correctly maps MONTHLY multiple days and YEARLY multiple dates`() {
        val entity = ReminderEntity(
            id = 3L,
            title = "Tax & Birthday",
            isEnabled = true,
            triggerType = "FIXED_TIME",
            timeTriggerRepeatMode = "YEARLY",
            timeTriggerMonthlyDays = "1,15,30",
            timeTriggerYearlyDates = "3-8;10-20;12-25"
        )

        val model = entity.toModel()

        assertEquals(listOf(1, 15, 30), model.timeTrigger?.monthlyDays)
        assertEquals(3, model.timeTrigger?.yearlyDates?.size)
        assertEquals(ReminderMonthDay(3, 8), model.timeTrigger?.yearlyDates?.get(0))
        assertEquals(ReminderMonthDay(10, 20), model.timeTrigger?.yearlyDates?.get(1))
        assertEquals(ReminderMonthDay(12, 25), model.timeTrigger?.yearlyDates?.get(2))
    }

    @Test
    fun `toEntity faithfully serializes domain model properties`() {
        val domainReminder = Reminder(
            id = 10L,
            title = "Work Task",
            notes = "Focus hours",
            isEnabled = true,
            triggerType = ReminderTriggerType.INTERVAL,
            timeTrigger = TimeTrigger(
                time = listOf(LocalTime.of(9, 0)),
                intervalMinutes = 90,
                repeatMode = RepeatMode.WEEKLY,
                repeatValues = listOf(1, 2, 3),
                monthlyDays = listOf(5, 20),
                yearlyDates = listOf(ReminderMonthDay(1, 1)),
                timeWindows = listOf(
                    ReminderTimeWindow(
                        startTime = LocalTime.of(9, 0),
                        endTime = LocalTime.of(17, 0),
                        activeDays = listOf(DayOfWeek.MONDAY)
                    )
                )
            ),
            action = ReminderAction(
                urgencyLevel = UrgencyLevel.HARD,
                notificationStyle = NotificationStyle.FULL_SCREEN,
                isPersistentAlert = true,
                snoozeIntervalMinutes = 15,
                
            ),
            
        )

        val entity = domainReminder.toEntity()

        assertEquals(10L, entity.id)
        assertEquals("Work Task", entity.title)
        assertEquals("INTERVAL", entity.triggerType)
        assertEquals(90, entity.timeTriggerIntervalMinutes)
        assertEquals("1,2,3", entity.timeTriggerRepeatValues)
        assertEquals("5,20", entity.timeTriggerMonthlyDays)
        assertEquals("1-1", entity.timeTriggerYearlyDates)
        assertEquals("09:00|17:00|MONDAY", entity.timeTriggerTimeWindows)
        assertEquals("09:00", entity.timeTriggerWindowStart)
        assertEquals("17:00", entity.timeTriggerWindowEnd)
        assertEquals("HARD", entity.urgencyLevel)
        assertEquals("FULL_SCREEN", entity.notificationStyle)
        assertTrue(entity.isPersistentAlert)
        assertEquals(15, entity.snoozeIntervalMinutes)
    }
}


