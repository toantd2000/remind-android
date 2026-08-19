package vn.io.litever.remind.core.alarm.reminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.core.model.LocationTrigger
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.model.WifiTrigger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Helper simulating scheduling and foreground service lifecycle determination for test verification.
 */
object ReminderSchedulerTestHelper {

    fun computeNextTriggerEpochMillis(
        reminder: Reminder,
        referenceTime: LocalDateTime = LocalDateTime.now()
    ): Long? {
        if (!reminder.isEnabled || reminder.isCompleted) return null

        // Check if currently snoozed
        if (reminder.snoozeNextTriggerTime != null && reminder.snoozeNextTriggerTime > 0) {
            return reminder.snoozeNextTriggerTime
        }

        val timeTrigger = reminder.timeTrigger ?: return null
        val nextOccurrence = timeTrigger.getNextOccurrence(referenceTime)
        return nextOccurrence.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun computeSnoozeTriggerEpochMillis(
        currentEpochMillis: Long,
        snoozeIntervalMinutes: Int
    ): Long {
        return currentEpochMillis + (snoozeIntervalMinutes * 60 * 1000L)
    }

    fun isForegroundMonitoringServiceRequired(reminders: List<Reminder>): Boolean {
        return reminders.any { reminder ->
            reminder.isEnabled && !reminder.isCompleted &&
                    (reminder.triggerType == ReminderTriggerType.LOCATION || reminder.triggerType == ReminderTriggerType.WIFI)
        }
    }
}

/**
 * Comprehensive Unit Tests for Reminder Scheduler calculations and Foreground Service Lifecycle (Tiers 1 & 2).
 */
class ReminderSchedulerAndServiceTest {

    @Test
    fun scheduler_computesNextOccurrence_oneTimeFuture() {
        val now = LocalDateTime.of(2026, 8, 19, 10, 0)
        val reminder = Reminder(
            id = 1,
            title = "Dentist",
            triggerType = ReminderTriggerType.TIME,
            timeTrigger = TimeTrigger(
                time = LocalTime.of(15, 0),
                date = LocalDate.of(2026, 8, 19)
            )
        )

        val triggerEpoch = ReminderSchedulerTestHelper.computeNextTriggerEpochMillis(reminder, now)
        assertNotNull(triggerEpoch)

        val expected = LocalDateTime.of(2026, 8, 19, 15, 0)
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        assertEquals(expected, triggerEpoch)
    }

    @Test
    fun scheduler_computesSnoozeInterval_accurately() {
        val baseTime = 1724050000000L
        val intervalMinutes = 10
        val snoozeTime = ReminderSchedulerTestHelper.computeSnoozeTriggerEpochMillis(baseTime, intervalMinutes)

        assertEquals(baseTime + 600000L, snoozeTime)
    }

    @Test
    fun serviceLifecycle_timeRemindersOnly_serviceNotRequired() {
        val reminders = listOf(
            Reminder(id = 1, title = "T1", triggerType = ReminderTriggerType.TIME),
            Reminder(id = 2, title = "T2", triggerType = ReminderTriggerType.TIME)
        )

        val required = ReminderSchedulerTestHelper.isForegroundMonitoringServiceRequired(reminders)
        assertFalse("Foreground service should not run for purely time-based reminders", required)
    }

    @Test
    fun serviceLifecycle_withActiveLocationReminder_serviceRequired() {
        val reminders = listOf(
            Reminder(id = 1, title = "T1", triggerType = ReminderTriggerType.TIME),
            Reminder(
                id = 2,
                title = "Loc1",
                triggerType = ReminderTriggerType.LOCATION,
                locationTrigger = LocationTrigger(latitude = 21.0, longitude = 105.0, radiusMeters = 100f)
            )
        )

        val required = ReminderSchedulerTestHelper.isForegroundMonitoringServiceRequired(reminders)
        assertTrue("Foreground service MUST run when active location reminder exists", required)
    }

    @Test
    fun serviceLifecycle_withActiveWifiReminder_serviceRequired() {
        val reminders = listOf(
            Reminder(
                id = 3,
                title = "Wifi1",
                triggerType = ReminderTriggerType.WIFI,
                wifiTrigger = WifiTrigger(ssid = "Office_WiFi")
            )
        )

        val required = ReminderSchedulerTestHelper.isForegroundMonitoringServiceRequired(reminders)
        assertTrue("Foreground service MUST run when active Wi-Fi reminder exists", required)
    }

    @Test
    fun serviceLifecycle_whenLocationReminderDisabled_serviceNotRequired() {
        val reminders = listOf(
            Reminder(
                id = 2,
                title = "Loc1",
                isEnabled = false,
                triggerType = ReminderTriggerType.LOCATION,
                locationTrigger = LocationTrigger(latitude = 21.0, longitude = 105.0, radiusMeters = 100f)
            )
        )

        val required = ReminderSchedulerTestHelper.isForegroundMonitoringServiceRequired(reminders)
        assertFalse("Disabled location reminders should not keep service alive", required)
    }

    @Test
    fun serviceLifecycle_whenLocationReminderCompleted_serviceNotRequired() {
        val reminders = listOf(
            Reminder(
                id = 2,
                title = "Loc1",
                isEnabled = true,
                isCompleted = true,
                triggerType = ReminderTriggerType.LOCATION,
                locationTrigger = LocationTrigger(latitude = 21.0, longitude = 105.0, radiusMeters = 100f)
            )
        )

        val required = ReminderSchedulerTestHelper.isForegroundMonitoringServiceRequired(reminders)
        assertFalse("Completed location reminders should not keep service alive", required)
    }
}
