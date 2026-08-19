package vn.io.litever.remind.core.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.io.litever.remind.core.database.entity.ChecklistItemEntity
import vn.io.litever.remind.core.model.ChecklistItem

class ChecklistItemMapperTest {

    @Test
    fun `toModel correctly maps entity to domain ChecklistItem`() {
        val entity = ChecklistItemEntity(
            id = 50L,
            reminderId = 12L,
            text = "Buy organic apples",
            isCompleted = true,
            orderIndex = 3
        )

        val model = entity.toModel()

        assertEquals(50L, model.id)
        assertEquals(12L, model.reminderId)
        assertEquals("Buy organic apples", model.text)
        assertTrue(model.isCompleted)
        assertEquals(3, model.orderIndex)
    }

    @Test
    fun `toEntity correctly maps domain model with parent reminder ID override`() {
        val model = ChecklistItem(
            id = 0L,
            reminderId = 0L,
            text = "Pack gym towel",
            isCompleted = false,
            orderIndex = 1
        )

        val entity = model.toEntity(parentReminderId = 99L)

        assertEquals(0L, entity.id)
        assertEquals(99L, entity.reminderId)
        assertEquals("Pack gym towel", entity.text)
        assertEquals(false, entity.isCompleted)
        assertEquals(1, entity.orderIndex)
    }
}
