package vn.io.litever.remind.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import vn.io.litever.remind.core.database.entity.ReminderEntity

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY createdAt DESC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    fun getReminderFlow(id: Long): Flow<ReminderEntity?>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 AND triggerType IN ('LOCATION', 'WIFI')")
    fun getActiveMonitoringReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 AND triggerType IN ('LOCATION', 'WIFI')")
    suspend fun getActiveMonitoringRemindersSync(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1")
    fun getActiveReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE triggerType = :triggerType")
    fun getRemindersByTriggerType(triggerType: String): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<ReminderEntity>): List<Long>

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    @Query("UPDATE reminders SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateReminderEnabled(id: Long, isEnabled: Boolean)

    @Query("UPDATE reminders SET currentSnoozeCount = :currentSnoozeCount, snoozeNextTriggerTime = :snoozeNextTriggerTime WHERE id = :id")
    suspend fun updateSnoozeState(id: Long, currentSnoozeCount: Int, snoozeNextTriggerTime: Long?)

    @Query("UPDATE reminders SET lastTriggeredTime = :lastTriggeredTime WHERE id = :id")
    suspend fun updateLastTriggeredTime(id: Long, lastTriggeredTime: Long)

    @Query("SELECT * FROM reminders WHERE isEnabled = 1")
    suspend fun getActiveRemindersSync(): List<ReminderEntity>
}
