package vn.io.litever.alarm.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AlarmPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val is24HourFormat: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_24_HOUR_FORMAT_KEY] ?: true
    }

    suspend fun set24HourFormat(is24Hour: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_24_HOUR_FORMAT_KEY] = is24Hour
        }
    }

    companion object {
        val IS_24_HOUR_FORMAT_KEY = booleanPreferencesKey("is_24_hour_format")
    }
}
