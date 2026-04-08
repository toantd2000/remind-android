package vn.io.litever.alarm.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

import androidx.datastore.preferences.core.stringPreferencesKey

class AlarmPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val is24HourFormat: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_24_HOUR_FORMAT_KEY] ?: true
    }

    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY] ?: "SYSTEM"
    }

    suspend fun set24HourFormat(is24Hour: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_24_HOUR_FORMAT_KEY] = is24Hour
        }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }

    companion object {
        val IS_24_HOUR_FORMAT_KEY = booleanPreferencesKey("is_24_hour_format")
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }
}
