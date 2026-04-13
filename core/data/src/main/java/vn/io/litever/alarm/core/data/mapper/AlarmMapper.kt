package vn.io.litever.alarm.core.data.mapper

import vn.io.litever.remind.core.database.model.ReminderEntity
import vn.io.litever.alarm.core.model.Alarm
import vn.io.litever.alarm.core.model.DayOfWeek
import java.time.LocalTime

fun ReminderEntity.toModel(): Alarm {
    return Alarm(
        id = id,
        time = LocalTime.of(hour, minute),
        label = label,
        isEnabled = isEnabled,
        repeatDays = repeatDays.split(",").filter { it.isNotEmpty() }.map { DayOfWeek.valueOf(it) },
        vibrationEnabled = vibrationEnabled,
        ringtoneUri = ringtoneUri,
        volume = volume
    )
}

fun Alarm.toEntity(): ReminderEntity {
    return ReminderEntity(
        id = id,
        hour = time.hour,
        minute = time.minute,
        label = label,
        isEnabled = isEnabled,
        repeatDays = repeatDays.joinToString(",") { it.name },
        vibrationEnabled = vibrationEnabled,
        ringtoneUri = ringtoneUri,
        volume = volume
    )
}
