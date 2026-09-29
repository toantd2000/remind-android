package vn.io.litever.remind.core.data.mapper

import vn.io.litever.remind.core.database.entity.ChecklistItemEntity
import vn.io.litever.remind.core.database.model.PopulatedReminder
import vn.io.litever.remind.core.database.entity.ReminderEntity
import vn.io.litever.remind.core.model.ChecklistItem
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderAction
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.RepeatMode
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.model.UrgencyLevel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

fun ReminderEntity.toModel(checklistItems: List<ChecklistItemEntity> = emptyList()): Reminder {
    val triggerTypeEnum = runCatching {
        ReminderTriggerType.valueOf(triggerType)
    }.getOrDefault(ReminderTriggerType.FIXED_TIME)

    val timesStr = timeTriggerTimes
    val timeList = if (timesStr.isNullOrBlank()) {
        emptyList()
    } else {
        timesStr.split(",").mapNotNull { runCatching { LocalTime.parse(it.trim()) }.getOrNull() }
    }
    
    val repeatValues = timeTriggerRepeatValues?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()

    val windowStartStr = timeTriggerWindowStart
    val windowEndStr = timeTriggerWindowEnd
    val windowDaysStr = timeTriggerWindowActiveDays
    val timeWindow = if (windowStartStr != null && windowEndStr != null) {
        val start = runCatching { LocalTime.parse(windowStartStr) }.getOrNull()
        val end = runCatching { LocalTime.parse(windowEndStr) }.getOrNull()
        val days = windowDaysStr?.split(",")?.mapNotNull {
            runCatching { DayOfWeek.valueOf(it.trim()) }.getOrNull()
        } ?: emptyList()
        if (start != null && end != null) {
            ReminderTimeWindow(start, end, days)
        } else null
    } else null

    val timeTrigger = TimeTrigger(
        time = timeList,
        intervalMinutes = timeTriggerIntervalMinutes,
        exactDate = timeTriggerExactDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        repeatMode = runCatching { RepeatMode.valueOf(timeTriggerRepeatMode) }.getOrDefault(RepeatMode.NONE),
        repeatValues = repeatValues,
        timeWindow = timeWindow
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
        snoozeIntervalMinutes = snoozeIntervalMinutes,
        snoozeRepeatCount = snoozeRepeatCount
    )

    return Reminder(
        id = id,
        title = title,
        notes = notes,
        isEnabled = isEnabled,
        triggerType = triggerTypeEnum,
        timeTrigger = timeTrigger,
        checklist = checklistItems.map { it.toModel() }.sortedBy { it.orderIndex },
        action = action,
        currentSnoozeCount = currentSnoozeCount,
        snoozeNextTriggerTime = snoozeNextTriggerTime,
        lastTriggeredTime = lastTriggeredTime,
        isCompleted = isCompleted,
        createdAt = createdAt
    )
}

fun PopulatedReminder.toModel(): Reminder {
    return reminder.toModel(checklistItems = checklist)
}

fun Reminder.toEntity(): ReminderEntity {
    return ReminderEntity(
        id = id,
        title = title,
        notes = notes,
        isEnabled = isEnabled,
        triggerType = triggerType.name,
        
        timeTriggerTimes = timeTrigger?.time?.joinToString(",") { it.toString() },
        timeTriggerIntervalMinutes = timeTrigger?.intervalMinutes,
        timeTriggerExactDate = timeTrigger?.exactDate?.toString(),
        timeTriggerRepeatMode = timeTrigger?.repeatMode?.name ?: "NONE",
        timeTriggerRepeatValues = timeTrigger?.repeatValues?.joinToString(","),
        timeTriggerWindowStart = timeTrigger?.timeWindow?.startTime?.toString(),
        timeTriggerWindowEnd = timeTrigger?.timeWindow?.endTime?.toString(),
        timeTriggerWindowActiveDays = timeTrigger?.timeWindow?.activeDays?.joinToString(",") { it.name },
        
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
        snoozeRepeatCount = action.snoozeRepeatCount,
        currentSnoozeCount = currentSnoozeCount,
        snoozeNextTriggerTime = snoozeNextTriggerTime,
        
        lastTriggeredTime = lastTriggeredTime,
        isCompleted = isCompleted,
        createdAt = createdAt
    )
}
