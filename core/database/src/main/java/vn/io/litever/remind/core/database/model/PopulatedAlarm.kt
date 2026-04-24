package vn.io.litever.remind.core.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class PopulatedAlarm(
    @Embedded
    val alarm: AlarmEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "alarmId"
    )
    val missions: List<MissionEntity>
)










