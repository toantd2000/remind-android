package vn.io.litever.remind.features.alarms.viewmodel

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.isActive
import vn.io.litever.remind.core.alarm.DraftAlarmStore
import vn.io.litever.remind.core.common.util.getAccessibleRingtoneUri
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.domain.repository.AlarmRepository
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.core.alarm.AlarmRingManager
import javax.inject.Inject

@HiltViewModel
class AlarmPreviewViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val alarmRepository: AlarmRepository,
    private val preferencesDataSource: AlarmPreferencesDataSource,
    private val draftAlarmStore: DraftAlarmStore,
    private val alarmRingManager: AlarmRingManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val alarmId: Long = checkNotNull(savedStateHandle["alarmId"])

    val alarm: StateFlow<Alarm?> = alarmRepository.getAllAlarms()
        .map { alarms -> 
            draftAlarmStore.getDraft() ?: alarms.find { it.id == alarmId } 
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = draftAlarmStore.getDraft()
        )

    val is24HourFormat: StateFlow<Boolean> = preferencesDataSource.is24HourFormat
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    private val _autoSilenceCountdown = MutableStateFlow<Int?>(null)
    val autoSilenceCountdown: StateFlow<Int?> = _autoSilenceCountdown.asStateFlow()

    private val mutedAlarmIds = alarmRingManager.mutedAlarmIds

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var autoSilenceJob: Job? = null
    private var ringingJob: Job? = null
    private var volumeIncreaseJob: Job? = null

    init {
        viewModelScope.launch {
            alarm.collect { alarm ->
                if (alarm != null && ringingJob == null) {
                    startPreviewRinging(alarm)
                    setupAutoSilence(alarm)
                }
            }
        }
        viewModelScope.launch {
            var wasMuted = false
            mutedAlarmIds.collect { mutedIds ->
                val isMuted = mutedIds.contains(alarmId)
                val currentAlarm = alarm.value
                if (currentAlarm != null) {
                    val targetVolume = if (isMuted) 0f else {
                        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
                        val systemMaxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_ALARM)
                        currentAlarm.volume.toFloat() / systemMaxVolume.toFloat()
                    }
                    withContext(Dispatchers.Main) {
                        mediaPlayer?.setVolume(targetVolume, targetVolume)
                    }
                    if (isMuted) {
                        vibrator?.cancel()
                        autoSilenceJob?.cancel()
                    } else if (wasMuted) {
                        // Restart auto-silence from beginning as per DECISION_LOG.md
                        setupAutoSilence(currentAlarm)
                    }
                }
                wasMuted = isMuted
            }
        }
    }

    private fun startPreviewRinging(alarm: Alarm) {
        ringingJob?.cancel()
        volumeIncreaseJob?.cancel()

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        val initialVolume = if (alarm.gradualVolumeDurationSeconds > 0) 1 else alarm.volume
        val uri = getAccessibleRingtoneUri(context, alarm.ringtoneUri)

        ringingJob = viewModelScope.launch(Dispatchers.IO) {
            val player = MediaPlayer().apply {
                setDataSource(context, uri)
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

            try {
                player.prepare()
                val started = withContext(Dispatchers.Main) {
                    if (isActive) {
                        player.start()
                        mediaPlayer = player
                        true
                    } else {
                        player.release()
                        false
                    }
                }

                if (started && alarm.gradualVolumeDurationSeconds > 0) {
                    volumeIncreaseJob = viewModelScope.launch(Dispatchers.IO) {
                        val durationMs = alarm.gradualVolumeDurationSeconds * 1000L
                        val startTime = System.currentTimeMillis()
                        val targetVolume = alarm.volume
                        val systemMaxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_ALARM)

                        while (System.currentTimeMillis() - startTime < durationMs && isActive) {
                            val elapsed = System.currentTimeMillis() - startTime
                            val currentVolume = 1 + ((targetVolume - 1) * elapsed / durationMs).toInt()
                            val currentVolumeScale = currentVolume.toFloat() / systemMaxVolume.toFloat()
                            
                            withContext(Dispatchers.Main) {
                                val isCurrentlyMuted = alarmRingManager.mutedAlarmIds.value.contains(alarmId)
                                if (!isCurrentlyMuted) {
                                    mediaPlayer?.setVolume(currentVolumeScale, currentVolumeScale)
                                }
                            }
                            delay(1000L)
                        }
                    }
                }

                if (started && alarm.vibrationEnabled) {
                    vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                        vibratorManager.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    }

                    val pattern = longArrayOf(0, 1000, 1000)
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    e.printStackTrace()
                }
                player.release()
            }
        }
    }

    private fun setupAutoSilence(alarm: Alarm) {
        autoSilenceJob?.cancel()
        if (alarm.autoSilenceMinutes > 0) {
            autoSilenceJob = viewModelScope.launch {
                var remainingSeconds = alarm.autoSilenceMinutes * 60
                while (remainingSeconds > 0) {
                    _autoSilenceCountdown.value = remainingSeconds
                    delay(1000L)
                    remainingSeconds--
                }
                _autoSilenceCountdown.value = null
                stopPreview()
            }
        }
    }

    fun stopPreview() {
        ringingJob?.cancel()
        volumeIncreaseJob?.cancel()
        autoSilenceJob?.cancel()
        
        // Don't use viewModelScope here as it's already cancelled when onCleared is called
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        
        vibrator?.cancel()
        vibrator = null

        draftAlarmStore.setDraft(null)
        alarmRingManager.unmute(alarmId)
    }

    fun startMissionPreview() {
        alarmRingManager.mute(alarmId)
    }

    override fun onCleared() {
        stopPreview()
        super.onCleared()
    }
}
