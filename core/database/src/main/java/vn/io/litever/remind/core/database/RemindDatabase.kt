package vn.io.litever.remind.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import vn.io.litever.remind.core.database.dao.AlarmDao
import vn.io.litever.remind.core.database.dao.ChecklistItemDao
import vn.io.litever.remind.core.database.dao.MissedAlarmDao
import vn.io.litever.remind.core.database.dao.MissionDao
import vn.io.litever.remind.core.database.dao.PhraseDao
import vn.io.litever.remind.core.database.dao.ReminderDao
import vn.io.litever.remind.core.database.entity.ChecklistItemEntity
import vn.io.litever.remind.core.database.entity.ReminderEntity
import vn.io.litever.remind.core.database.model.AlarmEntity
import vn.io.litever.remind.core.database.model.MissedAlarmEntity
import vn.io.litever.remind.core.database.model.MissionEntity
import vn.io.litever.remind.core.database.model.PhraseEntity

@Database(
    entities = [
        AlarmEntity::class,
        MissionEntity::class,
        PhraseEntity::class,
        MissedAlarmEntity::class,
        ReminderEntity::class,
        ChecklistItemEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class RemindDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun missionDao(): MissionDao
    abstract fun phraseDao(): PhraseDao
    abstract fun missedAlarmDao(): MissedAlarmDao
    abstract fun reminderDao(): ReminderDao
    abstract fun checklistItemDao(): ChecklistItemDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create missed_alarms table
                db.execSQL("CREATE TABLE IF NOT EXISTS `missed_alarms` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `alarmId` INTEGER NOT NULL, `alarmLabel` TEXT NOT NULL, `scheduledTime` INTEGER NOT NULL, `missedTime` INTEGER NOT NULL, `reason` TEXT NOT NULL)")

                // 2. Update alarms table: remove isMissed, add lastTriggeredTime
                db.execSQL("CREATE TABLE IF NOT EXISTS `alarms_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `hour` INTEGER NOT NULL, `minute` INTEGER NOT NULL, `label` TEXT NOT NULL, `isEnabled` INTEGER NOT NULL, `repeatDays` TEXT NOT NULL, `date` TEXT, `vibrationEnabled` INTEGER NOT NULL, `ringtoneUri` TEXT, `volume` INTEGER NOT NULL, `snoozeEnabled` INTEGER NOT NULL, `snoozeInterval` INTEGER NOT NULL, `snoozeRepeatCount` INTEGER NOT NULL, `autoSilenceMinutes` INTEGER NOT NULL, `currentSnoozeCount` INTEGER NOT NULL, `snoozeNextTriggerTime` INTEGER, `message` TEXT NOT NULL, `skippedAt` INTEGER, `gradualVolumeDurationSeconds` INTEGER NOT NULL, `lastTriggeredTime` INTEGER)")

                db.execSQL("INSERT INTO `alarms_new` (`id`, `hour`, `minute`, `label`, `isEnabled`, `repeatDays`, `date`, `vibrationEnabled`, `ringtoneUri`, `volume`, `snoozeEnabled`, `snoozeInterval`, `snoozeRepeatCount`, `autoSilenceMinutes`, `currentSnoozeCount`, `snoozeNextTriggerTime`, `message`, `skippedAt`, `gradualVolumeDurationSeconds`) SELECT `id`, `hour`, `minute`, `label`, `isEnabled`, `repeatDays`, `date`, `vibrationEnabled`, `ringtoneUri`, `volume`, `snoozeEnabled`, `snoozeInterval`, `snoozeRepeatCount`, `autoSilenceMinutes`, `currentSnoozeCount`, `snoozeNextTriggerTime`, `message`, `skippedAt`, `gradualVolumeDurationSeconds` FROM `alarms`")

                db.execSQL("DROP TABLE `alarms`")
                db.execSQL("ALTER TABLE `alarms_new` RENAME TO `alarms`")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `alarms` ADD COLUMN `useAlarmStream` INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `alarms` ADD COLUMN `overrideDndEnabled` INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `reminders` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `isEnabled` INTEGER NOT NULL,
                        `triggerType` TEXT NOT NULL,
                        `timeTriggerHour` INTEGER,
                        `timeTriggerMinute` INTEGER,
                        `timeTriggerDate` TEXT,
                        `timeTriggerRepeatDays` TEXT,
                        `locationLatitude` REAL,
                        `locationLongitude` REAL,
                        `locationRadiusMeters` REAL,
                        `locationName` TEXT,
                        `locationTransitionType` TEXT,
                        `locationWindowStartTime` TEXT,
                        `locationWindowEndTime` TEXT,
                        `locationWindowDays` TEXT,
                        `wifiSsid` TEXT,
                        `wifiBssid` TEXT,
                        `wifiTriggerType` TEXT,
                        `wifiWindowStartTime` TEXT,
                        `wifiWindowEndTime` TEXT,
                        `wifiWindowDays` TEXT,
                        `notificationStyle` TEXT NOT NULL,
                        `isPersistentAlert` INTEGER NOT NULL,
                        `ringtoneUri` TEXT,
                        `vibrationEnabled` INTEGER NOT NULL,
                        `volume` INTEGER NOT NULL,
                        `snoozeEnabled` INTEGER NOT NULL,
                        `snoozeIntervalMinutes` INTEGER NOT NULL,
                        `snoozeRepeatCount` INTEGER NOT NULL,
                        `currentSnoozeCount` INTEGER NOT NULL,
                        `snoozeNextTriggerTime` INTEGER,
                        `lastTriggeredTime` INTEGER,
                        `isCompleted` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `checklist_items` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `reminderId` INTEGER NOT NULL,
                        `text` TEXT NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `orderIndex` INTEGER NOT NULL,
                        FOREIGN KEY(`reminderId`) REFERENCES `reminders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_checklist_items_reminderId` ON `checklist_items` (`reminderId`)"
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `reminders` ADD COLUMN `timeTriggerMonthlyDays` TEXT")
                db.execSQL("ALTER TABLE `reminders` ADD COLUMN `timeTriggerYearlyDates` TEXT")
                db.execSQL("ALTER TABLE `reminders` ADD COLUMN `locationTimeWindows` TEXT")
                db.execSQL("ALTER TABLE `reminders` ADD COLUMN `wifiTimeWindows` TEXT")
                db.execSQL("ALTER TABLE `reminders` ADD COLUMN `autoSilenceMinutes` INTEGER NOT NULL DEFAULT 5")
                db.execSQL("ALTER TABLE `reminders` ADD COLUMN `useAlarmStream` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `reminders` ADD COLUMN `locationDate` TEXT")
                db.execSQL("ALTER TABLE `reminders` ADD COLUMN `locationRepeatDays` TEXT")
                db.execSQL("ALTER TABLE `reminders` ADD COLUMN `locationMonthlyDays` TEXT")
                db.execSQL("ALTER TABLE `reminders` ADD COLUMN `locationYearlyDates` TEXT")
                db.execSQL("ALTER TABLE `reminders` ADD COLUMN `wifiDate` TEXT")
                db.execSQL("ALTER TABLE `reminders` ADD COLUMN `wifiRepeatDays` TEXT")
                db.execSQL("ALTER TABLE `reminders` ADD COLUMN `wifiMonthlyDays` TEXT")
                db.execSQL("ALTER TABLE `reminders` ADD COLUMN `wifiYearlyDates` TEXT")
            }
        }
    }
}
