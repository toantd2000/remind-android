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
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.UrgencyLevel
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
                    ACTION_REMINDER_SNOOZE -> handleSnoozeAction(reminder)
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
                currentSnoozeCount = 0,
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
        fireReminderAlert(context, updatedReminder)
    }

    private suspend fun handleSnoozeAction(reminder: Reminder) {
        reminderAlertManager.stopAlert(reminder.id)
        reminderNotificationManager.cancelNotification(reminder.id)

        if (!reminder.canSnooze()) return

        val nextSnoozeCount = reminder.currentSnoozeCount + 1
        val snoozeIntervalMillis = reminder.action.snoozeIntervalMinutes * 60 * 1000L
        val nextTriggerTime = System.currentTimeMillis() + snoozeIntervalMillis

        val snoozedReminder = reminder.copy(
            currentSnoozeCount = nextSnoozeCount,
            snoozeNextTriggerTime = nextTriggerTime
        )

        reminderRepository.updateReminder(snoozedReminder)
        reminderScheduler.scheduleSnooze(snoozedReminder, nextTriggerTime)
    }

    private suspend fun handleDismissAction(reminder: Reminder) {
        reminderAlertManager.stopAlert(reminder.id)
        reminderNotificationManager.cancelNotification(reminder.id)

        val isInterval = reminder.triggerType == ReminderTriggerType.INTERVAL
        if (isInterval) {
            // Acknowledging an INTERVAL reminder shifts the cycle:
            // next cycle = actual acknowledge time + intervalMinutes
            val nowMillis = System.currentTimeMillis()
            val shiftedReminder = reminder.copy(
                isCompleted = false,
                currentSnoozeCount = 0,
                snoozeNextTriggerTime = null,
                lastTriggeredTime = nowMillis
            )
            reminderRepository.updateReminder(shiftedReminder)
            reminderScheduler.cancelSnooze(reminder)
            if (shiftedReminder.isEnabled) {
                reminderScheduler.schedule(shiftedReminder)
            }
        } else {
            val isRecurring = reminder.isRecurring
            val completedReminder = reminder.copy(
                isCompleted = true,
                isEnabled = if (isRecurring) reminder.isEnabled else false,
                snoozeNextTriggerTime = null
            )
            reminderRepository.updateReminder(completedReminder)
            reminderScheduler.cancelSnooze(reminder)
        }
    }

    private fun fireReminderAlert(context: Context, reminder: Reminder) {
        val alertIntent = Intent(ACTION_FIRE_REMINDER_ALERT).apply {
            setPackage(context.packageName)
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_NOTIFICATION_STYLE, reminder.action.notificationStyle.name)
            putExtra(EXTRA_IS_PERSISTENT, reminder.action.isPersistentAlert)
        }
        context.sendBroadcast(alertIntent)
    }

    companion object {
        const val ACTION_TRIGGER_REMINDER = "vn.io.litever.remind.core.alarm.ACTION_TRIGGER_REMINDER"
        const val ACTION_TRIGGER_REMINDER_SNOOZE = "vn.io.litever.remind.core.alarm.ACTION_TRIGGER_REMINDER_SNOOZE"
        const val ACTION_REMINDER_SNOOZE = "vn.io.litever.remind.core.alarm.ACTION_REMINDER_SNOOZE"
        const val ACTION_REMINDER_DISMISS = "vn.io.litever.remind.core.alarm.ACTION_REMINDER_DISMISS"
        const val ACTION_FIRE_REMINDER_ALERT = "vn.io.litever.remind.core.alarm.ACTION_FIRE_REMINDER_ALERT"

        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_IS_SNOOZE = "extra_is_snooze"
        const val EXTRA_NOTIFICATION_STYLE = "extra_notification_style"
        const val EXTRA_IS_PERSISTENT = "extra_is_persistent"
    }
}
