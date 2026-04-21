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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import vn.io.litever.remind.core.reminder.ReminderRingManager
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler.Companion.EXTRA_REMINDER_ID
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler.Companion.EXTRA_IS_SNOOZE
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.domain.scheduler.ReminderController
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.reminder.provider.ReminderIntentProvider
import vn.io.litever.remind.core.common.util.getAccessibleRingtoneUri
import vn.io.litever.remind.core.reminder.R
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

    @Inject
    lateinit var reminderController: ReminderController

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var autoSilenceJob: Job? = null
    private var ringingJob: Job? = null
    private var volumeIncreaseJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    private var currentActiveId: Long? = null
    private var hasStartedRinging = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        scope.launch {
            @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
            combine(
                reminderRingManager.ringingReminderId,
                reminderRingManager.mutedReminderIds
            ) { id, mutedIds -> id to (id != null && id in mutedIds) }
            .flatMapLatest { (id, isMuted) ->
                if (id == null || isMuted) {
                    kotlinx.coroutines.flow.flowOf(null)
                } else {
                    reminderRepository.getReminderFlow(id).map { reminder ->
                        val isSnoozing = reminder?.snoozeNextTriggerTime != null
                        if (reminder != null && !isSnoozing) reminder else null
                    }
                }
            }.collect { targetReminder ->
                val targetId = targetReminder?.id
                
                launch(Dispatchers.Main) {
                    if (targetId != currentActiveId) {
                        stopCurrentRinging()
                        if (targetReminder != null) {
                            startRinging(targetReminder)
                        }
                        currentActiveId = targetId
                    }
                }

                // Life cycle and notification logic
                val mainId = reminderRingManager.ringingReminderId.value
                if (mainId != null) {
                    hasStartedRinging = true
                    
                    // Only run auto-silence if the reminder is actually ringing (not muted/in mission)
                    if (targetId != null) {
                        // Only start a new timer if one isn't already running
                        if (autoSilenceJob == null || autoSilenceJob?.isActive == false) {
                            setupAutoSilence(mainId)
                        }
                    } else {
                        autoSilenceJob?.cancel()
                        autoSilenceJob = null
                        reminderRingManager.setAutoSilenceCountdown(null)
                    }
                    
                    updateNotification(mainId)
                } else if (hasStartedRinging) {
                        launch(Dispatchers.Main) {
                            stopForeground(STOP_FOREGROUND_REMOVE)
                            stopSelf()
                        }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val reminderId = intent?.getLongExtra(EXTRA_REMINDER_ID, -1L) ?: -1L
        
        // Always start foreground immediately to prevent crash, even if ID is missing
        val notification = createNotification(if (reminderId != -1L) reminderId else 0L)
        startForeground(1, notification)
        
        if (reminderId == -1L) {
            stopSelf()
            return START_NOT_STICKY
        }
        
        reminderRingManager.enqueueReminder(reminderId)
        
        val isSnoozeTrigger = intent?.getBooleanExtra(EXTRA_IS_SNOOZE, false) ?: false
        
        scope.launch {
            try {
                val reminder = reminderRepository.getReminderById(reminderId)
                if (reminder != null) {
                    // Reset snooze count if it's a fresh (non-snooze) trigger
                    val updatedReminder = if (!isSnoozeTrigger) {
                        reminder.copy(currentSnoozeCount = 0, snoozeNextTriggerTime = null, isMissed = false)
                    } else {
                        reminder.copy(snoozeNextTriggerTime = null, isMissed = false)
                    }

                    if (updatedReminder.repeatDays.isEmpty()) {
                        reminderRepository.updateReminder(updatedReminder.copy(isEnabled = false))
                    } else {
                        reminderRepository.updateReminder(updatedReminder)
                        reminderScheduler.schedule(updatedReminder)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return START_STICKY
    }

    private fun startRinging(reminder: Reminder) {
        ringingJob?.cancel()
        volumeIncreaseJob?.cancel()
        
        val audioManager = getSystemService(AUDIO_SERVICE) as android.media.AudioManager
        
        // Initial volume: 1 if gradual enabled, otherwise reminder.volume
        val initialVolume = if (reminder.gradualVolumeDurationSeconds > 0) 1 else reminder.volume
        audioManager.setStreamVolume(android.media.AudioManager.STREAM_ALARM, initialVolume, 0)
        
        val uri = getAccessibleRingtoneUri(this@ReminderService, reminder.ringtoneUri)
        
        // Use a dedicated job for the ringing session
        ringingJob = scope.launch {
            var player: MediaPlayer? = null
            try {
                player = MediaPlayer().apply {
                    setDataSource(this@ReminderService, uri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    val maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_ALARM)
                    val volumeScale = initialVolume.toFloat() / maxVolume.toFloat()
                    setVolume(volumeScale, volumeScale)
                }

                // Prepare on IO thread
                kotlinx.coroutines.withContext(Dispatchers.IO) {
                    player.prepare()
                }

                // IMPORTANT: Check if we are still active before starting and assigning to global variable
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    if (isActive) {
                        player.start()
                        mediaPlayer = player
                    } else {
                        player.release()
                    }
                }

                if (!isActive) return@launch

                // Handle gradual volume increase (stays on scope)
                if (reminder.gradualVolumeDurationSeconds > 0) {
                    volumeIncreaseJob = scope.launch {
                        val durationMs = reminder.gradualVolumeDurationSeconds * 1000L
                        val startTime = System.currentTimeMillis()
                        val targetVolume = reminder.volume
                        val systemMaxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_ALARM)
                        
                        while (System.currentTimeMillis() - startTime < durationMs && isActive) {
                            val elapsed = System.currentTimeMillis() - startTime
                            val currentVolume = 1 + ((targetVolume - 1) * elapsed / durationMs).toInt()
                            
                            kotlinx.coroutines.withContext(Dispatchers.Main) {
                                if (isActive) {
                                    val currentVolumeScale = currentVolume.toFloat() / systemMaxVolume.toFloat()
                                    mediaPlayer?.setVolume(currentVolumeScale, currentVolumeScale)
                                    audioManager.setStreamVolume(android.media.AudioManager.STREAM_ALARM, currentVolume, 0)
                                }
                            }
                            delay(1000L)
                        }
                    }
                }

                if (reminder.vibrationEnabled) {
                    vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                        vibratorManager.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        getSystemService(VIBRATOR_SERVICE) as Vibrator
                    }

                    val pattern = longArrayOf(0, 1000, 1000)
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                player?.release()
            }
        }
    }

    private fun setupAutoSilence(reminderId: Long) {
        autoSilenceJob?.cancel()
        autoSilenceJob = scope.launch {
            val reminder = reminderRepository.getReminderById(reminderId) ?: return@launch
            if (reminder.autoSilenceMinutes > 0) {
                var remainingSeconds = reminder.autoSilenceMinutes * 60
                while (remainingSeconds > 0) {
                    reminderRingManager.setAutoSilenceCountdown(remainingSeconds)
                    delay(1000L)
                    remainingSeconds--
                }
                reminderRingManager.setAutoSilenceCountdown(null)
                // Auto-silence acts like a snooze (or missed if out of count)
                launch(Dispatchers.Main) {
                    val currentReminder = reminderRepository.getReminderById(reminderId)
                    if (currentReminder != null && currentReminder.snoozeEnabled && currentReminder.currentSnoozeCount < currentReminder.snoozeRepeatCount) {
                        reminderController.snoozeReminder()
                    } else {
                        reminderController.markAsMissed()
                    }
                }
            }
        }
    }

    private fun stopCurrentRinging() {
        ringingJob?.cancel()
        volumeIncreaseJob?.cancel()
        autoSilenceJob?.cancel()
        reminderRingManager.setAutoSilenceCountdown(null)
        try {
            mediaPlayer?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaPlayer = null
        vibrator?.cancel()
    }

    private fun updateNotification(reminderId: Long) {
        val notification = createNotification(reminderId)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notification)
    }

    private fun createNotification(reminderId: Long): android.app.Notification {
        val deepLinkIntent = reminderIntentProvider.createRingingIntent(reminderId).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            reminderId.hashCode(),
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(getString(R.string.notification_reminder_title))
            .setContentText(getString(R.string.notification_reminder_text))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent) 
            .setFullScreenIntent(pendingIntent, true)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    override fun onDestroy() {
        stopCurrentRinging()
        scope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.reminder_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.reminder_channel_description)
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
