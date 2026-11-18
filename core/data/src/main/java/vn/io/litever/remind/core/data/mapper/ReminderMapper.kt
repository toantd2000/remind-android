package vn.io.litever.remind.core.data.mapper

import vn.io.litever.remind.core.database.entity.ReminderEntity
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderAction
import vn.io.litever.remind.core.model.ReminderMonthDay
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.RepeatMode
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.model.UrgencyLevel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

fun ReminderEntity.toModel(): Reminder {
    val triggerTypeEnum = runCatching {
        ReminderTriggerType.valueOf(triggerType)
    }.getOrDefault(ReminderTriggerType.FIXED_TIME)

    val timesStr = timeTriggerTimes
    val timeList = if (timesStr.isNullOrBlank()) {
        emptyList()
    } else {
        timesStr.split(",").mapNotNull { runCatching { LocalTime.parse(it.trim()) }.getOrNull() }
    }

    val repeatModeEnum = runCatching { RepeatMode.valueOf(timeTriggerRepeatMode) }.getOrDefault(RepeatMode.NONE)
    val repeatValues = timeTriggerRepeatValues?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()

    // Monthly days
    val parsedMonthly = parseMonthlyDays(timeTriggerMonthlyDays)
    val monthlyDays = if (parsedMonthly.isNotEmpty()) {
        parsedMonthly
    } else if (repeatModeEnum == RepeatMode.MONTHLY) {
        repeatValues
    } else {
        emptyList()
    }

    // Yearly dates
    val yearlyDates = parseYearlyDates(timeTriggerYearlyDates)

    // Multi-TimeWindows
    val parsedWindows = parseTimeWindowsList(timeTriggerTimeWindows)
    val timeWindows = if (parsedWindows.isNotEmpty()) {
        parsedWindows
    } else {
        // Fallback to legacy single window fields if present
        val windowStartStr = timeTriggerWindowStart
        val windowEndStr = timeTriggerWindowEnd
        val windowDaysStr = timeTriggerWindowActiveDays
        if (windowStartStr != null && windowEndStr != null) {
            val start = runCatching { LocalTime.parse(windowStartStr) }.getOrNull()
            val end = runCatching { LocalTime.parse(windowEndStr) }.getOrNull()
            val days = windowDaysStr?.split(",")?.mapNotNull {
                runCatching { DayOfWeek.valueOf(it.trim()) }.getOrNull()
            } ?: emptyList()
            if (start != null && end != null) {
                listOf(ReminderTimeWindow(start, end, days))
            } else emptyList()
        } else emptyList()
    }

    val parsedExactDates = timeTriggerExactDate
        ?.split(",")
        ?.mapNotNull { runCatching { LocalDate.parse(it.trim()) }.getOrNull() }
        ?.distinct()
        ?.sorted()
        ?: emptyList()

    val timeTrigger = TimeTrigger(
        time = timeList,
        intervalMinutes = timeTriggerIntervalMinutes,
        exactDate = parsedExactDates.firstOrNull(),
        exactDates = parsedExactDates,
        repeatMode = repeatModeEnum,
        repeatValues = repeatValues,
        monthlyDays = monthlyDays,
        yearlyDates = yearlyDates,
        timeWindows = timeWindows
    )

    val notificationStyleEnum = runCatching {
        NotificationStyle.valueOf(notificationStyle)
    }.getOrDefault(NotificationStyle.STANDARD)

    val urgencyLevelEnum = runCatching {
        UrgencyLevel.valueOf(urgencyLevel)
    }.getOrDefault(UrgencyLevel.MEDIUM)

    val action = ReminderAction(
        urgencyLevel = urgencyLevelEnum,
        notificationStyle = notificationStyleEnum,
        isPersistentAlert = isPersistentAlert,
        autoSilenceMinutes = autoSilenceMinutes,
        useAlarmStream = useAlarmStream,
        ringtoneUri = ringtoneUri,
        vibrationEnabled = vibrationEnabled,
        volume = volume,
        snoozeEnabled = snoozeEnabled,
        snoozeIntervalMinutes = snoozeIntervalMinutes
    )

    return Reminder(
        id = id,
        title = title,
        notes = notes,
        isEnabled = isEnabled,
        triggerType = triggerTypeEnum,
        timeTrigger = timeTrigger,
        action = action,
        snoozeNextTriggerTime = snoozeNextTriggerTime,
        lastTriggeredTime = lastTriggeredTime,
        createdAt = createdAt
    )
}

