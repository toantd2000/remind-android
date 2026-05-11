package vn.io.litever.remind.core.domain.repository

import kotlinx.coroutines.flow.Flow
import vn.io.litever.remind.core.model.Mission
import vn.io.litever.remind.core.model.Phrase

interface MissionRepository {
    fun getMissionsForAlarm(alarmId: Long): Flow<List<Mission>>
    suspend fun saveMission(mission: Mission): Long
    suspend fun deleteMission(mission: Mission)
    suspend fun deleteMissionsForAlarm(alarmId: Long)
    suspend fun getPredefinedPhrases(): Map<String, List<Phrase>>
    fun getCustomPhrases(alarmId: Long): Flow<List<Phrase>>
    suspend fun getPhrasesByIds(ids: List<Long>, alarmId: Long): List<Phrase>
    suspend fun savePhrase(phrase: Phrase): Long
    suspend fun deletePhrase(phrase: Phrase)
}










