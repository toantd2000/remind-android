package vn.io.litever.remind.core.domain.repository

import kotlinx.coroutines.flow.Flow
import vn.io.litever.remind.core.model.Alarm

interface AlarmRepository {
    fun getAllAlarms(): Flow<List<Alarm>>
    fun getAlarmFlow(id: Long): Flow<Alarm?>
    suspend fun getAlarmById(id: Long): Alarm?
    suspend fun insertAlarm(alarm: Alarm): Long
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun deleteAlarm(alarm: Alarm)
    suspend fun getMissionsForAlarm(alarmId: Long): List<vn.io.litever.remind.core.model.Mission>
}










