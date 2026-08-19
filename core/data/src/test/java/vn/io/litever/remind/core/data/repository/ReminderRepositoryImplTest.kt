package vn.io.litever.remind.core.data.repository

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import vn.io.litever.remind.core.database.dao.ChecklistItemDao
import vn.io.litever.remind.core.database.dao.ReminderDao
import vn.io.litever.remind.core.database.entity.ChecklistItemEntity
import vn.io.litever.remind.core.database.entity.ReminderEntity
import vn.io.litever.remind.core.database.model.PopulatedReminder
import vn.io.litever.remind.core.model.ChecklistItem
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType

class ReminderRepositoryImplTest {

    private val reminderDao = mockk<ReminderDao>(relaxed = true)
    private val checklistItemDao = mockk<ChecklistItemDao>(relaxed = true)
    private lateinit var repository: ReminderRepositoryImpl

    @Before
    fun setup() {
        repository = ReminderRepositoryImpl(reminderDao, checklistItemDao)
    }

    @Test
    fun `getAllReminders emits mapped domain reminders from DAO flow`() = runTest {
        val populatedList = listOf(
            PopulatedReminder(
                reminder = ReminderEntity(id = 1L, title = "Task 1", triggerType = "TIME"),
                checklist = listOf(ChecklistItemEntity(id = 10L, reminderId = 1L, text = "Subtask 1"))
            )
        )
        every { reminderDao.getAllReminders() } returns flowOf(populatedList)

        repository.getAllReminders().test {
            val emissions = awaitItem()
            assertEquals(1, emissions.size)
            assertEquals("Task 1", emissions[0].title)
            assertEquals(1, emissions[0].checklist.size)
            assertEquals("Subtask 1", emissions[0].checklist[0].text)
            awaitComplete()
        }
    }

    @Test
    fun `getReminderFlow emits null when DAO emits null`() = runTest {
        every { reminderDao.getReminderFlow(99L) } returns flowOf(null)

        repository.getReminderFlow(99L).test {
            val item = awaitItem()
            assertNull(item)
            awaitComplete()
        }
    }

    @Test
    fun `getReminderById returns mapped reminder when present`() = runTest {
        val populated = PopulatedReminder(
            reminder = ReminderEntity(id = 5L, title = "Doctor Appointment", triggerType = "TIME"),
            checklist = emptyList()
        )
        coEvery { reminderDao.getReminderById(5L) } returns populated

        val result = repository.getReminderById(5L)

        assertNotNull(result)
        assertEquals("Doctor Appointment", result?.title)
    }

    @Test
    fun `insertReminder inserts entity and batch inserts checklist items with generated ID`() = runTest {
        val reminder = Reminder(
            title = "Morning Routine",
            triggerType = ReminderTriggerType.TIME,
            checklist = listOf(
                ChecklistItem(text = "Drink Water"),
                ChecklistItem(text = "Stretch")
            )
        )

        coEvery { reminderDao.insertReminder(any()) } returns 42L

        val generatedId = repository.insertReminder(reminder)

        assertEquals(42L, generatedId)
        coVerify(exactly = 1) { reminderDao.insertReminder(match { it.title == "Morning Routine" }) }
        coVerify(exactly = 1) {
            checklistItemDao.insertChecklistItems(match { list ->
                list.size == 2 &&
                list[0].reminderId == 42L && list[0].orderIndex == 0 && list[0].text == "Drink Water" &&
                list[1].reminderId == 42L && list[1].orderIndex == 1 && list[1].text == "Stretch"
            })
        }
    }

    @Test
    fun `insertReminder without checklist items skips checklist DAO insert`() = runTest {
        val reminder = Reminder(
            title = "Simple Alarm",
            triggerType = ReminderTriggerType.TIME,
            checklist = emptyList()
        )

        coEvery { reminderDao.insertReminder(any()) } returns 100L

        val id = repository.insertReminder(reminder)

        assertEquals(100L, id)
        coVerify(exactly = 1) { reminderDao.insertReminder(any()) }
        coVerify(exactly = 0) { checklistItemDao.insertChecklistItems(any()) }
    }

    @Test
    fun `updateReminder updates entity and replaces checklist items`() = runTest {
        val reminder = Reminder(
            id = 7L,
            title = "Updated Task",
            triggerType = ReminderTriggerType.TIME,
            checklist = listOf(ChecklistItem(id = 1L, text = "New Item"))
        )

        repository.updateReminder(reminder)

        coVerify(exactly = 1) { reminderDao.updateReminder(match { it.id == 7L && it.title == "Updated Task" }) }
        coVerify(exactly = 1) { checklistItemDao.deleteChecklistItemsForReminder(7L) }
        coVerify(exactly = 1) { checklistItemDao.insertChecklistItems(match { it.size == 1 && it[0].reminderId == 7L }) }
    }

    @Test
    fun `deleteReminder delegates to DAO delete`() = runTest {
        val reminder = Reminder(id = 12L, title = "To Delete", triggerType = ReminderTriggerType.TIME)

        repository.deleteReminder(reminder)

        coVerify(exactly = 1) { reminderDao.deleteReminder(match { it.id == 12L }) }
    }

    @Test
    fun `updateChecklistItemCompletion directly invokes checklistItemDao completion update`() = runTest {
        repository.updateChecklistItemCompletion(itemId = 25L, isCompleted = true)

        coVerify(exactly = 1) { checklistItemDao.updateChecklistItemCompletion(25L, true) }
    }

    @Test
    fun `getActiveMonitoringReminders emits active background triggers`() = runTest {
        val activeList = listOf(
            PopulatedReminder(
                reminder = ReminderEntity(id = 2L, title = "Geofence Alert", triggerType = "LOCATION", isEnabled = true, isCompleted = false),
                checklist = emptyList()
            )
        )
        every { reminderDao.getActiveMonitoringReminders() } returns flowOf(activeList)

        repository.getActiveMonitoringReminders().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("Geofence Alert", list[0].title)
            awaitComplete()
        }
    }
}
