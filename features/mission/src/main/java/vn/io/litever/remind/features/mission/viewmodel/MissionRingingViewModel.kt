package vn.io.litever.remind.features.mission.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.domain.repository.MissionRepository
import vn.io.litever.remind.core.domain.repository.AlarmRepository
import vn.io.litever.remind.core.domain.scheduler.AlarmController
import vn.io.litever.remind.core.model.*
import vn.io.litever.remind.core.alarm.AlarmRingManager
import vn.io.litever.remind.core.alarm.DraftAlarmStore
import javax.inject.Inject

@HiltViewModel
class MissionRingingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val missionRepository: MissionRepository,
    private val alarmRepository: AlarmRepository,
    private val alarmController: AlarmController,
    private val alarmRingManager: AlarmRingManager,
    private val draftAlarmStore: DraftAlarmStore,
    private val analyticsLogger: vn.io.litever.remind.core.analytics.AnalyticsLogger
) : ViewModel() {
    private val MISSION_TIMEOUT_SECONDS = 30

    private val alarmId: Long = checkNotNull(savedStateHandle["alarmId"])
    private val isPreview: Boolean = savedStateHandle["isPreview"] ?: false

    private val _uiState = MutableStateFlow(MissionRingingUiState())
    val uiState = _uiState.asStateFlow()

    private val _userInput = MutableStateFlow("")
    val userInput = _userInput.asStateFlow()

    private val mathGenerator = MathProblemGenerator()
    private var timeoutJob: kotlinx.coroutines.Job? = null

    init {
        loadMissions()
    }

    private fun loadMissions() {
        viewModelScope.launch {
            val alarm = draftAlarmStore.getDraft() ?: alarmRepository.getAlarmById(alarmId) ?: return@launch
            if (alarm.missions.isNotEmpty()) {
                val missions = alarm.missions.sortedBy { it.order }.map { resolveRandomMission(it) }
                _uiState.update { it.copy(isLoading = false, missions = missions, alarm = alarm) }
                loadMissionData(0)
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun resolveRandomMission(mission: Mission): Mission {
        if (mission.type != MissionType.RANDOM) return mission
        val config = mission.config as? RandomMissionConfig ?: RandomMissionConfig()
        val candidates = config.candidateTypes.filter { it != MissionType.RANDOM }
            .ifEmpty { listOf(MissionType.MATH, MissionType.TYPING, MissionType.MEMORY_FIND_COLOR_TILES, MissionType.SHAKE) }
        val chosenType = candidates.random()
        val chosenConfig: MissionConfig = when (chosenType) {
            MissionType.MATH -> MathMissionConfig(difficulty = config.difficulty)
            MissionType.TYPING -> TypingMissionConfig(
                mode = when (config.difficulty) {
                    MathDifficulty.EASY, MathDifficulty.NORMAL -> TypingMode.NORMAL
                    MathDifficulty.HARD -> TypingMode.SHUFFLE_WORDS
                    MathDifficulty.VERY_HARD -> TypingMode.SHUFFLE_CHARS
                }
            )
            MissionType.MEMORY_FIND_COLOR_TILES -> when (config.difficulty) {
                MathDifficulty.EASY -> MemoryTilesMissionConfig(gridSize = 3, targetTiles = 3)
                MathDifficulty.NORMAL -> MemoryTilesMissionConfig(gridSize = 3, targetTiles = 4)
                MathDifficulty.HARD -> MemoryTilesMissionConfig(gridSize = 4, targetTiles = 5)
                MathDifficulty.VERY_HARD -> MemoryTilesMissionConfig(gridSize = 4, targetTiles = 7)
            }
            MissionType.SHAKE -> when (config.difficulty) {
                MathDifficulty.EASY -> ShakeMissionConfig(targetCount = 15, sensitivity = ShakeSensitivity.EASY)
                MathDifficulty.NORMAL -> ShakeMissionConfig(targetCount = 30, sensitivity = ShakeSensitivity.NORMAL)
                MathDifficulty.HARD -> ShakeMissionConfig(targetCount = 45, sensitivity = ShakeSensitivity.NORMAL)
                MathDifficulty.VERY_HARD -> ShakeMissionConfig(targetCount = 60, sensitivity = ShakeSensitivity.HARD)
            }
            MissionType.BARCODE -> BarcodeMissionConfig()
            MissionType.RANDOM -> MathMissionConfig(difficulty = config.difficulty)
        }
        return mission.copy(type = chosenType, config = chosenConfig)
    }

    private suspend fun loadMissionData(index: Int) {
        val missions = _uiState.value.missions
        if (index >= missions.size) return

        val mission = missions[index]
        val data = when (mission.type) {
            MissionType.TYPING -> {
                val config = mission.config as? TypingMissionConfig
                val basePhrases = missionRepository.getPhrasesByIds(config?.selectedPhraseIds ?: emptyList(), alarmId)
                val phrases = if (basePhrases.isEmpty()) {
                    listOf(Phrase(content = "I am awake", categoryId = "basic"))
                } else basePhrases

                val transformedPhrases = phrases.map { phrase ->
                    val newContent = when (config?.mode) {
                        vn.io.litever.remind.core.model.TypingMode.SHUFFLE_WORDS -> {
                            phrase.content.split(".").joinToString(".") { sentence ->
                                val parts = sentence.split(" ")
                                val words = parts.filter { it.isNotBlank() }.shuffled()
                                var wordIdx = 0
                                parts.joinToString(" ") { 
                                    if (it.isNotBlank()) words[wordIdx++] else it 
                                }
                            }
                        }
                        vn.io.litever.remind.core.model.TypingMode.SHUFFLE_CHARS -> {
                            phrase.content.split(" ").joinToString(" ") { word ->
                                if (word.isNotBlank()) {
                                    word.toList().shuffled().joinToString("")
                                } else {
                                    word
                                }
                            }
                        }
                        else -> phrase.content
                    }
                    phrase.copy(content = newContent)
                }

                val result = mutableListOf<Phrase>()
                val repeatCount = mission.repeatCount
                val fullRepeats = repeatCount / transformedPhrases.size
                for (i in 0 until fullRepeats) {
                    result.addAll(transformedPhrases)
                }
                val remainder = repeatCount % transformedPhrases.size
                result.addAll(transformedPhrases.shuffled().take(remainder))
                result.shuffle()
                result
            }
            MissionType.MATH -> {
                val config = mission.config as? MathMissionConfig
                List(mission.repeatCount) { mathGenerator.next(config?.difficulty ?: MathDifficulty.NORMAL) }
            }
            MissionType.MEMORY_FIND_COLOR_TILES -> {
                val config = mission.config as? MemoryTilesMissionConfig
                val gridSize = config?.gridSize ?: 3
                val targetCount = config?.targetTiles ?: 3
                val totalTiles = gridSize * gridSize
                List(mission.repeatCount) {
                    val indices = (0 until totalTiles).shuffled().take(targetCount)
                    MemoryGameBoard(gridSize, targetCount, indices)
                }
            }
            MissionType.SHAKE -> {
                val config = mission.config as? ShakeMissionConfig ?: ShakeMissionConfig()
                listOf(config)
            }
            MissionType.BARCODE -> {
                val config = mission.config as? BarcodeMissionConfig ?: BarcodeMissionConfig()
                listOf(config)
            }
            MissionType.RANDOM -> emptyList()
        }

        _uiState.update { 
            it.copy(
                currentMissionIndex = index,
                currentRepetition = 1,
                missionData = data,
                currentTargetData = data.firstOrNull(),
                timeoutCountdown = MISSION_TIMEOUT_SECONDS,
                validationState = StepValidationState.IDLE
            )
        }
        startTimeoutTimer()
    }

    private fun startTimeoutTimer() {
        timeoutJob?.cancel()
        timeoutJob = viewModelScope.launch {
            _uiState.update { it.copy(timeoutCountdown = MISSION_TIMEOUT_SECONDS) }
            while (_uiState.value.timeoutCountdown > 0) {
                delay(1000L)
                if (!_uiState.value.isTimerPaused) {
                    _uiState.update { it.copy(timeoutCountdown = (it.timeoutCountdown - 1).coerceAtLeast(0)) }
                }
            }
            abandonMission()
        }
    }

    fun onUserInteraction() {
        _uiState.update { it.copy(timeoutCountdown = MISSION_TIMEOUT_SECONDS) }
    }

    fun setTimerPaused(isPaused: Boolean) {
        _uiState.update { it.copy(isTimerPaused = isPaused) }
    }

    fun abandonMission() {
        viewModelScope.launch {
            alarmRingManager.unmute(alarmId)
            _uiState.update { it.copy(isAbandoned = true) }
        }
    }

    fun onUserInputChange(input: String) {
        _userInput.value = input
        onUserInteraction()
    }

    fun validateCurrentStep() {
        val state = _uiState.value
        val currentMission = state.currentMission ?: return
        val input = _userInput.value

        when (currentMission.type) {
            MissionType.TYPING -> {
                val target = (state.currentTargetData as? Phrase)?.content ?: return
                if (input == target) {
                    onRepetitionCompleted()
                }
            }
            MissionType.MATH -> {
                val target = (state.currentTargetData as? MathProblem)?.answer?.toString() ?: return
                if (input.trim() == target) {
                    viewModelScope.launch {
                        _uiState.update { it.copy(validationState = StepValidationState.CORRECT) }
                        setTimerPaused(true)
                        delay(450L)
                        _uiState.update { it.copy(validationState = StepValidationState.IDLE) }
                        setTimerPaused(false)
                        onRepetitionCompleted()
                    }
                } else {
                    viewModelScope.launch {
                        _uiState.update { it.copy(validationState = StepValidationState.WRONG) }
                        delay(600L)
                        _userInput.value = ""
                        _uiState.update { it.copy(validationState = StepValidationState.IDLE) }
                    }
                }
            }
            MissionType.MEMORY_FIND_COLOR_TILES,
            MissionType.SHAKE,
            MissionType.BARCODE -> {
                if (input == "SUCCESS") {
                    onRepetitionCompleted()
                }
            }
            MissionType.RANDOM -> {}
        }
    }

    private fun onRepetitionCompleted() {
        val state = _uiState.value
        val currentMission = state.currentMission ?: return

        if (state.currentRepetition < currentMission.repeatCount) {
            _uiState.update { 
                it.copy(
                    currentRepetition = it.currentRepetition + 1,
                    currentTargetData = it.missionData.getOrNull(it.currentRepetition),
                    timeoutCountdown = MISSION_TIMEOUT_SECONDS,
                    validationState = StepValidationState.IDLE
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
            if (!isPreview) {
                analyticsLogger.logEvent("mission_completed", mapOf(
                    "alarm_id" to alarmId,
                    "mission_type" to (state.currentMission?.type?.name ?: "UNKNOWN")
                ))
            }

            // Show "Complete" for 1 second
            setTimerPaused(true)
            _uiState.update { it.copy(isMissionJustCompleted = true) }
            delay(1000L)
            _uiState.update { it.copy(isMissionJustCompleted = false) }

            if (state.currentMissionIndex < state.missions.size - 1) {
                setTimerPaused(false)
                loadMissionData(state.currentMissionIndex + 1)
                _userInput.value = ""
            } else {
                finishAndDismiss()
            }
        }
    }

    fun finishAndDismiss() {
        _uiState.update { it.copy(isDismissed = true) }
    }

    override fun onCleared() {
        super.onCleared()
        timeoutJob?.cancel()
    }
}

enum class StepValidationState {
    IDLE, CORRECT, WRONG
}

data class MissionRingingUiState(
    val isLoading: Boolean = true,
    val alarm: Alarm? = null,
    val missions: List<Mission> = emptyList(),
    val currentMissionIndex: Int = 0,
    val currentRepetition: Int = 1,
    val missionData: List<Any> = emptyList(),
    val currentTargetData: Any? = null,
    val isCompleted: Boolean = false,
    val isDismissed: Boolean = false,
    val isAbandoned: Boolean = false,
    val timeoutCountdown: Int = 30,
    val isTimerPaused: Boolean = false,
    val validationState: StepValidationState = StepValidationState.IDLE,
    val isMissionJustCompleted: Boolean = false
) {
    val currentMission: Mission? get() = missions.getOrNull(currentMissionIndex)
}
