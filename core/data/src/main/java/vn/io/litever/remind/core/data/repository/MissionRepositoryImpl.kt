package vn.io.litever.remind.core.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import vn.io.litever.remind.core.data.mapper.toEntity
import vn.io.litever.remind.core.data.mapper.toModel
import vn.io.litever.remind.core.database.dao.MissionDao
import vn.io.litever.remind.core.database.dao.PhraseDao
import vn.io.litever.remind.core.domain.repository.MissionRepository
import vn.io.litever.remind.core.model.Mission
import vn.io.litever.remind.core.model.Phrase
import javax.inject.Inject

@Serializable
private data class PhraseDto(
    val id: Long,
    val content: String
)

@Serializable
private data class PredefinedPhrasesDto(
    val motivation: List<PhraseDto>,
    val basic: List<PhraseDto>
)

class MissionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val missionDao: MissionDao,
    private val phraseDao: PhraseDao
) : MissionRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getMissionsForAlarm(alarmId: Long): Flow<List<Mission>> {
        return missionDao.getMissionsForAlarm(alarmId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    override suspend fun saveMission(mission: Mission) {
        missionDao.insertMission(mission.toEntity())
    }

    override suspend fun deleteMission(mission: Mission) {
        missionDao.deleteMission(mission.toEntity())
    }

    override suspend fun deleteMissionsForAlarm(alarmId: Long) {
        missionDao.deleteMissionsForAlarm(alarmId)
    }

    override suspend fun getPredefinedPhrases(): Map<String, List<Phrase>> {
        return try {
            val jsonString = context.assets.open("phrases.json").bufferedReader().use { it.readText() }
            val dto = json.decodeFromString<PredefinedPhrasesDto>(jsonString)
            mapOf<String, List<Phrase>>(
                "motivation" to dto.motivation.map { item ->
                    Phrase(id = item.id, content = item.content, categoryId = "motivation", isCustom = false)
                },
                "basic" to dto.basic.map { item ->
                    Phrase(id = item.id, content = item.content, categoryId = "basic", isCustom = false)
                }
            )
        } catch (e: Exception) {
            emptyMap<String, List<Phrase>>()
        }
    }

    override fun getCustomPhrases(alarmId: Long): Flow<List<Phrase>> {
        return phraseDao.getCustomPhrasesForAlarm(alarmId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    override suspend fun getPhrasesByIds(ids: List<Long>, alarmId: Long): List<Phrase> {
        val predefined = getPredefinedPhrases().values.flatten().filter { ids.contains(it.id) }
        val custom = phraseDao.getPhrasesByIds(ids).map { it.toModel() }
        return predefined + custom
    }

    override suspend fun savePhrase(phrase: Phrase) {
        val safePhrase = phrase.copy(content = phrase.content.take(128))
        phraseDao.insertPhrase(safePhrase.toEntity())
    }

    override suspend fun deletePhrase(phrase: Phrase) {
        phraseDao.deletePhrase(phrase.toEntity())
    }
}










