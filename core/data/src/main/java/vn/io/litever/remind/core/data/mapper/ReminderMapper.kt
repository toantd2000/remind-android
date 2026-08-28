package vn.io.litever.remind.core.data.mapper

import vn.io.litever.remind.core.database.entity.ChecklistItemEntity
import vn.io.litever.remind.core.database.entity.ReminderEntity
import vn.io.litever.remind.core.database.model.PopulatedReminder
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.core.model.GeofenceTransitionType
import vn.io.litever.remind.core.model.LocationTrigger
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderMonthDay
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
            repeatDays = parseDaysOfWeek(timeTriggerRepeatDays),
            monthlyDays = parseMonthlyDays(timeTriggerMonthlyDays),
            yearlyDates = parseYearlyDates(timeTriggerYearlyDates)
        )
    } else null

    // Map LocationTrigger
    val lat = locationLatitude
    val lng = locationLongitude
    val locationTrigger = if (lat != null && lng != null) {
        val singleWindow = parseTimeWindow(
            startTimeStr = locationWindowStartTime,
            endTimeStr = locationWindowEndTime,
            daysStr = locationWindowDays
        )
        val windowsList = parseTimeWindowsList(locationTimeWindows)
        val finalWindows = if (windowsList.isNotEmpty()) windowsList else if (singleWindow != null) listOf(singleWindow) else emptyList()
        LocationTrigger(
            latitude = lat,
            longitude = lng,
            radiusMeters = locationRadiusMeters ?: 100f,
            locationName = locationName ?: "",
            transitionType = locationTransitionType?.let {
                runCatching { GeofenceTransitionType.valueOf(it) }.getOrNull()
            } ?: GeofenceTransitionType.ENTER,
            timeWindow = singleWindow,
            timeWindows = finalWindows,
            date = locationDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
            repeatDays = parseDaysOfWeek(locationRepeatDays),
            monthlyDays = parseMonthlyDays(locationMonthlyDays),
            yearlyDates = parseYearlyDates(locationYearlyDates)
        )
    } else null

    // Map WifiTrigger
    val ssid = wifiSsid
    val wifiTrigger = if (!ssid.isNullOrBlank()) {
        val singleWindow = parseTimeWindow(
            startTimeStr = wifiWindowStartTime,
            endTimeStr = wifiWindowEndTime,
            daysStr = wifiWindowDays
        )
        val windowsList = parseTimeWindowsList(wifiTimeWindows)
        val finalWindows = if (windowsList.isNotEmpty()) windowsList else if (singleWindow != null) listOf(singleWindow) else emptyList()
        WifiTrigger(
            ssid = ssid,
            bssid = wifiBssid,
            triggerType = wifiTriggerType?.let {
                runCatching { WifiTriggerType.valueOf(it) }.getOrNull()
            } ?: WifiTriggerType.ON_CONNECT,
            timeWindow = singleWindow,
            timeWindows = finalWindows,
            date = wifiDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
            repeatDays = parseDaysOfWeek(wifiRepeatDays),
            monthlyDays = parseMonthlyDays(wifiMonthlyDays),
            yearlyDates = parseYearlyDates(wifiYearlyDates)
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
        autoSilenceMinutes = autoSilenceMinutes,
        useAlarmStream = useAlarmStream,
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
    val locWindows = locationTrigger?.activeTimeWindows ?: emptyList()
    val firstLocWindow = locWindows.firstOrNull() ?: locationTrigger?.timeWindow

    val wWifiWindows = wifiTrigger?.activeTimeWindows ?: emptyList()
    val firstWifiWindow = wWifiWindows.firstOrNull() ?: wifiTrigger?.timeWindow

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
        timeTriggerMonthlyDays = formatMonthlyDays(timeTrigger?.monthlyDays),
        timeTriggerYearlyDates = formatYearlyDates(timeTrigger?.yearlyDates),

        // LocationTrigger fields
        locationLatitude = locationTrigger?.latitude,
        locationLongitude = locationTrigger?.longitude,
        locationRadiusMeters = locationTrigger?.radiusMeters,
        locationName = locationTrigger?.locationName,
        locationTransitionType = locationTrigger?.transitionType?.name,
        locationWindowStartTime = firstLocWindow?.startTime?.toString(),
        locationWindowEndTime = firstLocWindow?.endTime?.toString(),
        locationWindowDays = formatDaysOfWeek(firstLocWindow?.activeDays),
        locationTimeWindows = formatTimeWindowsList(locWindows),
        locationDate = locationTrigger?.date?.toString(),
        locationRepeatDays = formatDaysOfWeek(locationTrigger?.repeatDays),
        locationMonthlyDays = formatMonthlyDays(locationTrigger?.monthlyDays),
        locationYearlyDates = formatYearlyDates(locationTrigger?.yearlyDates),

        // WifiTrigger fields
        wifiSsid = wifiTrigger?.ssid,
        wifiBssid = wifiTrigger?.bssid,
        wifiTriggerType = wifiTrigger?.triggerType?.name,
        wifiWindowStartTime = firstWifiWindow?.startTime?.toString(),
        wifiWindowEndTime = firstWifiWindow?.endTime?.toString(),
        wifiWindowDays = formatDaysOfWeek(firstWifiWindow?.activeDays),
        wifiTimeWindows = formatTimeWindowsList(wWifiWindows),
        wifiDate = wifiTrigger?.date?.toString(),
        wifiRepeatDays = formatDaysOfWeek(wifiTrigger?.repeatDays),
        wifiMonthlyDays = formatMonthlyDays(wifiTrigger?.monthlyDays),
        wifiYearlyDates = formatYearlyDates(wifiTrigger?.yearlyDates),

        // Notification & Alert settings
        notificationStyle = notificationStyle.name,
        isPersistentAlert = isPersistentAlert,
        autoSilenceMinutes = autoSilenceMinutes,
        useAlarmStream = useAlarmStream,
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
