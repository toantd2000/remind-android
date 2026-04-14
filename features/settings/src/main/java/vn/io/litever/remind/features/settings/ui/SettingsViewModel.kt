package vn.io.litever.remind.features.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.datastore.ReminderPreferencesDataSource
import javax.inject.Inject

data class SettingsUiState(
    val is24HourFormat: Boolean = true,
    val timeFormat: String = "SYSTEM",
    val themeMode: String = "SYSTEM",
    val colorPalette: String = "DEFAULT",
    val language: String = "en"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesDataSource: ReminderPreferencesDataSource
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesDataSource.is24HourFormat,
        preferencesDataSource.timeFormat,
        preferencesDataSource.themeMode,
        preferencesDataSource.colorPalette,
        preferencesDataSource.language
    ) { is24Hour, timeFormat, theme, palette, lang ->
        SettingsUiState(
            is24HourFormat = is24Hour,
            timeFormat = timeFormat,
            themeMode = theme,
            colorPalette = palette,
            language = lang
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun setTimeFormat(format: String) {
        viewModelScope.launch {
            preferencesDataSource.setTimeFormat(format)
        }
    }

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

    fun setLanguage(language: String) {
        viewModelScope.launch {
            preferencesDataSource.setLanguage(language)
        }
    }
}
