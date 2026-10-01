package vn.io.litever.remind.core.alarm.reminder

import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import java.time.LocalDateTime

data class ReminderEvaluationResult(
    val reminder: Reminder,
    val isTriggered: Boolean,
    val reason: String = ""
)

class ReminderTriggerEvaluator {
    fun evaluateTimeTrigger(
        reminder: Reminder,
        now: LocalDateTime = LocalDateTime.now()
    ): ReminderEvaluationResult {
        if (!reminder.isEnabled) {
            return ReminderEvaluationResult(reminder, isTriggered = false, reason = "Disabled")
        }

        if (reminder.triggerType != ReminderTriggerType.FIXED_TIME && reminder.triggerType != ReminderTriggerType.INTERVAL) {
            return ReminderEvaluationResult(reminder, isTriggered = false, reason = "Not a time trigger")
        }

        return ReminderEvaluationResult(reminder, isTriggered = true, reason = "Time triggered")
    }
}
