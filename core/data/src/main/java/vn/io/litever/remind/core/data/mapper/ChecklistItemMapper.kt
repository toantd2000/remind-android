package vn.io.litever.remind.core.data.mapper

import vn.io.litever.remind.core.database.entity.ChecklistItemEntity
import vn.io.litever.remind.core.model.ChecklistItem

/**
 * Maps a Room [ChecklistItemEntity] to a pure domain [ChecklistItem].
 */
fun ChecklistItemEntity.toModel(): ChecklistItem {
    return ChecklistItem(
        id = id,
        reminderId = reminderId,
        text = text,
        isCompleted = isCompleted,
        orderIndex = orderIndex
    )
}

/**
 * Maps a domain [ChecklistItem] to a Room [ChecklistItemEntity].
 * @param parentReminderId Optional override for the parent reminder's foreign key ID.
 */
fun ChecklistItem.toEntity(parentReminderId: Long = reminderId): ChecklistItemEntity {
    return ChecklistItemEntity(
        id = id,
        reminderId = parentReminderId,
        text = text,
        isCompleted = isCompleted,
        orderIndex = orderIndex
    )
}
