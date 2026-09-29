package vn.io.litever.remind.core.model
import java.io.Serializable
data class ReminderMonthDay(
    val month: Int,
    val day: Int,
    val isLastDay: Boolean = false
) : Serializable
