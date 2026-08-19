package vn.io.litever.remind.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val isEnabled: Boolean = true,
    val triggerType: String, // TIME, LOCATION, WIFI
    
    // TimeTrigger fields
    val timeTriggerHour: Int? = null,
    val timeTriggerMinute: Int? = null,
    val timeTriggerDate: String? = null, // ISO "yyyy-MM-dd"
    val timeTriggerRepeatDays: String? = null, // Comma-separated enum names e.g. "MONDAY,TUESDAY"
    
    // LocationTrigger fields
    val locationLatitude: Double? = null,
    val locationLongitude: Double? = null,
    val locationRadiusMeters: Float? = null,
    val locationName: String? = null,
    val locationTransitionType: String? = null, // ENTER, EXIT, BOTH
    val locationWindowStartTime: String? = null, // ISO LocalTime "HH:mm"
    val locationWindowEndTime: String? = null, // ISO LocalTime "HH:mm"
    val locationWindowDays: String? = null, // Comma-separated enum names
    
    // WifiTrigger fields
    val wifiSsid: String? = null,
    val wifiBssid: String? = null,
    val wifiTriggerType: String? = null, // ON_CONNECT, ON_DISCONNECT, WHILE_CONNECTED
    val wifiWindowStartTime: String? = null, // ISO LocalTime "HH:mm"
    val wifiWindowEndTime: String? = null, // ISO LocalTime "HH:mm"
    val wifiWindowDays: String? = null, // Comma-separated enum names
    
    // Notification & Alert settings
    val notificationStyle: String = "STANDARD", // STANDARD, FULL_SCREEN
    val isPersistentAlert: Boolean = false,
    val ringtoneUri: String? = null,
    val vibrationEnabled: Boolean = true,
    val volume: Int = 100,
    
    // Snooze settings & tracking
    val snoozeEnabled: Boolean = true,
    val snoozeIntervalMinutes: Int = 5,
    val snoozeRepeatCount: Int = 3,
    val currentSnoozeCount: Int = 0,
    val snoozeNextTriggerTime: Long? = null,
    
    // State & Timestamps
    val lastTriggeredTime: Long? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
