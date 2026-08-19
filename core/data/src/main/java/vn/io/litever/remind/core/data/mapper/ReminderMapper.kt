package vn.io.litever.remind.core.data.mapper

import vn.io.litever.remind.core.database.entity.ChecklistItemEntity
import vn.io.litever.remind.core.database.entity.ReminderEntity
import vn.io.litever.remind.core.database.model.PopulatedReminder
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.core.model.GeofenceTransitionType
import vn.io.litever.remind.core.model.LocationTrigger
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.model.WifiTrigger
import vn.io.litever.remind.core.model.WifiTriggerType
import java.time.LocalDate
import java.time.LocalTime

/**
 * Maps a Room [ReminderEntity] and an optional list of [ChecklistItemEntity] to a domain [Reminder].
 */
fun ReminderEntity.toModel(checklistItems: List<ChecklistItemEntity> = emptyList()): Reminder {
    val triggerTypeEnum = runCatching {
        ReminderTriggerType.valueOf(triggerType)
    }.getOrDefault(ReminderTriggerType.TIME)

    val hour = timeTriggerHour
    val minute = timeTriggerMinute
    val timeTrigger = if (hour != null && minute != null) {
        TimeTrigger(
            time = LocalTime.of(hour, minute),
            date = timeTriggerDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
            repeatDays = parseDaysOfWeek(timeTriggerRepeatDays)
        )
    } else null

    // Map LocationTrigger
    val lat = locationLatitude
    val lng = locationLongitude
    val locationTrigger = if (lat != null && lng != null) {
        val timeWindow = parseTimeWindow(
            startTimeStr = locationWindowStartTime,
            endTimeStr = locationWindowEndTime,
            daysStr = locationWindowDays
        )
        LocationTrigger(
            latitude = lat,
            longitude = lng,
            radiusMeters = locationRadiusMeters ?: 100f,
            locationName = locationName ?: "",
            transitionType = locationTransitionType?.let {
                runCatching { GeofenceTransitionType.valueOf(it) }.getOrNull()
            } ?: GeofenceTransitionType.ENTER,
            timeWindow = timeWindow
        )
    } else null

    // Map WifiTrigger
    val ssid = wifiSsid
    val wifiTrigger = if (!ssid.isNullOrBlank()) {
        val timeWindow = parseTimeWindow(
            startTimeStr = wifiWindowStartTime,
            endTimeStr = wifiWindowEndTime,
            daysStr = wifiWindowDays
        )
        WifiTrigger(
            ssid = ssid,
            bssid = wifiBssid,
            triggerType = wifiTriggerType?.let {
                runCatching { WifiTriggerType.valueOf(it) }.getOrNull()
            } ?: WifiTriggerType.ON_CONNECT,
            timeWindow = timeWindow
        )
    } else null

    val notificationStyleEnum = runCatching {
        NotificationStyle.valueOf(notificationStyle)
    }.getOrDefault(NotificationStyle.STANDARD)

    return Reminder(
        id = id,
        title = title,
        notes = notes,
        isEnabled = isEnabled,
        triggerType = triggerTypeEnum,
        timeTrigger = timeTrigger,
        locationTrigger = locationTrigger,
        wifiTrigger = wifiTrigger,
        checklist = checklistItems.map { it.toModel() }.sortedBy { it.orderIndex },
        notificationStyle = notificationStyleEnum,
        isPersistentAlert = isPersistentAlert,
        ringtoneUri = ringtoneUri,
        vibrationEnabled = vibrationEnabled,
        volume = volume,
        snoozeEnabled = snoozeEnabled,
        snoozeIntervalMinutes = snoozeIntervalMinutes,
        snoozeRepeatCount = snoozeRepeatCount,
        currentSnoozeCount = currentSnoozeCount,
        snoozeNextTriggerTime = snoozeNextTriggerTime,
        lastTriggeredTime = lastTriggeredTime,
        isCompleted = isCompleted,
        createdAt = createdAt
    )
}

/**
 * Maps a Room relational aggregate [PopulatedReminder] to a domain [Reminder].
 */
fun PopulatedReminder.toModel(): Reminder {
    return reminder.toModel(checklistItems = checklist)
}

/**
 * Maps a pure domain [Reminder] to a Room [ReminderEntity].
 */
fun Reminder.toEntity(): ReminderEntity {
    return ReminderEntity(
        id = id,
        title = title,
        notes = notes,
        isEnabled = isEnabled,
        triggerType = triggerType.name,

        // TimeTrigger fields
        timeTriggerHour = timeTrigger?.time?.hour,
        timeTriggerMinute = timeTrigger?.time?.minute,
        timeTriggerDate = timeTrigger?.date?.toString(),
        timeTriggerRepeatDays = formatDaysOfWeek(timeTrigger?.repeatDays),

        // LocationTrigger fields
        locationLatitude = locationTrigger?.latitude,
        locationLongitude = locationTrigger?.longitude,
        locationRadiusMeters = locationTrigger?.radiusMeters,
        locationName = locationTrigger?.locationName,
        locationTransitionType = locationTrigger?.transitionType?.name,
        locationWindowStartTime = locationTrigger?.timeWindow?.startTime?.toString(),
        locationWindowEndTime = locationTrigger?.timeWindow?.endTime?.toString(),
        locationWindowDays = formatDaysOfWeek(locationTrigger?.timeWindow?.activeDays),

        // WifiTrigger fields
        wifiSsid = wifiTrigger?.ssid,
        wifiBssid = wifiTrigger?.bssid,
        wifiTriggerType = wifiTrigger?.triggerType?.name,
        wifiWindowStartTime = wifiTrigger?.timeWindow?.startTime?.toString(),
        wifiWindowEndTime = wifiTrigger?.timeWindow?.endTime?.toString(),
        wifiWindowDays = formatDaysOfWeek(wifiTrigger?.timeWindow?.activeDays),

        // Notification & Alert settings
        notificationStyle = notificationStyle.name,
        isPersistentAlert = isPersistentAlert,
        ringtoneUri = ringtoneUri,
        vibrationEnabled = vibrationEnabled,
        volume = volume,

        // Snooze settings & tracking
        snoozeEnabled = snoozeEnabled,
        snoozeIntervalMinutes = snoozeIntervalMinutes,
        snoozeRepeatCount = snoozeRepeatCount,
        currentSnoozeCount = currentSnoozeCount,
        snoozeNextTriggerTime = snoozeNextTriggerTime,

        // State & Timestamps
        lastTriggeredTime = lastTriggeredTime,
        isCompleted = isCompleted,
        createdAt = createdAt
    )
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

private fun parseTimeWindow(
    startTimeStr: String?,
    endTimeStr: String?,
    daysStr: String?
): ReminderTimeWindow? {
    if (startTimeStr.isNullOrBlank() || endTimeStr.isNullOrBlank()) return null
    val start = runCatching { LocalTime.parse(startTimeStr) }.getOrNull() ?: return null
    val end = runCatching { LocalTime.parse(endTimeStr) }.getOrNull() ?: return null
    val activeDays = parseDaysOfWeek(daysStr)
    return ReminderTimeWindow(
        startTime = start,
        endTime = end,
        activeDays = activeDays
    )
}
