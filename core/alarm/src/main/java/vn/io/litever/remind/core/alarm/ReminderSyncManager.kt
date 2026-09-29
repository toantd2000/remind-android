package vn.io.litever.remind.core.alarm

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import vn.io.litever.remind.core.alarm.alert.ReminderAlertManager
import vn.io.litever.remind.core.alarm.service.ReminderMonitorService
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.model.ReminderTriggerType
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler,
    private val reminderAlertManager: ReminderAlertManager? = null
) {

    suspend fun sync() {
        try {
            reminderRepository.checkAndResetRecurringChecklists()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val nowMillis = System.currentTimeMillis()
        val reminders = reminderRepository.getAllReminders().first()

        for (reminder in reminders) {
            if (!reminder.isEnabled || reminder.isCompleted) continue

            when (reminder.triggerType) {
                ReminderTriggerType.FIXED_TIME, ReminderTriggerType.INTERVAL -> {
                    val snoozeTime = reminder.snoozeNextTriggerTime
                    if (snoozeTime != null) {
                        if (snoozeTime > nowMillis) {
                            reminderScheduler.scheduleSnooze(reminder, snoozeTime)
                        } else {
                            val intent = Intent(context, vn.io.litever.remind.core.alarm.receiver.ReminderReceiver::class.java).apply {
                                action = ReminderScheduler.ACTION_TRIGGER_REMINDER_SNOOZE
                                putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminder.id)
                                putExtra(ReminderScheduler.EXTRA_IS_SNOOZE, true)
                            }
                            context.sendBroadcast(intent)
                        }
                    } else {
                        val timeTrigger = reminder.timeTrigger
                        if (timeTrigger != null) {
                            val lastTriggeredLdt = reminder.lastTriggeredTime?.let {
                                java.time.Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
                            }
                            val nextOccurrence = timeTrigger.getNextOccurrence(
                                now = LocalDateTime.now(),
                                lastTriggeredTime = lastTriggeredLdt,
                                isInterval = reminder.triggerType == ReminderTriggerType.INTERVAL
                            )
                            if (nextOccurrence != null) {
                                val nextEpoch = nextOccurrence.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                if (timeTrigger.isRecurring) {
                                    reminderScheduler.schedule(reminder)
                                    if (reminder.lastTriggeredTime != null) {
                                        reminderAlertManager?.startAlert(reminder)
                                    }
                                } else {
                                    if (nextEpoch > nowMillis) {
                                        reminderScheduler.schedule(reminder)
                                    } else {
                                        if (reminder.lastTriggeredTime != null) {
                                            reminderAlertManager?.startAlert(reminder)
                                        } else {
                                            val intent = Intent(context, vn.io.litever.remind.core.alarm.receiver.ReminderReceiver::class.java).apply {
                                                action = ReminderScheduler.ACTION_TRIGGER_REMINDER
                                                putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminder.id)
                                            }
                                            context.sendBroadcast(intent)
                                        }
                                    }
                                }
                            } else {
                                if (reminder.lastTriggeredTime != null) {
                                    reminderAlertManager?.startAlert(reminder)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        try {
            ReminderMonitorService.stop(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
