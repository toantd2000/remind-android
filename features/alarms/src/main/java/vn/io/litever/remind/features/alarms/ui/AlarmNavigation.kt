package vn.io.litever.remind.features.alarms.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

const val AlarmListRoute = "alarm_list_route"
const val AlarmEditRoute = "alarm_edit_route/{alarmId}"
const val AlarmRingingRoute = "alarm_ringing_route/{alarmId}"
const val AlarmPreviewRoute = "alarm_preview_route/{alarmId}"
const val ringtoneSelectionRoute = "ringtone_selection_route"
const val snoozeSettingsRoute = "snooze_settings_route"
const val AlarmMessageRoute = "alarm_message_route/{alarmId}"

fun NavGraphBuilder.alarmGraph(
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToRingtoneSelection: (String?) -> Unit,
    onNavigateToSnoozeSettings: (Boolean, Int, Int) -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToMissionRinging: (Long) -> Unit,
    onNavigateToMessage: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    onAddMissionClick: () -> Unit,
    onMissionClick: (vn.io.litever.remind.core.model.Mission) -> Unit,
    onNavigateToPreview: (Long) -> Unit,
    onNavigateToMissionPreview: (Long) -> Unit,
    navController: androidx.navigation.NavController
) {
    composable(route = AlarmListRoute) {
        AlarmListRoute(
            onAddAlarmClick = { onNavigateToEdit(0L) },
            onAlarmClick = { alarm -> onNavigateToEdit(alarm.id) },
            onNavigateToPreview = onNavigateToPreview,
            onNavigateToPermissions = onNavigateToPermissions
        )
    }
    composable(
        route = AlarmEditRoute,
        arguments = listOf(navArgument("alarmId") { type = NavType.LongType })
    ) { backStackEntry ->
        val alarmId = backStackEntry.arguments?.getLong("alarmId")
        
        AlarmEditRoute(
            alarmId = alarmId ?: 0L,
            onBackClick = onNavigateBack,
            onRingtoneSelectionClick = onNavigateToRingtoneSelection,
            onSnoozeSettingsClick = onNavigateToSnoozeSettings,
            onNavigateToPermissions = onNavigateToPermissions,
            onAddMissionClick = onAddMissionClick,
            onMissionClick = onMissionClick,
            onPreviewClick = onNavigateToPreview,
            navController = navController
        )
    }
    composable(route = snoozeSettingsRoute) {
        val prevSnoozeEnabled = navController.previousBackStackEntry?.savedStateHandle?.get<Boolean>("snoozeEnabled") ?: true
        val prevSnoozeInterval = navController.previousBackStackEntry?.savedStateHandle?.get<Int>("snoozeInterval") ?: 5
        val prevSnoozeRepeatCount = navController.previousBackStackEntry?.savedStateHandle?.get<Int>("snoozeRepeatCount") ?: 3
        
        SnoozeSettingsRoute(
            initialEnabled = prevSnoozeEnabled,
            initialInterval = prevSnoozeInterval,
            initialRepeatCount = prevSnoozeRepeatCount,
            onBackClick = onNavigateBack,
            onSave = { enabled, interval, repeatCount ->
                navController.previousBackStackEntry?.savedStateHandle?.set("snoozeEnabled", enabled)
                navController.previousBackStackEntry?.savedStateHandle?.set("snoozeInterval", interval)
                navController.previousBackStackEntry?.savedStateHandle?.set("snoozeRepeatCount", repeatCount)
                onNavigateBack()
            }
        )
    }
    composable(route = ringtoneSelectionRoute) { backStackEntry ->
        val initialUri = navController.previousBackStackEntry?.savedStateHandle?.get<String>("initialUri")
        RingtoneSelectionRoute(
            initialUri = initialUri,
            onBackClick = onNavigateBack,
            onRingtoneSelected = { uri ->
                navController.previousBackStackEntry?.savedStateHandle?.set("selectedRingtoneUri", uri)
                navController.previousBackStackEntry?.savedStateHandle?.set("selectedRingtoneUri_set", true)
                onNavigateBack()
            }
        )
    }
    composable(
        route = AlarmRingingRoute,
        arguments = listOf(navArgument("alarmId") { type = NavType.LongType }),
        deepLinks = listOf(navDeepLink { uriPattern = "app://remind/ring/{alarmId}" })
    ) { backStackEntry ->
        val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: -1L
        AlarmRingingRoute(
            alarmId = alarmId,
            onFinish = onNavigateBack,
            onStartMission = onNavigateToMissionRinging,
            onDismissSuccess = onNavigateToMessage,
            navController = navController
        )
    }
    composable(
        route = AlarmMessageRoute,
        arguments = listOf(navArgument("alarmId") { type = NavType.LongType }),
        deepLinks = listOf(navDeepLink { uriPattern = "app://remind/message/{alarmId}" })
    ) { backStackEntry ->
        val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: -1L
        AlarmMessageRoute(
            alarmId = alarmId,
            onFinish = onNavigateBack
        )
    }
    composable(
        route = AlarmPreviewRoute,
        arguments = listOf(navArgument("alarmId") { type = NavType.LongType })
    ) { backStackEntry ->
        val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: -1L
        AlarmPreviewRoute(
            alarmId = alarmId,
            onExit = onNavigateBack,
            onStartMissionPreview = onNavigateToMissionPreview
        )
    }
}










