package vn.io.litever.remind.features.alarms.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import vn.io.litever.remind.features.alarms.R
import vn.io.litever.remind.features.alarms.ui.state.NextAlarmUiState

@Composable
fun NextAlarmHeader(
    state: NextAlarmUiState,
    modifier: Modifier = Modifier
) {
    val isAllOff = state is NextAlarmUiState.AllOff
    val containerColor = if (isAllOff) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.tertiaryContainer
    }
    val contentColor = if (isAllOff) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onTertiaryContainer
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.spacing.medium,
                    vertical = MaterialTheme.spacing.smallMedium
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (isAllOff) ReMindIcons.NotificationsPaused else ReMindIcons.NotificationsActive,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            Text(
                text = formatNextAlarmText(state),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
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

@Preview(name = "Next Alarm - Light", showBackground = true)
@Composable
fun NextAlarmHeaderPreview() {
    ReMindTheme {
        NextAlarmHeader(
            state = NextAlarmUiState.Remaining(0, 7, 30)
        )
    }
}

@Preview(name = "Next Alarm All Off - Light", showBackground = true)
@Composable
fun NextAlarmHeaderAllOffPreview() {
    ReMindTheme {
        NextAlarmHeader(
            state = NextAlarmUiState.AllOff
        )
    }
}

@Preview(
    name = "Next Alarm - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0xFF19120CL
)
@Composable
fun NextAlarmHeaderDarkPreview() {
    ReMindTheme(darkTheme = true) {
        NextAlarmHeader(
            state = NextAlarmUiState.Remaining(0, 7, 30)
        )
    }
}
