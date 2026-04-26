package vn.io.litever.remind.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val WEATHER_JSON = stringPreferencesKey("weather_json")
        val LAST_UPDATED_MILLIS = longPreferencesKey("weather_last_updated_millis")
        val LOCATION_URL = stringPreferencesKey("weather_location_url")
    }

    val weatherJson: Flow<String?> = dataStore.data.map { it[PreferencesKeys.WEATHER_JSON] }
    val lastUpdatedMillis: Flow<Long> = dataStore.data.map { it[PreferencesKeys.LAST_UPDATED_MILLIS] ?: 0L }
    val locationUrl: Flow<String> = dataStore.data.map { it[PreferencesKeys.LOCATION_URL] ?: "" }

    suspend fun saveWeather(json: String, timestamp: Long, locationUrl: String?) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEATHER_JSON] = json
            preferences[PreferencesKeys.LAST_UPDATED_MILLIS] = timestamp
            if (locationUrl != null) {
                preferences[PreferencesKeys.LOCATION_URL] = locationUrl
            }
        }
    }
}
