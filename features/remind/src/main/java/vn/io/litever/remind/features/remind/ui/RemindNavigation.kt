package vn.io.litever.remind.features.remind.ui

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val remindRoute = "remind_route"

fun NavGraphBuilder.remindGraph() {
    composable(remindRoute) {
        RemindRoute()
    }
}
