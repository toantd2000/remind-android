package vn.io.litever.remind.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.datastore.WeatherPreferencesDataSource
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.model.ReminderResponse
import vn.io.litever.remind.core.network.ReminderApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val reminderApi: ReminderApi,
    private val preferencesDataSource: WeatherPreferencesDataSource,
    private val alarmPreferencesDataSource: AlarmPreferencesDataSource,
    private val json: Json
) : ReminderRepository {

    private suspend fun getCurrentLanguage(): String {
        return alarmPreferencesDataSource.language.first()
    }

    override fun getReminder(): Flow<ReminderResponse?> {
        return preferencesDataSource.reminderJson.map { jsonString ->
            if (jsonString != null) {
                try {
                    json.decodeFromString<ReminderResponse>(jsonString)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }

    override suspend fun refreshReminder(query: String?, force: Boolean) {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val lastUpdatedDate = preferencesDataSource.reminderLastUpdatedDate.first()
        val cachedLang = preferencesDataSource.reminderCachedLanguage.first()
        val currentLang = getCurrentLanguage()

        // Only refresh if it's a new day or language changed, unless forced
        if (!force && today == lastUpdatedDate && cachedLang == currentLang) {
            return
        }

        try {
            // Always pass empty string for automatic fetch as per user request
            val finalQuery = query ?: ""
            val response = reminderApi.getReminder(query = finalQuery, lang = currentLang)
            val jsonString = json.encodeToString(response)
            
            // Save reminder data and update the cached language
            preferencesDataSource.saveReminder(jsonString, today, currentLang)
        } catch (e: Exception) {
            // Log error or handle failure
        }
    }
}
