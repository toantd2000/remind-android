package vn.io.litever.remind.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    @SerialName("location_name") val locationName: String? = null,
    @SerialName("location_url") val locationUrl: String? = null,
    @SerialName("current") val current: CurrentWeather,
    @SerialName("daily_summary") val dailySummary: DailySummary,
    @SerialName("hourly_forecast") val hourlyForecast: List<HourlyForecast>,
    @SerialName("ai_analysis") val aiAnalysis: AiAnalysis,
    @SerialName("ai_status") val aiStatus: String = "completed"
)

@Serializable
data class CurrentWeather(
    @SerialName("last_updated") val lastUpdated: String,
    @SerialName("temp_c") val tempC: Double,
    @SerialName("feelslike_c") val feelsLikeC: Double,
    @SerialName("is_day") val isDay: Int,
    @SerialName("condition_text") val conditionText: String,
    @SerialName("condition_icon") val conditionIcon: String,
    @SerialName("condition_code") val conditionCode: Int,
    @SerialName("aqi_index") val aqiIndex: Int,
    @SerialName("precip_mm") val precipMm: Double
)

@Serializable
data class DailySummary(
    @SerialName("max_temp") val maxTemp: Double,
    @SerialName("min_temp") val minTemp: Double,
    @SerialName("chance_of_rain") val chanceOfRain: Int
)

@Serializable
data class HourlyForecast(
    @SerialName("time") val time: String,
    @SerialName("temp_c") val tempC: Double,
    @SerialName("chance_of_rain") val chanceOfRain: Int,
    @SerialName("condition_text") val conditionText: String
)

@Serializable
data class AiAnalysis(
    @SerialName("hint") val hint: String
)

@Serializable
data class WeatherLocation(
    val name: String,
    val region: String,
    val country: String,
    val url: String? = null
)

@Serializable
data class LocationSearchResponse(
    val id: Int,
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val url: String
)
