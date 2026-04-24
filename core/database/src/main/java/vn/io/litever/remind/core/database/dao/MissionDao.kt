package vn.io.litever.remind.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import vn.io.litever.remind.core.database.model.MissionEntity

@Dao
interface MissionDao {
    @Query("SELECT * FROM missions WHERE alarmId = :alarmId ORDER BY missionOrder ASC")
    fun getMissionsForAlarm(alarmId: Long): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions WHERE alarmId = :alarmId ORDER BY missionOrder ASC")
    suspend fun getMissionsForAlarmSync(alarmId: Long): List<MissionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: MissionEntity): Long

    @Delete
    suspend fun deleteMission(mission: MissionEntity)

    @Query("DELETE FROM missions WHERE alarmId = :alarmId")
    suspend fun deleteMissionsForAlarm(alarmId: Long)
}










