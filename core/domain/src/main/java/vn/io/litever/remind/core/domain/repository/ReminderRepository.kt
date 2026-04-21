package vn.io.litever.remind.core.domain.repository

import kotlinx.coroutines.flow.Flow
import vn.io.litever.remind.core.model.Reminder

interface ReminderRepository {
    fun getAllReminders(): Flow<List<Reminder>>
    fun getReminderFlow(id: Long): Flow<Reminder?>
    suspend fun getReminderById(id: Long): Reminder?
    suspend fun insertReminder(reminder: Reminder): Long
    suspend fun updateReminder(reminder: Reminder)
    suspend fun deleteReminder(reminder: Reminder)
    suspend fun getMissionsForReminder(reminderId: Long): List<vn.io.litever.remind.core.model.Mission>
}
