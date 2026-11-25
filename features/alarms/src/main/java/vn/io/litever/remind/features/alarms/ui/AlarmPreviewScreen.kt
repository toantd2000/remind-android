package vn.io.litever.remind.features.alarms.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.features.alarms.viewmodel.AlarmPreviewViewModel
import java.time.LocalTime

@Composable
fun AlarmPreviewRoute(
    alarmId: Long,
    onExit: () -> Unit,
    onStartMissionPreview: (Long) -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: AlarmPreviewViewModel = hiltViewModel()
) {
    val alarm by viewModel.alarm.collectAsState()
    val is24HourFormat by viewModel.is24HourFormat.collectAsState()
    val autoSilenceCountdown by viewModel.autoSilenceCountdown.collectAsState()

    val missionResult by navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow<String?>("mission_result", null)
        ?.collectAsState() ?: remember { mutableStateOf(null) }

    LaunchedEffect(missionResult) {
        when (missionResult) {
            "success" -> {
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("mission_result")
                onExit()
            }
            "abandoned" -> {
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("mission_result")
                // Do not exit, stay on preview screen so the alarm resumes ringing
            }
        }
    }

    AlarmPreviewContent(
        onExit = onExit,
        onStartMission = {
            viewModel.startMissionPreview()
            onStartMissionPreview(alarmId)
        },
        modifier = modifier,
        alarm = alarm,
        is24HourFormat = is24HourFormat,
        autoSilenceCountdown = autoSilenceCountdown
    )
}

@Composable
fun AlarmPreviewContent(
    onExit: () -> Unit,
    onStartMission: () -> Unit,
    modifier: Modifier = Modifier,
    alarm: Alarm? = null,
    is24HourFormat: Boolean = false,
    autoSilenceCountdown: Int? = null
) {
    AlarmRingingContent(
        onDismiss = onExit,
        onSnooze = {},
        onStartMission = onStartMission,
        modifier = modifier,
        alarm = alarm,
        is24HourFormat = is24HourFormat,
        autoSilenceCountdown = autoSilenceCountdown,
        isPreview = true,
        onExitPreview = onExit
    )
}

@Preview(showBackground = true)
@Composable
fun AlarmPreviewContentPreview() {
    ReMindTheme {
        AlarmPreviewContent(
            onExit = {},
            onStartMission = {},
            alarm = Alarm(
                id = 1,
                time = LocalTime.of(7, 30),
                label = "Wake up!",
                message = "Have a great day ahead!",
                isEnabled = true,
                snoozeEnabled = true,
                snoozeRepeatCount = 3,
                currentSnoozeCount = 1
            ),
            is24HourFormat = false,
            autoSilenceCountdown = 60
        )
    }
}
