package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.RepeatMode
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.features.reminders.R
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun interface ReminderStringProvider {
    fun getString(id: Int, vararg args: Any): String
}

data class NextTriggerDisplayInfo(
    val text: String,
    val icon: ImageVector,
    val isSnoozed: Boolean = false,
    val isInactive: Boolean = false
)

object ReminderCardFormatter {

    @Composable
    fun formatRecurrenceSchedule(timeTrigger: TimeTrigger?): String {
        if (timeTrigger == null) return ""
        return when (timeTrigger.repeatMode) {
            RepeatMode.NONE -> {
                val exactDate = timeTrigger.exactDate
                if (exactDate != null) {
                    formatDate(exactDate)
                } else {
                    stringResource(R.string.once_only)
                }
            }
            RepeatMode.DAILY -> stringResource(R.string.daily)
            RepeatMode.WEEKLY -> {
                val values = timeTrigger.repeatValues.distinct().sorted()
                if (values.isEmpty()) {
                    stringResource(R.string.repeat_interval_weekly)
                } else if (values.size == 7) {
                    stringResource(R.string.daily)
                } else if (values == listOf(1, 2, 3, 4, 5)) {
                    stringResource(R.string.weekdays)
                } else if (values == listOf(6, 7)) {
                    stringResource(R.string.weekends)
                } else {
                    val dayNames = listOf(
                        stringResource(R.string.day_mon),
                        stringResource(R.string.day_tue),
                        stringResource(R.string.day_wed),
                        stringResource(R.string.day_thu),
                        stringResource(R.string.day_fri),
                        stringResource(R.string.day_sat),
                        stringResource(R.string.day_sun)
                    )
                    values.mapNotNull { dayValue ->
                        if (dayValue in 1..7) dayNames[dayValue - 1] else null
                    }.joinToString(", ")
                }
            }
            RepeatMode.MONTHLY -> {
                val days = if (timeTrigger.monthlyDays.isNotEmpty()) timeTrigger.monthlyDays else timeTrigger.repeatValues
                if (days.isEmpty()) {
                    stringResource(R.string.repeat_interval_monthly)
                } else {
                    val sortedDays = days.distinct().sorted()
                    val dayFormat = stringResource(R.string.day_format)
                    val daysText = sortedDays.take(3).joinToString(", ") { String.format(dayFormat, it) }
                    val suffix = if (sortedDays.size > 3) "…" else ""
                    "$daysText$suffix"
                }
            }
            RepeatMode.YEARLY -> {
                val dates = timeTrigger.yearlyDates
                if (dates.isEmpty()) {
                    stringResource(R.string.repeat_interval_yearly)
                } else {
                    val datesText = dates.take(2).joinToString(", ") {
                        "${it.day.toString().padStart(2, '0')}/${it.month.toString().padStart(2, '0')}"
                    }
                    val suffix = if (dates.size > 2) "…" else ""
                    "$datesText$suffix"
                }
            }
        }
    }

