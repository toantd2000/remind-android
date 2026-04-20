package vn.io.litever.remind.features.reminder.viewmodel

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
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.features.reminder.ui.state.NextReminderUiState
import vn.io.litever.remind.features.reminder.ui.state.calculateNextReminder
import java.time.LocalTime
import javax.inject.Inject
import vn.io.litever.remind.core.datastore.ReminderPreferencesDataSource
import vn.io.litever.remind.features.reminder.R

data class ReminderEditUiState(
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
    val gradualVolumeDurationSeconds: Int = 0
)

@HiltViewModel
class ReminderEditViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler,
    private val preferencesDataSource: ReminderPreferencesDataSource,
    private val permissionChecker: vn.io.litever.remind.core.common.util.PermissionChecker,
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
        ReminderEditUiState(
            maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_ALARM),
            volume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_ALARM)
        )
    )
    val uiState: StateFlow<ReminderEditUiState> = _uiState.asStateFlow()

    val nextReminderState: StateFlow<NextReminderUiState> = _uiState
        .map { state ->
            calculateNextReminder(
                listOf(
                    Reminder(
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
            initialValue = NextReminderUiState.AllOff
        )

    fun loadReminder(reminderId: Long) {
        if (reminderId == 0L || _uiState.value.id == reminderId) return
        viewModelScope.launch {
            repository.getReminderById(reminderId)?.let { reminder ->
                _uiState.value = ReminderEditUiState(
                    id = reminder.id,
                    time = reminder.time,
                    label = reminder.label,
                    message = reminder.message,
                    repeatDays = reminder.repeatDays,
                    date = reminder.date,
                    vibrationEnabled = reminder.vibrationEnabled,
                    ringtoneUri = reminder.ringtoneUri,
                    ringtoneTitle = getRingtoneTitle(reminder.ringtoneUri),
                    volume = reminder.volume,
                    maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_ALARM),
                    isEnabled = reminder.isEnabled,
                    snoozeEnabled = reminder.snoozeEnabled,
                    snoozeInterval = reminder.snoozeInterval,
                    snoozeRepeatCount = reminder.snoozeRepeatCount,
                    autoSilenceMinutes = reminder.autoSilenceMinutes,
                    gradualVolumeDurationSeconds = reminder.gradualVolumeDurationSeconds
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
        stopRingtonePlayback()
        _uiState.update { it.copy(volume = volume) }
        // Update system volume immediately to provide feedback
        audioManager.setStreamVolume(android.media.AudioManager.STREAM_ALARM, volume, 0)
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
                        vibrator.vibrate(android.os.VibrationEffect.createWaveform(pattern, 0))
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

    fun refreshPermissions() {
        if (permissionChecker.hasCriticalPermissions()) {
            _uiState.update { it.copy(showPermissionDialog = false) }
        }
    }

    override fun onCleared() {
        stopRingtonePlayback()
        super.onCleared()
    }

    fun saveReminder(onSuccess: () -> Unit) {
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
            val state = _uiState.value
            val reminder = Reminder(
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
                gradualVolumeDurationSeconds = state.gradualVolumeDurationSeconds
            )
            
            if (state.id == 0L) {
                val savedId = repository.insertReminder(reminder)
                if (reminder.isEnabled) {
                    reminderScheduler.schedule(reminder.copy(id = savedId))
                }
            } else {
                repository.updateReminder(reminder)
                if (reminder.isEnabled) {
                    reminderScheduler.schedule(reminder)
                } else {
                    reminderScheduler.cancel(reminder)
                }
            }
            onSuccess()
        }
    }
}
