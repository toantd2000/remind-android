package vn.io.litever.remind.core.alarm.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import vn.io.litever.remind.core.alarm.AlarmRingManager
import vn.io.litever.remind.core.alarm.R
import vn.io.litever.remind.core.alarm.service.AlarmService
import javax.inject.Inject

/**
 * Handles Pro "Wake-up Check" flow:
 * 1) ACTION_WAKE_UP_CHECK: posts a high-priority confirmation notification & schedules a 60s timeout.
 * 2) ACTION_CONFIRM_AWAKE: user confirmed they are awake -> cancels notification and timeout.
 * 3) ACTION_WAKE_UP_CHECK_TIMEOUT: user did not confirm within 60s -> re-rings the alarm.
 */
@AndroidEntryPoint
class WakeUpCheckReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmRingManager: AlarmRingManager

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        if (alarmId == -1L) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        when (intent.action) {
            ACTION_WAKE_UP_CHECK -> {
                ensureChannel(context, notificationManager)

                val confirmIntent = Intent(context, WakeUpCheckReceiver::class.java).apply {
                    action = ACTION_CONFIRM_AWAKE
                    putExtra(EXTRA_ALARM_ID, alarmId)
                }
                val confirmPendingIntent = PendingIntent.getBroadcast(
                    context,
                    (alarmId.toInt() * 31) + 1,
                    confirmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_remind_notification)
                    .setContentTitle(context.getString(R.string.wakeup_check_title))
                    .setContentText(context.getString(R.string.wakeup_check_message))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setAutoCancel(true)
                    .addAction(
                        R.drawable.ic_remind_notification,
                        context.getString(R.string.wakeup_check_confirm),
                        confirmPendingIntent
                    )
                    .setContentIntent(confirmPendingIntent)
                    .build()

                notificationManager.notify(notificationIdFor(alarmId), notification)

                // Schedule 60s timeout to re-ring if user does not respond
                val timeoutIntent = Intent(context, WakeUpCheckReceiver::class.java).apply {
                    action = ACTION_WAKE_UP_CHECK_TIMEOUT
                    putExtra(EXTRA_ALARM_ID, alarmId)
                }
                val timeoutPendingIntent = PendingIntent.getBroadcast(
                    context,
                    (alarmId.toInt() * 31) + 2,
                    timeoutIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val triggerAt = System.currentTimeMillis() + 60_000L
                val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    alarmManager.canScheduleExactAlarms()
                } else true
                if (canExact) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, timeoutPendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, timeoutPendingIntent)
                }
            }

            ACTION_CONFIRM_AWAKE -> {
                notificationManager.cancel(notificationIdFor(alarmId))
                val timeoutIntent = Intent(context, WakeUpCheckReceiver::class.java).apply {
                    action = ACTION_WAKE_UP_CHECK_TIMEOUT
                    putExtra(EXTRA_ALARM_ID, alarmId)
                }
                val timeoutPendingIntent = PendingIntent.getBroadcast(
                    context,
                    (alarmId.toInt() * 31) + 2,
                    timeoutIntent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                if (timeoutPendingIntent != null) {
                    alarmManager.cancel(timeoutPendingIntent)
                }
            }

            ACTION_WAKE_UP_CHECK_TIMEOUT -> {
                notificationManager.cancel(notificationIdFor(alarmId))
                alarmRingManager.enqueueAlarm(alarmId)
                val serviceIntent = Intent(context, AlarmService::class.java).apply {
                    putExtra(vn.io.litever.remind.core.domain.scheduler.AlarmScheduler.EXTRA_ALARM_ID, alarmId)
                }
                context.startForegroundService(serviceIntent)
            }
        }
    }

    private fun ensureChannel(context: Context, manager: NotificationManager) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.wakeup_check_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)
    }

    private fun notificationIdFor(alarmId: Long): Int = 900_000 + alarmId.toInt()

    companion object {
        const val CHANNEL_ID = "alarm_wakeup_check_channel"
        const val EXTRA_ALARM_ID = "extra_wakeup_alarm_id"
        const val ACTION_WAKE_UP_CHECK = "vn.io.litever.remind.core.alarm.ACTION_WAKE_UP_CHECK"
        const val ACTION_CONFIRM_AWAKE = "vn.io.litever.remind.core.alarm.ACTION_CONFIRM_AWAKE"
        const val ACTION_WAKE_UP_CHECK_TIMEOUT = "vn.io.litever.remind.core.alarm.ACTION_WAKE_UP_CHECK_TIMEOUT"

        fun schedule(context: Context, alarmId: Long, delayMinutes: Int) {
            if (delayMinutes <= 0) return
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, WakeUpCheckReceiver::class.java).apply {
                action = ACTION_WAKE_UP_CHECK
                putExtra(EXTRA_ALARM_ID, alarmId)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId.toInt() * 31,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val triggerAt = System.currentTimeMillis() + delayMinutes * 60_000L
            val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else true
            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        }
    }
}
