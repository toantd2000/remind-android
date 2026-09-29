package vn.io.litever.remind.core.domain.repository

import kotlinx.coroutines.flow.Flow
import vn.io.litever.remind.core.model.Reminder

/**
 * Domain repository contract for managing Reminder entities and their associated checklists.
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
     * Inserts a new reminder along with any checklist items. Returns the generated reminder ID.
     */
    suspend fun insertReminder(reminder: Reminder): Long

    /**
     * Updates an existing reminder and synchronizes its checklist items.
     */
    suspend fun updateReminder(reminder: Reminder)

    /**
     * Deletes a reminder and cascades deletion to all associated checklist items.
     */
    suspend fun deleteReminder(reminder: Reminder)

    /**
     * Directly updates a checklist item's completion status without rewriting the entire parent reminder.
     */
    suspend fun updateChecklistItemCompletion(itemId: Long, isCompleted: Boolean)

    /**
     * Observes enabled, non-completed reminders that have active background triggers (Location or Wi-Fi).
     */
    fun getActiveMonitoringReminders(): Flow<List<Reminder>>

    /**
     * Resets all checklist items of the specified reminder to uncompleted (isCompleted = false)
     * and updates the last checklist reset timestamp.
     */
    suspend fun resetChecklist(reminderId: Long, resetTime: Long = System.currentTimeMillis())

    /**
     * Checks all active recurring/interval reminders and resets their checklists if their cycle
     * (day/week/month/year) has passed.
     */
    suspend fun checkAndResetRecurringChecklists()
}
