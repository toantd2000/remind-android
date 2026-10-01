package vn.io.litever.remind.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import vn.io.litever.remind.core.database.dao.ReminderDao
import vn.io.litever.remind.core.database.entity.ReminderEntity

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class ReminderDaoTest {

    private lateinit var database: RemindDatabase
    private lateinit var reminderDao: ReminderDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, RemindDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        reminderDao = database.reminderDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetReminderById_persistsSuccessfully() = runTest {
        val entity = ReminderEntity(
            title = "Test Reminder",
            notes = "Test Note",
            triggerType = "FIXED_TIME",
            timeTriggerTimes = "09:15"
        )

        val id = reminderDao.insertReminder(entity)
        val fetched = reminderDao.getReminderById(id)

        assertNotNull(fetched)
        assertEquals("Test Reminder", fetched?.title)
        assertEquals("09:15", fetched?.timeTriggerTimes)
    }

    @Test
    fun deleteReminder_deletesSuccessfully() = runTest {
        val reminderId = reminderDao.insertReminder(
            ReminderEntity(title = "Task to delete", triggerType = "FIXED_TIME")
        )

        val entity = reminderDao.getReminderById(reminderId)
        assertNotNull(entity)

        reminderDao.deleteReminder(entity!!)
        assertNull(reminderDao.getReminderById(reminderId))
    }

    @Test
    fun getActiveReminders_filtersOutDisabled() = runTest {
        reminderDao.insertReminder(ReminderEntity(title = "Active 1", triggerType = "FIXED_TIME", isEnabled = true))
        reminderDao.insertReminder(ReminderEntity(title = "Active 2", triggerType = "INTERVAL", isEnabled = true))
        reminderDao.insertReminder(ReminderEntity(title = "Disabled", triggerType = "FIXED_TIME", isEnabled = false))

        val activeList = reminderDao.getActiveReminders().first()
        assertEquals(2, activeList.size)
        val titles = activeList.map { it.title }
        assertTrue(titles.contains("Active 1"))
        assertTrue(titles.contains("Active 2"))
    }
}
