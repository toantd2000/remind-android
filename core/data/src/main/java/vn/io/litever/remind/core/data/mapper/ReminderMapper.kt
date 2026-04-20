package vn.io.litever.remind.core.data.mapper

import vn.io.litever.remind.core.database.model.ReminderEntity
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.DayOfWeek
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.Instant

fun ReminderEntity.toModel(): Reminder {
    return Reminder(
        id = id,
        time = LocalTime.of(hour, minute),
        label = label,
        isEnabled = isEnabled,
        repeatDays = repeatDays.split(",").filter { it.isNotEmpty() }.map { DayOfWeek.valueOf(it) },
        date = date?.let { java.time.LocalDate.parse(it) },
        vibrationEnabled = vibrationEnabled,
        ringtoneUri = ringtoneUri,
        volume = volume,
        snoozeEnabled = snoozeEnabled,
        snoozeInterval = snoozeInterval,
        snoozeRepeatCount = snoozeRepeatCount,
        autoSilenceMinutes = autoSilenceMinutes,
        currentSnoozeCount = currentSnoozeCount,
        snoozeNextTriggerTime = snoozeNextTriggerTime,
        isMissed = isMissed,
        message = message,
        skippedAt = skippedAt?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) },
        gradualVolumeDurationSeconds = gradualVolumeDurationSeconds
    )
}

fun Reminder.toEntity(): ReminderEntity {
    return ReminderEntity(
        id = id,
        hour = time.hour,
        minute = time.minute,
        label = label,
        isEnabled = isEnabled,
        repeatDays = repeatDays.joinToString(",") { it.name },
        date = date?.toString(),
        vibrationEnabled = vibrationEnabled,
        ringtoneUri = ringtoneUri,
        volume = volume,
        snoozeEnabled = snoozeEnabled,
        snoozeInterval = snoozeInterval,
        snoozeRepeatCount = snoozeRepeatCount,
        autoSilenceMinutes = autoSilenceMinutes,
        currentSnoozeCount = currentSnoozeCount,
        snoozeNextTriggerTime = snoozeNextTriggerTime,
        isMissed = isMissed,
        message = message,
        skippedAt = skippedAt?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
        gradualVolumeDurationSeconds = gradualVolumeDurationSeconds
    )
}
