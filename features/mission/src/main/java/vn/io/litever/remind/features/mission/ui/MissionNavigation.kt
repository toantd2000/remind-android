package vn.io.litever.remind.features.mission.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

const val typingMissionConfigRoute = "typing_mission_config_route/{reminderId}"
const val phraseSelectionRoute = "phrase_selection_route/{reminderId}"
const val missionRingingRoute = "mission_ringing_route/{reminderId}"

fun NavController.navigateToTypingMissionConfig(reminderId: Long) {
    this.navigate("typing_mission_config_route/$reminderId")
}

fun NavController.navigateToPhraseSelection(reminderId: Long) {
    this.navigate("phrase_selection_route/$reminderId")
}

fun NavController.navigateToMissionRinging(reminderId: Long) {
    this.navigate("mission_ringing_route/$reminderId")
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
        arguments = listOf(navArgument("reminderId") { type = NavType.LongType })
    ) { backStackEntry ->
        val reminderId = backStackEntry.arguments?.getLong("reminderId") ?: 0L
        val initialRepetitions = navController.currentBackStackEntry?.savedStateHandle?.get<Int>("repetitions") ?: 1
        val initialPhraseIds = navController.currentBackStackEntry?.savedStateHandle?.get<List<Long>>("selectedPhraseIds") ?: emptyList()
        
        TypingMissionConfigRoute(
            reminderId = reminderId,
            initialRepetitions = initialRepetitions,
            initialSelectedPhraseIds = initialPhraseIds,
            onBackClick = onBackClick,
            onNavigateToPhraseSelection = { ids -> onNavigateToPhraseSelection(reminderId, ids) },
            onSaveMission = onSaveMission
        )
    }
    
    composable(
        route = phraseSelectionRoute,
        arguments = listOf(navArgument("reminderId") { type = NavType.LongType })
    ) { backStackEntry ->
        val reminderId = backStackEntry.arguments?.getLong("reminderId") ?: 0L
        val initialPhraseIds = navController.currentBackStackEntry?.savedStateHandle?.get<List<Long>>("selectedPhraseIds") ?: emptyList()

        PhraseSelectionRoute(
            initialSelectedIds = initialPhraseIds,
            onBackClick = onBackClick,
            onPhrasesSelected = onPhrasesSelected
        )
    }

    composable(
        route = missionRingingRoute,
        arguments = listOf(navArgument("reminderId") { type = NavType.LongType })
    ) { backStackEntry ->
        val reminderId = backStackEntry.arguments?.getLong("reminderId") ?: 0L
        MissionRingingRoute(
            reminderId = reminderId,
            onFinish = { onMissionFinish(reminderId) },
            onAbandon = onBackClick,
            navController = navController
        )
    }
}