    fun formatRecurrenceSchedule(timeTrigger: TimeTrigger?, provider: ReminderStringProvider): String {
        if (timeTrigger == null) return ""
        return when (timeTrigger.repeatMode) {
            RepeatMode.NONE -> {
                val exactDate = timeTrigger.exactDate
                if (exactDate != null) {
                    formatDate(exactDate)
                } else {
                    provider.getString(R.string.once_only)
                }
            }
            RepeatMode.DAILY -> provider.getString(R.string.daily)
            RepeatMode.WEEKLY -> {
                val values = timeTrigger.repeatValues.distinct().sorted()
                if (values.isEmpty()) {
                    provider.getString(R.string.repeat_interval_weekly)
                } else if (values.size == 7) {
                    provider.getString(R.string.daily)
                } else if (values == listOf(1, 2, 3, 4, 5)) {
                    provider.getString(R.string.weekdays)
                } else if (values == listOf(6, 7)) {
                    provider.getString(R.string.weekends)
                } else {
                    values.mapNotNull { dayValue ->
                        when (dayValue) {
                            1 -> provider.getString(R.string.day_mon)
                            2 -> provider.getString(R.string.day_tue)
                            3 -> provider.getString(R.string.day_wed)
                            4 -> provider.getString(R.string.day_thu)
                            5 -> provider.getString(R.string.day_fri)
                            6 -> provider.getString(R.string.day_sat)
                            7 -> provider.getString(R.string.day_sun)
                            else -> null
                        }
                    }.joinToString(", ")
                }
            }
            RepeatMode.MONTHLY -> {
                val days = if (timeTrigger.monthlyDays.isNotEmpty()) timeTrigger.monthlyDays else timeTrigger.repeatValues
                if (days.isEmpty()) {
                    provider.getString(R.string.repeat_interval_monthly)
                } else {
                    val sortedDays = days.distinct().sorted()
                    val daysText = sortedDays.take(3).joinToString(", ") { provider.getString(R.string.day_format, it) }
                    val suffix = if (sortedDays.size > 3) "…" else ""
                    "$daysText$suffix"
                }
            }
            RepeatMode.YEARLY -> {
                val dates = timeTrigger.yearlyDates
                if (dates.isEmpty()) {
                    provider.getString(R.string.repeat_interval_yearly)
                } else {
                    val datesText = dates.take(2).joinToString(", ") {
                        "${it.day.toString().padStart(2, '0')}/${it.month.toString().padStart(2, '0')}"
                    }
                    val suffix = if (dates.size > 2) "…" else ""
                    "$datesText$suffix"
                }
            }
        }
    }

    @Composable
    fun formatIntervalFrequency(intervalMinutes: Int?): String {
        val mins = (intervalMinutes ?: 60).coerceAtLeast(1)
        return when {
            mins % 60 == 0 -> {
                val hours = mins / 60
                stringResource(R.string.interval_hours_format, hours)
            }
            mins > 60 -> {
                val hours = mins / 60
                val remainder = mins % 60
                stringResource(R.string.interval_hours_minutes_format, hours, remainder)
            }
            else -> {
                stringResource(R.string.interval_minutes_format, mins)
            }
        }
    }

    fun formatIntervalFrequency(intervalMinutes: Int?, provider: ReminderStringProvider): String {
        val mins = (intervalMinutes ?: 60).coerceAtLeast(1)
        return when {
            mins % 60 == 0 -> {
                val hours = mins / 60
                provider.getString(R.string.interval_hours_format, hours)
            }
            mins > 60 -> {
                val hours = mins / 60
                val remainder = mins % 60
                provider.getString(R.string.interval_hours_minutes_format, hours, remainder)
            }
            else -> {
                provider.getString(R.string.interval_minutes_format, mins)
            }
        }
    }

    @Composable
    fun formatTimeWindows(timeWindows: List<ReminderTimeWindow>, is24HourFormat: Boolean): String {
        if (timeWindows.isEmpty()) return ""
        val isAllDay = timeWindows.size == 1 &&
            timeWindows[0].startTime == LocalTime.of(0, 0) &&
            timeWindows[0].endTime == LocalTime.of(23, 59)

        if (isAllDay) {
            return stringResource(R.string.time_window_all_day_badge)
        }

        val windowStrings = timeWindows.take(2).map { window ->
            val start = TimeFormatUtils.formatTime(window.startTime, is24HourFormat)
            val end = TimeFormatUtils.formatTime(window.endTime, is24HourFormat)
            "$start - $end"
        }

        val joined = windowStrings.joinToString(", ")
        return if (timeWindows.size > 2) {
            "$joined +${timeWindows.size - 2}"
        } else {
            joined
        }
    }

    fun formatTimeWindows(timeWindows: List<ReminderTimeWindow>, is24HourFormat: Boolean, provider: ReminderStringProvider): String {
        if (timeWindows.isEmpty()) return ""
        val isAllDay = timeWindows.size == 1 &&
            timeWindows[0].startTime == LocalTime.of(0, 0) &&
            timeWindows[0].endTime == LocalTime.of(23, 59)

        if (isAllDay) {
            return provider.getString(R.string.time_window_all_day_badge)
        }

        val windowStrings = timeWindows.take(2).map { window ->
            val start = TimeFormatUtils.formatTime(window.startTime, is24HourFormat)
            val end = TimeFormatUtils.formatTime(window.endTime, is24HourFormat)
            "$start - $end"
        }

        val joined = windowStrings.joinToString(", ")
        return if (timeWindows.size > 2) {
            "$joined +${timeWindows.size - 2}"
        } else {
            joined
        }
    }

