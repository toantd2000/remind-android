package vn.io.litever.remind.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import vn.io.litever.remind.core.database.dao.ReminderDao
import vn.io.litever.remind.core.database.model.ReminderEntity

@Database(entities = [ReminderEntity::class], version = 1, exportSchema = false)
abstract class RemindDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
}
