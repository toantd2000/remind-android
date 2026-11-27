package vn.io.litever.remind.core.alarm.alert

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.audiofx.LoudnessEnhancer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import vn.io.litever.remind.core.alarm.notification.ReminderNotificationManager
import vn.io.litever.remind.core.common.audio.TtsSpeaker
import vn.io.litever.remind.core.common.util.getAccessibleRingtoneUri
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.UrgencyLevel
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

sealed interface AlertPlaybackState {
    object Idle : AlertPlaybackState
    data class Playing(
        val reminderId: Long,
        val reminder: Reminder,
        val isPersistent: Boolean,
        val volume: Int,
        val vibrationEnabled: Boolean,
        val remainingAutoSilenceSeconds: Int? = null
    ) : AlertPlaybackState
}

interface ReminderAlertManager {
    val playbackState: StateFlow<AlertPlaybackState>
    val autoSilenceCountdown: StateFlow<Int?>
    val activeAlertReminderIds: StateFlow<List<Long>>

    fun startAlert(reminder: Reminder)
    fun stopAlert(reminderId: Long)
    fun stopAllAlerts()
    fun mute(reminderId: Long)
    fun unmute(reminderId: Long)
    fun isAlerting(reminderId: Long): Boolean
    fun setAutoSilenceListener(listener: (reminderId: Long) -> Unit)
}

