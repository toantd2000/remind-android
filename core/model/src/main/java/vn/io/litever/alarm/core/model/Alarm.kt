package vn.io.litever.alarm.core.model

import java.time.LocalTime

data class Alarm(
    val id: Long = 0,
    val time: LocalTime,
    val label: String = "",
    val isEnabled: Boolean = true,
    val repeatDays: List<DayOfWeek> = emptyList()
)

enum class DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}
