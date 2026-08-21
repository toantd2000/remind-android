package vn.io.litever.remind.core.model

import java.io.Serializable

data class Reminder(
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val isEnabled: Boolean = true,
    val triggerType: ReminderTriggerType,
    val timeTrigger: TimeTrigger? = null,
    val locationTrigger: LocationTrigger? = null,
    val wifiTrigger: WifiTrigger? = null,
    val checklist: List<ChecklistItem> = emptyList(),
    val notificationStyle: NotificationStyle = NotificationStyle.STANDARD,
    val isPersistentAlert: Boolean = false,
    val ringtoneUri: String? = null,
    val vibrationEnabled: Boolean = true,
    val volume: Int = 100,
    val snoozeEnabled: Boolean = true,
    val snoozeIntervalMinutes: Int = 5,
    val snoozeRepeatCount: Int = 3,
    val currentSnoozeCount: Int = 0,
    val snoozeNextTriggerTime: Long? = null,
    val lastTriggeredTime: Long? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable {

    val totalChecklistCount: Int
        get() = checklist.size

    val completedChecklistCount: Int
        get() = checklist.count { it.isCompleted }

    val checklistProgress: Float
        get() = if (checklist.isEmpty()) 0f else completedChecklistCount.toFloat() / checklist.size

    val isAllChecklistCompleted: Boolean
        get() = checklist.isNotEmpty() && checklist.all { it.isCompleted }

    fun canSnooze(): Boolean {
        return snoozeEnabled && (snoozeRepeatCount == -1 || currentSnoozeCount < snoozeRepeatCount)
    }
}