@Singleton
class ReminderAlertManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderRepositoryProvider: Provider<ReminderRepository>? = null,
    private val reminderSchedulerProvider: Provider<ReminderScheduler>? = null,
    private val reminderNotificationManagerProvider: Provider<ReminderNotificationManager>? = null,
    private val reminderActionHandlerProvider: Provider<vn.io.litever.remind.core.domain.handler.ReminderActionHandler>? = null,
    private val ttsSpeaker: TtsSpeaker? = null,
    private val proRepositoryProvider: Provider<vn.io.litever.remind.core.domain.repository.ProRepository>? = null
) : ReminderAlertManager {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val stateMutex = Mutex()

    private var mediaPlayer: MediaPlayer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var extraLoudJob: Job? = null
    private var vibrator: Vibrator? = null
    private var focusRequest: AudioFocusRequest? = null
    private var autoSilenceJob: Job? = null
    private var autoSilenceListener: ((Long) -> Unit)? = null

    private val _playbackState = MutableStateFlow<AlertPlaybackState>(AlertPlaybackState.Idle)
    override val playbackState: StateFlow<AlertPlaybackState> = _playbackState.asStateFlow()

    private val _autoSilenceCountdown = MutableStateFlow<Int?>(null)
    override val autoSilenceCountdown: StateFlow<Int?> = _autoSilenceCountdown.asStateFlow()

    private val _activeAlertQueue = MutableStateFlow<List<Reminder>>(emptyList())
    private val _mutedAlertIds = MutableStateFlow<Set<Long>>(emptySet())

    override val activeAlertReminderIds: StateFlow<List<Long>> = _activeAlertQueue
        .map { list -> list.map { it.id } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (isCallActive()) {
                    try {
                        mediaPlayer?.pause()
                    } catch (_: Exception) { }
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                try {
                    if (mediaPlayer?.isPlaying == false) {
                        mediaPlayer?.start()
                    }
                } catch (_: Exception) { }
            }
        }
    }

    override fun setAutoSilenceListener(listener: (reminderId: Long) -> Unit) {
        this.autoSilenceListener = listener
    }

    override fun startAlert(reminder: Reminder) {
        if (reminder.action.urgencyLevel == UrgencyLevel.SOFT) {
            return
        }
        val currentList = _activeAlertQueue.value
        val existingIndex = currentList.indexOfFirst { it.id == reminder.id }
        val updatedList = if (existingIndex >= 0) {
            currentList.toMutableList().apply { set(existingIndex, reminder) }
        } else {
            currentList + reminder
        }
        _activeAlertQueue.value = updatedList
        evaluateAlertQueue()
    }

    override fun stopAlert(reminderId: Long) {
        _activeAlertQueue.update { list -> list.filterNot { it.id == reminderId } }
        _mutedAlertIds.update { set -> set - reminderId }
        evaluateAlertQueue()
    }

    override fun stopAllAlerts() {
        _activeAlertQueue.value = emptyList()
        _mutedAlertIds.value = emptySet()
        stopAudiblePlayback()
        stopAutoSilenceTimer()
        _playbackState.value = AlertPlaybackState.Idle
    }

    override fun mute(reminderId: Long) {
        _mutedAlertIds.update { it + reminderId }
        val currentPlaying = _playbackState.value
        if (currentPlaying is AlertPlaybackState.Playing && currentPlaying.reminderId == reminderId) {
            stopAudiblePlayback()
            evaluateAlertQueue()
        }
    }

    override fun unmute(reminderId: Long) {
        _mutedAlertIds.update { it - reminderId }
        evaluateAlertQueue()
    }

    override fun isAlerting(reminderId: Long): Boolean {
        return _activeAlertQueue.value.any { it.id == reminderId }
    }

    private fun evaluateAlertQueue() {
        val queue = _activeAlertQueue.value
        val mutedIds = _mutedAlertIds.value
        val nextAudible = queue.firstOrNull { it.id !in mutedIds }

        if (nextAudible == null) {
            stopAudiblePlayback()
            stopAutoSilenceTimer()
            _playbackState.value = AlertPlaybackState.Idle
            return
        }

        val currentPlaying = _playbackState.value
        if (currentPlaying is AlertPlaybackState.Playing && currentPlaying.reminderId == nextAudible.id) {
            return // Already playing target reminder
        }

        val isAlarmStyle = nextAudible.action.notificationStyle == NotificationStyle.FULL_SCREEN
        val isPersistent = if (isAlarmStyle) true else nextAudible.action.isPersistentAlert
        startAudiblePlayback(nextAudible, isPersistent)
        if (isPersistent && nextAudible.action.autoSilenceMinutes > 0) {
            startAutoSilenceTimer(nextAudible)
        } else {
            stopAutoSilenceTimer()
        }

        _playbackState.value = AlertPlaybackState.Playing(
            reminderId = nextAudible.id,
            reminder = nextAudible,
            isPersistent = isPersistent,
            volume = nextAudible.action.volume,
            vibrationEnabled = nextAudible.action.vibrationEnabled
        )
    }

    private fun startAudiblePlayback(reminder: Reminder, isPersistent: Boolean) {
        if (isCallActive()) {
            if (reminder.action.vibrationEnabled) startVibration(isPersistent)
            return
        }

        val isAlarmStyle = reminder.action.notificationStyle == NotificationStyle.FULL_SCREEN
        val usage = if (reminder.action.useAlarmStream || isAlarmStyle) {
            AudioAttributes.USAGE_ALARM
        } else {
            AudioAttributes.USAGE_NOTIFICATION_EVENT
        }
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(usage)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(audioAttributes)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(audioFocusChangeListener)
            .build()
        focusRequest = request
        audioManager.requestAudioFocus(request)

        val uri = if (!reminder.action.ringtoneUri.isNullOrBlank()) {
            getAccessibleRingtoneUri(context, reminder.action.ringtoneUri)
        } else if (isAlarmStyle || reminder.action.useAlarmStream) {
            getAccessibleRingtoneUri(context, null)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) ?: getAccessibleRingtoneUri(context, null)
        }

        try {
            val player = MediaPlayer().apply {
                setDataSource(context, uri)
                setAudioAttributes(audioAttributes)
                isLooping = isPersistent
            }
            mediaPlayer = player

            player.setOnPreparedListener { mp ->
                val volumeScale = (reminder.action.volume.coerceIn(0, 100)).toFloat() / 100f
                mp.setVolume(volumeScale, volumeScale)
                mp.start()
                if (reminder.action.vibrationEnabled) {
                    startVibration(isPersistent, audioAttributes)
                }
                scope.launch {
                    val isPro = runCatching { proRepositoryProvider?.get()?.isPro?.first() ?: false }.getOrDefault(false)
                    val effectiveAction = vn.io.litever.remind.core.model.ProFeaturePolicy.resolveEffectiveReminderAction(
                        action = reminder.action,
                        isPro = isPro
                    )
                    if (effectiveAction.announceTitleTts && reminder.title.isNotBlank()) {
                        ttsSpeaker?.speak(reminder.title, useAlarmStream = effectiveAction.useAlarmStream)
                    }
                    if (effectiveAction.extraLoudEnabled) {
                        extraLoudJob?.cancel()
                        extraLoudJob = scope.launch {
                            delay(40_000L)
                            if (mediaPlayer == mp) {
                                runCatching {
                                    mp.setVolume(1f, 1f)
                                    loudnessEnhancer?.release()
                                    loudnessEnhancer = LoudnessEnhancer(mp.audioSessionId).apply {
                                        setTargetGain(1200)
                                        enabled = true
                                    }
                                }
                            }
                        }
                    }
                }
            }

            player.setOnCompletionListener {
                if (!isPersistent) {
                    stopAlert(reminder.id)
                }
            }

            player.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: Vibrate anyway if audio setup failed
            if (reminder.action.vibrationEnabled) {
                startVibration(isPersistent, audioAttributes)
            }
        }
    }

    private fun startVibration(isPersistent: Boolean, audioAttributes: AudioAttributes? = null) {
        vibrator = (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator

        if (vibrator?.hasVibrator() != true) return

        val pattern = if (isPersistent) {
            longArrayOf(0, 500, 500, 500, 500)
        } else {
            longArrayOf(0, 300, 200, 300)
        }
        val repeatIndex = if (isPersistent) 0 else -1

        val effect = VibrationEffect.createWaveform(pattern, repeatIndex)
        if (audioAttributes != null) {
            val vibrationAttributes = android.os.VibrationAttributes.Builder()
                .setUsage(android.os.VibrationAttributes.USAGE_ALARM)
                .build()
            vibrator?.vibrate(effect, vibrationAttributes)
        } else {
            vibrator?.vibrate(effect)
        }
    }

    private fun startAutoSilenceTimer(reminder: Reminder) {
        stopAutoSilenceTimer()
        val minutes = reminder.action.autoSilenceMinutes
        if (minutes > 0) {
            autoSilenceJob = scope.launch {
                val totalSeconds = minutes * 60
                var remaining = totalSeconds
                while (remaining > 0) {
                    _autoSilenceCountdown.value = remaining
                    delay(1000L.milliseconds)
                    remaining--
                }
                _autoSilenceCountdown.value = null
                handleAutoSilenceTimeout(reminder.id)
            }
        }
    }

    private fun handleAutoSilenceTimeout(reminderId: Long) {
        stopAlert(reminderId)
        scope.launch(Dispatchers.IO) {
            try {
                val repository = reminderRepositoryProvider?.get()
                val scheduler = reminderSchedulerProvider?.get()
                val notificationManager = reminderNotificationManagerProvider?.get()
                val actionHandler = reminderActionHandlerProvider?.get()
                if (repository != null && actionHandler != null) {
                    val reminder = repository.getReminderById(reminderId)
                    if (reminder != null && reminder.isEnabled) {
                        if (reminder.canSnooze()) {
                            actionHandler.handleSnooze(reminder, reminder.action.snoozeIntervalMinutes)
                            notificationManager?.cancelNotification(reminderId)
                        } else {
                            if (reminder.triggerType == ReminderTriggerType.INTERVAL) {
                                actionHandler.handleComplete(reminder)
                            } else {
                                val isRecurring = reminder.isRecurring
                                val updatedReminder = reminder.copy(
                                    isEnabled = isRecurring,
                                    snoozeNextTriggerTime = null
                                )
                                actionHandler.handleDismiss(updatedReminder)
                            }
                            notificationManager?.cancelNotification(reminderId)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        autoSilenceListener?.invoke(reminderId)
    }

    private fun stopAudiblePlayback() {
        extraLoudJob?.cancel()
        extraLoudJob = null
        runCatching {
            loudnessEnhancer?.enabled = false
            loudnessEnhancer?.release()
        }
        loudnessEnhancer = null
        ttsSpeaker?.stop()

        mediaPlayer?.let {
            try {
                if (it.isPlaying) it.stop()
            } catch (_: Exception) { }
            try {
                it.release()
            } catch (_: Exception) { }
        }
        mediaPlayer = null

        try {
            vibrator?.cancel()
        } catch (_: Exception) { }
        vibrator = null

        focusRequest?.let {
            try {
                audioManager.abandonAudioFocusRequest(it)
            } catch (_: Exception) { }
        }
        focusRequest = null
    }

    private fun stopAutoSilenceTimer() {
        autoSilenceJob?.cancel()
        autoSilenceJob = null
        _autoSilenceCountdown.value = null
    }

    private fun isCallActive(): Boolean {
        return audioManager.mode in listOf(
            AudioManager.MODE_IN_CALL,
            AudioManager.MODE_IN_COMMUNICATION,
            AudioManager.MODE_RINGTONE
        )
    }

    companion object {
        const val DEFAULT_AUTO_SILENCE_MINUTES = 5
    }
}


