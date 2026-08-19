package vn.io.litever.remind.core.database.model

import androidx.room.Embedded
import androidx.room.Relation
import vn.io.litever.remind.core.database.entity.ChecklistItemEntity
import vn.io.litever.remind.core.database.entity.ReminderEntity

data class PopulatedReminder(
    @Embedded
    val reminder: ReminderEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "reminderId"
    )
    val checklist: List<ChecklistItemEntity> = emptyList()
)
