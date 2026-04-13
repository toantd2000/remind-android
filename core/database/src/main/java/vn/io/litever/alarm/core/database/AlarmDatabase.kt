package vn.io.litever.alarm.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import vn.io.litever.alarm.core.database.dao.AlarmDao
import vn.io.litever.alarm.core.database.model.AlarmEntity

@Database(entities = [AlarmEntity::class], version = 3, exportSchema = false)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
}
