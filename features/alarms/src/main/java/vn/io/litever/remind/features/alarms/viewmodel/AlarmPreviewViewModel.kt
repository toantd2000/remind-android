package vn.io.litever.remind.features.alarms.viewmodel

import android.os.Build
import vn.io.litever.remind.core.common.audio.AudioPlayer
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
    private val audioPlayer: AudioPlayer,
    @ApplicationContext private val context: android.content.Context
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

    private var autoSilenceJob: Job? = null
    private var hasStartedRinging = false

    init {
        viewModelScope.launch {
            alarm.collect { alarm ->
                if (alarm != null && !hasStartedRinging) {
                    startPreviewRinging(alarm)
                    setupAutoSilence(alarm)
                    hasStartedRinging = true
                }
            }
        }
        viewModelScope.launch {
            var wasMuted = false
            mutedAlarmIds.collect { mutedIds ->
                val isMuted = mutedIds.contains(alarmId)
                val currentAlarm = alarm.value
                if (currentAlarm != null) {
                    val targetVolume = if (isMuted) 0 else currentAlarm.volume
                    audioPlayer.setVolume(android.media.AudioAttributes.USAGE_ALARM, targetVolume)
                    
                    if (isMuted) {
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
        val uri = getAccessibleRingtoneUri(context, alarm.ringtoneUri)
        audioPlayer.play(
            uri = uri,
            usage = android.media.AudioAttributes.USAGE_ALARM,
            volume = alarm.volume,
            gradualVolumeDurationSeconds = alarm.gradualVolumeDurationSeconds,
            vibrationEnabled = alarm.vibrationEnabled
        )
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
        audioPlayer.stop()
        autoSilenceJob?.cancel()
        hasStartedRinging = false

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
