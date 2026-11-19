package vn.io.litever.remind.features.mission.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import vn.io.litever.remind.core.model.MathDifficulty
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.core.model.ShakeSensitivity
import vn.io.litever.remind.core.model.TypingMode

const val typingMissionConfigRoute = "typing_mission_config_route/{alarmId}"
const val memoryGameConfigRoute = "memory_game_config_route/{alarmId}"
const val mathMissionConfigRoute = "math_mission_config_route/{alarmId}"
const val shakeMissionConfigRoute = "shake_mission_config_route/{alarmId}"
const val barcodeMissionConfigRoute = "barcode_mission_config_route/{alarmId}"
const val randomMissionConfigRoute = "random_mission_config_route/{alarmId}"
const val phraseSelectionRoute = "phrase_selection_route/{alarmId}"
const val missionRingingRoute = "mission_ringing_route/{alarmId}?isPreview={isPreview}"

fun NavController.navigateToTypingMissionConfig(alarmId: Long) {
    this.navigate("typing_mission_config_route/$alarmId")
}

fun NavController.navigateToMathMissionConfig(alarmId: Long) {
    this.navigate("math_mission_config_route/$alarmId")
}

fun NavController.navigateToMemoryGameConfig(alarmId: Long) {
    this.navigate("memory_game_config_route/$alarmId")
}

fun NavController.navigateToShakeMissionConfig(alarmId: Long) {
    this.navigate("shake_mission_config_route/$alarmId")
}

fun NavController.navigateToBarcodeMissionConfig(alarmId: Long) {
    this.navigate("barcode_mission_config_route/$alarmId")
}

fun NavController.navigateToRandomMissionConfig(alarmId: Long) {
    this.navigate("random_mission_config_route/$alarmId")
}

fun NavController.navigateToPhraseSelection(alarmId: Long) {
    this.navigate("phrase_selection_route/$alarmId")
}

fun NavController.navigateToMissionRinging(alarmId: Long, isPreview: Boolean = false) {
    this.navigate("mission_ringing_route/$alarmId?isPreview=$isPreview")
}

