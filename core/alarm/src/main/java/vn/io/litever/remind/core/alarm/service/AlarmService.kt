package vn.io.litever.remind.core.alarm.service

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
import kotlinx.coroutines.withContext
import vn.io.litever.remind.core.alarm.AlarmRingManager
import vn.io.litever.remind.core.domain.scheduler.AlarmScheduler
import vn.io.litever.remind.core.domain.repository.AlarmRepository
import vn.io.litever.remind.core.domain.scheduler.AlarmController
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.core.alarm.provider.AlarmIntentProvider
import vn.io.litever.remind.core.common.util.getAccessibleRingtoneUri
import vn.io.litever.remind.core.alarm.R
import javax.inject.Inject

@AndroidEntryPoint
class AlarmService : Service() {
    @Inject
    lateinit var alarmRepository: AlarmRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var alarmRingManager: AlarmRingManager

    @Inject
    lateinit var alarmIntentProvider: AlarmIntentProvider

    @Inject
    lateinit var alarmController: AlarmController

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var autoSilenceJob: Job? = null
    private var ringingJob: Job? = null
    private var volumeIncreaseJob: Job? = null
    
    private data class RingingState(
        val mainId: Long?,
        val audibleAlarm: Alarm?,
        val fullAlarm: Alarm?,
        val isSnoozing: Boolean
    )

    override fun onBind(intent: Intent?): IBinder? = null

