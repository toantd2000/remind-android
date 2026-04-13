package vn.io.litever.alarm.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import vn.io.litever.alarm.core.data.mapper.toEntity
import vn.io.litever.alarm.core.data.mapper.toModel
import vn.io.litever.remind.core.database.dao.ReminderDao
import vn.io.litever.alarm.core.domain.repository.AlarmRepository
import vn.io.litever.alarm.core.model.Alarm
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao
) : AlarmRepository {
    override fun getAllAlarms(): Flow<List<Alarm>> {
        return reminderDao.getAllReminders().map { entities ->
            entities.map { it.toModel() }
        }
    }

    override suspend fun getAlarmById(id: Long): Alarm? {
        return reminderDao.getReminderById(id)?.toModel()
    }

    override suspend fun insertAlarm(alarm: Alarm): Long {
        return reminderDao.insertReminder(alarm.toEntity())
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        reminderDao.updateReminder(alarm.toEntity())
    }

    override suspend fun deleteAlarm(alarm: Alarm) {
        reminderDao.deleteReminder(alarm.toEntity())
    }
}
