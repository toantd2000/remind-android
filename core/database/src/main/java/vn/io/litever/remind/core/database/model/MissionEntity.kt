package vn.io.litever.remind.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "missions",
    foreignKeys = [
        ForeignKey(
            entity = AlarmEntity::class,
            parentColumns = ["id"],
            childColumns = ["alarmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("alarmId")]
)
data class MissionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val alarmId: Long,
    val type: String,
    val missionOrder: Int, // renamed from 'order' as it might be a reserved keyword in some SQL dialects
    val repeatCount: Int,
    val configJson: String? = null
)










