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
                if (reminder == null || (!reminder.isEnabled && action != ACTION_REMINDER_DISMISS && action != ACTION_REMINDER_SNOOZE && action != ACTION_REMINDER_CHECKLIST_TOGGLE)) {
                    return@launch
                }

                when (action) {
                    ACTION_TRIGGER_REMINDER -> handleTriggerReminder(context, reminder, isSnooze = false)
                    ACTION_TRIGGER_REMINDER_SNOOZE -> handleTriggerReminder(context, reminder, isSnooze = true)
                    ACTION_REMINDER_SNOOZE -> handleSnoozeAction(reminder)
                    ACTION_REMINDER_DISMISS -> handleDismissAction(reminder)
                    ACTION_REMINDER_CHECKLIST_TOGGLE -> {
                        val itemId = intent.getLongExtra(EXTRA_CHECKLIST_ITEM_ID, -1L)
                        val isCompleted = intent.getBooleanExtra(EXTRA_CHECKLIST_IS_COMPLETED, false)
                        if (itemId != -1L) {
                            handleChecklistToggleAction(context, reminder, itemId, isCompleted)
                        }
                    }
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

        if (updatedReminder.triggerType == ReminderTriggerType.TIME) {
            val timeTrigger = updatedReminder.timeTrigger
            reminderRepository.updateReminder(updatedReminder)
            if (!isSnooze && timeTrigger != null && timeTrigger.isRecurring) {
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
        val snoozeIntervalMillis = reminder.snoozeIntervalMinutes * 60 * 1000L
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

        val isRecurring = reminder.isRecurring
        val completedReminder = reminder.copy(
            isCompleted = true,
            isEnabled = if (isRecurring) reminder.isEnabled else false,
            snoozeNextTriggerTime = null
        )
        reminderRepository.updateReminder(completedReminder)
        reminderScheduler.cancelSnooze(reminder)
    }

    private suspend fun handleChecklistToggleAction(
        context: Context,
        reminder: Reminder,
        itemId: Long,
        isCompleted: Boolean
    ) {
        reminderRepository.updateChecklistItemCompletion(itemId, isCompleted)
        val refreshed = reminderRepository.getReminderById(reminder.id)
        if (refreshed != null) {
            reminderNotificationManager.updateNotification(refreshed)
            fireReminderAlert(context, refreshed)
        }
    }

    private fun fireReminderAlert(context: Context, reminder: Reminder) {
        val alertIntent = Intent(ACTION_FIRE_REMINDER_ALERT).apply {
            setPackage(context.packageName)
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_NOTIFICATION_STYLE, reminder.notificationStyle.name)
            putExtra(EXTRA_IS_PERSISTENT, reminder.isPersistentAlert)
        }
        context.sendBroadcast(alertIntent)
    }

    companion object {
        const val ACTION_TRIGGER_REMINDER = "vn.io.litever.remind.core.alarm.ACTION_TRIGGER_REMINDER"
        const val ACTION_TRIGGER_REMINDER_SNOOZE = "vn.io.litever.remind.core.alarm.ACTION_TRIGGER_REMINDER_SNOOZE"
        const val ACTION_REMINDER_SNOOZE = "vn.io.litever.remind.core.alarm.ACTION_REMINDER_SNOOZE"
        const val ACTION_REMINDER_DISMISS = "vn.io.litever.remind.core.alarm.ACTION_REMINDER_DISMISS"
        const val ACTION_REMINDER_CHECKLIST_TOGGLE = "vn.io.litever.remind.core.alarm.ACTION_REMINDER_CHECKLIST_TOGGLE"
        const val ACTION_FIRE_REMINDER_ALERT = "vn.io.litever.remind.core.alarm.ACTION_FIRE_REMINDER_ALERT"

        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_IS_SNOOZE = "extra_is_snooze"
        const val EXTRA_CHECKLIST_ITEM_ID = "extra_checklist_item_id"
        const val EXTRA_CHECKLIST_IS_COMPLETED = "extra_checklist_is_completed"
        const val EXTRA_NOTIFICATION_STYLE = "extra_notification_style"
        const val EXTRA_IS_PERSISTENT = "extra_is_persistent"
    }
}
