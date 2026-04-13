package vn.io.litever.alarm.features.alarm.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

const val alarmListRoute = "alarm_list_route"
const val alarmEditRoute = "alarm_edit_route/{alarmId}"
const val alarmRingingRoute = "alarm_ringing_route/{alarmId}"
const val ringtoneSelectionRoute = "ringtone_selection_route"

fun NavGraphBuilder.alarmGraph(
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToRingtoneSelection: (String?) -> Unit,
    onNavigateBack: () -> Unit,
    navController: androidx.navigation.NavController
) {
    composable(route = alarmListRoute) {
        AlarmListRoute(
            onAddAlarmClick = { onNavigateToEdit(0L) },
            onAlarmClick = { alarm -> onNavigateToEdit(alarm.id) },
        )
    }
    composable(
        route = alarmEditRoute,
        arguments = listOf(navArgument("alarmId") { type = NavType.LongType })
    ) { backStackEntry ->
        val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: 0L
        
        // Collect Result from Ringtone Selection
        val selectedRingtoneUri = backStackEntry.savedStateHandle.get<String>("selectedRingtoneUri")
        
        AlarmEditRoute(
            alarmId = alarmId,
            onBackClick = onNavigateBack,
            onRingtoneClick = { currentUri -> onNavigateToRingtoneSelection(currentUri) },
            selectedRingtoneUri = selectedRingtoneUri
        )
    }
    composable(route = ringtoneSelectionRoute) { backStackEntry ->
        val initialUri = navController.previousBackStackEntry?.savedStateHandle?.get<String>("initialUri")
        RingtoneSelectionRoute(
            initialUri = initialUri,
            onBackClick = onNavigateBack,
            onRingtoneSelected = { uri ->
                navController.previousBackStackEntry?.savedStateHandle?.set("selectedRingtoneUri", uri)
                onNavigateBack()
            }
        )
    }
    composable(
        route = alarmRingingRoute,
        arguments = listOf(navArgument("alarmId") { type = NavType.LongType }),
        deepLinks = listOf(navDeepLink { uriPattern = "app://alarm/ring/{alarmId}" })
    ) { backStackEntry ->
        val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: -1L
        AlarmRingingRoute(
            alarmId = alarmId,
            onFinish = onNavigateBack
        )
    }
}
