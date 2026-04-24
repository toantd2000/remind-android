package vn.io.litever.remind.core.model

import java.io.Serializable

enum class PhraseSource {
    SYSTEM,
    USER_SHARED,
    USER_PRIVATE
}

data class Phrase(
    val id: Long = 0,
    val content: String,
    val categoryId: String, // "motivation", "basic", "custom"
    val isCustom: Boolean = false,
    val isShared: Boolean = true,
    val alarmId: Long? = null
) : Serializable {
    val source: PhraseSource
        get() = when {
            !isCustom -> PhraseSource.SYSTEM
            isShared -> PhraseSource.USER_SHARED
            else -> PhraseSource.USER_PRIVATE
        }
}

data class PhraseCategory(
    val id: String,
    val name: String,
    val phrases: List<Phrase> = emptyList()
) : Serializable










