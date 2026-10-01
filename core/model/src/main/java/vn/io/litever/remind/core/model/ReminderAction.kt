package vn.io.litever.remind.core.model

import java.io.Serializable

data class ReminderAction(
    val urgencyLevel: UrgencyLevel = UrgencyLevel.MEDIUM,
    val notificationStyle: NotificationStyle = NotificationStyle.FULL_SCREEN,
    val isPersistentAlert: Boolean = false,
    val autoSilenceMinutes: Int = 5,
    val useAlarmStream: Boolean = true,
    val ringtoneUri: String? = null,
    val vibrationEnabled: Boolean = true,
    val volume: Int = 100,
    val snoozeEnabled: Boolean = true,
    val snoozeIntervalMinutes: Int = 5
) : Serializable {

    fun canSnooze(): Boolean {
        if (urgencyLevel == UrgencyLevel.SOFT) return false
        return snoozeEnabled
    }
}
