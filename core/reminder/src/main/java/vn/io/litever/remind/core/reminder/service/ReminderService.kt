package vn.io.litever.remind.core.reminder.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.reminder.ReminderRingManager
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler.Companion.EXTRA_REMINDER_ID
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.reminder.provider.ReminderIntentProvider
import vn.io.litever.remind.core.common.util.getAccessibleRingtoneUri
import javax.inject.Inject

@AndroidEntryPoint
class ReminderService : Service() {
    @Inject
    lateinit var reminderRepository: ReminderRepository

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    @Inject
    lateinit var reminderRingManager: ReminderRingManager

    @Inject
    lateinit var reminderIntentProvider: ReminderIntentProvider

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val reminderId = intent?.getLongExtra(EXTRA_REMINDER_ID, -1L) ?: -1L
        if (reminderId == -1L) return START_NOT_STICKY
        
        reminderRingManager.setRinging(reminderId)
        
        scope.launch {
            try {
                val reminder = reminderRepository.getReminderById(reminderId)
                if (reminder != null) {
                    if (reminder.repeatDays.isEmpty()) {
                        reminderRepository.updateReminder(reminder.copy(isEnabled = false))
                    } else {
                        reminderScheduler.schedule(reminder)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        startRinging(reminderId)
        
        val deepLinkIntent = reminderIntentProvider.createRingingIntent(reminderId).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            reminderId.hashCode(),
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Nhắc nhở!")
            .setContentText("Nhấn vào đây để xem và tắt")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent) 
            .setFullScreenIntent(pendingIntent, true)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
            
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notification)
        
        startForeground(1, notification)

        return START_STICKY
    }

    private fun startRinging(reminderId: Long) {
        if (mediaPlayer?.isPlaying == true) return
        
        scope.launch {
            val reminder = reminderRepository.getReminderById(reminderId) ?: return@launch
            
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            audioManager.setStreamVolume(android.media.AudioManager.STREAM_ALARM, reminder.volume, 0)
            
            val uri = getAccessibleRingtoneUri(this@ReminderService, reminder.ringtoneUri)
            
            launch(Dispatchers.Main) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(this@ReminderService, uri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    val maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_ALARM)
                    val volumeScale = reminder.volume.toFloat() / maxVolume.toFloat()
                    setVolume(volumeScale, volumeScale)
                    prepare()
                    start()
                }

                if (reminder.vibrationEnabled) {
                    vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                        vibratorManager.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    }

                    val pattern = longArrayOf(0, 1000, 1000)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(pattern, 0)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        vibrator?.cancel()
        reminderRingManager.setRinging(null)
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reminder Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for reminder ringing"
                setSound(null, null) 
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "reminder_channel"
    }
}
