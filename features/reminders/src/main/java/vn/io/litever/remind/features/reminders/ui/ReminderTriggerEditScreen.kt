package vn.io.litever.remind.features.reminders.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import vn.io.litever.designsystem.components.LiteverButton
import vn.io.litever.designsystem.components.LiteverScaffold
import vn.io.litever.designsystem.components.LiteverTopAppBar
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.ReMindBottomBar
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.features.reminders.R
import vn.io.litever.remind.features.reminders.ui.components.ReminderTriggerSection
import vn.io.litever.remind.features.reminders.viewmodel.ReminderEditUiState
import vn.io.litever.remind.features.reminders.viewmodel.ReminderEditViewModel
import vn.io.litever.remind.features.reminders.viewmodel.ReminderRepeatInterval
import java.time.DayOfWeek
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

@OptIn(ExperimentalMaterial3Api::class)
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
    val lazyListState = rememberLazyListState()

    LiteverScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
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
            item {
                ReminderTriggerSection(
                    uiState = uiState,
                    onTriggerTypeSelected = onTriggerTypeSelected,
                    onTimeChange = onTimeChange,
                    fixedTimes = fixedTimes,
                    onAddFixedTime = onAddFixedTime,
                    onRemoveFixedTime = onRemoveFixedTime,
                    onUpdateFixedTime = onUpdateFixedTime,
                    onDateChange = onDateChange,
                    onRepeatDaysChange = onRepeatDaysChange,
                    onRepeatDayToggle = onRepeatDayToggle,
                    onRepeatIntervalChange = onRepeatIntervalChange,
                    onMonthlyDayToggle = onMonthlyDayToggle,
                    onAddYearlyDate = onAddYearlyDate,
                    onRemoveYearlyDate = onRemoveYearlyDate,
                    onIntervalMinutesChange = onIntervalMinutesChange,
                    onTimeWindowsChange = onTimeWindowsChange,
                    onAddTimeWindow = onAddTimeWindow,
                    onRemoveTimeWindow = onRemoveTimeWindow,
                    is24HourFormat = true,
                    modifier = Modifier.fillMaxWidth()
                )
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
            onRemoveYearlyDate = {}
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
            onRemoveYearlyDate = {}
        )
    }
}
