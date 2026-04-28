package vn.io.litever.remind.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import vn.io.litever.remind.core.data.mapper.toEntity
import vn.io.litever.remind.core.data.mapper.toModel
import vn.io.litever.remind.core.database.dao.MissedAlarmDao
import vn.io.litever.remind.core.domain.repository.MissedAlarmRepository
import vn.io.litever.remind.core.model.MissedAlarm
import javax.inject.Inject

class MissedAlarmRepositoryImpl @Inject constructor(
    private val missedAlarmDao: MissedAlarmDao
) : MissedAlarmRepository {
    override fun getAllMissedAlarms(): Flow<List<MissedAlarm>> {
        return missedAlarmDao.getAllMissedAlarms().map { entities ->
            entities.map { it.toModel() }
        }
    }

    override suspend fun insertMissedAlarm(missedAlarm: MissedAlarm) {
        missedAlarmDao.insertMissedAlarm(missedAlarm.toEntity())
    }

    override suspend fun deleteAllMissedAlarms() {
        missedAlarmDao.deleteAllMissedAlarms()
    }

    override suspend fun deleteMissedAlarm(id: Long) {
        missedAlarmDao.deleteMissedAlarm(id)
    }
}
