package vn.io.litever.remind.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import vn.io.litever.remind.core.database.entity.ChecklistItemEntity

@Dao
interface ChecklistItemDao {
    @Query("SELECT * FROM checklist_items WHERE reminderId = :reminderId ORDER BY orderIndex ASC")
    fun getChecklistItemsForReminder(reminderId: Long): Flow<List<ChecklistItemEntity>>

    @Query("SELECT * FROM checklist_items WHERE reminderId = :reminderId ORDER BY orderIndex ASC")
    suspend fun getChecklistItemsForReminderSync(reminderId: Long): List<ChecklistItemEntity>

    @Query("SELECT * FROM checklist_items WHERE id = :id")
    suspend fun getChecklistItemById(id: Long): ChecklistItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItem(item: ChecklistItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItems(items: List<ChecklistItemEntity>): List<Long>

    @Update
    suspend fun updateChecklistItem(item: ChecklistItemEntity)

    @Query("UPDATE checklist_items SET isCompleted = :isCompleted WHERE id = :itemId")
    suspend fun updateChecklistItemCompletion(itemId: Long, isCompleted: Boolean)

    @Query("UPDATE checklist_items SET isCompleted = 0 WHERE reminderId = :reminderId")
    suspend fun resetChecklistCompletionForReminder(reminderId: Long)

    @Delete
    suspend fun deleteChecklistItem(item: ChecklistItemEntity)

    @Query("DELETE FROM checklist_items WHERE reminderId = :reminderId")
    suspend fun deleteChecklistItemsForReminder(reminderId: Long)
}
