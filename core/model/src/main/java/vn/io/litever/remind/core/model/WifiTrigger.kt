package vn.io.litever.remind.core.model

import java.io.Serializable
import java.time.LocalDate

data class WifiTrigger(
    val ssid: String,
    val bssid: String? = null,
    val triggerType: WifiTriggerType = WifiTriggerType.ON_CONNECT,
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
