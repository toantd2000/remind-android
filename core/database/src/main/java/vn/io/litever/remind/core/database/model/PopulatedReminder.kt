package vn.io.litever.remind.core.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class PopulatedReminder(
    @Embedded
    val reminder: ReminderEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "reminderId"
    )
    val missions: List<MissionEntity>
)
