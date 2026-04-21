package vn.io.litever.remind.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import vn.io.litever.remind.core.database.model.PhraseEntity

@Dao
interface PhraseDao {
    @Query("SELECT * FROM phrases WHERE id IN (:ids)")
    suspend fun getPhrasesByIds(ids: List<Long>): List<PhraseEntity>

    @Query("SELECT * FROM phrases WHERE (isShared = 1 AND isCustom = 1) OR (reminderId = :reminderId)")
    fun getCustomPhrasesForReminder(reminderId: Long): Flow<List<PhraseEntity>>

    @Query("SELECT * FROM phrases WHERE isShared = 1 AND isCustom = 1")
    fun getAllSharedCustomPhrases(): Flow<List<PhraseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrase(phrase: PhraseEntity): Long

    @Delete
    suspend fun deletePhrase(phrase: PhraseEntity)

    @Query("DELETE FROM phrases WHERE reminderId = :reminderId")
    suspend fun deletePhrasesForReminder(reminderId: Long)
}
