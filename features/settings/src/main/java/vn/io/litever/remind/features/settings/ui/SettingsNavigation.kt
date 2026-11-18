package vn.io.litever.remind.features.settings.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

const val settingsRoute = "settings_route"
const val generalSettingsRoute = "general_settings_route"
const val permissionsRoute = "permissions_route"
const val licensesRoute = "licenses_route"
const val attributionsRoute = "attributions_route"
const val paywallRoutePattern = "paywall_route?initialSlide={initialSlide}"

fun NavController.navigateToSettings(navOptions: NavOptions? = null) {
    this.navigate(settingsRoute, navOptions)
}

fun NavController.navigateToPaywall(initialSlide: Int = 0) {
    this.navigate("paywall_route?initialSlide=$initialSlide")
}

fun NavController.navigateToGeneralSettings() {
    this.navigate(generalSettingsRoute)
}

fun NavController.navigateToPermissions() {
    this.navigate(permissionsRoute)
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
    onNavigateToPaywall: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    composable(settingsRoute) {
        SettingsRoute(
            onNavigateToGeneralSettings = onNavigateToGeneralSettings,
            onNavigateToPermissions = onNavigateToPermissions,
            onNavigateToAttributions = onNavigateToAttributions,
            onNavigateToPaywall = onNavigateToPaywall
        )
    }

    composable(
        route = paywallRoutePattern,
        arguments = listOf(
            navArgument("initialSlide") {
                type = NavType.IntType
                defaultValue = 0
            }
        )
    ) { backStackEntry ->
        val initialSlide = backStackEntry.arguments?.getInt("initialSlide") ?: 0
        PaywallRoute(
            initialSlide = initialSlide,
            onNavigateBack = onNavigateBack
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
