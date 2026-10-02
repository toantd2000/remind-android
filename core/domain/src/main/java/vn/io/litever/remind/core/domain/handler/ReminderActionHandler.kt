package vn.io.litever.remind.core.domain.handler

import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class ReminderActionHandler @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler
) {

    suspend fun handleSnooze(reminder: Reminder, snoozeDurationMinutes: Int) {
        val now = LocalDateTime.now()
        val requestedSnoozeTime = now.plusMinutes(snoozeDurationMinutes.toLong())

        val nextOccurrence = reminder.timeTrigger?.getNextOccurrence(
            now = now,
            lastTriggeredTime = reminder.lastTriggeredTime?.let {
                LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(it), ZoneId.systemDefault())
            },
            isInterval = reminder.triggerType == ReminderTriggerType.INTERVAL
        )

        val actualSnoozeTime = if (nextOccurrence != null && requestedSnoozeTime.isAfter(nextOccurrence)) {
            nextOccurrence
        } else {
            requestedSnoozeTime
        }

        val actualSnoozeTimeMillis = actualSnoozeTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val updatedReminder = reminder.copy(snoozeNextTriggerTime = actualSnoozeTimeMillis)
        reminderRepository.updateReminder(updatedReminder)
        reminderScheduler.scheduleSnooze(updatedReminder, actualSnoozeTimeMillis)
    }

    suspend fun handleComplete(reminder: Reminder) {
        val now = LocalDateTime.now()
        val nowMillis = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val updatedReminder = reminder.copy(
            lastTriggeredTime = nowMillis,
            snoozeNextTriggerTime = null // Clear snooze on complete
        )

        reminderRepository.updateReminder(updatedReminder)
        
        // Cancel any existing snooze
        reminderScheduler.cancelSnooze(reminder)
        
        // Re-schedule based on the new lastTriggeredTime
        reminderScheduler.schedule(updatedReminder)
    }

    suspend fun handleDismiss(reminder: Reminder) {
        val updatedReminder = reminder.copy(snoozeNextTriggerTime = null)
        reminderRepository.updateReminder(updatedReminder)
        reminderScheduler.cancelSnooze(updatedReminder)
        // If we want to reschedule for the next time, schedule will handle it based on current time
        reminderScheduler.schedule(updatedReminder)
    }
}
