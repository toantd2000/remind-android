package vn.io.litever.remind.core.network

import retrofit2.http.GET
import retrofit2.http.Query
import vn.io.litever.remind.core.model.ReminderResponse

interface ReminderApi {
    @GET("reminder")
    suspend fun getReminder(
        @Query("q") query: String? = null,
        @Query("lang") lang: String = "vi"
    ): ReminderResponse
}
