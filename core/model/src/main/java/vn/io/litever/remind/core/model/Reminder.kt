package vn.io.litever.remind.core.model

import java.io.Serializable

data class Reminder(
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val isEnabled: Boolean = true,
    val triggerType: ReminderTriggerType,
    val timeTrigger: TimeTrigger? = null,
    val action: ReminderAction = ReminderAction(),
    val currentSnoozeCount: Int = 0,
    val snoozeNextTriggerTime: Long? = null,
    val lastTriggeredTime: Long? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable {

    val isRecurring: Boolean
        get() = timeTrigger?.isRecurring == true

    fun canSnooze(): Boolean {
        return action.canSnooze(currentSnoozeCount)
    }
}
