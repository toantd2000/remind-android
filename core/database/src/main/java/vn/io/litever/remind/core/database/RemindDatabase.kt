package vn.io.litever.remind.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import vn.io.litever.remind.core.database.dao.AlarmDao
import vn.io.litever.remind.core.database.dao.MissionDao
import vn.io.litever.remind.core.database.dao.PhraseDao
import vn.io.litever.remind.core.database.dao.MissedAlarmDao
import vn.io.litever.remind.core.database.model.AlarmEntity
import vn.io.litever.remind.core.database.model.MissionEntity
import vn.io.litever.remind.core.database.model.PhraseEntity
import vn.io.litever.remind.core.database.model.MissedAlarmEntity

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AlarmEntity::class,
        MissionEntity::class,
        PhraseEntity::class,
        MissedAlarmEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class RemindDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun missionDao(): MissionDao
    abstract fun phraseDao(): PhraseDao
    abstract fun missedAlarmDao(): MissedAlarmDao

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
    }
}
