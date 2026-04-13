package vn.io.litever.remind.features.reminder.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import vn.io.litever.remind.features.reminder.R
import vn.io.litever.remind.features.reminder.ui.state.NextReminderUiState

@Composable
fun NextReminderHeader(
    state: NextReminderUiState,
    modifier: Modifier = Modifier
) {
    Text(
        text = formatNextReminderText(state),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 24.dp, vertical = 16.dp)
    )
}

@Composable
private fun formatNextReminderText(state: NextReminderUiState): String {
    return when (state) {
        NextReminderUiState.AllOff -> stringResource(R.string.all_reminders_off)
        is NextReminderUiState.Remaining -> {
            val timeDescription = when {
                state.days > 0 -> stringResource(R.string.days_hours, state.days, state.hours)
                state.hours > 0 -> stringResource(R.string.hours_minutes, state.hours, state.minutes)
                state.minutes > 0 -> stringResource(R.string.just_minutes, state.minutes)
                else -> stringResource(R.string.less_than_one_minute)
            }
            stringResource(R.string.next_reminder_prefix, timeDescription)
        }
    }
}
