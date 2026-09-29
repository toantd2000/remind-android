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
    onIntervalMinutesChange: (String) -> Unit = {},
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
                            date = uiState.date,
                            onDateChange = onDateChange,
                            repeatDays = uiState.repeatDays,
                            onRepeatDaysChange = onRepeatDaysChange,
                            onRepeatDayToggle = onRepeatDayToggle,
                            repeatInterval = uiState.repeatInterval,
                            onRepeatIntervalChange = onRepeatIntervalChange,
                            monthlyDays = uiState.monthlyDays.map { it.day },
                            onMonthlyDayToggle = onMonthlyDayToggle,
                            yearlyDates = uiState.yearlyDates.map { vn.io.litever.remind.core.model.ReminderMonthDay(month = it.monthValue, day = it.dayOfMonth) },
                            onAddYearlyDate = onAddYearlyDate,
                            onRemoveYearlyDate = onRemoveYearlyDate,
                            is24HourFormat = true
                        )
                    }
                    ReminderTriggerType.INTERVAL -> { 
                        androidx.compose.material3.OutlinedTextField(value = uiState.intervalMinutes?.toString() ?: "", onValueChange = { onIntervalMinutesChange(it) }, label = { Text("Interval Minutes") }, modifier = Modifier.fillMaxWidth()) 
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
