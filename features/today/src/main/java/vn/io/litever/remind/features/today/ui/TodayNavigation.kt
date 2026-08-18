package vn.io.litever.remind.features.today.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val todayRoute = "today_route"
const val locationSearchRoute = "location_search_route"

fun NavGraphBuilder.todayGraph(
    onNavigateToLocationSearch: () -> Unit,
    onBackClick: () -> Unit
) {
    composable(todayRoute) {
        TodayRoute(
            onLocationClick = onNavigateToLocationSearch
        )
    }
    composable(locationSearchRoute) {
        LocationSearchRoute(
            onBackClick = onBackClick
        )
    }
}