    fun formatFixedTimes(times: List<LocalTime>, is24HourFormat: Boolean): String {
        if (times.isEmpty()) return ""
        val sorted = times.sorted()
        return when {
            sorted.size <= 2 -> sorted.joinToString(", ") { TimeFormatUtils.formatTime(it, is24HourFormat) }
            else -> {
                val firstTwo = sorted.take(2).joinToString(", ") { TimeFormatUtils.formatTime(it, is24HourFormat) }
                "$firstTwo +${sorted.size - 2}"
            }
        }
    }

    @Composable
    fun getNextTriggerDisplayInfo(
        reminder: Reminder,
        is24HourFormat: Boolean,
        now: LocalDateTime = LocalDateTime.now()
    ): NextTriggerDisplayInfo {
        if (!reminder.isEnabled) {
            return NextTriggerDisplayInfo(
                text = stringResource(R.string.reminder_status_disabled),
                icon = ReMindIcons.NotificationsPaused,
                isInactive = true
            )
        }

        val snoozeMillis = reminder.snoozeNextTriggerTime
        if (snoozeMillis != null) {
            val snoozeLdt = LocalDateTime.ofInstant(Instant.ofEpochMilli(snoozeMillis), ZoneId.systemDefault())
            val snoozeTimeStr = TimeFormatUtils.formatTime(snoozeLdt.toLocalTime(), is24HourFormat)
            val diffMinutes = ChronoUnit.MINUTES.between(now, snoozeLdt).coerceAtLeast(0)
            val remainingStr = formatSnoozeRemainingTime(diffMinutes)

            return NextTriggerDisplayInfo(
                text = stringResource(R.string.next_trigger_snoozed, snoozeTimeStr, remainingStr),
                icon = ReMindIcons.Snooze,
                isSnoozed = true
            )
        }

        val timeTrigger = reminder.timeTrigger
        val lastTriggeredLdt = reminder.lastTriggeredTime?.let {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
        }
        val isInterval = reminder.triggerType == ReminderTriggerType.INTERVAL
        val nextLdt = timeTrigger?.getNextOccurrence(now, lastTriggeredLdt, isInterval)

        if (nextLdt == null) {
            return NextTriggerDisplayInfo(
                text = stringResource(R.string.reminder_status_completed),
                icon = ReMindIcons.CheckCircle,
                isInactive = true
            )
        }

        val today = now.toLocalDate()
        val nextDate = nextLdt.toLocalDate()
        val nextTimeStr = TimeFormatUtils.formatTime(nextLdt.toLocalTime(), is24HourFormat)
        val diffMinutes = ChronoUnit.MINUTES.between(now, nextLdt).coerceAtLeast(0)
        val remainingStr = formatRemainingTime(diffMinutes, today, nextDate)

        val displayText = when {
            nextDate == today -> {
                stringResource(R.string.next_trigger_today, nextTimeStr, remainingStr)
            }
            nextDate == today.plusDays(1) -> {
                stringResource(R.string.next_trigger_tomorrow, nextTimeStr, remainingStr)
            }
            nextDate > today.plusDays(1) && nextDate <= today.plusDays(6) -> {
                val dayName = getDayOfWeekShortName(nextDate.dayOfWeek)
                val dateStr = "${nextDate.dayOfMonth.toString().padStart(2, '0')}/${nextDate.monthValue.toString().padStart(2, '0')}"
                stringResource(R.string.next_trigger_upcoming_day, dayName, dateStr, nextTimeStr, remainingStr)
            }
            else -> {
                val dateStr = "${nextDate.dayOfMonth.toString().padStart(2, '0')}/${nextDate.monthValue.toString().padStart(2, '0')}/${nextDate.year}"
                stringResource(R.string.next_trigger_far, dateStr, nextTimeStr)
            }
        }

        return NextTriggerDisplayInfo(
            text = displayText,
            icon = if (isInterval) ReMindIcons.Timer else ReMindIcons.Schedule,
            isSnoozed = false,
            isInactive = false
        )
    }

