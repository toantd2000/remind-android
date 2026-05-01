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
import androidx.core.app.NotificationCompat
import vn.io.litever.remind.core.common.audio.AudioPlayer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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

    @Inject
    lateinit var audioPlayer: AudioPlayer

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var autoSilenceJob: Job? = null
    
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
                alarmRingManager.mutedAlarmIds,
                alarmRingManager.isRingingScreenVisible
            ) { id, mutedIds, isVisible -> Triple(id, id != null && id in mutedIds, isVisible) }
            .flatMapLatest { (id, isMuted, isVisible) ->
                if (id == null) {
                    flowOf(RingingState(null, null, null, false) to isVisible)
                } else {
                    alarmRepository.getAlarmFlow(id).map { alarm ->
                        val isSnoozing = alarm?.snoozeNextTriggerTime != null
                        val audibleAlarm = if (!isMuted && !isSnoozing) alarm else null
                        RingingState(id, audibleAlarm, alarm, isSnoozing) to isVisible
                    }
                }
            }.collect { (state, isVisible) ->
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
                        updateNotification(mainId, isVisible)
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
        val notification = createNotification(if (alarmId != -1L) alarmId else 0L, isVisible = false)
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
                        alarm.copy(
                            currentSnoozeCount = 0,
                            snoozeNextTriggerTime = null,
                            lastTriggeredTime = System.currentTimeMillis()
                        )
                    } else {
                        alarm.copy(snoozeNextTriggerTime = null)
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
        val uri = getAccessibleRingtoneUri(this@AlarmService, alarm.ringtoneUri)
        audioPlayer.play(
            uri = uri,
            volume = alarm.volume,
            gradualVolumeDurationSeconds = alarm.gradualVolumeDurationSeconds,
            vibrationEnabled = alarm.vibrationEnabled
        )
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
        audioPlayer.stop()
    }

    private fun stopAutoSilence() {
        autoSilenceJob?.cancel()
        autoSilenceJob = null
        alarmRingManager.setAutoSilenceCountdown(null)
    }

    private fun updateNotification(alarmId: Long, isVisible: Boolean) {
        val notification = createNotification(alarmId, isVisible)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notification)
    }

    private fun createNotification(alarmId: Long, isVisible: Boolean): android.app.Notification {
        val deepLinkIntent = alarmIntentProvider.createRingingIntent(alarmId).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            alarmId.hashCode(),
            deepLinkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = if (isVisible) CHANNEL_ID_SILENT else CHANNEL_ID_HIGH
        val priority = if (isVisible) NotificationCompat.PRIORITY_DEFAULT else NotificationCompat.PRIORITY_MAX

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(getString(R.string.notification_alarm_title))
            .setContentText(getString(R.string.notification_alarm_text))
            .setPriority(priority)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent) 
            .apply {
                if (!isVisible) {
                    setFullScreenIntent(pendingIntent, true)
                }
            }
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
            val manager = getSystemService(NotificationManager::class.java)
            
            // High priority channel for heads-up and full screen intent
            val highChannel = NotificationChannel(
                CHANNEL_ID_HIGH,
                getString(R.string.alarm_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.alarm_channel_description)
                setSound(null, null) 
                enableVibration(false)
            }
            
            // Silent channel for when the ringing screen is already visible
            val silentChannel = NotificationChannel(
                CHANNEL_ID_SILENT,
                getString(R.string.alarm_channel_name_silent),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.alarm_channel_description_silent)
                setSound(null, null) 
                enableVibration(false)
            }
            
            manager.createNotificationChannel(highChannel)
            manager.createNotificationChannel(silentChannel)
            
            // Cleanup old channel if exists
            manager.deleteNotificationChannel("alarm_channel")
        }
    }

    companion object {
        const val CHANNEL_ID_HIGH = "alarm_channel_high"
        const val CHANNEL_ID_SILENT = "alarm_channel_silent"
    }
}










