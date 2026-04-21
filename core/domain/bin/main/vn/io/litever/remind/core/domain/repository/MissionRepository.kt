package vn.io.litever.remind.core.domain.repository

import kotlinx.coroutines.flow.Flow
import vn.io.litever.remind.core.model.Mission
import vn.io.litever.remind.core.model.Phrase

interface MissionRepository {
    fun getMissionsForReminder(reminderId: Long): Flow<List<Mission>>
    suspend fun saveMission(mission: Mission)
    suspend fun deleteMission(mission: Mission)
    suspend fun deleteMissionsForReminder(reminderId: Long)
    suspend fun getPredefinedPhrases(): Map<String, List<Phrase>>
    fun getCustomPhrases(reminderId: Long): Flow<List<Phrase>>
    suspend fun getPhrasesByIds(ids: List<Long>, reminderId: Long): List<Phrase>
    suspend fun savePhrase(phrase: Phrase)
    suspend fun deletePhrase(phrase: Phrase)
}
