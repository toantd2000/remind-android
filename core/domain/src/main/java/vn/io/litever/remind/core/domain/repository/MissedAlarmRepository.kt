package vn.io.litever.remind.core.domain.repository

import kotlinx.coroutines.flow.Flow
import vn.io.litever.remind.core.model.MissedAlarm

interface MissedAlarmRepository {
    fun getAllMissedAlarms(): Flow<List<MissedAlarm>>
    suspend fun insertMissedAlarm(missedAlarm: MissedAlarm)
    suspend fun deleteAllMissedAlarms()
    suspend fun deleteMissedAlarm(id: Long)
}
