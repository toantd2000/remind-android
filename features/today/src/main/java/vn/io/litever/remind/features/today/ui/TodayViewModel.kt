package vn.io.litever.remind.features.today.ui

import android.content.ContentValues.TAG
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val weatherRepository: vn.io.litever.remind.core.domain.repository.WeatherRepository,
    private val todayRepository: vn.io.litever.remind.core.domain.repository.TodayRepository,
    private val alarmRepository: vn.io.litever.remind.core.domain.repository.AlarmRepository,
    private val preferencesDataSource: vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
) : ViewModel() {
    private var lastProcessingRefreshMillis = 0L

    val is24HourFormat: StateFlow<Boolean> = preferencesDataSource.is24HourFormat
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val nextAlarmState: StateFlow<vn.io.litever.remind.core.model.NextAlarmUiState> = alarmRepository.getAllAlarms()
        .map { alarms ->
            val enabledAlarms = alarms.filter { it.isEnabled }
            vn.io.litever.remind.core.model.calculateNextAlarm(enabledAlarms)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = vn.io.litever.remind.core.model.NextAlarmUiState.AllOff
        )

    val weather: StateFlow<vn.io.litever.remind.core.model.WeatherResponse?> = weatherRepository.getRemindWeather()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val todayBriefing: StateFlow<vn.io.litever.remind.core.model.TodayBriefing?> = todayRepository.getTodayBriefing()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val isProcessing: StateFlow<Boolean> = combine(weather, todayBriefing) { w, r ->
        w?.aiStatus == "processing" || r?.aiStatus == "processing"
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private var processingPollJob: Job? = null

    init {
        refresh(force = false)
        observeProcessingState()
    }

    private fun observeProcessingState() {
        viewModelScope.launch {
            isProcessing.collect { processing ->
                processingPollJob?.cancel()
                if (processing) {
                    processingPollJob = launch {
                        repeat(6) {
                            delay(10_000L)
                            if (isProcessing.value) {
                                refresh(force = true)
                            }
                        }
                    }
                }
            }
        }
    }

    fun refresh(force: Boolean = false) {
        if (_isRefreshing.value) return
        
        viewModelScope.launch {
            android.util.Log.d(TAG, "Starting refresh (force = $force)...")
            _isRefreshing.value = true
            try {
                coroutineScope {
                    launch { weatherRepository.refreshWeather(force = force) }
                    launch { todayRepository.refreshTodayBriefing(force = force) }
                }
                lastProcessingRefreshMillis = System.currentTimeMillis()
                android.util.Log.d(TAG, "Refresh completed successfully.")
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Refresh failed: ${e.message}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun onResume() {
        if (isProcessing.value) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastProcessingRefreshMillis > 15_000L) {
                refresh(force = true)
            }
        } else {
            // Auto refresh if cache expired (> 1 hour or new day)
            refresh(force = false)
        }
    }
}
