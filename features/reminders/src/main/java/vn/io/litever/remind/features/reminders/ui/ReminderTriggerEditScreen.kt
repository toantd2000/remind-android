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
import java.time.DayOfWeek
import vn.io.litever.remind.core.model.GeofenceTransitionType
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.features.reminders.R

import vn.io.litever.remind.features.reminders.ui.components.ReminderTimeConfigSection
import vn.io.litever.remind.features.reminders.ui.components.ReminderTriggerTypeSelector

import vn.io.litever.remind.features.reminders.viewmodel.ReminderEditUiState
import vn.io.litever.remind.features.reminders.viewmodel.ReminderEditViewModel
import vn.io.litever.remind.features.reminders.viewmodel.ReminderRepeatInterval
import java.time.LocalDate
import java.time.LocalTime

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import vn.io.litever.designsystem.components.LiteverFilterChip
import vn.io.litever.remind.features.reminders.ui.components.ReminderTimeWindowConfigSection

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
        fixedTimes = uiState.fixedTimes,
        onAddFixedTime = viewModel::addFixedTime,
        onRemoveFixedTime = viewModel::removeFixedTime,
        onUpdateFixedTime = viewModel::updateFixedTime,
        onDateChange = viewModel::setDate,
        onRepeatDaysChange = viewModel::setRepeatDays,
        onRepeatDayToggle = viewModel::toggleRepeatDay,
        onRepeatIntervalChange = viewModel::setRepeatInterval,
        onMonthlyDayToggle = viewModel::toggleMonthlyDay,
        onAddYearlyDate = viewModel::addYearlyDate,
        onRemoveYearlyDate = viewModel::removeYearlyDate,
        onIntervalMinutesChange = { str -> viewModel.setIntervalMinutes(str.toIntOrNull()) },
        onTimeWindowsChange = viewModel::setTimeWindows,
        onAddTimeWindow = viewModel::addTimeWindow,
        onRemoveTimeWindow = viewModel::removeTimeWindow,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReminderTriggerEditScreen(
    uiState: ReminderEditUiState,
    onBackClick: () -> Unit,
    onTriggerTypeSelected: (ReminderTriggerType) -> Unit,
    onTimeChange: (LocalTime) -> Unit,
    fixedTimes: List<LocalTime> = listOf(uiState.time),
    onAddFixedTime: (LocalTime) -> Unit = {},
    onRemoveFixedTime: (Int) -> Unit = {},
    onUpdateFixedTime: (Int, LocalTime) -> Unit = { _, t -> onTimeChange(t) },
    onDateChange: (LocalDate?) -> Unit,
    onRepeatDaysChange: (List<DayOfWeek>) -> Unit,
    onRepeatDayToggle: (DayOfWeek) -> Unit,
    onRepeatIntervalChange: (ReminderRepeatInterval) -> Unit,
    onMonthlyDayToggle: (Int) -> Unit,
    onAddYearlyDate: (Int, Int) -> Unit,
    onRemoveYearlyDate: (Int) -> Unit,
    onIntervalMinutesChange: (String) -> Unit = {},
    onTimeWindowsChange: (List<ReminderTimeWindow>) -> Unit = {},
    onAddTimeWindow: (ReminderTimeWindow) -> Unit = {},
    onRemoveTimeWindow: (Int) -> Unit = {},
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
                    ReminderTriggerType.FIXED_TIME -> {
                        ReminderTimeConfigSection(
                            time = uiState.time,
                            onTimeChange = onTimeChange,
                            fixedTimes = fixedTimes,
                            onAddFixedTime = onAddFixedTime,
                            onRemoveFixedTime = onRemoveFixedTime,
                            onUpdateFixedTime = onUpdateFixedTime,
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
                            is24HourFormat = true
                        )
                    }
                    ReminderTriggerType.INTERVAL -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
                        ) {
                            androidx.compose.material3.Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = LiteverTheme.shapes.large,
                                color = LiteverTheme.colors.surfaceContainerLow
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(LiteverTheme.spacing.medium)
                                ) {
                                    Text(
                                        text = stringResource(R.string.interval_section_title),
                                        style = LiteverTheme.typography.titleMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        color = LiteverTheme.colors.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(LiteverTheme.spacing.small))

                                    androidx.compose.material3.OutlinedTextField(
                                        value = uiState.intervalMinutes?.toString() ?: "",
                                        onValueChange = { onIntervalMinutesChange(it.filter { char -> char.isDigit() }) },
                                        label = { Text(stringResource(R.string.interval_minutes_label)) },
                                        placeholder = { Text(stringResource(R.string.interval_minutes_hint)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(LiteverTheme.spacing.small))

                                    // Quick interval presets
                                    val presets = listOf(
                                        15 to stringResource(R.string.interval_preset_15m),
                                        30 to stringResource(R.string.interval_preset_30m),
                                        60 to stringResource(R.string.interval_preset_1h),
                                        120 to stringResource(R.string.interval_preset_2h),
                                        180 to stringResource(R.string.interval_preset_3h),
                                        240 to stringResource(R.string.interval_preset_4h)
                                    )

                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        presets.forEach { (minutes, label) ->
                                            LiteverFilterChip(
                                                selected = uiState.intervalMinutes == minutes,
                                                onClick = { onIntervalMinutesChange(minutes.toString()) },
                                                label = { Text(label, style = LiteverTheme.typography.labelSmall) }
                                            )
                                        }
                                    }
                                }
                            }

                            // Multi-TimeWindow Configuration
                            ReminderTimeWindowConfigSection(
                                timeWindows = uiState.timeWindows,
                                onTimeWindowsChange = onTimeWindowsChange,
                                onAddTimeWindow = onAddTimeWindow,
                                onRemoveTimeWindow = onRemoveTimeWindow,
                                onSetAllDayPreset = {
                                    onTimeWindowsChange(
                                        listOf(
                                            ReminderTimeWindow(
                                                startTime = LocalTime.of(0, 0),
                                                endTime = LocalTime.of(23, 59),
                                                activeDays = emptyList()
                                            )
                                        )
                                    )
                                },
                                is24HourFormat = true
                            )
                        }
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
            
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReminderTriggerEditScreenDarkPreview() {
    ReMindTheme(darkTheme = true) {
        ReminderTriggerEditScreen(
            uiState = ReminderEditUiState(triggerType = ReminderTriggerType.FIXED_TIME),
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
            
        )
    }
}
