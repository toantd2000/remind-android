package vn.io.litever.remind.core.model

import java.io.Serializable

enum class MissionType : Serializable {
    TYPING, MATH, QR_CODE, SHAKE, STEP, COLOR_MATCH, TAP_CHALLENGE, FIND_ITEM
}

data class Mission(
    val id: Long = 0,
    val alarmId: Long,
    val type: MissionType,
    val order: Int,
    val repeatCount: Int = 1,
    val config: MissionConfig? = null
) : Serializable

sealed interface MissionConfig : Serializable

data class TypingMissionConfig(
    val selectedPhraseIds: List<Long> = emptyList()
) : MissionConfig

enum class MathDifficulty { EASY, NORMAL, HARD }

data class MathMissionConfig(
    val difficulty: MathDifficulty = MathDifficulty.NORMAL
) : MissionConfig










