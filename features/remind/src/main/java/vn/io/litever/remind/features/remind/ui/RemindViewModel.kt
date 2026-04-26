package vn.io.litever.remind.features.remind.ui

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

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                coroutineScope {
                    launch { weatherRepository.refreshWeather(force = true) }
                    launch { reminderRepository.refreshReminder(force = true) }
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
