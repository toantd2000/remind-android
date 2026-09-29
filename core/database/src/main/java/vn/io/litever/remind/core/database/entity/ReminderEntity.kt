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
    val triggerType: String, // FIXED_TIME, INTERVAL
    
    // TimeTrigger fields
    val timeTriggerTimes: String? = null, // Comma-separated list of times e.g. "08:00,12:00,20:00"
    val timeTriggerIntervalMinutes: Int? = null, // e.g. 120
    val timeTriggerExactDate: String? = null, // ISO "yyyy-MM-dd" for RepeatMode.NONE
    val timeTriggerRepeatMode: String = "NONE", // NONE, DAILY, WEEKLY, MONTHLY, YEARLY
    val timeTriggerRepeatValues: String? = null, // Comma-separated DayOfWeek values (1..7) e.g. "1,3,5"
    val timeTriggerMonthlyDays: String? = null, // Comma-separated days of month e.g. "1,15,30"
    val timeTriggerYearlyDates: String? = null, // Semicolon-separated Month-Day e.g. "3-8;10-20"
    val timeTriggerTimeWindows: String? = null, // Semicolon-separated Multi-TimeWindows e.g. "08:00|11:30|MONDAY,TUESDAY;13:30|17:30|"
    
    // Legacy single window fields (kept for backward compatibility)
    val timeTriggerWindowStart: String? = null, // ISO LocalTime "HH:mm"
    val timeTriggerWindowEnd: String? = null, // ISO LocalTime "HH:mm"
    val timeTriggerWindowActiveDays: String? = null,
    
    // Action & Urgency fields
    val urgencyLevel: String = "MEDIUM", // SOFT, MEDIUM, HARD
    val notificationStyle: String = "STANDARD", // STANDARD, FULL_SCREEN
    val isPersistentAlert: Boolean = false,
    val autoSilenceMinutes: Int = 5,
    val useAlarmStream: Boolean = true,
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
    val lastChecklistResetTime: Long? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
