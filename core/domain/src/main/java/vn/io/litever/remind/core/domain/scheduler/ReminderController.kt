package vn.io.litever.remind.core.domain.scheduler

interface ReminderController {
    fun dismissReminder(reminderId: Long? = null)
    fun snoozeReminder(reminderId: Long? = null)
    fun markAsMissed(reminderId: Long? = null)
}
