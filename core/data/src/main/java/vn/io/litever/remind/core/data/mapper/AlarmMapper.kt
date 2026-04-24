package vn.io.litever.remind.core.data.mapper

import vn.io.litever.remind.core.database.model.AlarmEntity
import vn.io.litever.remind.core.database.model.PopulatedAlarm
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.core.model.DayOfWeek
import java.time.LocalTime
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.Instant

fun AlarmEntity.toModel(): Alarm {
    return Alarm(
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

fun PopulatedAlarm.toModel(): Alarm {
    return alarm.toModel().copy(
        missions = missions.map { it.toModel() }
    )
}

fun Alarm.toEntity(): AlarmEntity {
    return AlarmEntity(
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










