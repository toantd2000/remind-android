package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.features.reminders.R

@Composable
fun ReminderEmptyState(
    selectedFilter: ReminderFilterType,
    modifier: Modifier = Modifier
) {
    val (icon: ImageVector, titleRes: Int, descRes: Int) = when (selectedFilter) {
        ReminderFilterType.ALL -> Triple(
            ReMindIcons.NotificationsNone,
            R.string.no_reminders,
            R.string.empty_reminders_description
        )
        ReminderFilterType.ACTIVE -> Triple(
            ReMindIcons.CheckCircleOutline,
            R.string.no_active_reminders,
            R.string.empty_active_description
        )
        ReminderFilterType.COMPLETED -> Triple(
            ReMindIcons.DoneAll,
            R.string.no_completed_reminders,
            R.string.empty_completed_description
        )
        ReminderFilterType.TIME -> Triple(
            ReMindIcons.AccessTime,
            R.string.no_time_reminders,
            R.string.empty_reminders_description
        )
        ReminderFilterType.LOCATION -> Triple(
            ReMindIcons.LocationOff,
            R.string.no_location_reminders,
            R.string.empty_reminders_description
        )
        ReminderFilterType.WIFI -> Triple(
            ReMindIcons.WifiOff,
            R.string.no_wifi_reminders,
            R.string.empty_reminders_description
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(titleRes),
            style = LiteverTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = LiteverTheme.colors.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(descRes),
            style = LiteverTheme.typography.bodyMedium,
            color = LiteverTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
