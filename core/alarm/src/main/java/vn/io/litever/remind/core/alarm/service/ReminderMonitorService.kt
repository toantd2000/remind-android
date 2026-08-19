package vn.io.litever.remind.core.alarm.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vn.io.litever.remind.core.alarm.R
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import javax.inject.Inject

@AndroidEntryPoint
class ReminderMonitorService : Service() {

    @Inject
    lateinit var reminderRepository: ReminderRepository

    @Inject
    lateinit var locationTracker: LocationTracker

    @Inject
    lateinit var wifiTracker: WifiTracker

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var activeReminderCount = 0

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startFlowCollection()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildForegroundNotification(activeReminderCount)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startFlowCollection() {
        scope.launch {
            reminderRepository.getActiveMonitoringReminders()
                .distinctUntilChanged()
                .collect { reminders ->
                    withContext(Dispatchers.Main) {
                        handleRemindersChanged(reminders)
                    }
                }
        }
    }

    private fun handleRemindersChanged(reminders: List<Reminder>) {
        activeReminderCount = reminders.size
        if (reminders.isEmpty()) {
            locationTracker.stopTracking()
            wifiTracker.stopTracking()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        val notification = buildForegroundNotification(reminders.size)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)

        val locationReminders = reminders.filter { it.triggerType == ReminderTriggerType.LOCATION }
        val wifiReminders = reminders.filter { it.triggerType == ReminderTriggerType.WIFI }

        if (locationReminders.isNotEmpty()) {
            locationTracker.startTracking(locationReminders)
        } else {
            locationTracker.stopTracking()
        }

        if (wifiReminders.isNotEmpty()) {
            wifiTracker.startTracking(wifiReminders)
        } else {
            wifiTracker.stopTracking()
        }
    }

    override fun onDestroy() {
        locationTracker.stopTracking()
        wifiTracker.stopTracking()
        scope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.reminder_channel_name_monitor),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.reminder_channel_desc_monitor)
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(activeCount: Int): Notification {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            ?: Intent(Intent.ACTION_VIEW, Uri.parse("app://remind"))
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (activeCount > 0) {
            "Active monitoring: $activeCount reminder${if (activeCount > 1) "s" else ""}"
        } else {
            getString(R.string.reminder_monitor_text)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_remind_notification)
            .setContentTitle(getString(R.string.reminder_monitor_title))
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()

        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT
        return notification
    }

    companion object {
        const val NOTIFICATION_ID = 2001
        const val CHANNEL_ID = "reminder_monitoring_channel"
        const val ACTION_START = "vn.io.litever.remind.action.START_MONITORING"
        const val ACTION_SYNC = "vn.io.litever.remind.action.SYNC_MONITORING"

        fun start(context: Context) {
            val intent = Intent(context, ReminderMonitorService::class.java).apply {
                action = ACTION_START
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun sync(context: Context) {
            val intent = Intent(context, ReminderMonitorService::class.java).apply {
                action = ACTION_SYNC
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, ReminderMonitorService::class.java)
            context.stopService(intent)
        }
    }
}
