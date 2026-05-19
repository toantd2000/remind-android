package vn.io.litever.remind.features.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import javax.inject.Inject

data class SettingsUiState(
    val is24HourFormat: Boolean = true,
    val timeFormat: String = "SYSTEM",
    val themeMode: String = "SYSTEM",
    val colorPalette: String = "DEFAULT",
    val language: String = if (java.util.Locale.getDefault().language == "vi") "vi" else "en",
    val useBuiltInSpeaker: Boolean = true,
    val isPreNotificationEnabled: Boolean = true,
    val adsDisabledUntil: Long = 0L
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesDataSource: AlarmPreferencesDataSource
) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<SettingsUiState> = combine(
        listOf<Flow<Any>>(
            preferencesDataSource.is24HourFormat,
            preferencesDataSource.timeFormat,
            preferencesDataSource.themeMode,
            preferencesDataSource.colorPalette,
            preferencesDataSource.language,
            preferencesDataSource.useBuiltInSpeaker,
            preferencesDataSource.isPreNotificationEnabled,
            preferencesDataSource.adsDisabledUntil
        )
    ) { params ->
        SettingsUiState(
            is24HourFormat = params[0] as Boolean,
            timeFormat = params[1] as String,
            themeMode = params[2] as String,
            colorPalette = params[3] as String,
            language = params[4] as String,
            useBuiltInSpeaker = params[5] as Boolean,
            isPreNotificationEnabled = params[6] as Boolean,
            adsDisabledUntil = params[7] as Long
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


    fun setBuiltInSpeaker(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataSource.setBuiltInSpeaker(enabled)
        }
    }

    fun setPreNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataSource.setPreNotificationEnabled(enabled)
        }
    }

    fun disableAdsFor24Hours(context: android.content.Context) {
        viewModelScope.launch {
            val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
            val duration = if (isDebug) 30 * 1000L else 24 * 60 * 60 * 1000L
            preferencesDataSource.setAdsDisabledUntil(System.currentTimeMillis() + duration)
        }
    }
}










