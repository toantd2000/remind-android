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
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReminderControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderRingManager: ReminderRingManager,
    private val reminderRepository: ReminderRepository
) : ReminderController {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun dismissReminder(reminderId: Long?) {
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

            withContext(Dispatchers.IO) {
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
        currentReminderId?.let { 
            reminderRingManager.dequeueReminder(it)
            notificationManager.cancel(it.toInt()) 
        }
    }

    override suspend fun snoozeReminder(reminderId: Long?) {
        val currentReminderId = reminderId ?: reminderRingManager.ringingReminderId.value
        if (currentReminderId != null) {
            val reminder = withContext(Dispatchers.IO) { reminderRepository.getReminderById(currentReminderId) }
            if (reminder != null && reminder.snoozeEnabled && reminder.currentSnoozeCount < reminder.snoozeRepeatCount) {
                val interval = if (reminder.snoozeInterval > 0) reminder.snoozeInterval else 5
                val triggerTime = System.currentTimeMillis() + interval * 60 * 1000L

                val updatedReminder = reminder.copy(
                    currentSnoozeCount = reminder.currentSnoozeCount + 1,
                    snoozeNextTriggerTime = triggerTime
                )
                withContext(Dispatchers.IO) { reminderRepository.updateReminder(updatedReminder) }
                
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

                val showIntent = Intent(Intent.ACTION_VIEW, Uri.parse("app://remind")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingShowIntent = PendingIntent.getActivity(
                    context,
                    0,
                    showIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, pendingShowIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            }
        }
        // NO OVERLAP RULE: Don't dequeue here. The alarm stays in queue during snooze
        // to prevent next alarm from ringing until this session is finished.
    }

    override suspend fun markAsMissed(reminderId: Long?) {
        val currentReminderId = reminderId ?: reminderRingManager.ringingReminderId.value
        if (currentReminderId != null) {
            val reminder = withContext(Dispatchers.IO) { reminderRepository.getReminderById(currentReminderId) }
            if (reminder != null) {
                // AUTO-SNOOZE LOGIC: If we haven't reached snooze limit, snooze instead of marking as missed
                if (reminder.snoozeEnabled && reminder.currentSnoozeCount < reminder.snoozeRepeatCount) {
                    snoozeReminder(currentReminderId)
                    return
                }

                withContext(Dispatchers.IO) {
                    val updatedReminder = reminder.copy(
                        isMissed = true,
                        snoozeNextTriggerTime = null
                    )
                    reminderRepository.updateReminder(updatedReminder)

                    // Deep link to message screen
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("app://remind/message/${reminder.id}")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        reminder.id.hashCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    // Show a silent notification
                    val notification = androidx.core.app.NotificationCompat.Builder(context, "reminder_channel")
                        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                        .setContentTitle(context.getString(vn.io.litever.remind.core.reminder.R.string.missed_alarm_title))
                        .setContentText(reminder.label.ifEmpty { context.getString(vn.io.litever.remind.core.reminder.R.string.missed_alarm_text) })
                        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build()
                    notificationManager.notify(currentReminderId.toInt(), notification)
                }
            }
        }
        currentReminderId?.let { 
            reminderRingManager.setAcknowledgingReminderId(it)
            reminderRingManager.dequeueReminder(it) 
        }
    }

    override suspend fun cancelSnooze(reminderId: Long) {
        // Cancel any pending snooze
        val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_TRIGGER_REMINDER
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_IS_SNOOZE, true)
        }
        val pendingSnooze = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            snoozeIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingSnooze != null) {
            alarmManager.cancel(pendingSnooze)
            pendingSnooze.cancel()
        }

        withContext(Dispatchers.IO) {
            val reminder = reminderRepository.getReminderById(reminderId)
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
}
