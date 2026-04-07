package vn.io.litever.alarm.core.domain.repository

import kotlinx.coroutines.flow.Flow
import vn.io.litever.alarm.core.model.Alarm

interface AlarmRepository {
    fun getAllAlarms(): Flow<List<Alarm>>
    suspend fun insertAlarm(alarm: Alarm)
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun deleteAlarm(alarm: Alarm)
}
