package vn.io.litever.remind.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import vn.io.litever.remind.core.data.mapper.toEntity
import vn.io.litever.remind.core.data.mapper.toModel
import vn.io.litever.remind.core.database.dao.AlarmDao
import vn.io.litever.remind.core.database.dao.MissionDao
import vn.io.litever.remind.core.domain.repository.AlarmRepository
import vn.io.litever.remind.core.model.Alarm
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    private val alarmDao: AlarmDao,
    private val missionDao: MissionDao
) : AlarmRepository {
    override fun getAllAlarms(): Flow<List<Alarm>> {
        return alarmDao.getAllAlarms().map { entities ->
            entities.map { it.toModel() }
        }
    }

    override fun getAlarmFlow(id: Long): Flow<Alarm?> {
        return alarmDao.getAlarmFlow(id).map { it?.toModel() }
    }

    override suspend fun getAlarmById(id: Long): Alarm? {
        return alarmDao.getAlarmById(id)?.toModel()
    }

    override suspend fun getMissionsForAlarm(alarmId: Long): List<vn.io.litever.remind.core.model.Mission> {
        return missionDao.getMissionsForAlarmSync(alarmId).map { it.toModel() }
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










