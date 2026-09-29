package vn.io.litever.remind.core.domain.util

import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.RepeatMode
import java.time.Instant
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

object ChecklistCycleUtil {

    /**
     * Determines whether a reminder's checklist items should be automatically reset
     * to uncompleted state based on its repeat cycle (daily, weekly, monthly, yearly, interval)
     * and when it was last reset / triggered.
     */
    fun shouldResetChecklist(
        reminder: Reminder,
        now: LocalDateTime = LocalDateTime.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Boolean {
        // Only reset if there are checklist items and at least one is completed
        if (reminder.checklist.isEmpty() || reminder.checklist.none { it.isCompleted }) {
            return false
        }

        // Must be a recurring reminder or an interval reminder
        val timeTrigger = reminder.timeTrigger
        val isInterval = reminder.triggerType == ReminderTriggerType.INTERVAL
        val repeatMode = timeTrigger?.repeatMode ?: RepeatMode.NONE
        val isRecurring = timeTrigger?.isRecurring == true || isInterval

        if (!isRecurring) {
            return false
        }

        val lastResetMillis = reminder.lastChecklistResetTime
            ?: reminder.lastTriggeredTime
            ?: reminder.createdAt

        val lastResetDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(lastResetMillis),
            zoneId
        )

        return when {
            repeatMode == RepeatMode.DAILY || isInterval -> {
                now.toLocalDate().isAfter(lastResetDateTime.toLocalDate())
            }
            repeatMode == RepeatMode.WEEKLY -> {
                now.toLocalDate().isAfter(lastResetDateTime.toLocalDate())
            }
            repeatMode == RepeatMode.MONTHLY -> {
                val currentYm = YearMonth.from(now)
                val lastYm = YearMonth.from(lastResetDateTime)
                currentYm.isAfter(lastYm) || (currentYm == lastYm && now.dayOfMonth > lastResetDateTime.dayOfMonth && timeTrigger?.monthlyDays?.contains(now.dayOfMonth) == true)
            }
            repeatMode == RepeatMode.YEARLY -> {
                now.year > lastResetDateTime.year || (now.year == lastResetDateTime.year && (now.monthValue > lastResetDateTime.monthValue || (now.monthValue == lastResetDateTime.monthValue && now.dayOfMonth > lastResetDateTime.dayOfMonth)))
            }
            else -> false
        }
    }
}
