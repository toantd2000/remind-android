package vn.io.litever.remind.features.remind.ui

import android.content.ContentValues.TAG
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemindViewModel @Inject constructor(
    private val weatherRepository: vn.io.litever.remind.core.domain.repository.WeatherRepository,
    private val reminderRepository: vn.io.litever.remind.core.domain.repository.ReminderRepository
) : ViewModel() {
    private var lastProcessingRefreshMillis = 0L

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val weather: StateFlow<vn.io.litever.remind.core.model.WeatherResponse?> = weatherRepository.getRemindWeather()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val reminder: StateFlow<vn.io.litever.remind.core.model.ReminderResponse?> = reminderRepository.getReminder()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val isProcessing: StateFlow<Boolean> = combine(weather, reminder) { w, r ->
        w?.aiStatus == "processing" || r?.aiStatus == "processing"
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        refresh()
    }

    fun refresh() {
        if (_isRefreshing.value) return
        
        viewModelScope.launch {
            android.util.Log.d(TAG, "Starting refresh...")
            _isRefreshing.value = true
            try {
                coroutineScope {
                    launch { weatherRepository.refreshWeather(force = true) }
                    launch { reminderRepository.refreshReminder(force = true) }
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

    fun checkAndRefreshIfProcessing() {
        val currentTime = System.currentTimeMillis()
        if (isProcessing.value && (currentTime - lastProcessingRefreshMillis > 60000)) {
            refresh()
        }
    }
}
