package vn.io.litever.remind.features.alarms.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import vn.io.litever.remind.features.alarms.viewmodel.AlarmPreviewViewModel

@Composable
fun AlarmPreviewRoute(
    alarmId: Long,
    onExit: () -> Unit,
    onStartMissionPreview: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlarmPreviewViewModel = hiltViewModel()
) {
    val alarm by viewModel.alarm.collectAsState()
    val is24HourFormat by viewModel.is24HourFormat.collectAsState()
    val autoSilenceCountdown by viewModel.autoSilenceCountdown.collectAsState()

    AlarmRingingContent(
        onDismiss = onExit, // In preview, dismiss just exits
        onSnooze = {},      // In preview, snooze does nothing (just for display)
        onStartMission = { 
            viewModel.startMissionPreview()
            onStartMissionPreview(alarmId) 
        },
        modifier = modifier,
        alarm = alarm,
        is24HourFormat = is24HourFormat,
        autoSilenceCountdown = autoSilenceCountdown,
        isPreview = true,
        onExitPreview = onExit
    )
}
