package vn.io.litever.remind.features.mission.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.domain.repository.MissionRepository
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.domain.scheduler.ReminderController
import vn.io.litever.remind.core.model.Mission
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.core.model.Phrase
import vn.io.litever.remind.core.model.TypingMissionConfig
import vn.io.litever.remind.core.model.MathMissionConfig
import vn.io.litever.remind.core.model.MathDifficulty
import vn.io.litever.remind.core.model.MathProblem
import vn.io.litever.remind.core.reminder.ReminderRingManager
import javax.inject.Inject

@HiltViewModel
class MissionRingingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val missionRepository: MissionRepository,
    private val reminderRepository: ReminderRepository,
    private val reminderController: ReminderController,
    private val reminderRingManager: ReminderRingManager
) : ViewModel() {
    private val MISSION_TIMEOUT_SECONDS = 30

    private val reminderId: Long = checkNotNull(savedStateHandle["reminderId"])

    private val _uiState = MutableStateFlow(MissionRingingUiState())
    val uiState = _uiState.asStateFlow()

    private val _userInput = MutableStateFlow("")
    val userInput = _userInput.asStateFlow()

    private var timeoutJob: kotlinx.coroutines.Job? = null

    init {
        loadMissions()
    }

    private fun loadMissions() {
        viewModelScope.launch {
            val reminder = reminderRepository.getReminderById(reminderId) ?: return@launch
            if (reminder.missions.isNotEmpty()) {
                val missions = reminder.missions.sortedBy { it.order }
                _uiState.update { it.copy(missions = missions, reminder = reminder) }
                loadMissionData(0)
            }
        }
    }

    private suspend fun loadMissionData(index: Int) {
        val missions = _uiState.value.missions
        if (index >= missions.size) return

        val mission = missions[index]
        val data = when (mission.type) {
            MissionType.TYPING -> {
                val config = mission.config as? TypingMissionConfig
                val basePhrases = missionRepository.getPhrasesByIds(config?.selectedPhraseIds ?: emptyList(), reminderId)
                val phrases = if (basePhrases.isEmpty()) {
                    listOf(Phrase(content = "I am awake", categoryId = "basic"))
                } else basePhrases

                val result = mutableListOf<Phrase>()
                val repeatCount = mission.repeatCount
                val fullRepeats = repeatCount / phrases.size
                for (i in 0 until fullRepeats) {
                    result.addAll(phrases)
                }
                val remainder = repeatCount % phrases.size
                result.addAll(phrases.shuffled().take(remainder))
                result.shuffle()
                result
            }
            MissionType.MATH -> {
                val config = mission.config as? MathMissionConfig
                List(mission.repeatCount) { generateMathProblem(config?.difficulty ?: MathDifficulty.NORMAL) }
            }
            else -> emptyList<Any>()
        }

        _uiState.update { 
            it.copy(
                currentMissionIndex = index,
                currentRepetition = 1,
                missionData = data,
                currentTargetData = data.firstOrNull()
            )
        }
        startTimeoutTimer()
    }

    private fun startTimeoutTimer() {
        timeoutJob?.cancel()
        timeoutJob = viewModelScope.launch {
            var remaining = MISSION_TIMEOUT_SECONDS
            while (remaining > 0) {
                _uiState.update { it.copy(timeoutCountdown = remaining) }
                kotlinx.coroutines.delay(1000L)
                remaining--
            }
            abandonMission()
        }
    }

    fun abandonMission() {
        viewModelScope.launch {
            reminderRingManager.unmute(reminderId)
            _uiState.update { it.copy(isAbandoned = true) }
        }
    }

    fun onUserInputChange(input: String) {
        _userInput.value = input
        startTimeoutTimer() // Reset timer on every interaction
        val state = _uiState.value
        val currentMission = state.currentMission ?: return

        when (currentMission.type) {
            MissionType.TYPING -> {
                val target = (state.currentTargetData as? Phrase)?.content ?: return
                if (input.trim().equals(target.trim(), ignoreCase = true)) {
                    onRepetitionCompleted()
                }
            }
            MissionType.MATH -> {
                val target = (state.currentTargetData as? MathProblem)?.answer?.toString() ?: return
                if (input.trim() == target) {
                    onRepetitionCompleted()
                }
            }
            else -> { /* Handle other mission types */ }
        }
    }

    private fun onRepetitionCompleted() {
        val state = _uiState.value
        val currentMission = state.currentMission ?: return

        if (state.currentRepetition < currentMission.repeatCount) {
            _uiState.update { 
                it.copy(
                    currentRepetition = it.currentRepetition + 1,
                    currentTargetData = it.missionData.getOrNull(it.currentRepetition)
                )
            }
            _userInput.value = ""
        } else {
            onMissionCompleted()
        }
    }

    private fun onMissionCompleted() {
        val state = _uiState.value
        viewModelScope.launch {
            // Show "Complete" for 1 second
            _uiState.update { it.copy(isMissionJustCompleted = true) }
            delay(1000L)
            _uiState.update { it.copy(isMissionJustCompleted = false) }

            if (state.currentMissionIndex < state.missions.size - 1) {
                loadMissionData(state.currentMissionIndex + 1)
                _userInput.value = ""
            } else {
                finishAndDismiss()
            }
        }
    }

    fun finishAndDismiss() {
        // Just signal the UI to navigate back, the alarm was already dismissed
        _uiState.update { it.copy(isDismissed = true) }
    }

    private fun generateMathProblem(difficulty: MathDifficulty): MathProblem {
        val random = java.util.Random()
        return when (difficulty) {
            MathDifficulty.EASY -> {
                val a = random.nextInt(9) + 1
                val b = random.nextInt(9) + 1
                MathProblem("$a + $b", a + b)
            }
            MathDifficulty.NORMAL -> {
                val a = random.nextInt(90) + 10
                val b = random.nextInt(90) + 10
                MathProblem("$a + $b", a + b)
            }
            MathDifficulty.HARD -> {
                val a = random.nextInt(20) + 5
                val b = random.nextInt(20) + 5
                val c = random.nextInt(10) + 2
                MathProblem("$a + $b * $c", a + b * c)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timeoutJob?.cancel()
    }
}

data class MissionRingingUiState(
    val reminder: vn.io.litever.remind.core.model.Reminder? = null,
    val missions: List<Mission> = emptyList(),
    val currentMissionIndex: Int = 0,
    val currentRepetition: Int = 1,
    val missionData: List<Any> = emptyList(), // Can be Phrases, Math problems, etc.
    val currentTargetData: Any? = null,
    val isCompleted: Boolean = false,
    val isDismissed: Boolean = false,
    val isAbandoned: Boolean = false,
    val timeoutCountdown: Int = 30,
    val isMissionJustCompleted: Boolean = false
) {
    val currentMission: Mission? get() = missions.getOrNull(currentMissionIndex)
}

