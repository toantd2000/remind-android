package vn.io.litever.remind.features.mission

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.io.litever.remind.core.model.Mission
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.core.model.Phrase
import vn.io.litever.remind.features.mission.viewmodel.MissionRingingUiState

class MissionRingingUiStateTest {

    @Test
    fun defaultState_hasExpectedDefaults() {
        val state = MissionRingingUiState()
        assertTrue(state.isLoading)
        assertNull(state.alarm)
        assertTrue(state.missions.isEmpty())
        assertEquals(0, state.currentMissionIndex)
        assertEquals(1, state.currentRepetition)
        assertTrue(state.missionData.isEmpty())
        assertNull(state.currentTargetData)
        assertFalse(state.isCompleted)
        assertFalse(state.isDismissed)
        assertFalse(state.isAbandoned)
        assertEquals(30, state.timeoutCountdown)
        assertFalse(state.isMissionJustCompleted)
        assertNull(state.currentMission)
    }

    @Test
    fun currentMission_withValidIndex_returnsMission() {
        val mission1 = Mission(id = 1L, alarmId = 10L, type = MissionType.TYPING, order = 0, repeatCount = 3)
        val mission2 = Mission(id = 2L, alarmId = 10L, type = MissionType.MATH, order = 1, repeatCount = 5)
        
        val state = MissionRingingUiState(
            missions = listOf(mission1, mission2),
            currentMissionIndex = 1
        )
        
        assertEquals(mission2, state.currentMission)
    }

    @Test
    fun currentMission_withOutOfBoundsIndex_returnsNull() {
        val mission1 = Mission(id = 1L, alarmId = 10L, type = MissionType.TYPING, order = 0, repeatCount = 3)
        
        val state = MissionRingingUiState(
            missions = listOf(mission1),
            currentMissionIndex = 5
        )
        
        assertNull(state.currentMission)
    }

    @Test
    fun currentMission_withNegativeIndex_returnsNull() {
        val mission1 = Mission(id = 1L, alarmId = 10L, type = MissionType.TYPING, order = 0, repeatCount = 3)
        
        val state = MissionRingingUiState(
            missions = listOf(mission1),
            currentMissionIndex = -1
        )
        
        assertNull(state.currentMission)
    }

    @Test
    fun stateCopy_updatesRepetitionAndTargetDataCorrectly() {
        val phrase1 = Phrase(id = 1L, content = "Wake up now", categoryId = "motivation")
        val phrase2 = Phrase(id = 2L, content = "Seize the day", categoryId = "motivation")
        val mission = Mission(id = 1L, alarmId = 10L, type = MissionType.TYPING, order = 0, repeatCount = 2)

        val initialState = MissionRingingUiState(
            isLoading = false,
            missions = listOf(mission),
            currentMissionIndex = 0,
            currentRepetition = 1,
            missionData = listOf(phrase1, phrase2),
            currentTargetData = phrase1
        )

        val updatedState = initialState.copy(
            currentRepetition = 2,
            currentTargetData = phrase2
        )

        assertEquals(2, updatedState.currentRepetition)
        assertEquals(phrase2, updatedState.currentTargetData)
        assertEquals("Seize the day", (updatedState.currentTargetData as Phrase).content)
    }

    @Test
    fun stateCopy_missionJustCompletedFlag_transitionsAccurately() {
        val state = MissionRingingUiState(isLoading = false)
        assertFalse(state.isMissionJustCompleted)

        val completedState = state.copy(isMissionJustCompleted = true)
        assertTrue(completedState.isMissionJustCompleted)

        val resumedState = completedState.copy(isMissionJustCompleted = false)
        assertFalse(resumedState.isMissionJustCompleted)
    }
}
