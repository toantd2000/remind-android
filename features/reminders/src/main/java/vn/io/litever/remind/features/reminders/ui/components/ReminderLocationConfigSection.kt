package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverOutlinedCard
import vn.io.litever.designsystem.components.LiteverSegmentedButton
import vn.io.litever.designsystem.components.LiteverSingleChoiceSegmentedButtonRow
import vn.io.litever.designsystem.components.LiteverTextField
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.core.model.GeofenceTransitionType
import vn.io.litever.remind.core.model.ReminderMonthDay
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.features.reminders.R
import vn.io.litever.remind.features.reminders.viewmodel.ReminderRepeatInterval
import java.time.LocalDate

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
    repeatInterval: ReminderRepeatInterval = ReminderRepeatInterval.ONCE,
    onRepeatIntervalChange: (ReminderRepeatInterval) -> Unit = {},
    date: LocalDate? = null,
    onDateChange: (LocalDate?) -> Unit = {},
    repeatDays: List<DayOfWeek> = emptyList(),
    onRepeatDaysChange: (List<DayOfWeek>) -> Unit = {},
    onRepeatDayToggle: (DayOfWeek) -> Unit = {},
    monthlyDays: List<Int> = emptyList(),
    onMonthlyDayToggle: (Int) -> Unit = {},
    yearlyDates: List<ReminderMonthDay> = emptyList(),
    onAddYearlyDate: (Int, Int) -> Unit = { _, _ -> },
    onRemoveYearlyDate: (Int) -> Unit = {},
    timeWindows: List<ReminderTimeWindow>,
    onTimeWindowsChange: (List<ReminderTimeWindow>) -> Unit,
    onAddTimeWindow: (ReminderTimeWindow) -> Unit,
    onRemoveTimeWindow: (Int) -> Unit,
    onSetAllDayPreset: () -> Unit,
    is24HourFormat: Boolean,
    modifier: Modifier = Modifier,
    showLocationError: Boolean = false,
    locationFocusRequester: FocusRequester? = null
) {
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

            val locationFieldModifier = if (locationFocusRequester != null) {
                Modifier.fillMaxWidth().focusRequester(locationFocusRequester)
            } else {
                Modifier.fillMaxWidth()
            }

            // Location Name
            LiteverTextField(
                value = locationName,
                onValueChange = onLocationNameChange,
                modifier = locationFieldModifier,
                placeholder = stringResource(R.string.location_name_placeholder),
                label = stringResource(R.string.location_name_label),
                singleLine = true
            )

            if (showLocationError) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.reminder_location_required),
                    style = LiteverTheme.typography.bodySmall,
                    color = LiteverTheme.colors.error,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

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

            // Unified Date & Repeat Configuration
            ReminderRepeatConfigSection(
                repeatInterval = repeatInterval,
                onRepeatIntervalChange = onRepeatIntervalChange,
                date = date,
                onDateChange = onDateChange,
                repeatDays = repeatDays,
                onRepeatDaysChange = onRepeatDaysChange,
                onRepeatDayToggle = onRepeatDayToggle,
                monthlyDays = monthlyDays,
                onMonthlyDayToggle = onMonthlyDayToggle,
                yearlyDates = yearlyDates,
                onAddYearlyDate = onAddYearlyDate,
                onRemoveYearlyDate = onRemoveYearlyDate
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Defined Multi Time Window Trigger with 24/7 vs Time Window Toggle
            ReminderTimeWindowConfigSection(
                timeWindows = timeWindows,
                onTimeWindowsChange = onTimeWindowsChange,
                onAddTimeWindow = onAddTimeWindow,
                onRemoveTimeWindow = onRemoveTimeWindow,
                onSetAllDayPreset = onSetAllDayPreset,
                is24HourFormat = is24HourFormat
            )
        }
    }
}