fun NavGraphBuilder.missionGraph(
    onNavigateToPhraseSelection: (Long, List<Long>) -> Unit,
    onPhrasesSelected: (List<Long>) -> Unit,
    onSaveMission: (vn.io.litever.remind.core.model.Mission) -> Unit,
    onMissionFinish: (Long) -> Unit,
    onBackClick: () -> Unit,
    onNavigateToPaywall: (Int) -> Unit = {},
    navController: NavController
) {
    composable(
        route = typingMissionConfigRoute,
        arguments = listOf(navArgument("alarmId") { type = NavType.LongType })
    ) { backStackEntry ->
        val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: 0L
        
        val initialRepetitions = navController.previousBackStackEntry?.savedStateHandle?.get<Int>("repetitions") ?: 1
        val initialPhraseIds = navController.previousBackStackEntry?.savedStateHandle?.get<List<Long>>("selectedPhraseIds") ?: emptyList()
        val initialModeStr = navController.previousBackStackEntry?.savedStateHandle?.get<String>("typingMode")
        val initialMode = initialModeStr?.let { runCatching { TypingMode.valueOf(it) }.getOrNull() } ?: TypingMode.NORMAL
        
        val updatedPhraseIds by backStackEntry.savedStateHandle.getStateFlow<List<Long>?>("selectedPhraseIds", null).collectAsState()
        
        TypingMissionConfigRoute(
            alarmId = alarmId,
            initialRepetitions = initialRepetitions,
            initialSelectedPhraseIds = updatedPhraseIds ?: initialPhraseIds,
            initialMode = initialMode,
            onBackClick = onBackClick,
            onNavigateToPhraseSelection = { ids -> onNavigateToPhraseSelection(alarmId, ids) },
            onSaveMission = onSaveMission
        )
    }

    composable(
        route = mathMissionConfigRoute,
        arguments = listOf(navArgument("alarmId") { type = NavType.LongType })
    ) { backStackEntry ->
        val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: 0L
        val initialRepetitions = navController.previousBackStackEntry?.savedStateHandle?.get<Int>("repetitions") ?: 3
        val initialDifficultyStr = navController.previousBackStackEntry?.savedStateHandle?.get<String>("mathDifficulty")
        val initialDifficulty = initialDifficultyStr?.let { runCatching { MathDifficulty.valueOf(it) }.getOrNull() } ?: MathDifficulty.NORMAL

        MathMissionConfigRoute(
            alarmId = alarmId,
            initialRepetitions = initialRepetitions,
            initialDifficulty = initialDifficulty,
            onBackClick = onBackClick,
            onSaveMission = onSaveMission
        )
    }

    composable(
        route = memoryGameConfigRoute,
        arguments = listOf(navArgument("alarmId") { type = NavType.LongType })
    ) { backStackEntry ->
        val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: 0L
        
        val initialRepetitions = navController.previousBackStackEntry?.savedStateHandle?.get<Int>("repetitions") ?: 1
        
        MemoryGameConfigRoute(
            alarmId = alarmId,
            initialRepetitions = initialRepetitions,
            onBackClick = onBackClick,
            onSaveMission = onSaveMission
        )
    }

    composable(
        route = shakeMissionConfigRoute,
        arguments = listOf(navArgument("alarmId") { type = NavType.LongType })
    ) { backStackEntry ->
        val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: 0L
        val targetCount = navController.previousBackStackEntry?.savedStateHandle?.get<Int>("shakeTargetCount") ?: 30
        val sensitivityStr = navController.previousBackStackEntry?.savedStateHandle?.get<String>("shakeSensitivity")
        val sensitivity = sensitivityStr?.let { runCatching { ShakeSensitivity.valueOf(it) }.getOrNull() }
            ?: ShakeSensitivity.NORMAL

        ShakeMissionConfigRoute(
            alarmId = alarmId,
            initialTargetCount = targetCount,
            initialSensitivity = sensitivity,
            onBackClick = onBackClick,
            onSaveMission = onSaveMission
        )
    }

    composable(
        route = barcodeMissionConfigRoute,
        arguments = listOf(navArgument("alarmId") { type = NavType.LongType })
    ) { backStackEntry ->
        val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: 0L
        val targetBarcode = navController.previousBackStackEntry?.savedStateHandle?.get<String>("targetBarcode") ?: ""
        val barcodeLabel = navController.previousBackStackEntry?.savedStateHandle?.get<String>("barcodeLabel") ?: ""

        BarcodeMissionConfigRoute(
            alarmId = alarmId,
            initialBarcode = targetBarcode,
            initialLabel = barcodeLabel,
            onBackClick = onBackClick,
            onSaveMission = onSaveMission
        )
    }

    composable(
        route = randomMissionConfigRoute,
        arguments = listOf(navArgument("alarmId") { type = NavType.LongType })
    ) { backStackEntry ->
        val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: 0L
        val initialRepetitions = navController.previousBackStackEntry?.savedStateHandle?.get<Int>("repetitions") ?: 1
        val difficultyStr = navController.previousBackStackEntry?.savedStateHandle?.get<String>("randomDifficulty")
        val difficulty = difficultyStr?.let { runCatching { MathDifficulty.valueOf(it) }.getOrNull() }
            ?: MathDifficulty.NORMAL
        val candidateNames = navController.previousBackStackEntry?.savedStateHandle?.get<List<String>>("randomCandidates")
        val candidates = candidateNames?.mapNotNull { runCatching { MissionType.valueOf(it) }.getOrNull() }
            ?.ifEmpty { null }
            ?: listOf(MissionType.MATH, MissionType.TYPING, MissionType.MEMORY_FIND_COLOR_TILES, MissionType.SHAKE)

        RandomMissionConfigRoute(
            alarmId = alarmId,
            initialCandidateTypes = candidates,
            initialDifficulty = difficulty,
            initialRepetitions = initialRepetitions,
            onBackClick = onBackClick,
            onNavigateToPaywall = onNavigateToPaywall,
            onSaveMission = onSaveMission
        )
    }
    
    composable(
        route = phraseSelectionRoute,
        arguments = listOf(navArgument("alarmId") { type = NavType.LongType })
    ) { backStackEntry ->
        val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: 0L
        val initialPhraseIds = navController.previousBackStackEntry?.savedStateHandle?.get<List<Long>>("selectedPhraseIds") ?: emptyList()

        PhraseSelectionRoute(
            initialSelectedIds = initialPhraseIds,
            onBackClick = onBackClick,
            onPhrasesSelected = onPhrasesSelected
        )
    }

    composable(
        route = missionRingingRoute,
        arguments = listOf(
            navArgument("alarmId") { type = NavType.LongType },
            navArgument("isPreview") { 
                type = NavType.BoolType
                defaultValue = false
            }
        )
    ) { backStackEntry ->
        val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: 0L
        MissionRingingRoute(
            onFinish = { onMissionFinish(alarmId) },
            onAbandon = onBackClick,
            navController = navController
        )
    }
}










