package vn.io.litever.remind.core.model

import java.io.Serializable
import java.time.LocalDate

data class LocationTrigger(
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float = 100f,
    val locationName: String = "",
    val transitionType: GeofenceTransitionType = GeofenceTransitionType.ENTER,
    val timeWindow: ReminderTimeWindow? = null,
    val timeWindows: List<ReminderTimeWindow> = if (timeWindow != null) listOf(timeWindow) else emptyList(),
    val date: LocalDate? = null,
    val repeatDays: List<DayOfWeek> = emptyList(),
    val monthlyDays: List<Int> = emptyList(),
    val yearlyDates: List<ReminderMonthDay> = emptyList()
) : Serializable {
    val activeTimeWindows: List<ReminderTimeWindow>
        get() = if (timeWindows.isNotEmpty()) timeWindows else if (timeWindow != null) listOf(timeWindow) else emptyList()

    val isRecurring: Boolean
        get() = repeatDays.isNotEmpty() || monthlyDays.isNotEmpty() || yearlyDates.isNotEmpty()
}
