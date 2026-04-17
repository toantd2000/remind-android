package vn.io.litever.remind.core.reminder.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.io.litever.remind.core.reminder.ReminderRingManager
import vn.io.litever.remind.core.reminder.receiver.ReminderReceiver
import vn.io.litever.remind.core.domain.scheduler.ReminderController
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler.Companion.ACTION_TRIGGER_REMINDER
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler.Companion.EXTRA_REMINDER_ID
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler.Companion.EXTRA_IS_SNOOZE
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class ReminderControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderRingManager: ReminderRingManager,
    private val reminderRepository: ReminderRepository
) : ReminderController {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun dismissReminder(reminderId: Long?) {
        val currentReminderId = reminderId ?: reminderRingManager.ringingReminderId.value
        if (currentReminderId != null) {
            // Cancel any pending snooze
            val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
                action = ACTION_TRIGGER_REMINDER
                putExtra(EXTRA_REMINDER_ID, currentReminderId)
                putExtra(EXTRA_IS_SNOOZE, true)
            }
            val pendingSnooze = PendingIntent.getBroadcast(
                context,
                currentReminderId.hashCode(),
                snoozeIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingSnooze != null) {
                alarmManager.cancel(pendingSnooze)
                pendingSnooze.cancel()
            }

            runBlocking {
                val reminder = reminderRepository.getReminderById(currentReminderId)
                if (reminder != null) {
                    val updatedReminder = reminder.copy(
                        currentSnoozeCount = 0,
                        snoozeNextTriggerTime = null,
                        isMissed = false
                    )
                    reminderRepository.updateReminder(updatedReminder)
                }
            }
        }
        currentReminderId?.let { reminderRingManager.dequeueReminder(it) }
        // Also cancel missed notification if it exists (we use ID = reminderId)
        currentReminderId?.let { notificationManager.cancel(it.toInt()) }
    }

    override fun snoozeReminder(reminderId: Long?) {
        val currentReminderId = reminderId ?: reminderRingManager.ringingReminderId.value
        if (currentReminderId != null) {
            val reminder = runBlocking { reminderRepository.getReminderById(currentReminderId) }
            if (reminder != null && reminder.snoozeEnabled && reminder.currentSnoozeCount < reminder.snoozeRepeatCount) {
                val interval = if (reminder.snoozeInterval > 0) reminder.snoozeInterval else 5
                val triggerTime = System.currentTimeMillis() + interval * 60 * 1000L

                val updatedReminder = reminder.copy(
                    currentSnoozeCount = reminder.currentSnoozeCount + 1,
                    snoozeNextTriggerTime = triggerTime
                )
                runBlocking { reminderRepository.updateReminder(updatedReminder) }
                
                val intent = Intent(context, ReminderReceiver::class.java).apply {
                    action = ACTION_TRIGGER_REMINDER
                    putExtra(EXTRA_REMINDER_ID, currentReminderId)
                    putExtra(EXTRA_IS_SNOOZE, true)
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    currentReminderId.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            }
        }
        currentReminderId?.let { reminderRingManager.dequeueReminder(it) }
    }

    override fun markAsMissed(reminderId: Long?) {
        val currentReminderId = reminderId ?: reminderRingManager.ringingReminderId.value
        if (currentReminderId != null) {
            runBlocking {
                val reminder = reminderRepository.getReminderById(currentReminderId)
                if (reminder != null) {
                    val updatedReminder = reminder.copy(
                        isMissed = true,
                        snoozeNextTriggerTime = null
                    )
                    reminderRepository.updateReminder(updatedReminder)

                    // Show a silent notification
                    val notification = androidx.core.app.NotificationCompat.Builder(context, "reminder_channel")
                        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                        .setContentTitle("Missed Alarm")
                        .setContentText(reminder.label.ifEmpty { "You missed an alarm" })
                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .build()
                    notificationManager.notify(currentReminderId.toInt(), notification)
                }
            }
        }
        currentReminderId?.let { reminderRingManager.dequeueReminder(it) }
    }
}
