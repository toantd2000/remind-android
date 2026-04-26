package vn.io.litever.remind.core.domain.repository

import kotlinx.coroutines.flow.Flow
import vn.io.litever.remind.core.model.ReminderResponse

interface ReminderRepository {
    fun getReminder(): Flow<ReminderResponse?>
    suspend fun refreshReminder(query: String? = null, force: Boolean = false)
}
