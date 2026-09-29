package vn.io.litever.remind.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Relation


@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val isEnabled: Boolean = true,
    val triggerType: String, // FIXED_TIME, INTERVAL
    
    // TimeTrigger fields
    val timeTriggerTimes: String? = null, // Comma-separated or JSON list of times
    val timeTriggerIntervalMinutes: Int? = null,
    val timeTriggerExactDate: String? = null, // ISO "yyyy-MM-dd"
    val timeTriggerRepeatMode: String = "NONE",
    val timeTriggerRepeatValues: String? = null, // Comma-separated ints
    val timeTriggerWindowStart: String? = null, // ISO LocalTime "HH:mm"
    val timeTriggerWindowEnd: String? = null, // ISO LocalTime "HH:mm"
    val timeTriggerWindowActiveDays: String? = null,
    
    // Action fields
    val urgencyLevel: String = "MEDIUM", // SOFT, MEDIUM, HARD
    val notificationStyle: String = "STANDARD", // STANDARD, FULL_SCREEN
    val isPersistentAlert: Boolean = false,
    val autoSilenceMinutes: Int = 5,
    val useAlarmStream: Boolean = true,
    val ringtoneUri: String? = null,
    val vibrationEnabled: Boolean = true,
    val volume: Int = 100,
    
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


