package vn.io.litever.alarm.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import android.text.format.DateFormat

class AlarmPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val context: Context
) {
    val timeFormat: Flow<String> = dataStore.data.map { preferences ->
        preferences[TIME_FORMAT_KEY] ?: "SYSTEM"
    }

    val is24HourFormat: Flow<Boolean> = dataStore.data.map { preferences ->
        when (preferences[TIME_FORMAT_KEY] ?: "SYSTEM") {
            "H12" -> false
            "H24" -> true
            else -> DateFormat.is24HourFormat(context)
        }
    }

    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY] ?: "SYSTEM"
    }

    val colorPalette: Flow<String> = dataStore.data.map { preferences ->
        preferences[COLOR_PALETTE_KEY] ?: "DEFAULT"
    }

    suspend fun set24HourFormat(is24Hour: Boolean) {
        dataStore.edit { preferences ->
            preferences[TIME_FORMAT_KEY] = if (is24Hour) "H24" else "H12"
        }
    }

    suspend fun setTimeFormat(format: String) {
        dataStore.edit { preferences ->
            preferences[TIME_FORMAT_KEY] = format
        }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }

    suspend fun setColorPalette(palette: String) {
        dataStore.edit { preferences ->
            preferences[COLOR_PALETTE_KEY] = palette
        }
    }

    companion object {
        val TIME_FORMAT_KEY = stringPreferencesKey("time_format")
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val COLOR_PALETTE_KEY = stringPreferencesKey("color_palette")
    }
}
