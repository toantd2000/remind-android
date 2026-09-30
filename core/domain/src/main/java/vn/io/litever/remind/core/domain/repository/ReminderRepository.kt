package vn.io.litever.remind.core.domain.repository

import kotlinx.coroutines.flow.Flow
import vn.io.litever.remind.core.model.Reminder

/**
 * Domain repository contract for managing Reminder entities.
 */
interface ReminderRepository {

    /**
     * Observes the complete list of reminders ordered by creation date descending.
     */
    fun getAllReminders(): Flow<List<Reminder>>

    /**
     * Observes a single reminder by its unique identifier. Emits null if not found.
     */
    fun getReminderFlow(id: Long): Flow<Reminder?>

    /**
     * Retrieves a single reminder snapshot by ID.
     */
    suspend fun getReminderById(id: Long): Reminder?

    /**
     * Inserts a new reminder. Returns the generated reminder ID.
     */
    suspend fun insertReminder(reminder: Reminder): Long

    /**
     * Updates an existing reminder.
     */
    suspend fun updateReminder(reminder: Reminder)

    /**
     * Deletes a reminder.
     */
    suspend fun deleteReminder(reminder: Reminder)

    /**
     * Observes enabled, non-completed reminders that have active background triggers (Location or Wi-Fi).
     */
    fun getActiveMonitoringReminders(): Flow<List<Reminder>>
}
