package vn.io.litever.remind.core.domain.repository

import kotlinx.coroutines.flow.Flow
import vn.io.litever.remind.core.model.TodayBriefing

interface TodayRepository {
    fun getTodayBriefing(): Flow<TodayBriefing?>
    suspend fun refreshTodayBriefing(query: String? = null, force: Boolean = false)
}
