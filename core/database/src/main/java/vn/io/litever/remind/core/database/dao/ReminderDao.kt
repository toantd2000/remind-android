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
import vn.io.litever.remind.core.database.model.PopulatedReminder

@Dao
interface ReminderDao {
    @Transaction
    @Query("SELECT * FROM reminders ORDER BY createdAt DESC")
    fun getAllReminders(): Flow<List<PopulatedReminder>>

    @Transaction
    @Query("SELECT * FROM reminders WHERE id = :id")
    fun getReminderFlow(id: Long): Flow<PopulatedReminder?>

    @Transaction
    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): PopulatedReminder?

    @Transaction
    @Query("SELECT * FROM reminders WHERE isEnabled = 1 AND isCompleted = 0 AND triggerType IN ('LOCATION', 'WIFI')")
    fun getActiveMonitoringReminders(): Flow<List<PopulatedReminder>>

    @Transaction
    @Query("SELECT * FROM reminders WHERE isEnabled = 1 AND isCompleted = 0 AND triggerType IN ('LOCATION', 'WIFI')")
    suspend fun getActiveMonitoringRemindersSync(): List<PopulatedReminder>

    @Transaction
    @Query("SELECT * FROM reminders WHERE isEnabled = 1 AND isCompleted = 0")
    fun getActiveReminders(): Flow<List<PopulatedReminder>>

    @Transaction
    @Query("SELECT * FROM reminders WHERE triggerType = :triggerType")
    fun getRemindersByTriggerType(triggerType: String): Flow<List<PopulatedReminder>>

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

    @Transaction
    suspend fun updateReminderWithChecklist(
        reminder: ReminderEntity,
        checklist: List<vn.io.litever.remind.core.database.entity.ChecklistItemEntity>,
        checklistItemDao: ChecklistItemDao
    ) {
        updateReminder(reminder)
        checklistItemDao.deleteChecklistItemsForReminder(reminder.id)
        if (checklist.isNotEmpty()) {
            checklistItemDao.insertChecklistItems(checklist)
        }
    }

    @Query("UPDATE reminders SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateReminderCompleted(id: Long, isCompleted: Boolean)

    @Query("UPDATE reminders SET currentSnoozeCount = :currentSnoozeCount, snoozeNextTriggerTime = :snoozeNextTriggerTime WHERE id = :id")
    suspend fun updateSnoozeState(id: Long, currentSnoozeCount: Int, snoozeNextTriggerTime: Long?)

    @Query("UPDATE reminders SET lastTriggeredTime = :lastTriggeredTime WHERE id = :id")
    suspend fun updateLastTriggeredTime(id: Long, lastTriggeredTime: Long)
}
