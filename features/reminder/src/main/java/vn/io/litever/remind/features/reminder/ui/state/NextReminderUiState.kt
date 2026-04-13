package vn.io.litever.remind.features.reminder.ui.state

import vn.io.litever.remind.core.model.Reminder
import java.time.Duration
import java.time.LocalDateTime

sealed interface NextReminderUiState {
    object AllOff : NextReminderUiState
    data class Remaining(
        val days: Long,
        val hours: Long,
        val minutes: Long
    ) : NextReminderUiState
}

fun calculateNextReminder(enabledReminders: List<Reminder>): NextReminderUiState {
    if (enabledReminders.isEmpty()) return NextReminderUiState.AllOff
    
    val now = LocalDateTime.now()
    val nextOccurrences = enabledReminders.map { reminder ->
        reminder.getNextOccurrence(now)
    }
    
    val earliest = nextOccurrences.minOrNull() ?: return NextReminderUiState.AllOff
    val duration = Duration.between(now, earliest)
    
    val totalMinutes = duration.toMinutes()
    val days = duration.toDays()
    val hours = duration.toHours() % 24
    val minutes = totalMinutes % 60
    
    return NextReminderUiState.Remaining(days, hours, minutes)
}
