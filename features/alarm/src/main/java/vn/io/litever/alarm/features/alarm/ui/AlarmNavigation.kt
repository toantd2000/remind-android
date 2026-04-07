package vn.io.litever.alarm.features.alarm.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val alarmListRoute = "alarm_list_route"
const val alarmEditRoute = "alarm_edit_route"

fun NavGraphBuilder.alarmGraph(
    onNavigateToEdit: () -> Unit,
    onNavigateBack: () -> Unit
) {
    composable(route = alarmListRoute) {
        AlarmListRoute(
            onAddAlarmClick = onNavigateToEdit
        )
    }
    composable(route = alarmEditRoute) {
        AlarmEditRoute(
            onNavigateBack = onNavigateBack
        )
    }
}
