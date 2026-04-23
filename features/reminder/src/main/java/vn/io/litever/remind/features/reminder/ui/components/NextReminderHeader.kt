package vn.io.litever.remind.features.reminder.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.remind.features.reminder.R
import vn.io.litever.remind.features.reminder.ui.state.NextReminderUiState

@Composable
fun NextReminderHeader(
    state: NextReminderUiState,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Alarm,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = formatNextReminderText(state),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun NextReminderHeaderPreview() {
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme {
        NextReminderHeader(
            state = NextReminderUiState.Remaining(0, 7, 30)
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun NextReminderHeaderAllOffPreview() {
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme {
        NextReminderHeader(
            state = NextReminderUiState.AllOff
        )
    }
}

