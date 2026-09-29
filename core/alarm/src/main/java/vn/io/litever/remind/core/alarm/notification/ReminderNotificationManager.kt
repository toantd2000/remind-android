package vn.io.litever.remind.core.alarm.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.io.litever.remind.core.alarm.R
import vn.io.litever.remind.core.alarm.receiver.ReminderReceiver
import vn.io.litever.remind.core.common.util.PermissionChecker
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionChecker: PermissionChecker
) {
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    /**
     * Creates and registers standard and urgent notification channels.
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            // Standard Reminder Channel
            val standardChannel = NotificationChannel(
                CHANNEL_ID_STANDARD,
                context.getString(R.string.reminder_channel_name_standard),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.reminder_channel_desc_standard)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
                setSound(defaultSoundUri, audioAttributes)
                setShowBadge(true)
                enableLights(true)
            }

            // Urgent / Alarm Style Channel
            val urgentChannel = NotificationChannel(
                CHANNEL_ID_URGENT,
                context.getString(R.string.reminder_channel_name_urgent),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.reminder_channel_desc_urgent)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                setSound(defaultSoundUri, audioAttributes)
                setShowBadge(true)
                enableLights(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setBypassDnd(true)
                }
            }

            notificationManager.createNotificationChannel(standardChannel)
            notificationManager.createNotificationChannel(urgentChannel)
        }
    }

    /**
     * Builds and displays the notification for the given reminder.
     */
    fun showNotification(reminder: Reminder) {
        if (!permissionChecker.hasNotificationPermission()) return

        val notification = buildNotification(reminder)
        val notificationId = getNotificationId(reminder.id)
        notificationManager.notify(notificationId, notification)
    }

    /**
     * Alias for showNotification for consistent API naming across callers.
     */
    fun showReminderNotification(reminder: Reminder) {
        showNotification(reminder)
    }

    /**
     * Updates an active notification (e.g. after a checklist toggle) without re-alerting.
     */
    fun updateNotification(reminder: Reminder) {
        showNotification(reminder)
    }

    /**
     * Cancels the active notification for the given reminder ID.
     */
    fun cancelNotification(reminderId: Long) {
        val notificationId = getNotificationId(reminderId)
        notificationManager.cancel(notificationId)
    }

    /**
     * Alias for cancelNotification for consistent API naming across callers.
     */
    fun cancelReminderNotification(reminderId: Long) {
        cancelNotification(reminderId)
    }

    /**
     * Cancels all notifications managed by this application.
     */
    fun cancelAll() {
        notificationManager.cancelAll()
    }

    /**
     * Builds a Notification object for the given Reminder model.
     */
    fun buildNotification(reminder: Reminder): Notification {
        val isAlarmRinging = reminder.action.notificationStyle == NotificationStyle.FULL_SCREEN
        val canUseFsi = permissionChecker.hasFullScreenIntentPermission()

        val channelId = if (isAlarmRinging || reminder.action.isPersistentAlert) {
            CHANNEL_ID_URGENT
        } else {
            CHANNEL_ID_STANDARD
        }

        val title = reminder.title.ifBlank {
            context.getString(R.string.reminder_default_title)
        }

        val contentText = formatContentText(reminder)
        val bigTextStyle = formatBigTextStyle(reminder, title)

        // Content intent (tap notification body to view ringing screen)
        val contentPendingIntent = createContentPendingIntent(reminder.id)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_remind_notification)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(bigTextStyle)
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setAutoCancel(!isAlarmRinging && !reminder.action.isPersistentAlert)

        // Sound configuration:
        // For Standard mode: uses system default notification sound (1-shot)
        // For Alarm Ringing mode: custom ringtone if provided
        if (isAlarmRinging && !reminder.action.ringtoneUri.isNullOrBlank()) {
            try {
                builder.setSound(Uri.parse(reminder.action.ringtoneUri))
            } catch (_: Exception) {}
        } else if (!isAlarmRinging) {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            builder.setSound(defaultSoundUri)
        }

        if (!reminder.action.vibrationEnabled) {
            builder.setVibrate(longArrayOf(0))
        }

        // Configure Full-Screen Intent or Heads-up for ALL Reminders
        if (canUseFsi) {
            val fsiPendingIntent = createFullScreenPendingIntent(reminder.id)
            builder.setPriority(NotificationCompat.PRIORITY_MAX)
            builder.setCategory(if (isAlarmRinging) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)
            builder.setFullScreenIntent(fsiPendingIntent, true)
            if (isAlarmRinging && reminder.action.isPersistentAlert) {
                builder.setOngoing(true)
            }
        } else {
            // Android 14+ Fallback: High priority heads-up without FSI
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
            builder.setCategory(if (isAlarmRinging) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)
            if (isAlarmRinging && reminder.action.isPersistentAlert) {
                builder.setOngoing(true)
            }
        }

        // Action: Snooze (omitted if snooze disabled or snooze limit reached)
        if (reminder.action.snoozeEnabled && reminder.canSnooze()) {
            val snoozePendingIntent = createSnoozePendingIntent(reminder.id)
            val snoozeLabel = context.getString(
                R.string.reminder_action_snooze_interval,
                reminder.action.snoozeIntervalMinutes
            )
            builder.addAction(0, snoozeLabel, snoozePendingIntent)
        }

        // Action: Dismiss
        val dismissPendingIntent = createDismissPendingIntent(reminder.id)
        val dismissLabel = context.getString(R.string.reminder_action_dismiss)
        builder.addAction(0, dismissLabel, dismissPendingIntent)

        val notification = builder.build()
        if (isAlarmRinging && reminder.action.isPersistentAlert) {
            notification.flags = notification.flags or Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT
        }
        return notification
    }

    fun formatContentText(reminder: Reminder): String {
        val hasNotes = reminder.notes.isNotBlank()
        val hasChecklist = reminder.checklist.isNotEmpty()

        if (hasNotes && hasChecklist) {
            val nextItem = reminder.checklist.sortedBy { it.orderIndex }.firstOrNull { !it.isCompleted }
            val nextItemSnippet = nextItem?.let { " - ${it.text}" } ?: ""
            return "${reminder.notes} [${reminder.completedChecklistCount}/${reminder.totalChecklistCount} done]$nextItemSnippet"
        }

        if (hasChecklist) {
            val nextItem = reminder.checklist.sortedBy { it.orderIndex }.firstOrNull { !it.isCompleted }
            val nextItemSnippet = nextItem?.let { " - ${it.text}" } ?: ""
            return "[${reminder.completedChecklistCount}/${reminder.totalChecklistCount} done]$nextItemSnippet"
        }

        if (hasNotes) {
            return reminder.notes
        }

        return context.getString(R.string.reminder_default_triggered)
    }

    fun formatBigTextStyle(reminder: Reminder, title: String): NotificationCompat.BigTextStyle {
        val bigText = buildString {
            if (reminder.notes.isNotBlank()) {
                append(reminder.notes)
            }

            if (reminder.checklist.isNotEmpty()) {
                if (isNotEmpty()) append("\n\n")
                append(context.getString(
                    R.string.reminder_checklist_summary_header,
                    reminder.completedChecklistCount,
                    reminder.totalChecklistCount
                ))
                reminder.checklist.sortedBy { it.orderIndex }.forEach { item ->
                    append("\n")
                    append(if (item.isCompleted) "[✓] " else "[  ] ")
                    append(item.text)
                }
            }
        }

        val summaryText = if (reminder.checklist.isNotEmpty()) {
            "[${reminder.completedChecklistCount}/${reminder.totalChecklistCount} done]"
        } else {
            null
        }

        return NotificationCompat.BigTextStyle()
            .setBigContentTitle(title)
            .bigText(bigText)
            .also {
                if (summaryText != null) {
                    it.setSummaryText(summaryText)
                }
            }
    }

    private fun createContentPendingIntent(reminderId: Long): PendingIntent {
        val uriString = "app://remind/ringing/$reminderId"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
            setPackage(context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            (reminderId * 100).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createFullScreenPendingIntent(reminderId: Long): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("app://remind/ringing/$reminderId")).apply {
            setPackage(context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            (reminderId * 100 + 99).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createSnoozePendingIntent(reminderId: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER_SNOOZE
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminderId)
        }
        return PendingIntent.getBroadcast(
            context,
            (reminderId * 100 + 1).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createDismissPendingIntent(reminderId: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER_DISMISS
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminderId)
        }
        return PendingIntent.getBroadcast(
            context,
            (reminderId * 100 + 2).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createChecklistTogglePendingIntent(reminderId: Long, itemId: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER_CHECKLIST_TOGGLE
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminderId)
            putExtra(ReminderReceiver.EXTRA_CHECKLIST_ITEM_ID, itemId)
            putExtra(ReminderReceiver.EXTRA_CHECKLIST_IS_COMPLETED, true)
        }
        return PendingIntent.getBroadcast(
            context,
            (reminderId * 1000 + itemId).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun getNotificationId(reminderId: Long): Int {
        return (NOTIFICATION_ID_OFFSET + (reminderId % 100000)).toInt()
    }

    companion object {
        const val CHANNEL_ID_STANDARD = "reminder_standard_channel"
        const val CHANNEL_ID_URGENT = "reminder_urgent_channel"
        const val NOTIFICATION_ID_OFFSET = 30000
    }
}
