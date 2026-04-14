package vn.io.litever.remind.core.datastore

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
import java.util.Locale

class ReminderPreferencesDataSource @Inject constructor(
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

    val language: Flow<String> = dataStore.data.map { preferences ->

        preferences[LANGUAGE_KEY] ?: run {
            val systemLocale = Locale.getDefault().language
            if (systemLocale == "vi") "vi" else "en"
        }
    }

    val snoozeDuration: Flow<Int> = dataStore.data.map { it[ALARM_SNOOZE_DURATION] ?: 5 }
    val silenceDuration: Flow<Int> = dataStore.data.map { it[ALARM_SILENCE_DURATION] ?: 10 }
    val isIncreasingVolume: Flow<Boolean> = dataStore.data.map { it[ALARM_INCREASING_VOLUME] ?: false }
    val useBuiltInSpeaker: Flow<Boolean> = dataStore.data.map { it[ALARM_BUILT_IN_SPEAKER] ?: true }
    val isPreNotificationEnabled: Flow<Boolean> = dataStore.data.map { it[ALARM_PRE_NOTIFICATION] ?: true }

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

    suspend fun setLanguage(language: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }

    suspend fun setSnoozeDuration(duration: Int) {
        dataStore.edit { it[ALARM_SNOOZE_DURATION] = duration }
    }

    suspend fun setSilenceDuration(duration: Int) {
        dataStore.edit { it[ALARM_SILENCE_DURATION] = duration }
    }

    suspend fun setIncreasingVolume(enabled: Boolean) {
        dataStore.edit { it[ALARM_INCREASING_VOLUME] = enabled }
    }

    suspend fun setBuiltInSpeaker(enabled: Boolean) {
        dataStore.edit { it[ALARM_BUILT_IN_SPEAKER] = enabled }
    }

    suspend fun setPreNotificationEnabled(enabled: Boolean) {
        dataStore.edit { it[ALARM_PRE_NOTIFICATION] = enabled }
    }

    companion object {
        val TIME_FORMAT_KEY = stringPreferencesKey("time_format")
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val COLOR_PALETTE_KEY = stringPreferencesKey("color_palette")
        val LANGUAGE_KEY = stringPreferencesKey("language")
        val ALARM_SNOOZE_DURATION = androidx.datastore.preferences.core.intPreferencesKey("alarm_snooze_duration")
        val ALARM_SILENCE_DURATION = androidx.datastore.preferences.core.intPreferencesKey("alarm_silence_duration")
        val ALARM_INCREASING_VOLUME = booleanPreferencesKey("alarm_increasing_volume")
        val ALARM_BUILT_IN_SPEAKER = booleanPreferencesKey("alarm_built_in_speaker")
        val ALARM_PRE_NOTIFICATION = booleanPreferencesKey("alarm_pre_notification")
    }
}
