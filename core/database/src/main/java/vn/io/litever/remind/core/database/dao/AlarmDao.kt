package vn.io.litever.remind.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import vn.io.litever.remind.core.database.model.AlarmEntity
import vn.io.litever.remind.core.database.model.PopulatedAlarm

@Dao
interface AlarmDao {
    @androidx.room.Transaction
    @Query("SELECT * FROM alarms")
    fun getAllAlarms(): Flow<List<PopulatedAlarm>>

    @androidx.room.Transaction
    @Query("SELECT * FROM alarms WHERE id = :id")
    fun getAlarmFlow(id: Long): Flow<PopulatedAlarm?>

    @androidx.room.Transaction
    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Long): PopulatedAlarm?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)
}










