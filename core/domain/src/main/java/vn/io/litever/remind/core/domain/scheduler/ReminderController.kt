package vn.io.litever.remind.core.domain.scheduler

interface ReminderController {
    suspend fun dismissReminder(reminderId: Long? = null)
    suspend fun snoozeReminder(reminderId: Long? = null)
    suspend fun markAsMissed(reminderId: Long? = null)
    suspend fun cancelSnooze(reminderId: Long)
}
