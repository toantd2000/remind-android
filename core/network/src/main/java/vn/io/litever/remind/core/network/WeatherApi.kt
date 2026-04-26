package vn.io.litever.remind.core.network

import retrofit2.http.GET
import retrofit2.http.Query
import vn.io.litever.remind.core.model.WeatherResponse

interface WeatherApi {
    @GET("weather/remind")
    suspend fun getRemindWeather(
        @Query("q") query: String? = null,
        @Query("lang") lang: String = "vi"
    ): WeatherResponse

    @GET("weather/search")
    suspend fun searchLocation(
        @Query("q") query: String,
        @Query("lang") lang: String = "vi"
    ): List<vn.io.litever.remind.core.model.LocationSearchResponse>
}
