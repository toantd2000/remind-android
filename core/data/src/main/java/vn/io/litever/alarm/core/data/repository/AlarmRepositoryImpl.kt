package vn.io.litever.alarm.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import vn.io.litever.alarm.core.data.mapper.toEntity
import vn.io.litever.alarm.core.data.mapper.toModel
import vn.io.litever.alarm.core.database.dao.AlarmDao
import vn.io.litever.alarm.core.domain.repository.AlarmRepository
import vn.io.litever.alarm.core.model.Alarm
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    private val alarmDao: AlarmDao
) : AlarmRepository {
    override fun getAllAlarms(): Flow<List<Alarm>> {
        return alarmDao.getAllAlarms().map { entities ->
            entities.map { it.toModel() }
        }
    }

    override suspend fun getAlarmById(id: Long): Alarm? {
        return alarmDao.getAlarmById(id)?.toModel()
    }

    override suspend fun insertAlarm(alarm: Alarm): Long {
        return alarmDao.insertAlarm(alarm.toEntity())
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.updateAlarm(alarm.toEntity())
    }

    override suspend fun deleteAlarm(alarm: Alarm) {
        alarmDao.deleteAlarm(alarm.toEntity())
    }
}
