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
const val licensesRoute = "licenses_route"
const val attributionsRoute = "attributions_route"


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

fun NavController.navigateToLicenses() {
    this.navigate(licensesRoute)
}

fun NavController.navigateToAttributions() {
    this.navigate(attributionsRoute)
}


fun NavGraphBuilder.settingsGraph(
    onNavigateToGeneralSettings: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToAttributions: () -> Unit,
    onNavigateToLicenses: () -> Unit,
    onNavigateBack: () -> Unit
) {

    composable(settingsRoute) {
        SettingsRoute(
            onNavigateToGeneralSettings = onNavigateToGeneralSettings,
            onNavigateToPermissions = onNavigateToPermissions,
            onNavigateToAttributions = onNavigateToAttributions
        )
    }


    composable(generalSettingsRoute) {
        GeneralSettingsRoute(
            onNavigateBack = onNavigateBack
        )
    }

    composable(permissionsRoute) {
        PermissionSettingsRoute(
            onNavigateBack = onNavigateBack
        )
    }

    composable(licensesRoute) {
        LicensesScreen(
            onNavigateBack = onNavigateBack
        )
    }

    composable(attributionsRoute) {
        AttributionsScreen(
            onNavigateToLicenses = onNavigateToLicenses,
            onNavigateBack = onNavigateBack
        )
    }
}











