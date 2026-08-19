package vn.io.litever.remind.core.data.repository

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import vn.io.litever.remind.core.database.dao.ChecklistItemDao
import vn.io.litever.remind.core.database.dao.ReminderDao
import vn.io.litever.remind.core.database.entity.ChecklistItemEntity
import vn.io.litever.remind.core.database.entity.ReminderEntity
import vn.io.litever.remind.core.database.model.PopulatedReminder
import vn.io.litever.remind.core.model.ChecklistItem
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.core.model.GeofenceTransitionType
import vn.io.litever.remind.core.model.LocationTrigger
import vn.io.litever.remind.core.model.NotificationStyle
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.model.WifiTrigger
import vn.io.litever.remind.core.model.WifiTriggerType
import java.time.LocalTime

/**
 * Adversarial Challenger Test for ReminderRepositoryImpl and Data Layer transformations.
 */
class ReminderRepositoryChallengerTest {

    private val reminderDao = mockk<ReminderDao>(relaxed = true)
    private val checklistItemDao = mockk<ChecklistItemDao>(relaxed = true)
    private lateinit var repository: ReminderRepositoryImpl

    @Before
    fun setup() {
        repository = ReminderRepositoryImpl(reminderDao, checklistItemDao)
    }

    @Test
    fun reactiveFlow_tracksStateChangesAcrossMultipleEmissions() = runTest {
        val stateFlow = MutableStateFlow<List<PopulatedReminder>>(emptyList())
        every { reminderDao.getAllReminders() } returns stateFlow

        repository.getAllReminders().test {
            // Initial empty emission
            assertEquals(0, awaitItem().size)

            // Emit 1 reminder
            val reminder1 = PopulatedReminder(
                reminder = ReminderEntity(id = 1L, title = "First", triggerType = "TIME"),
                checklist = listOf(ChecklistItemEntity(id = 10L, reminderId = 1L, text = "Subtask 1", isCompleted = false))
            )
            stateFlow.value = listOf(reminder1)

            val emission1 = awaitItem()
            assertEquals(1, emission1.size)
            assertEquals("First", emission1[0].title)
            assertEquals(0.0f, emission1[0].checklistProgress, 0.001f)

            // Emit updated reminder (checklist item completed)
            val reminder1Updated = PopulatedReminder(
                reminder = ReminderEntity(id = 1L, title = "First", triggerType = "TIME"),
                checklist = listOf(ChecklistItemEntity(id = 10L, reminderId = 1L, text = "Subtask 1", isCompleted = true))
            )
            stateFlow.value = listOf(reminder1Updated)

            val emission2 = awaitItem()
            assertEquals(1, emission2.size)
            assertEquals(1.0f, emission2[0].checklistProgress, 0.001f)
            assertTrue(emission2[0].isAllChecklistCompleted)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertComplexReminderWithGeofenceWifiAndChecklist_mapsAllFieldsAccurately() = runTest {
        val reminder = Reminder(
            title = "Office Arrival",
            notes = "Check in with team",
            isEnabled = true,
            triggerType = ReminderTriggerType.LOCATION,
            locationTrigger = LocationTrigger(
                latitude = 10.7769,
                longitude = 106.7009,
                radiusMeters = 200f,
                locationName = "Bitexco",
                transitionType = GeofenceTransitionType.ENTER,
                timeWindow = ReminderTimeWindow(
                    startTime = LocalTime.of(8, 0),
                    endTime = LocalTime.of(10, 0),
                    activeDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)
                )
            ),
            checklist = listOf(
                ChecklistItem(text = "ID Badge", isCompleted = false, orderIndex = 0),
                ChecklistItem(text = "Laptop Charger", isCompleted = true, orderIndex = 1)
            ),
            notificationStyle = NotificationStyle.FULL_SCREEN,
            isPersistentAlert = true,
            snoozeIntervalMinutes = 10,
            snoozeRepeatCount = 5
        )

        coEvery { reminderDao.insertReminder(any()) } returns 999L

        val id = repository.insertReminder(reminder)
        assertEquals(999L, id)

        coVerify(exactly = 1) {
            reminderDao.insertReminder(match { entity ->
                entity.title == "Office Arrival" &&
                entity.locationLatitude == 10.7769 &&
                entity.locationLongitude == 106.7009 &&
                entity.locationRadiusMeters == 200f &&
                entity.locationName == "Bitexco" &&
                entity.locationTransitionType == "ENTER" &&
                entity.locationWindowStartTime == "08:00" &&
                entity.locationWindowEndTime == "10:00" &&
                entity.locationWindowDays == "MONDAY,TUESDAY" &&
                entity.notificationStyle == "FULL_SCREEN" &&
                entity.isPersistentAlert &&
                entity.snoozeIntervalMinutes == 10 &&
                entity.snoozeRepeatCount == 5
            })
        }

        coVerify(exactly = 1) {
            checklistItemDao.insertChecklistItems(match { items ->
                items.size == 2 &&
                items[0].reminderId == 999L && items[0].text == "ID Badge" && !items[0].isCompleted && items[0].orderIndex == 0 &&
                items[1].reminderId == 999L && items[1].text == "Laptop Charger" && items[1].isCompleted && items[1].orderIndex == 1
            })
        }
    }
}
