package vn.io.litever.remind.features.reminders.ui

import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import vn.io.litever.remind.features.reminders.viewmodel.ReminderEditViewModel

const val RemindersListRoute = "reminders_list_route"
const val remindersRoute = RemindersListRoute
const val ReminderListRoute = RemindersListRoute
const val ReminderEditRoute = "reminder_edit_route/{reminderId}"
const val ReminderTriggerEditRoute = "reminder_trigger_edit_route/{reminderId}"
const val ReminderRingingRoute = "reminder_ringing_route/{reminderId}"

fun createReminderEditRoute(reminderId: Long = 0L): String = "reminder_edit_route/$reminderId"
fun createReminderTriggerEditRoute(reminderId: Long = 0L): String = "reminder_trigger_edit_route/$reminderId"
fun createReminderRingingRoute(reminderId: Long): String = "reminder_ringing_route/$reminderId"

fun NavController.navigateToReminders(navOptions: NavOptions? = null) {
    this.navigate(RemindersListRoute, navOptions)
}

fun NavController.navigateToReminderEdit(reminderId: Long = 0L, navOptions: NavOptions? = null) {
    this.navigate(createReminderEditRoute(reminderId), navOptions)
}

fun NavController.navigateToReminderTriggerEdit(reminderId: Long = 0L, navOptions: NavOptions? = null) {
    this.navigate(createReminderTriggerEditRoute(reminderId), navOptions)
}

fun NavController.navigateToReminderRinging(reminderId: Long, navOptions: NavOptions? = null) {
    this.navigate(createReminderRingingRoute(reminderId), navOptions ?: androidx.navigation.navOptions {
        launchSingleTop = true
    })
}

fun NavGraphBuilder.remindersGraph(
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToRingtoneSelection: (String?) -> Unit = {},
    onNavigateToSnoozeSettings: (Boolean, Int, Int) -> Unit = { _, _, _ -> },
    onNavigateToPermissions: () -> Unit,
    onNavigateBack: () -> Unit,
    navController: NavController
) {
    composable(
        route = RemindersListRoute,
        deepLinks = listOf(
            navDeepLink { uriPattern = "app://remind/reminders" }
        )
    ) {
        ReminderListRoute(
            onAddReminderClick = { onNavigateToEdit(0L) },
            onReminderClick = { reminder ->
                if (reminder.snoozeNextTriggerTime != null) {
                    navController.navigateToReminderRinging(reminder.id)
                } else {
                    onNavigateToEdit(reminder.id)
                }
            },
            onEditReminderClick = { reminderId ->
                onNavigateToEdit(reminderId)
            },
            onNavigateToPermissions = onNavigateToPermissions
        )
    }

    composable(
        route = ReminderEditRoute,
        arguments = listOf(
            navArgument("reminderId") {
                type = NavType.LongType
                defaultValue = 0L
            }
        ),
        deepLinks = listOf(
            navDeepLink { uriPattern = "app://remind/reminder/{reminderId}" }
        )
    ) { backStackEntry ->
        val reminderId = backStackEntry.arguments?.getLong("reminderId") ?: 0L
        ReminderEditRoute(
            reminderId = reminderId,
            onBackClick = onNavigateBack,
            onNavigateToRingtoneSelection = onNavigateToRingtoneSelection,
            onNavigateToSnoozeSettings = onNavigateToSnoozeSettings,
            onNavigateToPermissions = onNavigateToPermissions,
            onNavigateToTriggerEdit = { id ->
                navController.navigateToReminderTriggerEdit(id)
            },
            navController = navController
        )
    }

    composable(
        route = ReminderTriggerEditRoute,
        arguments = listOf(
            navArgument("reminderId") {
                type = NavType.LongType
                defaultValue = 0L
            }
        )
    ) { backStackEntry ->
        val reminderId = backStackEntry.arguments?.getLong("reminderId") ?: 0L
        val parentEntry = remember(backStackEntry) {
            try {
                navController.getBackStackEntry(createReminderEditRoute(reminderId))
            } catch (e: Exception) {
                navController.previousBackStackEntry ?: backStackEntry
            }
        }
        val viewModel: ReminderEditViewModel = hiltViewModel(parentEntry)
        ReminderTriggerEditRoute(
            viewModel = viewModel,
            onBackClick = { navController.popBackStack() },
            navController = navController
        )
    }

    composable(
        route = ReminderRingingRoute,
        arguments = listOf(
            navArgument("reminderId") {
                type = NavType.LongType
                defaultValue = -1L
            }
        ),
        deepLinks = listOf(
            navDeepLink { uriPattern = "app://remind/ringing/{reminderId}" }
        )
    ) { backStackEntry ->
        val reminderId = backStackEntry.arguments?.getLong("reminderId") ?: -1L
        ReminderRingingRoute(
            reminderId = reminderId,
            onFinish = {
                if (navController.currentBackStackEntry?.destination?.route == ReminderRingingRoute) {
                    navController.popBackStack()
                }
            },
            navController = navController
        )
    }
}
