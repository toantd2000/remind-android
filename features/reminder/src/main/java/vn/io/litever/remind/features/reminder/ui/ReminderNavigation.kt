package vn.io.litever.remind.features.reminder.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

const val reminderListRoute = "reminder_list_route"
const val reminderEditRoute = "reminder_edit_route/{reminderId}"
const val reminderRingingRoute = "reminder_ringing_route/{reminderId}"
const val ringtoneSelectionRoute = "ringtone_selection_route"

fun NavGraphBuilder.reminderGraph(
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToRingtoneSelection: (String?) -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateBack: () -> Unit,
    navController: androidx.navigation.NavController
) {
    composable(route = reminderListRoute) {
        ReminderListRoute(
            onAddReminderClick = { onNavigateToEdit(0L) },
            onReminderClick = { reminder -> onNavigateToEdit(reminder.id) },
            onNavigateToPermissions = onNavigateToPermissions
        )
    }
    composable(
        route = reminderEditRoute,
        arguments = listOf(navArgument("reminderId") { type = NavType.LongType })
    ) { backStackEntry ->
        val reminderId = backStackEntry.arguments?.getLong("reminderId")
        
        // Collect Result from Ringtone Selection
        val selectedRingtoneUri = backStackEntry.savedStateHandle.get<String>("selectedRingtoneUri")
        
        ReminderEditRoute(
            reminderId = reminderId ?: 0L,
            onBackClick = onNavigateBack,
            onRingtoneSelectionClick = onNavigateToRingtoneSelection,
            onNavigateToPermissions = onNavigateToPermissions,
            navController = navController
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
        route = reminderRingingRoute,
        arguments = listOf(navArgument("reminderId") { type = NavType.LongType }),
        deepLinks = listOf(navDeepLink { uriPattern = "app://remind/ring/{reminderId}" })
    ) { backStackEntry ->
        val reminderId = backStackEntry.arguments?.getLong("reminderId") ?: -1L
        ReminderRingingRoute(
            reminderId = reminderId,
            onFinish = onNavigateBack
        )
    }
}
