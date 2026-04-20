package vn.io.litever.remind.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String,
    val isEnabled: Boolean,
    val repeatDays: String, // JSON string or comma separated IDs
    val date: String?,
    val vibrationEnabled: Boolean,
    val ringtoneUri: String?,
    val volume: Int,
    val snoozeEnabled: Boolean,
    val snoozeInterval: Int,
    val snoozeRepeatCount: Int,
    val autoSilenceMinutes: Int,
    val currentSnoozeCount: Int,
    val snoozeNextTriggerTime: Long?,
    val isMissed: Boolean,
    val message: String,
    val skippedAt: Long?,
    val gradualVolumeDurationSeconds: Int
)
