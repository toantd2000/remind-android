package vn.io.litever.remind.features.mission.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

const val typingMissionConfigRoute = "typing_mission_config_route/{alarmId}"
const val phraseSelectionRoute = "phrase_selection_route/{alarmId}"
const val missionRingingRoute = "mission_ringing_route/{alarmId}?isPreview={isPreview}"

fun NavController.navigateToTypingMissionConfig(alarmId: Long) {
    this.navigate("typing_mission_config_route/$alarmId")
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
    navController: NavController
) {
    composable(
        route = typingMissionConfigRoute,
        arguments = listOf(navArgument("alarmId") { type = NavType.LongType })
    ) { backStackEntry ->
        val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: 0L
        
        val initialRepetitions = navController.previousBackStackEntry?.savedStateHandle?.get<Int>("repetitions") ?: 1
        val initialPhraseIds = navController.previousBackStackEntry?.savedStateHandle?.get<List<Long>>("selectedPhraseIds") ?: emptyList()
        
        val updatedPhraseIds by backStackEntry.savedStateHandle.getStateFlow<List<Long>?>("selectedPhraseIds", null).collectAsState()
        
        TypingMissionConfigRoute(
            alarmId = alarmId,
            initialRepetitions = initialRepetitions,
            initialSelectedPhraseIds = updatedPhraseIds ?: initialPhraseIds,
            onBackClick = onBackClick,
            onNavigateToPhraseSelection = { ids -> onNavigateToPhraseSelection(alarmId, ids) },
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
        val isPreview = backStackEntry.arguments?.getBoolean("isPreview") ?: false
        MissionRingingRoute(
            alarmId = alarmId,
            isPreview = isPreview,
            onFinish = { onMissionFinish(alarmId) },
            onAbandon = onBackClick,
            navController = navController
        )
    }
}










