package vn.io.litever.alarm.core.data.mapper

import vn.io.litever.alarm.core.database.model.AlarmEntity
import vn.io.litever.alarm.core.model.Alarm
import vn.io.litever.alarm.core.model.DayOfWeek
import java.time.LocalTime

fun AlarmEntity.toModel(): Alarm {
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

fun Alarm.toEntity(): AlarmEntity {
    return AlarmEntity(
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
