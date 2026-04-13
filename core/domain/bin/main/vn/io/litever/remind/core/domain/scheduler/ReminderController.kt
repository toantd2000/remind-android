package vn.io.litever.remind.core.domain.scheduler

interface ReminderController {
    fun dismissReminder()
    fun snoozeReminder()
}
