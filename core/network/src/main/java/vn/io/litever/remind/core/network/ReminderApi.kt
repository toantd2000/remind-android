package vn.io.litever.remind.core.network

import retrofit2.http.GET
import retrofit2.http.Query

interface ReminderApi {
    @GET("reminder")
    suspend fun getReminders(
        @Query("lang") lang: String = "vi"
    ): Any // To be defined when API is ready
}
