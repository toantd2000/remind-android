package vn.io.litever.remind.core.network

import retrofit2.http.GET
import retrofit2.http.Query
import vn.io.litever.remind.core.model.TodayBriefing

interface TodayApi {
    @GET("reminder")
    suspend fun getTodayBriefing(
        @Query("q") query: String? = null,
        @Query("lang") lang: String = "vi"
    ): TodayBriefing
}
