package vn.io.litever.remind.core.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.alarm.alert.ReminderAlertManager
import vn.io.litever.remind.core.alarm.notification.ReminderNotificationManager
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.RepeatMode
import vn.io.litever.remind.core.model.UrgencyLevel
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderRepository: ReminderRepository

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    @Inject
    lateinit var reminderNotificationManager: ReminderNotificationManager

    @Inject
    lateinit var reminderAlertManager: ReminderAlertManager

    @Inject
    lateinit var reminderActionHandler: vn.io.litever.remind.core.domain.handler.ReminderActionHandler

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        if (reminderId == -1L) return

        val pendingResult = try { goAsync() } catch (_: Exception) { null }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            try {
                val reminder = reminderRepository.getReminderById(reminderId)
                if (reminder == null || (!reminder.isEnabled && action != ACTION_REMINDER_DISMISS && action != ACTION_REMINDER_SNOOZE)) {
                    return@launch
                }

                when (action) {
                    ACTION_TRIGGER_REMINDER -> handleTriggerReminder(context, reminder, isSnooze = false)
                    ACTION_TRIGGER_REMINDER_SNOOZE -> handleTriggerReminder(context, reminder, isSnooze = true)
                    ACTION_REMINDER_SNOOZE -> {
                        val snoozeMinutes = if (intent.hasExtra(EXTRA_SNOOZE_MINUTES)) {
                            intent.getIntExtra(EXTRA_SNOOZE_MINUTES, 5)
                        } else null
                        handleSnoozeAction(reminder, snoozeMinutes)
                    }
                    ACTION_REMINDER_DISMISS -> handleDismissAction(reminder)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult?.finish()
            }
        }
    }

    private suspend fun handleTriggerReminder(context: Context, reminder: Reminder, isSnooze: Boolean) {
        val nowMillis = System.currentTimeMillis()
        val updatedReminder = if (!isSnooze) {
            reminder.copy(
                snoozeNextTriggerTime = null,
                lastTriggeredTime = nowMillis
            )
        } else {
            reminder.copy(snoozeNextTriggerTime = null)
        }

        if (updatedReminder.triggerType == ReminderTriggerType.FIXED_TIME) {
            val timeTrigger = updatedReminder.timeTrigger
            reminderRepository.updateReminder(updatedReminder)
            if (!isSnooze && timeTrigger != null && timeTrigger.isRecurring) {
                reminderScheduler.schedule(updatedReminder)
            }
        } else if (updatedReminder.triggerType == ReminderTriggerType.INTERVAL) {
            reminderRepository.updateReminder(updatedReminder)
            // For SOFT reminders (Fire & Forget), automatically schedule next occurrence
            if (!isSnooze && updatedReminder.action.urgencyLevel == UrgencyLevel.SOFT && updatedReminder.isEnabled) {
                reminderScheduler.schedule(updatedReminder)
            }
        } else {
            reminderRepository.updateReminder(updatedReminder)
        }

        reminderNotificationManager.showNotification(updatedReminder)
        reminderAlertManager.startAlert(updatedReminder)
    }

    private suspend fun handleSnoozeAction(reminder: Reminder, snoozeMinutes: Int? = null) {
        reminderAlertManager.stopAlert(reminder.id)
        reminderNotificationManager.cancelNotification(reminder.id)

        if (!reminder.canSnooze()) return

        val minutes = snoozeMinutes ?: reminder.action.snoozeIntervalMinutes
        reminderActionHandler.handleSnooze(reminder, minutes)
    }

    private suspend fun handleDismissAction(reminder: Reminder) {
        reminderAlertManager.stopAlert(reminder.id)
        reminderNotificationManager.cancelNotification(reminder.id)

        val isInterval = reminder.triggerType == ReminderTriggerType.INTERVAL
        if (isInterval) {
            // Acknowledging an INTERVAL reminder shifts the cycle:
            // next cycle = actual acknowledge time + intervalMinutes
            reminderActionHandler.handleComplete(reminder)
        } else {
            val now = LocalDateTime.now()
            val trigger = reminder.timeTrigger
            val hasNextExactOccurrence = trigger != null &&
                trigger.repeatMode == RepeatMode.NONE &&
                trigger.effectiveExactDates.isNotEmpty() &&
                trigger.getNextOccurrence(now = now, lastTriggeredTime = now, isInterval = false) != null
            val shouldStayEnabled = reminder.isRecurring || hasNextExactOccurrence
            val updatedReminder = reminder.copy(
                isEnabled = if (shouldStayEnabled) reminder.isEnabled else false,
                snoozeNextTriggerTime = null,
                lastTriggeredTime = if (shouldStayEnabled) null else reminder.lastTriggeredTime
            )
            reminderActionHandler.handleDismiss(updatedReminder)
        }
    }

    companion object {
        const val ACTION_TRIGGER_REMINDER = "vn.io.litever.remind.core.alarm.ACTION_TRIGGER_REMINDER"
        const val ACTION_TRIGGER_REMINDER_SNOOZE = "vn.io.litever.remind.core.alarm.ACTION_TRIGGER_REMINDER_SNOOZE"
        const val ACTION_REMINDER_SNOOZE = "vn.io.litever.remind.core.alarm.ACTION_REMINDER_SNOOZE"
        const val ACTION_REMINDER_DISMISS = "vn.io.litever.remind.core.alarm.ACTION_REMINDER_DISMISS"

        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
        const val EXTRA_IS_SNOOZE = "extra_is_snooze"
    }
}


