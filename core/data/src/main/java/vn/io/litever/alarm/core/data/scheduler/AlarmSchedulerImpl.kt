package vn.io.litever.alarm.core.data.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.io.litever.alarm.core.domain.scheduler.AlarmScheduler
import vn.io.litever.alarm.core.model.Alarm
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class AlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(alarm: Alarm) {
        val intent = Intent(ACTION_TRIGGER_ALARM).apply {
            setPackage(context.packageName)
            putExtra(EXTRA_ALARM_ID, alarm.id)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = calculateTriggerTime(alarm)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                setExactAlarm(triggerTime, pendingIntent)
            }
        } else {
            setExactAlarm(triggerTime, pendingIntent)
        }
    }

    private fun setExactAlarm(triggerTime: Long, pendingIntent: PendingIntent) {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    override fun cancel(alarm: Alarm) {
        val intent = Intent(ACTION_TRIGGER_ALARM).apply {
            setPackage(context.packageName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun calculateTriggerTime(alarm: Alarm): Long {
        val now = LocalDateTime.now()
        var alarmTime = now.withHour(alarm.time.hour).withMinute(alarm.time.minute).withSecond(0).withNano(0)

        // Nếu giờ hiện tại đã qua mất giờ báo thức của ngày hôm nay, lập lịch cho ngày mai
        if (alarmTime.isBefore(now) || alarmTime.isEqual(now)) {
            alarmTime = alarmTime.plusDays(1)
        }

        // TODO: Xử lý logic vòng lặp theo repeatDays (DayOfWeek)
        // Hiện tại chỉ đánh thức lần gần nhất

        return alarmTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000
    }

    companion object {
        const val ACTION_TRIGGER_ALARM = "vn.io.litever.alarm.ACTION_TRIGGER_ALARM"
        const val EXTRA_ALARM_ID = "ALARM_ID"
    }
}
