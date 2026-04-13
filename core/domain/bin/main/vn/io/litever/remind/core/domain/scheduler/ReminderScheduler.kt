package vn.io.litever.remind.core.domain.scheduler

import vn.io.litever.remind.core.model.Reminder

interface ReminderScheduler {
    fun schedule(reminder: Reminder)
    fun cancel(reminder: Reminder)

    companion object {
        const val ACTION_TRIGGER_REMINDER = "vn.io.litever.remind.ACTION_TRIGGER_REMINDER"
        const val EXTRA_REMINDER_ID = "REMINDER_ID"
    }
}
