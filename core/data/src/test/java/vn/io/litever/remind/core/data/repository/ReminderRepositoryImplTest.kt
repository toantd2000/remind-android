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
import vn.io.litever.remind.core.database.dao.ReminderDao
import vn.io.litever.remind.core.database.entity.ReminderEntity
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType

class ReminderRepositoryImplTest {

    private val reminderDao = mockk<ReminderDao>(relaxed = true)
    private lateinit var repository: ReminderRepositoryImpl

    @Before
    fun setup() {
        repository = ReminderRepositoryImpl(reminderDao)
    }

    @Test
    fun `getAllReminders emits mapped domain reminders from DAO flow`() = runTest {
        val entityList = listOf(
            ReminderEntity(id = 1L, title = "Task 1", triggerType = "TIME")
        )
        every { reminderDao.getAllReminders() } returns flowOf(entityList)

        repository.getAllReminders().test {
            val emissions = awaitItem()
            assertEquals(1, emissions.size)
            assertEquals("Task 1", emissions[0].title)
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
        val entity = ReminderEntity(id = 5L, title = "Doctor Appointment", triggerType = "TIME")
        coEvery { reminderDao.getReminderById(5L) } returns entity

        val result = repository.getReminderById(5L)

        assertNotNull(result)
        assertEquals("Doctor Appointment", result?.title)
    }

    @Test
    fun `insertReminder inserts entity and returns generated ID`() = runTest {
        val reminder = Reminder(
            title = "Morning Routine",
            triggerType = ReminderTriggerType.FIXED_TIME
        )

        coEvery { reminderDao.insertReminder(any()) } returns 42L

        val generatedId = repository.insertReminder(reminder)

        assertEquals(42L, generatedId)
        coVerify(exactly = 1) { reminderDao.insertReminder(match { it.title == "Morning Routine" }) }
    }

    @Test
    fun `updateReminder updates entity`() = runTest {
        val reminder = Reminder(
            id = 7L,
            title = "Updated Task",
            triggerType = ReminderTriggerType.FIXED_TIME
        )

        repository.updateReminder(reminder)

        coVerify(exactly = 1) {
            reminderDao.updateReminder(
                match { it.id == 7L && it.title == "Updated Task" }
            )
        }
    }

    @Test
    fun `deleteReminder delegates to DAO delete`() = runTest {
        val reminder = Reminder(id = 12L, title = "To Delete", triggerType = ReminderTriggerType.FIXED_TIME)

        repository.deleteReminder(reminder)

        coVerify(exactly = 1) { reminderDao.deleteReminder(match { it.id == 12L }) }
    }

    @Test
    fun `getActiveMonitoringReminders emits active background triggers`() = runTest {
        val activeList = listOf(
            ReminderEntity(id = 2L, title = "Geofence Alert", triggerType = "LOCATION", isEnabled = true)
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
