package vn.io.litever.remind.core.domain.repository

import kotlinx.coroutines.flow.Flow
import vn.io.litever.remind.core.model.WeatherResponse

interface WeatherRepository {
    fun getRemindWeather(): Flow<WeatherResponse?>
    suspend fun refreshWeather(force: Boolean = false)
}
