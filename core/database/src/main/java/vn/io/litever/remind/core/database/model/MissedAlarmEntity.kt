package vn.io.litever.remind.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "missed_alarms")
data class MissedAlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val alarmId: Long,
    val alarmLabel: String,
    val scheduledTime: Long,
    val missedTime: Long,
    val reason: String
)
