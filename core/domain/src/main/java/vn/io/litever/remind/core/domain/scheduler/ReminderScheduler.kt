package vn.io.litever.remind.core.domain.scheduler

import vn.io.litever.remind.core.model.Reminder

/**
 * Domain contract for scheduling and cancelling exact system alarms for Reminders and Snooze events.
 */
interface ReminderScheduler {

    /**
     * Schedules an exact alarm for a time-based reminder.
     * If the reminder is not time-based or has no valid future occurrence, this is a no-op.
     */
    fun schedule(reminder: Reminder)

    /**
     * Schedules an exact snooze alarm for any reminder type at the specified epoch millisecond timestamp.
     */
    fun scheduleSnooze(reminder: Reminder, triggerTime: Long)

    /**
     * Cancels any scheduled primary alarm for the given reminder.
     */
    fun cancel(reminder: Reminder)

    /**
     * Cancels any active snooze alarm for the given reminder.
     */
    fun cancelSnooze(reminder: Reminder)

    /**
     * Cancels all scheduled alarms (both primary and snooze) for the given reminder.
     */
    fun cancelAll(reminder: Reminder) {
        cancel(reminder)
        cancelSnooze(reminder)
    }

    companion object {
        const val ACTION_TRIGGER_REMINDER = "vn.io.litever.remind.core.alarm.ACTION_TRIGGER_REMINDER"
        const val ACTION_TRIGGER_REMINDER_SNOOZE = "vn.io.litever.remind.core.alarm.ACTION_TRIGGER_REMINDER_SNOOZE"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_IS_SNOOZE = "extra_is_snooze"
    }
}
