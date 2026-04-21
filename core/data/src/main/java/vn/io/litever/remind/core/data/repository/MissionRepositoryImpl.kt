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
private data class PredefinedPhrasesDto(
    val motivation: List<String>,
    val basic: List<String>
)

class MissionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val missionDao: MissionDao,
    private val phraseDao: PhraseDao
) : MissionRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getMissionsForReminder(reminderId: Long): Flow<List<Mission>> {
        return missionDao.getMissionsForReminder(reminderId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    override suspend fun saveMission(mission: Mission) {
        missionDao.insertMission(mission.toEntity())
    }

    override suspend fun deleteMission(mission: Mission) {
        missionDao.deleteMission(mission.toEntity())
    }

    override suspend fun deleteMissionsForReminder(reminderId: Long) {
        missionDao.deleteMissionsForReminder(reminderId)
    }

    override suspend fun getPredefinedPhrases(): Map<String, List<Phrase>> {
        return try {
            val jsonString = context.assets.open("phrases.json").bufferedReader().use { it.readText() }
            val dto = json.decodeFromString<PredefinedPhrasesDto>(jsonString)
            mapOf<String, List<Phrase>>(
                "motivation" to dto.motivation.map { content ->
                    Phrase(content = content, categoryId = "motivation", isCustom = false)
                },
                "basic" to dto.basic.map { content ->
                    Phrase(content = content, categoryId = "basic", isCustom = false)
                }
            )
        } catch (e: Exception) {
            emptyMap<String, List<Phrase>>()
        }
    }

    override fun getCustomPhrases(reminderId: Long): Flow<List<Phrase>> {
        return phraseDao.getCustomPhrasesForReminder(reminderId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    override suspend fun getPhrasesByIds(ids: List<Long>, reminderId: Long): List<Phrase> {
        val predefined = getPredefinedPhrases().values.flatten().filter { ids.contains(it.id) }
        val custom = phraseDao.getPhrasesByIds(ids).map { it.toModel() }
        return predefined + custom
    }

    override suspend fun savePhrase(phrase: Phrase) {
        phraseDao.insertPhrase(phrase.toEntity())
    }

    override suspend fun deletePhrase(phrase: Phrase) {
        phraseDao.deletePhrase(phrase.toEntity())
    }
}