    fun getNextTriggerDisplayInfo(
        reminder: Reminder,
        is24HourFormat: Boolean,
        provider: ReminderStringProvider,
        now: LocalDateTime = LocalDateTime.now()
    ): NextTriggerDisplayInfo {
        if (!reminder.isEnabled) {
            return NextTriggerDisplayInfo(
                text = provider.getString(R.string.reminder_status_disabled),
                icon = ReMindIcons.NotificationsPaused,
                isInactive = true
            )
        }

        val snoozeMillis = reminder.snoozeNextTriggerTime
        if (snoozeMillis != null) {
            val snoozeLdt = LocalDateTime.ofInstant(Instant.ofEpochMilli(snoozeMillis), ZoneId.systemDefault())
            val snoozeTimeStr = TimeFormatUtils.formatTime(snoozeLdt.toLocalTime(), is24HourFormat)
            val diffMinutes = ChronoUnit.MINUTES.between(now, snoozeLdt).coerceAtLeast(0)
            val remainingStr = formatSnoozeRemainingTime(diffMinutes, provider)

            return NextTriggerDisplayInfo(
                text = provider.getString(R.string.next_trigger_snoozed, snoozeTimeStr, remainingStr),
                icon = ReMindIcons.Snooze,
                isSnoozed = true
            )
        }

        val timeTrigger = reminder.timeTrigger
        val lastTriggeredLdt = reminder.lastTriggeredTime?.let {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
        }
        val isInterval = reminder.triggerType == ReminderTriggerType.INTERVAL
        val nextLdt = timeTrigger?.getNextOccurrence(now, lastTriggeredLdt, isInterval)

        if (nextLdt == null) {
            return NextTriggerDisplayInfo(
                text = provider.getString(R.string.reminder_status_completed),
                icon = ReMindIcons.CheckCircle,
                isInactive = true
            )
        }

        val today = now.toLocalDate()
        val nextDate = nextLdt.toLocalDate()
        val nextTimeStr = TimeFormatUtils.formatTime(nextLdt.toLocalTime(), is24HourFormat)
        val diffMinutes = ChronoUnit.MINUTES.between(now, nextLdt).coerceAtLeast(0)
        val remainingStr = formatRemainingTime(diffMinutes, today, nextDate, provider)

        val displayText = when {
            nextDate == today -> {
                provider.getString(R.string.next_trigger_today, nextTimeStr, remainingStr)
            }
            nextDate == today.plusDays(1) -> {
                provider.getString(R.string.next_trigger_tomorrow, nextTimeStr, remainingStr)
            }
            nextDate > today.plusDays(1) && nextDate <= today.plusDays(6) -> {
                val dayName = getDayOfWeekShortName(nextDate.dayOfWeek, provider)
                val dateStr = "${nextDate.dayOfMonth.toString().padStart(2, '0')}/${nextDate.monthValue.toString().padStart(2, '0')}"
                provider.getString(R.string.next_trigger_upcoming_day, dayName, dateStr, nextTimeStr, remainingStr)
            }
            else -> {
                val dateStr = "${nextDate.dayOfMonth.toString().padStart(2, '0')}/${nextDate.monthValue.toString().padStart(2, '0')}/${nextDate.year}"
                provider.getString(R.string.next_trigger_far, dateStr, nextTimeStr)
            }
        }

        return NextTriggerDisplayInfo(
            text = displayText,
            icon = if (isInterval) ReMindIcons.Timer else ReMindIcons.Schedule,
            isSnoozed = false,
            isInactive = false
        )
    }

    @Composable
    private fun formatRemainingTime(diffMinutes: Long, today: LocalDate, nextDate: LocalDate): String {
        return when {
            diffMinutes <= 1 -> stringResource(R.string.remaining_soon)
            diffMinutes < 60 -> stringResource(R.string.remaining_minutes, diffMinutes.toInt())
            diffMinutes < 24 * 60 -> {
                val hours = (diffMinutes / 60).toInt()
                val mins = (diffMinutes % 60).toInt()
                if (mins == 0) {
                    stringResource(R.string.remaining_hours, hours)
                } else {
                    stringResource(R.string.remaining_hours_minutes, hours, mins)
                }
            }
            else -> {
                val days = ChronoUnit.DAYS.between(today, nextDate).toInt().coerceAtLeast(1)
                stringResource(R.string.remaining_days, days)
            }
        }
    }

