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
import vn.io.litever.remind.core.database.dao.ChecklistItemDao
import vn.io.litever.remind.core.database.dao.ReminderDao
import vn.io.litever.remind.core.database.entity.ChecklistItemEntity
import vn.io.litever.remind.core.database.entity.ReminderEntity

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class ReminderDaoTest {

    private lateinit var database: RemindDatabase
    private lateinit var reminderDao: ReminderDao
    private lateinit var checklistItemDao: ChecklistItemDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, RemindDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        reminderDao = database.reminderDao()
        checklistItemDao = database.checklistItemDao()
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
            triggerType = "TIME",
            timeTriggerHour = 9,
            timeTriggerMinute = 15
        )

        val id = reminderDao.insertReminder(entity)
        val fetched = reminderDao.getReminderById(id)

        assertNotNull(fetched)
        assertEquals("Test Reminder", fetched?.reminder?.title)
        assertEquals(9, fetched?.reminder?.timeTriggerHour)
        assertEquals(15, fetched?.reminder?.timeTriggerMinute)
    }

    @Test
    fun relationalPopulatedReminderQuery_returnsChecklistItems() = runTest {
        val reminderId = reminderDao.insertReminder(
            ReminderEntity(title = "Shopping", triggerType = "LOCATION")
        )

        val items = listOf(
            ChecklistItemEntity(reminderId = reminderId, text = "Apples", orderIndex = 0),
            ChecklistItemEntity(reminderId = reminderId, text = "Bananas", orderIndex = 1)
        )
        checklistItemDao.insertChecklistItems(items)

        val populated = reminderDao.getReminderById(reminderId)
        assertNotNull(populated)
        assertEquals(2, populated?.checklist?.size)
        assertEquals("Apples", populated?.checklist?.get(0)?.text)
        assertEquals("Bananas", populated?.checklist?.get(1)?.text)
    }

    @Test
    fun deleteReminder_cascadesDeletionToChecklistItems() = runTest {
        val reminderId = reminderDao.insertReminder(
            ReminderEntity(title = "Task with Checklist", triggerType = "TIME")
        )

        checklistItemDao.insertChecklistItems(
            listOf(
                ChecklistItemEntity(reminderId = reminderId, text = "Item 1"),
                ChecklistItemEntity(reminderId = reminderId, text = "Item 2")
            )
        )

        val beforeDeleteItems = checklistItemDao.getChecklistItemsForReminderSync(reminderId)
        assertEquals(2, beforeDeleteItems.size)

        // Delete parent reminder
        val reminderEntity = reminderDao.getReminderById(reminderId)!!.reminder
        reminderDao.deleteReminder(reminderEntity)

        // Verify parent is deleted
        assertNull(reminderDao.getReminderById(reminderId))

        // Verify checklist items were cascade deleted
        val afterDeleteItems = checklistItemDao.getChecklistItemsForReminderSync(reminderId)
        assertTrue(afterDeleteItems.isEmpty())
    }

    @Test
    fun updateChecklistItemCompletion_togglesOnlyTargetItem() = runTest {
        val reminderId = reminderDao.insertReminder(ReminderEntity(title = "Checklist", triggerType = "TIME"))
        val itemId1 = checklistItemDao.insertChecklistItem(
            ChecklistItemEntity(reminderId = reminderId, text = "Step 1", isCompleted = false)
        )
        val itemId2 = checklistItemDao.insertChecklistItem(
            ChecklistItemEntity(reminderId = reminderId, text = "Step 2", isCompleted = false)
        )

        checklistItemDao.updateChecklistItemCompletion(itemId1, true)

        val items = checklistItemDao.getChecklistItemsForReminderSync(reminderId)
        val item1 = items.first { it.id == itemId1 }
        val item2 = items.first { it.id == itemId2 }

        assertTrue(item1.isCompleted)
        assertEquals(false, item2.isCompleted)
    }

    @Test
    fun getActiveMonitoringReminders_filtersOutDisabledOrCompletedOrTimeTriggers() = runTest {
        reminderDao.insertReminder(ReminderEntity(title = "Active Location", triggerType = "LOCATION", isEnabled = true, isCompleted = false))
        reminderDao.insertReminder(ReminderEntity(title = "Active Wifi", triggerType = "WIFI", isEnabled = true, isCompleted = false))
        reminderDao.insertReminder(ReminderEntity(title = "Disabled Location", triggerType = "LOCATION", isEnabled = false, isCompleted = false))
        reminderDao.insertReminder(ReminderEntity(title = "Completed Location", triggerType = "LOCATION", isEnabled = true, isCompleted = true))
        reminderDao.insertReminder(ReminderEntity(title = "Active Time", triggerType = "TIME", isEnabled = true, isCompleted = false))

        val activeList = reminderDao.getActiveMonitoringReminders().first()
        assertEquals(2, activeList.size)
        val titles = activeList.map { it.reminder.title }
        assertTrue(titles.contains("Active Location"))
        assertTrue(titles.contains("Active Wifi"))
    }
}
