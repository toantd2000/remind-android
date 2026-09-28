package vn.io.litever.remind.features.reminders.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import vn.io.litever.designsystem.components.LiteverButton
import vn.io.litever.designsystem.components.LiteverScaffold
import vn.io.litever.designsystem.components.LiteverTopAppBar
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.ReMindBottomBar
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.core.model.GeofenceTransitionType
import vn.io.litever.remind.core.model.ReminderMonthDay
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.WifiTriggerType
import vn.io.litever.remind.features.reminders.R
import vn.io.litever.remind.features.reminders.ui.components.ReminderLocationConfigSection
import vn.io.litever.remind.features.reminders.ui.components.ReminderTimeConfigSection
import vn.io.litever.remind.features.reminders.ui.components.ReminderTriggerTypeSelector
import vn.io.litever.remind.features.reminders.ui.components.ReminderWifiConfigSection
import vn.io.litever.remind.features.reminders.viewmodel.ReminderEditUiState
import vn.io.litever.remind.features.reminders.viewmodel.ReminderEditViewModel
import vn.io.litever.remind.features.reminders.viewmodel.ReminderRepeatInterval
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun ReminderTriggerEditRoute(
    viewModel: ReminderEditViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler {
        onBackClick()
    }

    ReminderTriggerEditScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onTriggerTypeSelected = viewModel::setTriggerType,
        onTimeChange = viewModel::setTime,
        onDateChange = viewModel::setDate,
        onRepeatDaysChange = viewModel::setRepeatDays,
        onRepeatDayToggle = viewModel::toggleRepeatDay,
        onRepeatIntervalChange = viewModel::setRepeatInterval,
        onMonthlyDayToggle = viewModel::toggleMonthlyDay,
        onAddYearlyDate = viewModel::addYearlyDate,
        onRemoveYearlyDate = viewModel::removeYearlyDate,
        onLocationNameChange = viewModel::setLocationName,
        onLatitudeChange = viewModel::setLatitude,
        onLongitudeChange = viewModel::setLongitude,
        onRadiusChange = viewModel::setRadiusMeters,
        onTransitionTypeChange = viewModel::setGeofenceTransitionType,
        onLocationTimeWindowsChange = viewModel::setLocationTimeWindows,
        onAddLocationTimeWindow = viewModel::addLocationTimeWindow,
        onRemoveLocationTimeWindow = viewModel::removeLocationTimeWindow,
        onSetAllDayPresetLocation = viewModel::setAllDayPresetLocation,
        onWifiSsidChange = viewModel::setWifiSsid,
        onWifiBssidChange = viewModel::setWifiBssid,
        onWifiTriggerTypeChange = viewModel::setWifiTriggerType,
        onWifiTimeWindowsChange = viewModel::setWifiTimeWindows,
        onAddWifiTimeWindow = viewModel::addWifiTimeWindow,
        onRemoveWifiTimeWindow = viewModel::removeWifiTimeWindow,
        onSetAllDayPresetWifi = viewModel::setAllDayPresetWifi,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTriggerEditScreen(
    uiState: ReminderEditUiState,
    onBackClick: () -> Unit,
    onTriggerTypeSelected: (ReminderTriggerType) -> Unit,
    onTimeChange: (LocalTime) -> Unit,
    onDateChange: (LocalDate?) -> Unit,
    onRepeatDaysChange: (List<DayOfWeek>) -> Unit,
    onRepeatDayToggle: (DayOfWeek) -> Unit,
    onRepeatIntervalChange: (ReminderRepeatInterval) -> Unit,
    onMonthlyDayToggle: (Int) -> Unit,
    onAddYearlyDate: (Int, Int) -> Unit,
    onRemoveYearlyDate: (Int) -> Unit,
    onLocationNameChange: (String) -> Unit,
    onLatitudeChange: (Double) -> Unit,
    onLongitudeChange: (Double) -> Unit,
    onRadiusChange: (Float) -> Unit,
    onTransitionTypeChange: (GeofenceTransitionType) -> Unit,
    onLocationTimeWindowsChange: (List<ReminderTimeWindow>) -> Unit,
    onAddLocationTimeWindow: (ReminderTimeWindow) -> Unit,
    onRemoveLocationTimeWindow: (Int) -> Unit,
    onSetAllDayPresetLocation: () -> Unit,
    onWifiSsidChange: (String) -> Unit,
    onWifiBssidChange: (String?) -> Unit,
    onWifiTriggerTypeChange: (WifiTriggerType) -> Unit,
    onWifiTimeWindowsChange: (List<ReminderTimeWindow>) -> Unit,
    onAddWifiTimeWindow: (ReminderTimeWindow) -> Unit,
    onRemoveWifiTimeWindow: (Int) -> Unit,
    onSetAllDayPresetWifi: () -> Unit,
    modifier: Modifier = Modifier
) {
    val locationFocusRequester = remember { FocusRequester() }
    val wifiFocusRequester = remember { FocusRequester() }
    val lazyListState = rememberLazyListState()

    LiteverScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                LiteverTopAppBar(
                    title = stringResource(R.string.edit_trigger_title),
                    onBackClick = onBackClick,
                    navigationIcon = {
                        Icon(
                            imageVector = ReMindIcons.Close,
                            contentDescription = "Back"
                        )
                    }
                )
                ReminderTriggerTypeSelector(
                    selectedTriggerType = uiState.triggerType,
                    onTriggerTypeSelected = onTriggerTypeSelected
                )
            }
        },
        bottomBar = {
            ReMindBottomBar {
                LiteverButton(
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.action_done))
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(LiteverTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
        ) {

            // 2. Active Trigger Configuration
            item {
                when (uiState.triggerType) {
                    ReminderTriggerType.TIME -> {
                        ReminderTimeConfigSection(
                            time = uiState.time,
                            onTimeChange = onTimeChange,
                            date = uiState.date,
                            onDateChange = onDateChange,
                            repeatDays = uiState.repeatDays,
                            onRepeatDaysChange = onRepeatDaysChange,
                            onRepeatDayToggle = onRepeatDayToggle,
                            repeatInterval = uiState.repeatInterval,
                            onRepeatIntervalChange = onRepeatIntervalChange,
                            monthlyDays = uiState.monthlyDays,
                            onMonthlyDayToggle = onMonthlyDayToggle,
                            yearlyDates = uiState.yearlyDates,
                            onAddYearlyDate = onAddYearlyDate,
                            onRemoveYearlyDate = onRemoveYearlyDate,
                            is24HourFormat = uiState.is24HourFormat
                        )
                    }
                    ReminderTriggerType.LOCATION -> {
                        val locWindows = uiState.locationTimeWindows.ifEmpty {
                            if (uiState.locationTimeWindow != null) listOf(uiState.locationTimeWindow) else emptyList()
                        }
                        ReminderLocationConfigSection(
                            locationName = uiState.locationName,
                            onLocationNameChange = onLocationNameChange,
                            latitude = uiState.latitude,
                            onLatitudeChange = onLatitudeChange,
                            longitude = uiState.longitude,
                            onLongitudeChange = onLongitudeChange,
                            radiusMeters = uiState.radiusMeters,
                            onRadiusChange = onRadiusChange,
                            transitionType = uiState.geofenceTransitionType,
                            onTransitionTypeChange = onTransitionTypeChange,
                            repeatInterval = uiState.repeatInterval,
                            onRepeatIntervalChange = onRepeatIntervalChange,
                            date = uiState.date,
                            onDateChange = onDateChange,
                            repeatDays = uiState.repeatDays,
                            onRepeatDaysChange = onRepeatDaysChange,
                            onRepeatDayToggle = onRepeatDayToggle,
                            monthlyDays = uiState.monthlyDays,
                            onMonthlyDayToggle = onMonthlyDayToggle,
                            yearlyDates = uiState.yearlyDates,
                            onAddYearlyDate = onAddYearlyDate,
                            onRemoveYearlyDate = onRemoveYearlyDate,
                            timeWindows = locWindows,
                            onTimeWindowsChange = onLocationTimeWindowsChange,
                            onAddTimeWindow = onAddLocationTimeWindow,
                            onRemoveTimeWindow = onRemoveLocationTimeWindow,
                            onSetAllDayPreset = onSetAllDayPresetLocation,
                            is24HourFormat = uiState.is24HourFormat,
                            showLocationError = uiState.showLocationError,
                            locationFocusRequester = locationFocusRequester
                        )
                    }
                    ReminderTriggerType.WIFI -> {
                        val wifiWindows = uiState.wifiTimeWindows.ifEmpty {
                            if (uiState.wifiTimeWindow != null) listOf(uiState.wifiTimeWindow) else emptyList()
                        }
                        ReminderWifiConfigSection(
                            ssid = uiState.wifiSsid,
                            onSsidChange = onWifiSsidChange,
                            bssid = uiState.wifiBssid,
                            onBssidChange = onWifiBssidChange,
                            triggerType = uiState.wifiTriggerType,
                            onTriggerTypeChange = onWifiTriggerTypeChange,
                            repeatInterval = uiState.repeatInterval,
                            onRepeatIntervalChange = onRepeatIntervalChange,
                            date = uiState.date,
                            onDateChange = onDateChange,
                            repeatDays = uiState.repeatDays,
                            onRepeatDaysChange = onRepeatDaysChange,
                            onRepeatDayToggle = onRepeatDayToggle,
                            monthlyDays = uiState.monthlyDays,
                            onMonthlyDayToggle = onMonthlyDayToggle,
                            yearlyDates = uiState.yearlyDates,
                            onAddYearlyDate = onAddYearlyDate,
                            onRemoveYearlyDate = onRemoveYearlyDate,
                            timeWindows = wifiWindows,
                            onTimeWindowsChange = onWifiTimeWindowsChange,
                            onAddTimeWindow = onAddWifiTimeWindow,
                            onRemoveTimeWindow = onRemoveWifiTimeWindow,
                            onSetAllDayPreset = onSetAllDayPresetWifi,
                            is24HourFormat = uiState.is24HourFormat,
                            showWifiError = uiState.showWifiError,
                            wifiFocusRequester = wifiFocusRequester
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderTriggerEditScreenPreview() {
    ReMindTheme {
        ReminderTriggerEditScreen(
            uiState = ReminderEditUiState(),
            onBackClick = {},
            onTriggerTypeSelected = {},
            onTimeChange = {},
            onDateChange = {},
            onRepeatDaysChange = {},
            onRepeatDayToggle = {},
            onRepeatIntervalChange = {},
            onMonthlyDayToggle = {},
            onAddYearlyDate = { _, _ -> },
            onRemoveYearlyDate = {},
            onLocationNameChange = {},
            onLatitudeChange = {},
            onLongitudeChange = {},
            onRadiusChange = {},
            onTransitionTypeChange = {},
            onLocationTimeWindowsChange = {},
            onAddLocationTimeWindow = {},
            onRemoveLocationTimeWindow = {},
            onSetAllDayPresetLocation = {},
            onWifiSsidChange = {},
            onWifiBssidChange = {},
            onWifiTriggerTypeChange = {},
            onWifiTimeWindowsChange = {},
            onAddWifiTimeWindow = {},
            onRemoveWifiTimeWindow = {},
            onSetAllDayPresetWifi = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReminderTriggerEditScreenDarkPreview() {
    ReMindTheme(darkTheme = true) {
        ReminderTriggerEditScreen(
            uiState = ReminderEditUiState(triggerType = ReminderTriggerType.LOCATION),
            onBackClick = {},
            onTriggerTypeSelected = {},
            onTimeChange = {},
            onDateChange = {},
            onRepeatDaysChange = {},
            onRepeatDayToggle = {},
            onRepeatIntervalChange = {},
            onMonthlyDayToggle = {},
            onAddYearlyDate = { _, _ -> },
            onRemoveYearlyDate = {},
            onLocationNameChange = {},
            onLatitudeChange = {},
            onLongitudeChange = {},
            onRadiusChange = {},
            onTransitionTypeChange = {},
            onLocationTimeWindowsChange = {},
            onAddLocationTimeWindow = {},
            onRemoveLocationTimeWindow = {},
            onSetAllDayPresetLocation = {},
            onWifiSsidChange = {},
            onWifiBssidChange = {},
            onWifiTriggerTypeChange = {},
            onWifiTimeWindowsChange = {},
            onAddWifiTimeWindow = {},
            onRemoveWifiTimeWindow = {},
            onSetAllDayPresetWifi = {}
        )
    }
}
