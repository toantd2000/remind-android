package vn.io.litever.remind.core.model

import java.io.Serializable

data class Phrase(
    val id: Long = 0,
    val content: String,
    val categoryId: String, // "motivation", "basic", "custom"
    val isCustom: Boolean = false,
    val isShared: Boolean = true,
    val reminderId: Long? = null
) : Serializable

data class PhraseCategory(
    val id: String,
    val name: String,
    val phrases: List<Phrase> = emptyList()
) : Serializable
