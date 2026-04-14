package vn.io.litever.remind.features.settings.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

const val settingsRoute = "settings_route"
const val generalSettingsRoute = "general_settings_route"
const val qaRoute = "qa_route"
const val permissionsRoute = "permissions_route"
const val alarmSettingsRoute = "alarm_settings_route"

fun NavController.navigateToSettings(navOptions: NavOptions? = null) {
    this.navigate(settingsRoute, navOptions)
}

fun NavController.navigateToGeneralSettings() {
    this.navigate(generalSettingsRoute)
}

fun NavController.navigateToQA() {
    this.navigate(qaRoute)
}

fun NavController.navigateToPermissions() {
    this.navigate(permissionsRoute)
}

fun NavController.navigateToAlarmSettings() {
    this.navigate(alarmSettingsRoute)
}

fun NavGraphBuilder.settingsGraph(
    onNavigateToGeneralSettings: () -> Unit,
    onNavigateToQA: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToAlarmSettings: () -> Unit,
    onNavigateBack: () -> Unit
) {
    composable(settingsRoute) {
        SettingsRoute(
            onNavigateToGeneralSettings = onNavigateToGeneralSettings,
            onNavigateToQA = onNavigateToQA,
            onNavigateToPermissions = onNavigateToPermissions,
            onNavigateToAlarmSettings = onNavigateToAlarmSettings
        )
    }

    composable(generalSettingsRoute) {
        GeneralSettingsRoute(
            onNavigateBack = onNavigateBack
        )
    }
    
    // Placeholder for Phase 2 & 3
    composable(qaRoute) {
        // Placeholder QAScreen
    }
    
    composable(permissionsRoute) {
        PermissionSettingsRoute(
            onNavigateBack = onNavigateBack
        )
    }
    
    composable(alarmSettingsRoute) {
        // Placeholder AlarmSettingsScreen
    }
}
