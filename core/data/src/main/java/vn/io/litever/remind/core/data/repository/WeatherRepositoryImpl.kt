package vn.io.litever.remind.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import vn.io.litever.remind.core.datastore.WeatherPreferencesDataSource
import vn.io.litever.remind.core.domain.repository.WeatherRepository
import vn.io.litever.remind.core.model.WeatherResponse
import vn.io.litever.remind.core.network.WeatherApi
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val weatherApi: WeatherApi,
    private val preferencesDataSource: WeatherPreferencesDataSource,
    private val json: Json
) : WeatherRepository {

    private fun getCurrentLanguage(): String {
        return Locale.getDefault().language
    }

    override fun getRemindWeather(): Flow<WeatherResponse?> {
        return preferencesDataSource.weatherJson.map { jsonString ->
            if (jsonString != null) {
                try {
                    json.decodeFromString<WeatherResponse>(jsonString)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }

    override suspend fun refreshWeather(force: Boolean) {
        val lastUpdated = preferencesDataSource.lastUpdatedMillis.first()
        val cachedLang = preferencesDataSource.cachedLanguage.first()
        val currentLang = getCurrentLanguage()
        val currentTime = System.currentTimeMillis()
        
        // Cache for 1 hour (3600000 ms), unless forced OR language changed
        if (!force && (currentTime - lastUpdated < 3600000) && (cachedLang == currentLang)) {
            return
        }

        // Use selected location name if available
        val savedName = preferencesDataSource.selectedLocationName.first()
        val query = if (savedName.isNotBlank()) savedName else ""

        try {
            val response = weatherApi.getRemindWeather(query = query, lang = currentLang)
            val jsonString = json.encodeToString(response)
            
            // Save weather data and update the location identifier for the next request
            preferencesDataSource.saveWeather(
                json = jsonString,
                timestamp = currentTime,
                locationUrl = response.locationUrl ?: response.locationName,
                lang = currentLang
            )
        } catch (e: Exception) {
            // Log error or handle failure
        }
    }

    override suspend fun searchLocation(query: String): List<vn.io.litever.remind.core.model.LocationSearchResponse> {
        return try {
            weatherApi.searchLocation(query, lang = getCurrentLanguage())
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun saveSelectedLocation(name: String, country: String) {
        preferencesDataSource.saveSelectedLocation(name, country)
    }

    override fun getSelectedLocationName(): Flow<String> = preferencesDataSource.selectedLocationName

    override fun getSelectedLocationCountry(): Flow<String> = preferencesDataSource.selectedLocationCountry
}
