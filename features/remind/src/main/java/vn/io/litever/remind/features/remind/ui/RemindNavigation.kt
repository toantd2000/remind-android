package vn.io.litever.remind.features.remind.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val remindRoute = "remind_route"
const val locationSearchRoute = "location_search_route"

fun NavGraphBuilder.remindGraph(
    onNavigateToLocationSearch: () -> Unit,
    onBackClick: () -> Unit
) {
    composable(remindRoute) {
        RemindRoute(
            onLocationClick = onNavigateToLocationSearch
        )
    }
    composable(locationSearchRoute) {
        LocationSearchRoute(
            onBackClick = onBackClick
        )
    }
}
