package vn.io.litever.remind.features.alarms.viewmodel

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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.common.audio.AudioPlayer
import vn.io.litever.remind.core.domain.repository.AlarmRepository
import vn.io.litever.remind.core.domain.scheduler.AlarmScheduler
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.features.alarms.ui.state.NextAlarmUiState
import vn.io.litever.remind.features.alarms.ui.state.calculateNextAlarm
import java.time.LocalTime
import javax.inject.Inject
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.alarm.DraftAlarmStore
import vn.io.litever.remind.features.alarms.R

data class AlarmEditUiState(
    val isLoading: Boolean = true,
    val id: Long = 0,
    val time: LocalTime = LocalTime.now().plusMinutes(1),
    val label: String = "",
    val message: String = "",
    val repeatDays: List<DayOfWeek> = emptyList(),
    val date: java.time.LocalDate? = null,
    val vibrationEnabled: Boolean = true,
    val ringtoneUri: String? = null,
    val ringtoneTitle: String = "Default",
    val volume: Int = 0,
    val maxVolume: Int = 15,
    val isRingtonePlaying: Boolean = false,
    val ringtoneProgress: Float = 0f,
    val isEnabled: Boolean = true,
    val showPermissionDialog: Boolean = false,
    val snoozeEnabled: Boolean = true,
    val snoozeInterval: Int = 5,
    val snoozeRepeatCount: Int = 3,
    val autoSilenceMinutes: Int = 3,
    val gradualVolumeDurationSeconds: Int = 0,
    val missions: List<vn.io.litever.remind.core.model.Mission> = emptyList()
)

