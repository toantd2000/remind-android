package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverOutlinedCard
import vn.io.litever.designsystem.components.LiteverSegmentedButton
import vn.io.litever.designsystem.components.LiteverSingleChoiceSegmentedButtonRow
import vn.io.litever.designsystem.components.LiteverTextField
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.common.util.TimeFormatUtils
import vn.io.litever.remind.core.model.GeofenceTransitionType
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.features.reminders.R

@Composable
fun ReminderLocationConfigSection(
    locationName: String,
    onLocationNameChange: (String) -> Unit,
    latitude: Double,
    onLatitudeChange: (Double) -> Unit,
    longitude: Double,
    onLongitudeChange: (Double) -> Unit,
    radiusMeters: Float,
    onRadiusChange: (Float) -> Unit,
    transitionType: GeofenceTransitionType,
    onTransitionTypeChange: (GeofenceTransitionType) -> Unit,
    timeWindow: ReminderTimeWindow?,
    onTimeWindowChange: (ReminderTimeWindow?) -> Unit,
    is24HourFormat: Boolean,
    modifier: Modifier = Modifier
) {
    var showTimeWindowDialog by remember { mutableStateOf(false) }

    LiteverOutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.section_location_trigger),
                style = LiteverTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LiteverTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Location Name
            LiteverTextField(
                value = locationName,
                onValueChange = onLocationNameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = stringResource(R.string.location_name_placeholder),
                label = stringResource(R.string.location_name_label),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Coordinates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LiteverTextField(
                    value = if (latitude != 0.0) latitude.toString() else "",
                    onValueChange = { str ->
                        str.toDoubleOrNull()?.let { onLatitudeChange(it) } ?: if (str.isEmpty()) onLatitudeChange(0.0) else Unit
                    },
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.latitude_label),
                    placeholder = "21.0285",
                    singleLine = true
                )
                LiteverTextField(
                    value = if (longitude != 0.0) longitude.toString() else "",
                    onValueChange = { str ->
                        str.toDoubleOrNull()?.let { onLongitudeChange(it) } ?: if (str.isEmpty()) onLongitudeChange(0.0) else Unit
                    },
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.longitude_label),
                    placeholder = "105.8542",
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Radius Slider
            Text(
                text = stringResource(R.string.radius_label, radiusMeters.toInt()),
                style = LiteverTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = LiteverTheme.colors.onSurface
            )
            Slider(
                value = radiusMeters,
                onValueChange = onRadiusChange,
                valueRange = 50f..1000f,
                steps = 18,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Geofence Transition Type
            Text(
                text = stringResource(R.string.geofence_transition_label),
                style = LiteverTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = LiteverTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            LiteverSingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                GeofenceTransitionType.entries.forEachIndexed { index, type ->
                    val isSelected = type == transitionType
                    val labelRes = when (type) {
                        GeofenceTransitionType.ENTER -> R.string.transition_enter
                        GeofenceTransitionType.EXIT -> R.string.transition_exit
                        GeofenceTransitionType.BOTH -> R.string.transition_both
                    }

                    LiteverSegmentedButton(
                        onClick = { onTransitionTypeChange(type) },
                        selected = isSelected,
                        label = {
                            Text(
                                text = stringResource(labelRes),
                                style = LiteverTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Defined Time Window Trigger
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(LiteverTheme.shapes.medium)
                    .clickable { showTimeWindowDialog = true },
                color = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.4f),
                shape = LiteverTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        vn.io.litever.remind.core.designsystem.components.ReMindSettingIcon(
                            imageVector = Icons.Rounded.DateRange,
                            selected = timeWindow != null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.time_window_section_title),
                                style = LiteverTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = LiteverTheme.colors.onSurface
                            )
                            val summary = if (timeWindow != null) {
                                val s = TimeFormatUtils.formatTime(timeWindow.startTime, is24HourFormat)
                                val e = TimeFormatUtils.formatTime(timeWindow.endTime, is24HourFormat)
                                "$s - $e"
                            } else {
                                stringResource(R.string.time_window_desc)
                            }
                            Text(
                                text = summary,
                                style = LiteverTheme.typography.bodySmall,
                                color = LiteverTheme.colors.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    if (showTimeWindowDialog) {
        ReminderTimeWindowDialog(
            initialTimeWindow = timeWindow,
            is24HourFormat = is24HourFormat,
            onDismissRequest = { showTimeWindowDialog = false },
            onConfirm = { newWindow ->
                onTimeWindowChange(newWindow)
                showTimeWindowDialog = false
            }
        )
    }
}
