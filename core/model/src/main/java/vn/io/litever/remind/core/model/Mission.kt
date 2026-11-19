package vn.io.litever.remind.core.model

import java.io.Serializable

enum class MissionType : Serializable {
    TYPING,
    MATH,
    MEMORY_FIND_COLOR_TILES,
    SHAKE,
    BARCODE,
    RANDOM
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

enum class MathDifficulty { EASY, NORMAL, HARD, VERY_HARD }

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

enum class ShakeSensitivity : Serializable { EASY, NORMAL, HARD }

data class ShakeMissionConfig(
    val targetCount: Int = 30,
    val sensitivity: ShakeSensitivity = ShakeSensitivity.NORMAL
) : MissionConfig

data class BarcodeMissionConfig(
    val targetBarcode: String = "",
    val label: String = ""
) : MissionConfig

data class RandomMissionConfig(
    val candidateTypes: List<MissionType> = listOf(
        MissionType.MATH,
        MissionType.TYPING,
        MissionType.MEMORY_FIND_COLOR_TILES,
        MissionType.SHAKE
    ),
    val difficulty: MathDifficulty = MathDifficulty.NORMAL
) : MissionConfig








