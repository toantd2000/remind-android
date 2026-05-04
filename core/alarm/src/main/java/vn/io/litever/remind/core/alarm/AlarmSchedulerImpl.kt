package vn.io.litever.remind.core.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.io.litever.remind.core.alarm.receiver.AlarmReceiver
import vn.io.litever.remind.core.domain.scheduler.AlarmScheduler
import vn.io.litever.remind.core.model.Alarm
import android.net.Uri
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class AlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(alarm: Alarm) {
        val triggerTime = alarm.getNextOccurrence()
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        setAlarmInternal(alarm, triggerTime, isSnooze = false)
    }

    override fun scheduleSnooze(alarm: Alarm, triggerTime: Long) {
        setAlarmInternal(alarm, triggerTime, isSnooze = true)
    }

    private fun setAlarmInternal(alarm: Alarm, triggerTime: Long, isSnooze: Boolean) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = if (isSnooze) AlarmScheduler.ACTION_TRIGGER_SNOOZE else AlarmScheduler.ACTION_TRIGGER_ALARM
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmScheduler.EXTRA_IS_SNOOZE, isSnooze)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getRequestCode(alarm.id, isSnooze),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                setAlarmClock(triggerTime, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            setAlarmClock(triggerTime, pendingIntent)
        }
    }

    private fun setAlarmClock(triggerTime: Long, pendingIntent: PendingIntent) {
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

    override fun cancel(alarm: Alarm) {
        cancelAlarmInternal(alarm, isSnooze = false)
    }

    override fun cancelSnooze(alarm: Alarm) {
        cancelAlarmInternal(alarm, isSnooze = true)
    }

    private fun cancelAlarmInternal(alarm: Alarm, isSnooze: Boolean) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = if (isSnooze) AlarmScheduler.ACTION_TRIGGER_SNOOZE else AlarmScheduler.ACTION_TRIGGER_ALARM
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmScheduler.EXTRA_IS_SNOOZE, isSnooze)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getRequestCode(alarm.id, isSnooze),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun getRequestCode(alarmId: Long, isSnooze: Boolean): Int {
        val idHash = alarmId.hashCode()
        return if (isSnooze) idHash + 1000000000 else idHash
    }
}










