package vn.io.litever.remind.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import vn.io.litever.remind.core.database.dao.MissionDao
import vn.io.litever.remind.core.database.dao.PhraseDao
import vn.io.litever.remind.core.database.dao.ReminderDao
import vn.io.litever.remind.core.database.model.MissionEntity
import vn.io.litever.remind.core.database.model.PhraseEntity
import vn.io.litever.remind.core.database.model.ReminderEntity

@Database(
    entities = [
        ReminderEntity::class,
        MissionEntity::class,
        PhraseEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class RemindDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun missionDao(): MissionDao
    abstract fun phraseDao(): PhraseDao
}
