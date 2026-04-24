package vn.io.litever.remind.core.data.mapper

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import vn.io.litever.remind.core.database.model.MissionEntity
import vn.io.litever.remind.core.model.*

@Serializable
internal sealed class MissionConfigDto {
    @Serializable
    internal data class Typing(val selectedPhraseIds: List<Long>) : MissionConfigDto()
    
    @Serializable
    internal data class Math(val difficulty: MathDifficulty) : MissionConfigDto()
}

private val json = Json { ignoreUnknownKeys = true }

fun MissionEntity.toModel(): Mission {
    val config = configJson?.let {
        try {
            val dto = json.decodeFromString<MissionConfigDto>(it)
            when (dto) {
                is MissionConfigDto.Typing -> TypingMissionConfig(dto.selectedPhraseIds)
                is MissionConfigDto.Math -> MathMissionConfig(dto.difficulty)
            } as MissionConfig
        } catch (e: Exception) {
            null
        }
    }
    
    return Mission(
        id = id,
        alarmId = alarmId,
        type = MissionType.valueOf(type),
        order = missionOrder,
        repeatCount = repeatCount,
        config = config
    )
}

fun Mission.toEntity(): MissionEntity {
    val configJsonString = config?.let { configModel ->
        val dto = when (configModel) {
            is TypingMissionConfig -> MissionConfigDto.Typing(configModel.selectedPhraseIds)
            is MathMissionConfig -> MissionConfigDto.Math(configModel.difficulty)
            else -> null
        }
        dto?.let { json.encodeToString(MissionConfigDto.serializer(), it) }
    }
    
    return MissionEntity(
        id = id,
        alarmId = alarmId,
        type = type.name,
        missionOrder = order,
        repeatCount = repeatCount,
        configJson = configJsonString
    )
}










