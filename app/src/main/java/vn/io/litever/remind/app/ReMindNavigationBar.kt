package vn.io.litever.remind.app

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import vn.io.litever.remind.features.alarms.ui.AlarmListRoute
import vn.io.litever.remind.features.settings.ui.settingsRoute
import vn.io.litever.remind.features.today.ui.todayRoute

/**
 * Navigation destination items for bottom bar.
 */
enum class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    val labelResId: Int
) {
    ALARMS(
        route = AlarmListRoute,
        icon = Icons.Rounded.Alarm,
        labelResId = R.string.navigation_alarms
    ),
    TODAY(
        route = todayRoute,
        icon = Icons.Rounded.Lightbulb,
        labelResId = R.string.navigation_today
    ),
    SETTINGS(
        route = settingsRoute,
        icon = Icons.Rounded.Settings,
        labelResId = R.string.navigation_settings
    )
}

/**
 * Bottom Navigation Bar for ReMind with bold text when item is selected.
 */
@Composable
fun ReMindNavigationBar(
    currentRoute: String?,
    onNavigateToDestination: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
    ) {
        TopLevelDestination.entries.forEach { destination ->
            val isSelected = currentRoute == destination.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = stringResource(destination.labelResId)
                    )
                },
                label = {
                    Text(
                        text = stringResource(destination.labelResId),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = isSelected,
                onClick = { onNavigateToDestination(destination.route) }
            )
        }
    }
}
