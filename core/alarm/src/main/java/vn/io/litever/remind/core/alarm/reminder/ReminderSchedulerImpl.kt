package vn.io.litever.remind.core.alarm.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.io.litever.remind.core.alarm.receiver.ReminderReceiver
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ReminderScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(reminder: Reminder) {
        if (!reminder.isEnabled || reminder.isCompleted) return
        if (reminder.triggerType != ReminderTriggerType.TIME) return

        val timeTrigger = reminder.timeTrigger ?: return
        val nextOccurrence = timeTrigger.getNextOccurrence(LocalDateTime.now())
        val triggerTime = nextOccurrence
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        if (triggerTime <= System.currentTimeMillis()) return

        setAlarmInternal(reminder, triggerTime, isSnooze = false)
    }

    override fun scheduleSnooze(reminder: Reminder, triggerTime: Long) {
        if (!reminder.isEnabled || reminder.isCompleted) return
        if (triggerTime <= System.currentTimeMillis()) return

        setAlarmInternal(reminder, triggerTime, isSnooze = true)
    }

    override fun cancel(reminder: Reminder) {
        cancelAlarmInternal(reminder.id, isSnooze = false)
    }

    override fun cancelSnooze(reminder: Reminder) {
        cancelAlarmInternal(reminder.id, isSnooze = true)
    }

    private fun setAlarmInternal(reminder: Reminder, triggerTime: Long, isSnooze: Boolean) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = if (isSnooze) ReminderScheduler.ACTION_TRIGGER_REMINDER_SNOOZE else ReminderScheduler.ACTION_TRIGGER_REMINDER
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminder.id)
            putExtra(ReminderScheduler.EXTRA_IS_SNOOZE, isSnooze)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getRequestCode(reminder.id, isSnooze),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val requiresAlarmClock = reminder.notificationStyle == NotificationStyle.FULL_SCREEN || reminder.isPersistentAlert

        if (requiresAlarmClock) {
            setAlarmClock(reminder.id, triggerTime, pendingIntent)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
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

    private fun setAlarmClock(reminderId: Long, triggerTime: Long, pendingIntent: PendingIntent) {
        val showIntent = Intent(Intent.ACTION_VIEW, Uri.parse("app://remind/reminder/$reminderId")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingShowIntent = PendingIntent.getActivity(
            context,
            getRequestCode(reminderId, isSnooze = false),
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, pendingShowIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    private fun cancelAlarmInternal(reminderId: Long, isSnooze: Boolean) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = if (isSnooze) ReminderScheduler.ACTION_TRIGGER_REMINDER_SNOOZE else ReminderScheduler.ACTION_TRIGGER_REMINDER
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, reminderId)
            putExtra(ReminderScheduler.EXTRA_IS_SNOOZE, isSnooze)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getRequestCode(reminderId, isSnooze),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    /**
     * Namespace separation for broadcast request codes.
     */
    private fun getRequestCode(reminderId: Long, isSnooze: Boolean): Int {
        val baseHash = (reminderId.hashCode() and 0x0FFFFFFF)
        return if (isSnooze) {
            250000000 + baseHash
        } else {
            200000000 + baseHash
        }
    }
}
