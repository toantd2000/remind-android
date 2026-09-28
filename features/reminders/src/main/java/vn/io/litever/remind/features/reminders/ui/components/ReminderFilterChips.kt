package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.features.reminders.R

enum class ReminderFilterType {
    ALL,
    ACTIVE,
    COMPLETED,
    TIME,
    LOCATION,
    WIFI
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderFilterChips(
    selectedFilter: ReminderFilterType,
    onFilterSelected: (ReminderFilterType) -> Unit,
    modifier: Modifier = Modifier,
    counts: Map<ReminderFilterType, Int> = emptyMap()
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ReminderFilterType.entries.forEach { filterType ->
            val isSelected = filterType == selectedFilter
            val count = counts[filterType]
            val label = when (filterType) {
                ReminderFilterType.ALL -> stringResource(R.string.filter_all)
                ReminderFilterType.ACTIVE -> stringResource(R.string.filter_active)
                ReminderFilterType.COMPLETED -> stringResource(R.string.filter_completed)
                ReminderFilterType.TIME -> stringResource(R.string.filter_time)
                ReminderFilterType.LOCATION -> stringResource(R.string.filter_location)
                ReminderFilterType.WIFI -> stringResource(R.string.filter_wifi)
            }
            val displayLabel = if (count != null && count > 0) "$label ($count)" else label
            val icon: ImageVector = when (filterType) {
                ReminderFilterType.ALL -> ReMindIcons.FilterList
                ReminderFilterType.ACTIVE -> ReMindIcons.NotificationsActive
                ReminderFilterType.COMPLETED -> ReMindIcons.CheckCircle
                ReminderFilterType.TIME -> ReMindIcons.AccessTime
                ReminderFilterType.LOCATION -> ReMindIcons.LocationOn
                ReminderFilterType.WIFI -> ReMindIcons.Wifi
            }

            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filterType) },
                label = { Text(displayLabel) },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = LiteverTheme.colors.primaryContainer,
                    selectedLabelColor = LiteverTheme.colors.onPrimaryContainer,
                    selectedLeadingIconColor = LiteverTheme.colors.onPrimaryContainer
                )
            )
        }
    }
}
