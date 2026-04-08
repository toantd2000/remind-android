package vn.io.litever.alarm.features.alarm.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

const val alarmListRoute = "alarm_list_route"
const val alarmEditRoute = "alarm_edit_route"

fun NavGraphBuilder.alarmGraph(
    onNavigateToEdit: () -> Unit,
    onNavigateBack: () -> Unit
) {
    composable(route = alarmListRoute) {
        AlarmListRoute(
            onAddAlarmClick = onNavigateToEdit,
            onAlarmClick = { alarm ->

            },
        )
    }
    composable(route = alarmEditRoute) {
        AlarmEditRoute(
            onNavigateBack = onNavigateBack
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
