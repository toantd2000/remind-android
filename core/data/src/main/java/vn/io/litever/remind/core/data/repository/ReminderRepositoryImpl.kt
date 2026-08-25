package vn.io.litever.remind.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import vn.io.litever.remind.core.data.mapper.toEntity
import vn.io.litever.remind.core.data.mapper.toModel
import vn.io.litever.remind.core.database.dao.ChecklistItemDao
import vn.io.litever.remind.core.database.dao.ReminderDao
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.model.Reminder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao,
    private val checklistItemDao: ChecklistItemDao
) : ReminderRepository {

    override fun getAllReminders(): Flow<List<Reminder>> {
        return reminderDao.getAllReminders().map { populatedList ->
            populatedList.map { it.toModel() }
        }
    }

    override fun getReminderFlow(id: Long): Flow<Reminder?> {
        return reminderDao.getReminderFlow(id).map { it?.toModel() }
    }

    override suspend fun getReminderById(id: Long): Reminder? {
        return reminderDao.getReminderById(id)?.toModel()
    }

    override suspend fun insertReminder(reminder: Reminder): Long {
        // 1. Insert parent entity to generate unique primary key
        val reminderEntity = reminder.toEntity()
        val reminderId = reminderDao.insertReminder(reminderEntity)

        // 2. Batch insert checklist items associated with the generated reminder ID
        if (reminder.checklist.isNotEmpty()) {
            val checklistEntities = reminder.checklist.mapIndexed { index, item ->
                item.copy(reminderId = reminderId, orderIndex = index)
                    .toEntity(parentReminderId = reminderId)
            }
            checklistItemDao.insertChecklistItems(checklistEntities)
        }

        return reminderId
    }

    override suspend fun updateReminder(reminder: Reminder) {
        val reminderEntity = reminder.toEntity()
        val checklistEntities = if (reminder.checklist.isNotEmpty()) {
            reminder.checklist.mapIndexed { index, item ->
                item.copy(reminderId = reminder.id, orderIndex = index)
                    .toEntity(parentReminderId = reminder.id)
            }
        } else {
            emptyList()
        }

        reminderDao.updateReminderWithChecklist(reminderEntity, checklistEntities, checklistItemDao)
    }

    override suspend fun deleteReminder(reminder: Reminder) {
        // Deleting the ReminderEntity triggers SQLite Foreign Key CASCADE
        // automatically deleting all associated checklist items
        reminderDao.deleteReminder(reminder.toEntity())
    }

    override suspend fun updateChecklistItemCompletion(itemId: Long, isCompleted: Boolean) {
        // Atomic fast-path update directly on the checklist item
        checklistItemDao.updateChecklistItemCompletion(itemId, isCompleted)
    }

    override fun getActiveMonitoringReminders(): Flow<List<Reminder>> {
        return reminderDao.getActiveMonitoringReminders().map { populatedList ->
            populatedList.map { it.toModel() }
        }
    }
}
