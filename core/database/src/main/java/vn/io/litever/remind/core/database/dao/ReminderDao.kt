package vn.io.litever.remind.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import vn.io.litever.remind.core.database.model.ReminderEntity
import vn.io.litever.remind.core.database.model.PopulatedReminder

@Dao
interface ReminderDao {
    @androidx.room.Transaction
    @Query("SELECT * FROM reminders")
    fun getAllReminders(): Flow<List<PopulatedReminder>>

    @androidx.room.Transaction
    @Query("SELECT * FROM reminders WHERE id = :id")
    fun getReminderFlow(id: Long): Flow<PopulatedReminder?>

    @androidx.room.Transaction
    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): PopulatedReminder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)
}