fun Reminder.toEntity(): ReminderEntity {
    val windows = timeTrigger?.timeWindows ?: emptyList()
    val firstWindow = windows.firstOrNull()

    return ReminderEntity(
        id = id,
        title = title,
        notes = notes,
        isEnabled = isEnabled,
        triggerType = triggerType.name,

        timeTriggerTimes = timeTrigger?.time?.joinToString(",") { it.toString() },
        timeTriggerIntervalMinutes = timeTrigger?.intervalMinutes,
        timeTriggerExactDate = timeTrigger?.effectiveExactDates?.takeIf { it.isNotEmpty() }?.joinToString(",") { it.toString() },
        timeTriggerRepeatMode = timeTrigger?.repeatMode?.name ?: "NONE",
        timeTriggerRepeatValues = timeTrigger?.repeatValues?.joinToString(","),
        timeTriggerMonthlyDays = formatMonthlyDays(timeTrigger?.monthlyDays),
        timeTriggerYearlyDates = formatYearlyDates(timeTrigger?.yearlyDates),
        timeTriggerTimeWindows = formatTimeWindowsList(windows),

        // Legacy compatibility
        timeTriggerWindowStart = firstWindow?.startTime?.toString(),
        timeTriggerWindowEnd = firstWindow?.endTime?.toString(),
        timeTriggerWindowActiveDays = firstWindow?.activeDays?.joinToString(",") { it.name },

        urgencyLevel = action.urgencyLevel.name,
        notificationStyle = action.notificationStyle.name,
        isPersistentAlert = action.isPersistentAlert,
        autoSilenceMinutes = action.autoSilenceMinutes,
        useAlarmStream = action.useAlarmStream,
        ringtoneUri = action.ringtoneUri,
        vibrationEnabled = action.vibrationEnabled,
        volume = action.volume,

        snoozeEnabled = action.snoozeEnabled,
        snoozeIntervalMinutes = action.snoozeIntervalMinutes,
        snoozeRepeatCount = 3,
        currentSnoozeCount = 0,
        snoozeNextTriggerTime = snoozeNextTriggerTime,

        lastTriggeredTime = lastTriggeredTime,
        createdAt = createdAt
    )
}

private fun parseMonthlyDays(str: String?): List<Int> {
    if (str.isNullOrBlank()) return emptyList()
    return str.split(",")
        .filter { it.isNotBlank() }
        .mapNotNull { it.trim().toIntOrNull() }
}

private fun formatMonthlyDays(days: List<Int>?): String? {
    if (days.isNullOrEmpty()) return null
    return days.joinToString(",")
}

private fun parseYearlyDates(str: String?): List<ReminderMonthDay> {
    if (str.isNullOrBlank()) return emptyList()
    return str.split(";")
        .filter { it.isNotBlank() }
        .mapNotNull { entry ->
            val parts = entry.split("-")
            if (parts.size == 2) {
                val m = parts[0].trim().toIntOrNull()
                val d = parts[1].trim().toIntOrNull()
                if (m != null && d != null) ReminderMonthDay(m, d) else null
            } else null
        }
}

private fun formatYearlyDates(dates: List<ReminderMonthDay>?): String? {
    if (dates.isNullOrEmpty()) return null
    return dates.joinToString(";") { "${it.month}-${it.day}" }
}

private fun parseDaysOfWeek(daysStr: String?): List<DayOfWeek> {
    if (daysStr.isNullOrBlank()) return emptyList()
    return daysStr.split(",")
        .filter { it.isNotBlank() }
        .mapNotNull { name ->
            runCatching { DayOfWeek.valueOf(name.trim()) }.getOrNull()
        }
}

private fun formatDaysOfWeek(days: List<DayOfWeek>?): String? {
    if (days.isNullOrEmpty()) return null
    return days.joinToString(",") { it.name }
}

private fun parseTimeWindowsList(str: String?): List<ReminderTimeWindow> {
    if (str.isNullOrBlank()) return emptyList()
    return str.split(";")
        .filter { it.isNotBlank() }
        .mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size >= 2) {
                val start = runCatching { LocalTime.parse(parts[0].trim()) }.getOrNull()
                val end = runCatching { LocalTime.parse(parts[1].trim()) }.getOrNull()
                val days = if (parts.size > 2) parseDaysOfWeek(parts[2].trim()) else emptyList()
                if (start != null && end != null) {
                    ReminderTimeWindow(startTime = start, endTime = end, activeDays = days)
                } else null
            } else null
        }
}

private fun formatTimeWindowsList(windows: List<ReminderTimeWindow>?): String? {
    if (windows.isNullOrEmpty()) return null
    return windows.joinToString(";") { w ->
        val days = formatDaysOfWeek(w.activeDays) ?: ""
        "${w.startTime}|${w.endTime}|$days"
    }
}
