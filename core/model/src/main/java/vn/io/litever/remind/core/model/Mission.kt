package vn.io.litever.remind.core.model

import java.io.Serializable

enum class MissionType : Serializable {
    TYPING, MATH, QR_CODE, SHAKE, STEP, COLOR_MATCH, TAP_CHALLENGE, FIND_ITEM, MEMORY_FIND_COLOR_TILES
}

data class Mission(
    val id: Long = 0,
    val alarmId: Long,
    val type: MissionType,
    val order: Int,
    val repeatCount: Int = 1,
    val config: MissionConfig? = null
) : Serializable

enum class TypingMode : Serializable {
    NORMAL, SHUFFLE_WORDS, SHUFFLE_CHARS
}

sealed interface MissionConfig : Serializable

data class TypingMissionConfig(
    val selectedPhraseIds: List<Long> = emptyList(),
    val mode: TypingMode = TypingMode.NORMAL
) : MissionConfig

enum class MathDifficulty { EASY, NORMAL, HARD }

data class MathMissionConfig(
    val difficulty: MathDifficulty = MathDifficulty.NORMAL
) : MissionConfig

data class MemoryTilesMissionConfig(
    val gridSize: Int = 3,
    val targetTiles: Int = 3
) : MissionConfig

data class MemoryGameBoard(
    val gridSize: Int,
    val targetTiles: Int,
    val targetIndices: List<Int>
)








