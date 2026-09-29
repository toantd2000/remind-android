package vn.io.litever.remind.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import vn.io.litever.remind.core.data.mapper.*

import vn.io.litever.remind.core.database.dao.ChecklistItemDao
import vn.io.litever.remind.core.database.dao.ReminderDao
import vn.io.litever.remind.core.database.entity.ChecklistItemEntity
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
        // Always use id = 0L for newly created reminder items so Room auto-generates primary keys
        if (reminder.checklist.isNotEmpty()) {
            val checklistEntities = reminder.checklist.mapIndexed { index, item ->
                ChecklistItemEntity(
                    id = 0L,
                    reminderId = reminderId,
                    text = item.text,
                    isCompleted = item.isCompleted,
                    orderIndex = index
                )
            }
            checklistItemDao.insertChecklistItems(checklistEntities)
        }

        return reminderId
    }

    override suspend fun updateReminder(reminder: Reminder) {
        val reminderEntity = reminder.toEntity()
        val checklistEntities = if (reminder.checklist.isNotEmpty()) {
            reminder.checklist.mapIndexed { index, item ->
                ChecklistItemEntity(
                    id = if (item.id <= 0L || item.id > 1000000000000L) 0L else item.id,
                    reminderId = reminder.id,
                    text = item.text,
                    isCompleted = item.isCompleted,
                    orderIndex = index
                )
            }
        } else {
            emptyList<ChecklistItemEntity>()
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

    override suspend fun resetChecklist(reminderId: Long, resetTime: Long) {
        checklistItemDao.resetChecklistCompletionForReminder(reminderId)
        reminderDao.updateLastChecklistResetTime(reminderId, resetTime)
    }

    override suspend fun checkAndResetRecurringChecklists() {
        val activeReminders = reminderDao.getActiveRemindersSync()
        val now = java.time.LocalDateTime.now()
        for (populated in activeReminders) {
            val reminder = populated.toModel()
            if (vn.io.litever.remind.core.domain.util.ChecklistCycleUtil.shouldResetChecklist(reminder, now)) {
                resetChecklist(reminder.id, System.currentTimeMillis())
            }
        }
    }
}
