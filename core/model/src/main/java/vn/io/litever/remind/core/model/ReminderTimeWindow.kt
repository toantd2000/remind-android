package vn.io.litever.remind.core.model

import java.io.Serializable
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

data class ReminderTimeWindow(
    val startTime: LocalTime,
    val endTime: LocalTime,
    val activeDays: List<DayOfWeek> = emptyList()
) : Serializable {

    fun isWithinWindow(dateTime: LocalDateTime): Boolean {
        val currentTime = dateTime.toLocalTime()
        val currentDay = DayOfWeek.valueOf(dateTime.dayOfWeek.name)
        val yesterday = DayOfWeek.valueOf(dateTime.minusDays(1).dayOfWeek.name)

        return if (!startTime.isAfter(endTime)) {
            // Same-day window (e.g. 09:00 -> 17:00, or startTime == endTime)
            val isDayActive = activeDays.isEmpty() || activeDays.contains(currentDay)
            val isTimeInRange = !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime)
            isDayActive && isTimeInRange
        } else {
            // Overnight window crossing midnight (e.g. 22:00 -> 04:00)
            // Evening part (>= startTime) belongs to TODAY's schedule
            val isEveningPart = !currentTime.isBefore(startTime)
            val isEveningActive = activeDays.isEmpty() || activeDays.contains(currentDay)

            // Morning part (<= endTime) belongs to YESTERDAY's schedule
            val isMorningPart = !currentTime.isAfter(endTime)
            val isMorningActive = activeDays.isEmpty() || activeDays.contains(yesterday)

            (isEveningPart && isEveningActive) || (isMorningPart && isMorningActive)
        }
    }

    companion object {
        val ALL_DAY = ReminderTimeWindow(
            startTime = LocalTime.of(0, 0),
            endTime = LocalTime.of(23, 59),
            activeDays = emptyList()
        )
    }
}
