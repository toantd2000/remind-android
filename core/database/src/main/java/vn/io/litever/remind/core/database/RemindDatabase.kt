package vn.io.litever.remind.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import vn.io.litever.remind.core.database.dao.AlarmDao
import vn.io.litever.remind.core.database.dao.MissionDao
import vn.io.litever.remind.core.database.dao.PhraseDao
import vn.io.litever.remind.core.database.model.AlarmEntity
import vn.io.litever.remind.core.database.model.MissionEntity
import vn.io.litever.remind.core.database.model.PhraseEntity

@Database(
    entities = [
        AlarmEntity::class,
        MissionEntity::class,
        PhraseEntity::class
    ],
    version = 1, // Initial release version
    exportSchema = false
)
abstract class RemindDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun missionDao(): MissionDao
    abstract fun phraseDao(): PhraseDao
}