    private fun formatRemainingTime(diffMinutes: Long, today: LocalDate, nextDate: LocalDate, provider: ReminderStringProvider): String {
        return when {
            diffMinutes <= 1 -> provider.getString(R.string.remaining_soon)
            diffMinutes < 60 -> provider.getString(R.string.remaining_minutes, diffMinutes.toInt())
            diffMinutes < 24 * 60 -> {
                val hours = (diffMinutes / 60).toInt()
                val mins = (diffMinutes % 60).toInt()
                if (mins == 0) {
                    provider.getString(R.string.remaining_hours, hours)
                } else {
                    provider.getString(R.string.remaining_hours_minutes, hours, mins)
                }
            }
            else -> {
                val days = ChronoUnit.DAYS.between(today, nextDate).toInt().coerceAtLeast(1)
                provider.getString(R.string.remaining_days, days)
            }
        }
    }

    @Composable
    private fun formatSnoozeRemainingTime(diffMinutes: Long): String {
        return when {
            diffMinutes <= 1 -> stringResource(R.string.snooze_remaining_soon)
            diffMinutes < 60 -> stringResource(R.string.snooze_remaining_minutes, diffMinutes.toInt())
            else -> {
                val hours = (diffMinutes / 60).toInt()
                val mins = (diffMinutes % 60).toInt()
                if (mins == 0) {
                    stringResource(R.string.snooze_remaining_hours, hours)
                } else {
                    stringResource(R.string.snooze_remaining_hours_minutes, hours, mins)
                }
            }
        }
    }

    private fun formatSnoozeRemainingTime(diffMinutes: Long, provider: ReminderStringProvider): String {
        return when {
            diffMinutes <= 1 -> provider.getString(R.string.snooze_remaining_soon)
            diffMinutes < 60 -> provider.getString(R.string.snooze_remaining_minutes, diffMinutes.toInt())
            else -> {
                val hours = (diffMinutes / 60).toInt()
                val mins = (diffMinutes % 60).toInt()
                if (mins == 0) {
                    provider.getString(R.string.snooze_remaining_hours, hours)
                } else {
                    provider.getString(R.string.snooze_remaining_hours_minutes, hours, mins)
                }
            }
        }
    }

    @Composable
    private fun getDayOfWeekShortName(day: DayOfWeek): String {
        return when (day) {
            DayOfWeek.MONDAY -> stringResource(R.string.day_mon)
            DayOfWeek.TUESDAY -> stringResource(R.string.day_tue)
            DayOfWeek.WEDNESDAY -> stringResource(R.string.day_wed)
            DayOfWeek.THURSDAY -> stringResource(R.string.day_thu)
            DayOfWeek.FRIDAY -> stringResource(R.string.day_fri)
            DayOfWeek.SATURDAY -> stringResource(R.string.day_sat)
            DayOfWeek.SUNDAY -> stringResource(R.string.day_sun)
        }
    }

    private fun getDayOfWeekShortName(day: DayOfWeek, provider: ReminderStringProvider): String {
        return when (day) {
            DayOfWeek.MONDAY -> provider.getString(R.string.day_mon)
            DayOfWeek.TUESDAY -> provider.getString(R.string.day_tue)
            DayOfWeek.WEDNESDAY -> provider.getString(R.string.day_wed)
            DayOfWeek.THURSDAY -> provider.getString(R.string.day_thu)
            DayOfWeek.FRIDAY -> provider.getString(R.string.day_fri)
            DayOfWeek.SATURDAY -> provider.getString(R.string.day_sat)
            DayOfWeek.SUNDAY -> provider.getString(R.string.day_sun)
        }
    }

    private fun formatDate(date: LocalDate): String {
        return "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthValue.toString().padStart(2, '0')}/${date.year}"
    }
}
