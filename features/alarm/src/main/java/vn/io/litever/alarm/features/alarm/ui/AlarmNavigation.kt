package vn.io.litever.alarm.features.alarm.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

const val alarmListRoute = "alarm_list_route"
const val alarmEditRoute = "alarm_edit_route/{alarmId}"
const val alarmRingingRoute = "alarm_ringing_route/{alarmId}"

fun NavGraphBuilder.alarmGraph(
    onNavigateToEdit: (Long) -> Unit,
    onNavigateBack: () -> Unit
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
        AlarmEditRoute(
            alarmId = alarmId,
            onBackClick = onNavigateBack
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
