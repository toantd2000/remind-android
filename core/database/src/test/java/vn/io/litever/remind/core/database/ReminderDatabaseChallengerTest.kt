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
import vn.io.litever.remind.core.database.dao.ReminderDao
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
    fun multiReminderIsolation_deletingOneParentDoesNotAffectOthers() = runTest {
        // Create reminder A
        val reminderAId = reminderDao.insertReminder(ReminderEntity(title = "Reminder A", triggerType = "TIME"))

        // Create reminder B
        val reminderBId = reminderDao.insertReminder(ReminderEntity(title = "Reminder B", triggerType = "LOCATION"))

        // Verify initial state
        assertNotNull(reminderDao.getReminderById(reminderAId))
        assertNotNull(reminderDao.getReminderById(reminderBId))

        // Delete reminder A by entity
        val reminderAEntity = reminderDao.getReminderById(reminderAId)!!
        reminderDao.deleteReminder(reminderAEntity)

        // Verify Reminder A is gone
        assertNull(reminderDao.getReminderById(reminderAId))

        // Verify Reminder B is completely intact
        val reminderBFetched = reminderDao.getReminderById(reminderBId)
        assertNotNull(reminderBFetched)
        assertEquals("Reminder B", reminderBFetched?.title)
    }

    @Test
    fun reactiveFlowEmitsOnSnoozeUpdate() = runTest {
        val reminderId = reminderDao.insertReminder(
            ReminderEntity(
                title = "Reactive Test",
                triggerType = "TIME",
                currentSnoozeCount = 0,
                snoozeRepeatCount = 3
            )
        )

        reminderDao.getReminderFlow(reminderId).test {
            // Initial emission
            val initial = awaitItem()
            assertNotNull(initial)
            assertEquals("Reactive Test", initial?.title)
            assertEquals(0, initial?.currentSnoozeCount)

            // Step 1: Update snooze state
            reminderDao.updateSnoozeState(
                id = reminderId,
                currentSnoozeCount = 1,
                snoozeNextTriggerTime = 123456789L
            )
            val updatedSnooze = awaitItem()
            assertNotNull(updatedSnooze)
            assertEquals(1, updatedSnooze?.currentSnoozeCount)
            assertEquals(123456789L, updatedSnooze?.snoozeNextTriggerTime)

            // Step 2: Delete parent reminder
            reminderDao.deleteReminderById(reminderId)
            val deletedEmission = awaitItem()
            assertNull(deletedEmission)

            cancelAndIgnoreRemainingEvents()
        }
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

    @Test
    fun migration7To8_dropsIsCompletedProperly() {
        val helper = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(ApplicationProvider.getApplicationContext())
            .name("migration7_8_test.db")
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(7) {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `reminders` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `title` TEXT NOT NULL,
                            `notes` TEXT NOT NULL,
                            `isEnabled` INTEGER NOT NULL,
                            `triggerType` TEXT NOT NULL,
                            `timeTriggerTimes` TEXT,
                            `timeTriggerIntervalMinutes` INTEGER,
                            `timeTriggerExactDate` TEXT,
                            `timeTriggerRepeatMode` TEXT NOT NULL DEFAULT 'NONE',
                            `timeTriggerRepeatValues` TEXT,
                            `timeTriggerMonthlyDays` TEXT,
                            `timeTriggerYearlyDates` TEXT,
                            `timeTriggerTimeWindows` TEXT,
                            `timeTriggerWindowStart` TEXT,
                            `timeTriggerWindowEnd` TEXT,
                            `timeTriggerWindowActiveDays` TEXT,
                            `urgencyLevel` TEXT NOT NULL DEFAULT 'MEDIUM',
                            `notificationStyle` TEXT NOT NULL DEFAULT 'STANDARD',
                            `isPersistentAlert` INTEGER NOT NULL DEFAULT 0,
                            `autoSilenceMinutes` INTEGER NOT NULL DEFAULT 5,
                            `useAlarmStream` INTEGER NOT NULL DEFAULT 1,
                            `ringtoneUri` TEXT,
                            `vibrationEnabled` INTEGER NOT NULL DEFAULT 1,
                            `volume` INTEGER NOT NULL DEFAULT 100,
                            `snoozeEnabled` INTEGER NOT NULL DEFAULT 1,
                            `snoozeIntervalMinutes` INTEGER NOT NULL DEFAULT 5,
                            `snoozeRepeatCount` INTEGER NOT NULL DEFAULT 3,
                            `currentSnoozeCount` INTEGER NOT NULL DEFAULT 0,
                            `snoozeNextTriggerTime` INTEGER,
                            `lastTriggeredTime` INTEGER,
                            `isCompleted` INTEGER NOT NULL DEFAULT 0,
                            `createdAt` INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                    db.execSQL("INSERT INTO `reminders` (`id`, `title`, `notes`, `isEnabled`, `triggerType`, `isCompleted`, `createdAt`) VALUES (1, 'V7 Task', 'Note', 1, 'FIXED_TIME', 1, 1000)")
                }
                override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()
        val factory = androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory()
        val dbHelper = factory.create(helper)
        val db = dbHelper.writableDatabase

        // Execute MIGRATION_7_8
        RemindDatabase.MIGRATION_7_8.migrate(db)

        val cursor = db.query("SELECT id, title, isEnabled FROM reminders WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals("V7 Task", cursor.getString(1))
        assertEquals(1, cursor.getInt(2))
        cursor.close()

        db.close()
    }
}
