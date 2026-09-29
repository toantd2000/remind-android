package vn.io.litever.remind.core.data.repository

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import vn.io.litever.remind.core.database.dao.ChecklistItemDao
import vn.io.litever.remind.core.database.dao.ReminderDao
import vn.io.litever.remind.core.database.entity.ChecklistItemEntity
import vn.io.litever.remind.core.database.entity.ReminderEntity
import vn.io.litever.remind.core.database.model.PopulatedReminder
import vn.io.litever.remind.core.model.ChecklistItem
import java.time.DayOfWeek
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderAction
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.RepeatMode
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.model.UrgencyLevel
import java.time.LocalTime

class ReminderRepositoryChallengerTest {

    private val reminderDao = mockk<ReminderDao>(relaxed = true)
    private val checklistItemDao = mockk<ChecklistItemDao>(relaxed = true)
    private lateinit var repository: ReminderRepositoryImpl

    @Before
    fun setup() {
        repository = ReminderRepositoryImpl(reminderDao, checklistItemDao)
    }

    @Test
    fun reactiveFlow_tracksStateChangesAcrossMultipleEmissions() = runTest {
        val stateFlow = MutableStateFlow<List<PopulatedReminder>>(emptyList())
        every { reminderDao.getAllReminders() } returns stateFlow

        repository.getAllReminders().test {
            assertEquals(0, awaitItem().size)

            val reminder1 = PopulatedReminder(
                reminder = ReminderEntity(id = 1L, title = "First", triggerType = "FIXED_TIME"),
                checklist = listOf(ChecklistItemEntity(id = 10L, reminderId = 1L, text = "Subtask 1", isCompleted = false))
            )
            stateFlow.value = listOf(reminder1)

            val emission1 = awaitItem()
            assertEquals(1, emission1.size)
            assertEquals("First", emission1[0].title)
            assertEquals(0.0f, emission1[0].checklistProgress, 0.001f)

            val reminder1Updated = PopulatedReminder(
                reminder = ReminderEntity(id = 1L, title = "First", triggerType = "FIXED_TIME"),
                checklist = listOf(ChecklistItemEntity(id = 10L, reminderId = 1L, text = "Subtask 1", isCompleted = true))
            )
            stateFlow.value = listOf(reminder1Updated)

            val emission2 = awaitItem()
            assertEquals(1, emission2.size)
            assertEquals(1.0f, emission2[0].checklistProgress, 0.001f)
            assertTrue(emission2[0].isAllChecklistCompleted)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertComplexReminderWithMultiWindowIntervalAndChecklist_mapsAllFieldsAccurately() = runTest {
        val reminder = Reminder(
            title = "Drink Water Interval",
            notes = "Stay hydrated during workday",
            isEnabled = true,
            triggerType = ReminderTriggerType.INTERVAL,
            timeTrigger = TimeTrigger(
                intervalMinutes = 60,
                repeatMode = RepeatMode.DAILY,
                timeWindows = listOf(
                    ReminderTimeWindow(
                        startTime = LocalTime.of(8, 0),
                        endTime = LocalTime.of(11, 30),
                        activeDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)
                    ),
                    ReminderTimeWindow(
                        startTime = LocalTime.of(13, 30),
                        endTime = LocalTime.of(17, 30)
                    )
                )
            ),
            checklist = listOf(
                ChecklistItem(text = "Glass 1", isCompleted = false, orderIndex = 0),
                ChecklistItem(text = "Glass 2", isCompleted = true, orderIndex = 1)
            ),
            action = ReminderAction(
                urgencyLevel = UrgencyLevel.SOFT,
                notificationStyle = NotificationStyle.STANDARD,
                isPersistentAlert = false,
                snoozeIntervalMinutes = 10,
                snoozeRepeatCount = 5
            )
        )

        coEvery { reminderDao.insertReminder(any()) } returns 999L

        val id = repository.insertReminder(reminder)
        assertEquals(999L, id)

        coVerify(exactly = 1) {
            reminderDao.insertReminder(match { entity ->
                entity.title == "Drink Water Interval" &&
                entity.triggerType == "INTERVAL" &&
                entity.timeTriggerIntervalMinutes == 60 &&
                entity.timeTriggerRepeatMode == "DAILY" &&
                entity.timeTriggerTimeWindows?.contains("08:00|11:30|MONDAY,TUESDAY") == true &&
                entity.urgencyLevel == "SOFT" &&
                entity.notificationStyle == "STANDARD" &&
                !entity.isPersistentAlert
            })
        }
    }
}