    private var currentActiveId: Long? = null
    private var lastAutoSilencedId: Long? = null
    private var hasStartedRinging = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        scope.launch {
            @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
            combine(
                alarmRingManager.ringingAlarmId,
                alarmRingManager.mutedAlarmIds
            ) { id, mutedIds -> id to (id != null && id in mutedIds) }
            .flatMapLatest { (id, isMuted) ->
                if (id == null) {
                    flowOf(RingingState(null, null, null, false))
                } else {
                    alarmRepository.getAlarmFlow(id).map { alarm ->
                        val isSnoozing = alarm?.snoozeNextTriggerTime != null
                        val audibleAlarm = if (!isMuted && !isSnoozing) alarm else null
                        RingingState(id, audibleAlarm, alarm, isSnoozing)
                    }
                }
            }.collect { state ->
                val targetAlarm = state.audibleAlarm
                val targetId = targetAlarm?.id
                val mainId = state.mainId
                val isSnoozing = state.isSnoozing

                // Synchronize all lifecycle changes on Main thread to avoid race conditions
                withContext(Dispatchers.Main) {
                    if (targetId != currentActiveId) {
                        stopAudibleRinging()
                        if (targetAlarm != null) {
                            startRinging(targetAlarm)
                        }
                        currentActiveId = targetId
                    }

                    // AUTO-SILENCE LOGIC:
                    // Per user request:
                    // - Stop when mission starts (muted) or snoozing.
                    // - Restart from beginning when back from mission or snooze ends.
                    if (targetAlarm != null) {
                        // If it's not running, or it's a different alarm, start/restart it
                        if (autoSilenceJob == null || autoSilenceJob?.isActive == false || lastAutoSilencedId != targetId) {
                            setupAutoSilence(targetAlarm)
                            lastAutoSilencedId = targetId
                        }
                    } else {
                        stopAutoSilence()
                        lastAutoSilencedId = null 
                    }

                    if (mainId != null) {
                        hasStartedRinging = true
                        updateNotification(mainId)
                    } else if (hasStartedRinging) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L) ?: -1L
        
        // Always start foreground immediately to prevent crash, even if ID is missing
        val notification = createNotification(if (alarmId != -1L) alarmId else 0L)
        startForeground(1, notification)
        
        if (alarmId == -1L) {
            stopSelf()
            return START_NOT_STICKY
        }
        
        alarmRingManager.enqueueAlarm(alarmId)
        
        val isSnoozeTrigger = intent?.getBooleanExtra(AlarmScheduler.EXTRA_IS_SNOOZE, false) ?: false
        
        scope.launch {
            try {
                val alarm = alarmRepository.getAlarmById(alarmId)
                if (alarm != null) {
                    // Reset snooze count if it's a fresh (non-snooze) trigger
                    val updatedAlarm = if (!isSnoozeTrigger) {
                        alarm.copy(currentSnoozeCount = 0, snoozeNextTriggerTime = null, isMissed = false)
                    } else {
                        alarm.copy(snoozeNextTriggerTime = null, isMissed = false)
                    }

                    if (updatedAlarm.repeatDays.isEmpty()) {
                        alarmRepository.updateAlarm(updatedAlarm.copy(isEnabled = false))
                    } else {
                        alarmRepository.updateAlarm(updatedAlarm)
                        alarmScheduler.schedule(updatedAlarm)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return START_STICKY
    }

    private fun startRinging(alarm: Alarm) {
        ringingJob?.cancel()
        volumeIncreaseJob?.cancel()
        
        val audioManager = getSystemService(AUDIO_SERVICE) as android.media.AudioManager
        
        // Initial volume: 1 if gradual enabled, otherwise alarm.volume
        val initialVolume = if (alarm.gradualVolumeDurationSeconds > 0) 1 else alarm.volume
        audioManager.setStreamVolume(android.media.AudioManager.STREAM_ALARM, initialVolume, 0)
        
        val uri = getAccessibleRingtoneUri(this@AlarmService, alarm.ringtoneUri)
        
        // Use a dedicated job for the ringing session
        ringingJob = scope.launch {
            var player: MediaPlayer? = null
            try {
                player = MediaPlayer().apply {
                    setDataSource(this@AlarmService, uri)
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
                if (alarm.gradualVolumeDurationSeconds > 0) {
                    volumeIncreaseJob = scope.launch {
                        val durationMs = alarm.gradualVolumeDurationSeconds * 1000L
                        val startTime = System.currentTimeMillis()
                        val targetVolume = alarm.volume
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

                if (alarm.vibrationEnabled) {
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

    private fun setupAutoSilence(alarm: Alarm) {
        autoSilenceJob?.cancel()
        autoSilenceJob = scope.launch {
            val alarmId = alarm.id
            if (alarm.autoSilenceMinutes > 0) {
                var remainingSeconds = alarm.autoSilenceMinutes * 60
                while (remainingSeconds > 0) {
                    alarmRingManager.setAutoSilenceCountdown(remainingSeconds)
                    delay(1000L)
                    remainingSeconds--
                }
                alarmRingManager.setAutoSilenceCountdown(null)
                // Auto-silence acts like a snooze (or missed if out of count)
                val currentAlarm = withContext(Dispatchers.IO) { alarmRepository.getAlarmById(alarmId) }
                if (currentAlarm != null && currentAlarm.snoozeEnabled && currentAlarm.currentSnoozeCount < currentAlarm.snoozeRepeatCount) {
                    alarmController.snoozeAlarm(alarmId)
                } else {
                    alarmController.markAsMissed(alarmId)
                }
            }
        }
    }

    private fun stopCurrentRinging() {
        stopAudibleRinging()
        stopAutoSilence()
    }

    private fun stopAudibleRinging() {
        ringingJob?.cancel()
        volumeIncreaseJob?.cancel()
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

    private fun stopAutoSilence() {
        autoSilenceJob?.cancel()
        autoSilenceJob = null
        alarmRingManager.setAutoSilenceCountdown(null)
    }

    private fun updateNotification(alarmId: Long) {
        val notification = createNotification(alarmId)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notification)
    }

    private fun createNotification(alarmId: Long): android.app.Notification {
        val deepLinkIntent = alarmIntentProvider.createRingingIntent(alarmId).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            alarmId.hashCode(),
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(getString(R.string.notification_alarm_title))
            .setContentText(getString(R.string.notification_alarm_text))
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
                getString(R.string.alarm_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.alarm_channel_description)
                setSound(null, null) 
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "alarm_channel"
    }
}










