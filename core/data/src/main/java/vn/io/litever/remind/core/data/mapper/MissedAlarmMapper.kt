package vn.io.litever.remind.core.data.mapper

import vn.io.litever.remind.core.database.model.MissedAlarmEntity
import vn.io.litever.remind.core.model.MissedAlarm
import vn.io.litever.remind.core.model.MissedReason

fun MissedAlarmEntity.toModel(): MissedAlarm {
    return MissedAlarm(
        id = id,
        alarmId = alarmId,
        alarmLabel = alarmLabel,
        scheduledTime = scheduledTime,
        missedTime = missedTime,
        reason = MissedReason.valueOf(reason)
    )
}

fun MissedAlarm.toEntity(): MissedAlarmEntity {
    return MissedAlarmEntity(
        id = id,
        alarmId = alarmId,
        alarmLabel = alarmLabel,
        scheduledTime = scheduledTime,
        missedTime = missedTime,
        reason = reason.name
    )
}
