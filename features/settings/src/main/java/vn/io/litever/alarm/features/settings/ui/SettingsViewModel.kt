package vn.io.litever.alarm.features.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.io.litever.alarm.core.datastore.AlarmPreferencesDataSource
import javax.inject.Inject

import kotlinx.coroutines.flow.combine

data class SettingsUiState(
    val is24HourFormat: Boolean = true,
    val themeMode: String = "SYSTEM",
    val colorPalette: String = "DYNAMIC"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesDataSource: AlarmPreferencesDataSource
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesDataSource.is24HourFormat,
        preferencesDataSource.themeMode,
        preferencesDataSource.colorPalette
    ) { is24Hour, theme, palette ->
        SettingsUiState(
            is24HourFormat = is24Hour,
            themeMode = theme,
            colorPalette = palette
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun set24HourFormat(is24Hour: Boolean) {
        viewModelScope.launch {
            preferencesDataSource.set24HourFormat(is24Hour)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesDataSource.setThemeMode(mode)
        }
    }

    fun setColorPalette(palette: String) {
        viewModelScope.launch {
            preferencesDataSource.setColorPalette(palette)
        }
    }
}
