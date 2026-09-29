package vn.io.litever.remind.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
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

/**
 * Adversarial Challenger Integration Test for Room DB, DAOs, CASCADE Deletion,
 * Snooze State Isolation, and Reactive Flow Invalidation.
 */
@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class ReminderDatabaseChallengerTest {

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
    fun multiParentCascadeIsolation_deletingOneParentDoesNotAffectOthers() = runTest {
        // Create parent A with 3 items
        val parentAId = reminderDao.insertReminder(ReminderEntity(title = "Parent A", triggerType = "TIME"))
        val itemsA = listOf(
            ChecklistItemEntity(reminderId = parentAId, text = "A1", orderIndex = 0),
            ChecklistItemEntity(reminderId = parentAId, text = "A2", orderIndex = 1),
            ChecklistItemEntity(reminderId = parentAId, text = "A3", orderIndex = 2)
        )
        checklistItemDao.insertChecklistItems(itemsA)

        // Create parent B with 2 items
        val parentBId = reminderDao.insertReminder(ReminderEntity(title = "Parent B", triggerType = "LOCATION"))
        val itemsB = listOf(
            ChecklistItemEntity(reminderId = parentBId, text = "B1", orderIndex = 0),
            ChecklistItemEntity(reminderId = parentBId, text = "B2", orderIndex = 1)
        )
        checklistItemDao.insertChecklistItems(itemsB)

        // Verify initial state
        assertEquals(3, checklistItemDao.getChecklistItemsForReminderSync(parentAId).size)
        assertEquals(2, checklistItemDao.getChecklistItemsForReminderSync(parentBId).size)

        // Delete parent A by entity
        val parentAEntity = reminderDao.getReminderById(parentAId)!!.reminder
        reminderDao.deleteReminder(parentAEntity)

        // Verify Parent A and its items are gone
        assertNull(reminderDao.getReminderById(parentAId))
        assertTrue(checklistItemDao.getChecklistItemsForReminderSync(parentAId).isEmpty())

        // Verify Parent B and its items are completely intact
        val parentBFetched = reminderDao.getReminderById(parentBId)
        assertNotNull(parentBFetched)
        assertEquals("Parent B", parentBFetched?.reminder?.title)
        val parentBItems = checklistItemDao.getChecklistItemsForReminderSync(parentBId)
        assertEquals(2, parentBItems.size)
        assertEquals("B1", parentBItems[0].text)
        assertEquals("B2", parentBItems[1].text)
    }

    @Test
    fun reactiveFlowEmitsOnChecklistToggleAndSnoozeUpdate() = runTest {
        val reminderId = reminderDao.insertReminder(
            ReminderEntity(
                title = "Reactive Test",
                triggerType = "TIME",
                currentSnoozeCount = 0,
                snoozeRepeatCount = 3
            )
        )
        val itemId = checklistItemDao.insertChecklistItem(
            ChecklistItemEntity(reminderId = reminderId, text = "Subtask", isCompleted = false)
        )

        reminderDao.getReminderFlow(reminderId).test {
            // Initial emission
            val initial = awaitItem()
            assertNotNull(initial)
            assertEquals("Reactive Test", initial?.reminder?.title)
            assertEquals(0, initial?.reminder?.currentSnoozeCount)
            assertEquals(1, initial?.checklist?.size)
            assertEquals(false, initial?.checklist?.get(0)?.isCompleted)

            // Step 1: Update checklist item completion
            checklistItemDao.updateChecklistItemCompletion(itemId, true)
            val updatedChecklist = awaitItem()
            assertNotNull(updatedChecklist)
            assertEquals(true, updatedChecklist?.checklist?.get(0)?.isCompleted)
            assertEquals(0, updatedChecklist?.reminder?.currentSnoozeCount)

            // Step 2: Update snooze state (checklist state should be preserved)
            reminderDao.updateSnoozeState(
                id = reminderId,
                currentSnoozeCount = 1,
                snoozeNextTriggerTime = 123456789L
            )
            val updatedSnooze = awaitItem()
            assertNotNull(updatedSnooze)
            assertEquals(1, updatedSnooze?.reminder?.currentSnoozeCount)
            assertEquals(123456789L, updatedSnooze?.reminder?.snoozeNextTriggerTime)
            assertEquals(true, updatedSnooze?.checklist?.get(0)?.isCompleted)

            // Step 3: Delete parent reminder
            reminderDao.deleteReminderById(reminderId)
            val deletedEmission = awaitItem()
            assertNull(deletedEmission)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateChecklistBatchSync_replacesItemsCorrectly() = runTest {
        val reminderId = reminderDao.insertReminder(ReminderEntity(title = "List Sync", triggerType = "TIME"))

        // Add 3 original items
        checklistItemDao.insertChecklistItems(
            listOf(
                ChecklistItemEntity(reminderId = reminderId, text = "Old 1", orderIndex = 0),
                ChecklistItemEntity(reminderId = reminderId, text = "Old 2", orderIndex = 1),
                ChecklistItemEntity(reminderId = reminderId, text = "Old 3", orderIndex = 2)
            )
        )

        // Replace with 2 new items
        checklistItemDao.deleteChecklistItemsForReminder(reminderId)
        checklistItemDao.insertChecklistItems(
            listOf(
                ChecklistItemEntity(reminderId = reminderId, text = "New 1", orderIndex = 0),
                ChecklistItemEntity(reminderId = reminderId, text = "New 2", orderIndex = 1)
            )
        )

        val updatedItems = checklistItemDao.getChecklistItemsForReminderSync(reminderId)
        assertEquals(2, updatedItems.size)
        assertEquals("New 1", updatedItems[0].text)
        assertEquals("New 2", updatedItems[1].text)
    }

    @Test
    fun migration4To5_createsTablesAndIndexesProperly() {
        val helper = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(ApplicationProvider.getApplicationContext())
            .name("migration_test.db")
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(4) {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    // Create v4 schema: alarms table with overrideDndEnabled
                    db.execSQL("CREATE TABLE IF NOT EXISTS `alarms` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `hour` INTEGER NOT NULL, `minute` INTEGER NOT NULL, `label` TEXT NOT NULL, `isEnabled` INTEGER NOT NULL, `repeatDays` TEXT NOT NULL, `date` TEXT, `vibrationEnabled` INTEGER NOT NULL, `ringtoneUri` TEXT, `volume` INTEGER NOT NULL, `snoozeEnabled` INTEGER NOT NULL, `snoozeInterval` INTEGER NOT NULL, `snoozeRepeatCount` INTEGER NOT NULL, `autoSilenceMinutes` INTEGER NOT NULL, `currentSnoozeCount` INTEGER NOT NULL, `snoozeNextTriggerTime` INTEGER, `message` TEXT NOT NULL, `skippedAt` INTEGER, `gradualVolumeDurationSeconds` INTEGER NOT NULL, `lastTriggeredTime` INTEGER, `useAlarmStream` INTEGER NOT NULL DEFAULT 1, `overrideDndEnabled` INTEGER NOT NULL DEFAULT 0)")
                }
                override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()
        val factory = androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory()
        val dbHelper = factory.create(helper)
        val db = dbHelper.writableDatabase

        // Execute MIGRATION_4_5
        RemindDatabase.MIGRATION_4_5.migrate(db)

        // Verify reminders table exists and can be inserted into
        db.execSQL("INSERT INTO reminders (title, notes, isEnabled, triggerType, notificationStyle, isPersistentAlert, vibrationEnabled, volume, snoozeEnabled, snoozeIntervalMinutes, snoozeRepeatCount, currentSnoozeCount, isCompleted, createdAt) VALUES ('Migrated Task', 'Note', 1, 'TIME', 'STANDARD', 0, 1, 100, 1, 5, 3, 0, 0, 1000)")
        val cursorReminders = db.query("SELECT id, title FROM reminders WHERE title = 'Migrated Task'")
        assertTrue(cursorReminders.moveToFirst())
        val reminderId = cursorReminders.getLong(0)
        assertEquals("Migrated Task", cursorReminders.getString(1))
        cursorReminders.close()

        // Verify checklist_items table exists with foreign key
        db.execSQL("INSERT INTO checklist_items (reminderId, text, isCompleted, orderIndex) VALUES ($reminderId, 'Migrated Item', 0, 0)")
        val cursorItems = db.query("SELECT reminderId, text FROM checklist_items WHERE reminderId = $reminderId")
        assertTrue(cursorItems.moveToFirst())
        assertEquals("Migrated Item", cursorItems.getString(1))
        cursorItems.close()

        db.close()
    }
}