@HiltViewModel
class AlarmEditViewModel @Inject constructor(
    private val repository: AlarmRepository,
    private val missionRepository: vn.io.litever.remind.core.domain.repository.MissionRepository,
    private val alarmScheduler: AlarmScheduler,
    private val preferencesDataSource: AlarmPreferencesDataSource,
    private val permissionChecker: vn.io.litever.remind.core.common.util.PermissionChecker,
    private val draftAlarmStore: DraftAlarmStore,
    private val audioPlayer: AudioPlayer,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
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
                        message = state.message,
                        isEnabled = true,
                        repeatDays = state.repeatDays,
                        date = state.date
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
        if (alarmId == 0L) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        if (_uiState.value.id == alarmId && !_uiState.value.isLoading) return
        
        viewModelScope.launch {
            repository.getAlarmById(alarmId)?.let { alarm ->
                val missions = missionRepository.getMissionsForAlarm(alarmId).first()
                _uiState.update { it.copy(
                        isLoading = false,
                        id = alarm.id,
                        time = alarm.time,
                        label = alarm.label,
                        message = alarm.message,
                        repeatDays = alarm.repeatDays,
                        date = alarm.date,
                        vibrationEnabled = alarm.vibrationEnabled,
                        ringtoneUri = alarm.ringtoneUri,
                        ringtoneTitle = getRingtoneTitle(alarm.ringtoneUri),
                        volume = alarm.volume,
                        maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_ALARM),
                        isEnabled = alarm.isEnabled,
                        snoozeEnabled = alarm.snoozeEnabled,
                        snoozeInterval = alarm.snoozeInterval,
                        snoozeRepeatCount = alarm.snoozeRepeatCount,
                        autoSilenceMinutes = alarm.autoSilenceMinutes,
                        gradualVolumeDurationSeconds = alarm.gradualVolumeDurationSeconds,
                        missions = missions
                    )
                }
            }
        }
    }

    fun preparePreview() {
        draftAlarmStore.setDraft(createDraftAlarm())
    }

    fun addMission(mission: vn.io.litever.remind.core.model.Mission) {
        if (_uiState.value.missions.size >= 5) return
        val newMission = mission.copy(
            alarmId = _uiState.value.id,
            order = _uiState.value.missions.size
        )
        _uiState.update { it.copy(missions = it.missions + newMission) }
    }

    fun addMission(type: vn.io.litever.remind.core.model.MissionType) {
        addMission(
            vn.io.litever.remind.core.model.Mission(
                alarmId = _uiState.value.id,
                type = type,
                order = _uiState.value.missions.size
            )
        )
    }

    fun updateMission(mission: vn.io.litever.remind.core.model.Mission) {
        _uiState.update { state ->
            val updatedMissions = state.missions.map {
                if (it.order == mission.order) mission else it
            }
            state.copy(missions = updatedMissions)
        }
    }

    fun removeMission(mission: vn.io.litever.remind.core.model.Mission) {
        _uiState.update { state ->
            val updatedMissions = (state.missions - mission).mapIndexed { index, m ->
                m.copy(order = index)
            }
            state.copy(missions = updatedMissions)
        }
    }

    fun updateTime(time: LocalTime) {
        _uiState.update { it.copy(time = time) }
    }

    fun updateLabel(label: String) {
        _uiState.update { it.copy(label = label) }
    }

    fun updateMessage(message: String) {
        _uiState.update { it.copy(message = message) }
    }

    fun toggleRepeatDay(day: DayOfWeek) {
        _uiState.update { state ->
            val updatedDays = if (state.repeatDays.contains(day)) {
                state.repeatDays - day
            } else {
                state.repeatDays + day
            }
            state.copy(repeatDays = updatedDays, date = null)
        }
    }

    fun updateDate(date: java.time.LocalDate?) {
        _uiState.update { it.copy(date = date, repeatDays = emptyList()) }
    }

    fun updateVibration(enabled: Boolean) {
        stopRingtonePlayback()
        _uiState.update { it.copy(vibrationEnabled = enabled) }
    }

    fun updateRingtone(uri: String?) {
        stopRingtonePlayback()
        _uiState.update { it.copy(ringtoneUri = uri, ringtoneTitle = getRingtoneTitle(uri)) }
    }
    
    private fun getRingtoneTitle(uriString: String?): String {
        if (uriString == null) return context.getString(R.string.default_ringtone)
        return try {
            val uri = android.net.Uri.parse(uriString)
            android.media.RingtoneManager.getRingtone(context, uri)?.getTitle(context) 
                ?: context.getString(R.string.default_ringtone)
        } catch (e: Exception) {
            context.getString(R.string.default_ringtone)
        }
    }

    fun updateVolume(volume: Int) {
        _uiState.update { it.copy(volume = volume) }
        audioPlayer.setVolume(android.media.AudioAttributes.USAGE_ALARM, volume)
    }

    fun updateSnoozeSettings(enabled: Boolean, interval: Int, repeatCount: Int) {
        _uiState.update { 
            it.copy(
                snoozeEnabled = enabled, 
                snoozeInterval = interval, 
                snoozeRepeatCount = repeatCount
            ) 
        }
    }

    fun updateAutoSilence(minutes: Int) {
        _uiState.update { it.copy(autoSilenceMinutes = minutes) }
    }

    fun updateGradualVolumeDuration(seconds: Int) {
        _uiState.update { it.copy(gradualVolumeDurationSeconds = seconds) }
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
        
        val uri = vn.io.litever.remind.core.common.util.getAccessibleRingtoneUri(context, _uiState.value.ringtoneUri)

        audioPlayer.play(
            uri = uri,
            usage = android.media.AudioAttributes.USAGE_MEDIA,
            contentType = android.media.AudioAttributes.CONTENT_TYPE_MUSIC,
            volume = _uiState.value.volume,
            vibrationEnabled = _uiState.value.vibrationEnabled
        )
        
        startProgressSimulation()
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

    fun stopRingtonePlayback() {
        progressJob?.cancel()
        audioPlayer.stop()
        _uiState.update { it.copy(isRingtonePlaying = false, ringtoneProgress = 0f) }
    }

    fun refreshPermissions() {
        if (permissionChecker.hasCriticalPermissions()) {
            _uiState.update { it.copy(showPermissionDialog = false) }
        }
    }

    override fun onCleared() {
        stopRingtonePlayback()
        super.onCleared()
    }

    fun saveAlarm(onSuccess: () -> Unit) {
        // Automatically enable the alarm whenever the user saves it
        _uiState.update { it.copy(isEnabled = true) }
        val state = _uiState.value
        
        // Prevent saving enabled alarm if permissions are missing
        if (!permissionChecker.hasCriticalPermissions()) {
            _uiState.update { it.copy(showPermissionDialog = true) }
            return
        }

        performSave(onSuccess)
    }

    fun saveAnyway(onSuccess: () -> Unit) {
        _uiState.update { it.copy(isEnabled = false, showPermissionDialog = false) }
        performSave(onSuccess)
    }

    fun dismissPermissionDialog() {
        _uiState.update { it.copy(showPermissionDialog = false) }
    }

    private fun performSave(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val alarm = createDraftAlarm()
            val state = _uiState.value
            
            if (state.id == 0L) {
                val savedId = repository.insertAlarm(alarm)
                // Save missions for new alarm
                state.missions.forEach { mission ->
                    missionRepository.saveMission(mission.copy(alarmId = savedId))
                }
                if (alarm.isEnabled) {
                    alarmScheduler.schedule(alarm.copy(id = savedId))
                }
            } else {
                repository.updateAlarm(alarm)
                // Update missions for existing alarm
                missionRepository.deleteMissionsForAlarm(state.id)
                state.missions.forEach { mission ->
                    missionRepository.saveMission(mission)
                }
                if (alarm.isEnabled) {
                    alarmScheduler.schedule(alarm)
                } else {
                    alarmScheduler.cancel(alarm)
                }
            }
            onSuccess()
        }
    }

    fun createDraftAlarm(): Alarm {
        val state = _uiState.value
        return Alarm(
            id = state.id,
            time = state.time,
            label = state.label,
            message = state.message,
            isEnabled = state.isEnabled,
            repeatDays = state.repeatDays,
            date = state.date,
            vibrationEnabled = state.vibrationEnabled,
            ringtoneUri = state.ringtoneUri,
            volume = state.volume,
            snoozeEnabled = state.snoozeEnabled,
            snoozeInterval = state.snoozeInterval,
            snoozeRepeatCount = state.snoozeRepeatCount,
            autoSilenceMinutes = state.autoSilenceMinutes,
            gradualVolumeDurationSeconds = state.gradualVolumeDurationSeconds,
            missions = state.missions
        )
    }
}










