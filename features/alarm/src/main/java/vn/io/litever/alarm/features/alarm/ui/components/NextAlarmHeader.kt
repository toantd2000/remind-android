package vn.io.litever.alarm.features.alarm.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import vn.io.litever.alarm.features.alarm.R
import vn.io.litever.alarm.features.alarm.ui.state.NextAlarmUiState

@Composable
fun NextAlarmHeader(
    state: NextAlarmUiState,
    modifier: Modifier = Modifier
) {
    Text(
        text = formatNextAlarmText(state),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 24.dp, vertical = 16.dp)
    )
}

@Composable
private fun formatNextAlarmText(state: NextAlarmUiState): String {
    return when (state) {
        NextAlarmUiState.AllOff -> stringResource(R.string.all_alarms_off)
        is NextAlarmUiState.Remaining -> {
            val timeDescription = when {
                state.days > 0 -> stringResource(R.string.days_hours, state.days, state.hours)
                state.hours > 0 -> stringResource(R.string.hours_minutes, state.hours, state.minutes)
                state.minutes > 0 -> stringResource(R.string.just_minutes, state.minutes)
                else -> stringResource(R.string.less_than_one_minute)
            }
            stringResource(R.string.next_alarm_prefix, timeDescription)
        }
    }
}
