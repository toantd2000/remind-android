package vn.io.litever.remind.app

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.features.alarms.ui.AlarmListRoute
import vn.io.litever.remind.features.reminders.ui.RemindersListRoute
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
    TODAY(
        route = todayRoute,
        icon = ReMindIcons.Lightbulb,
        labelResId = R.string.navigation_today
    ),
    ALARMS(
        route = AlarmListRoute,
        icon = ReMindIcons.Alarm,
        labelResId = R.string.navigation_alarms
    ),
    REMINDERS(
        route = RemindersListRoute,
        icon = ReMindIcons.Notifications,
        labelResId = R.string.navigation_reminders
    ),
    SETTINGS(
        route = settingsRoute,
        icon = ReMindIcons.Settings,
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
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                    )
                },
                selected = isSelected,
                onClick = { onNavigateToDestination(destination.route) }
            )
        }
    }
}
