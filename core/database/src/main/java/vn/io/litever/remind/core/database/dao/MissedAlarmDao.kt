package vn.io.litever.remind.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import vn.io.litever.remind.core.database.model.MissedAlarmEntity

@Dao
interface MissedAlarmDao {
    @Query("SELECT * FROM missed_alarms ORDER BY missedTime DESC")
    fun getAllMissedAlarms(): Flow<List<MissedAlarmEntity>>

    @Insert
    suspend fun insertMissedAlarm(missedAlarm: MissedAlarmEntity)

    @Query("DELETE FROM missed_alarms")
    suspend fun deleteAllMissedAlarms()

    @Query("DELETE FROM missed_alarms WHERE id = :id")
    suspend fun deleteMissedAlarm(id: Long)
}
