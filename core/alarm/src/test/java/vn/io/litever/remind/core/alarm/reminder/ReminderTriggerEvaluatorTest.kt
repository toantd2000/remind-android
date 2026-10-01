package vn.io.litever.remind.core.alarm.reminder

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import java.time.LocalDateTime

class ReminderTriggerEvaluatorTest {

    private val evaluator = ReminderTriggerEvaluator()

    @Test
    fun evaluateTimeTrigger_enabledFixedTime_returnsTriggered() {
        val reminder = Reminder(
            id = 1L,
            title = "Morning Medicine",
            isEnabled = true,
            triggerType = ReminderTriggerType.FIXED_TIME
        )
        val result = evaluator.evaluateTimeTrigger(reminder, LocalDateTime.now())
        assertTrue(result.isTriggered)
    }

    @Test
    fun evaluateTimeTrigger_enabledInterval_returnsTriggered() {
        val reminder = Reminder(
            id = 2L,
            title = "Drink Water",
            isEnabled = true,
            triggerType = ReminderTriggerType.INTERVAL
        )
        val result = evaluator.evaluateTimeTrigger(reminder, LocalDateTime.now())
        assertTrue(result.isTriggered)
    }

    @Test
    fun evaluateTimeTrigger_disabled_returnsNotTriggered() {
        val reminder = Reminder(
            id = 3L,
            title = "Disabled",
            isEnabled = false,
            triggerType = ReminderTriggerType.FIXED_TIME
        )
        val result = evaluator.evaluateTimeTrigger(reminder, LocalDateTime.now())
        assertFalse(result.isTriggered)
    }
}
