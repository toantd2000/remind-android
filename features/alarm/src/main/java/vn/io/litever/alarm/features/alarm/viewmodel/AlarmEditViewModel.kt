package vn.io.litever.alarm.features.alarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vn.io.litever.alarm.core.domain.repository.AlarmRepository
import vn.io.litever.alarm.core.domain.scheduler.AlarmScheduler
import vn.io.litever.alarm.core.model.Alarm
import vn.io.litever.alarm.core.model.DayOfWeek
import vn.io.litever.alarm.features.alarm.ui.state.NextAlarmUiState
import vn.io.litever.alarm.features.alarm.ui.state.calculateNextAlarm
import java.time.LocalTime
import javax.inject.Inject
import vn.io.litever.alarm.core.datastore.AlarmPreferencesDataSource

data class AlarmEditUiState(
    val id: Long = 0,
    val time: LocalTime = LocalTime.now().plusMinutes(1),
    val label: String = "",
    val repeatDays: List<DayOfWeek> = emptyList(),
    val vibrationEnabled: Boolean = true,
    val ringtoneUri: String? = null,
    val ringtoneTitle: String = "Default",
    val volume: Int = 0,
    val maxVolume: Int = 15,
    val isRingtonePlaying: Boolean = false,
    val ringtoneProgress: Float = 0f,
    val isEnabled: Boolean = true
)

@HiltViewModel
class AlarmEditViewModel @Inject constructor(
    private val repository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler,
    private val preferencesDataSource: AlarmPreferencesDataSource,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
    private val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
    }
    private var mediaPlayer: android.media.MediaPlayer? = null
    private var progressJob: kotlinx.coroutines.Job? = null

    val is24HourFormat: StateFlow<Boolean> = preferencesDataSource.is24HourFormat
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    private val _uiState = MutableStateFlow(
        AlarmEditUiState(
            maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_ALARM),
            volume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_ALARM)
        )
    )
    val uiState: StateFlow<AlarmEditUiState> = _uiState.asStateFlow()

    val nextAlarmState: StateFlow<NextAlarmUiState> = _uiState
        .map { state ->
            calculateNextAlarm(
                listOf(
                    Alarm(
                        id = state.id,
                        time = state.time,
                        label = state.label,
                        isEnabled = true,
                        repeatDays = state.repeatDays
                    )
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NextAlarmUiState.AllOff
        )

    fun loadAlarm(alarmId: Long) {
        if (alarmId == 0L) return
        viewModelScope.launch {
            repository.getAlarmById(alarmId)?.let { alarm ->
                _uiState.value = AlarmEditUiState(
                    id = alarm.id,
                    time = alarm.time,
                    label = alarm.label,
                    repeatDays = alarm.repeatDays,
                    vibrationEnabled = alarm.vibrationEnabled,
                    ringtoneUri = alarm.ringtoneUri,
                    ringtoneTitle = getRingtoneTitle(alarm.ringtoneUri),
                    volume = alarm.volume,
                    maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_ALARM),
                    isEnabled = alarm.isEnabled
                )
            }
        }
    }

    fun updateTime(time: LocalTime) {
        _uiState.update { it.copy(time = time) }
    }

    fun updateLabel(label: String) {
        _uiState.update { it.copy(label = label) }
    }

    fun toggleRepeatDay(day: DayOfWeek) {
        _uiState.update { state ->
            val updatedDays = if (state.repeatDays.contains(day)) {
                state.repeatDays - day
            } else {
                state.repeatDays + day
            }
            state.copy(repeatDays = updatedDays)
        }
    }

    fun updateVibration(enabled: Boolean) {
        stopRingtonePlayback()
        _uiState.update { it.copy(vibrationEnabled = enabled) }
    }

    fun updateRingtone(uri: String?) {
        _uiState.update { it.copy(ringtoneUri = uri, ringtoneTitle = getRingtoneTitle(uri)) }
    }
    
    private fun getRingtoneTitle(uriString: String?): String {
        if (uriString == null) return "Default"
        return try {
            val uri = android.net.Uri.parse(uriString)
            android.media.RingtoneManager.getRingtone(context, uri).getTitle(context)
        } catch (e: Exception) {
            "Default"
        }
    }

    fun updateVolume(volume: Int) {
        stopRingtonePlayback()
        _uiState.update { it.copy(volume = volume) }
        // Update system volume immediately to provide feedback
        audioManager.setStreamVolume(android.media.AudioManager.STREAM_ALARM, volume, 0)
    }

    fun toggleRingtonePlayback() {
        val isCurrentlyPlaying = _uiState.value.isRingtonePlaying
        if (isCurrentlyPlaying) {
            stopRingtonePlayback()
        } else {
            startRingtonePlayback()
        }
    }

    private fun startRingtonePlayback() {
        progressJob?.cancel()
        _uiState.update { it.copy(isRingtonePlaying = true, ringtoneProgress = 0f) }
        
        val uri = vn.io.litever.alarm.core.common.util.getAccessibleRingtoneUri(context, _uiState.value.ringtoneUri)

        try {
            mediaPlayer?.release()
            mediaPlayer = android.media.MediaPlayer().apply {
                setDataSource(context, uri)
                setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                val volumeScale = _uiState.value.volume.toFloat() / _uiState.value.maxVolume.toFloat()
                setVolume(volumeScale, volumeScale)
                prepareAsync()
                setOnPreparedListener { 
                    it.start()
                    // Start vibration if enabled
                    if (_uiState.value.vibrationEnabled) {
                        val pattern = longArrayOf(0, 500, 500)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            vibrator.vibrate(android.os.VibrationEffect.createWaveform(pattern, 0))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(pattern, 0)
                        }
                    }
                    // Start progress simulation since MediaPlayer doesn't give precise "played/total" easily for all URIs
                    startProgressSimulation()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopRingtonePlayback()
        }
    }

    private fun startProgressSimulation() {
        progressJob = viewModelScope.launch {
            val duration = 30000L // 30 seconds simulation
            val interval = 100L
            var current = 0L
            while (current < duration && _uiState.value.isRingtonePlaying) {
                kotlinx.coroutines.delay(interval)
                current += interval
                _uiState.update { it.copy(ringtoneProgress = current.toFloat() / duration) }
            }
            if (current >= duration) stopRingtonePlayback()
        }
    }

    private fun stopRingtonePlayback() {
        progressJob?.cancel()
        vibrator.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        _uiState.update { it.copy(isRingtonePlaying = false, ringtoneProgress = 0f) }
    }

    override fun onCleared() {
        stopRingtonePlayback()
        super.onCleared()
    }

    fun saveAlarm(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            val alarm = Alarm(
                id = state.id,
                time = state.time,
                label = state.label,
                isEnabled = state.isEnabled,
                repeatDays = state.repeatDays,
                vibrationEnabled = state.vibrationEnabled,
                ringtoneUri = state.ringtoneUri,
                volume = state.volume
            )
            
            if (state.id == 0L) {
                val savedId = repository.insertAlarm(alarm)
                alarmScheduler.schedule(alarm.copy(id = savedId))
            } else {
                repository.updateAlarm(alarm)
                if (alarm.isEnabled) {
                    alarmScheduler.schedule(alarm)
                } else {
                    alarmScheduler.cancel(alarm)
                }
            }
            onSuccess()
        }
    }
}
