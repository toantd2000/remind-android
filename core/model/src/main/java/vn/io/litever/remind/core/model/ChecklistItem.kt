package vn.io.litever.remind.core.model

import java.io.Serializable

data class ChecklistItem(
    val id: Long = 0,
    val reminderId: Long = 0,
    val text: String,
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0
) : Serializable
