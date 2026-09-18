package vn.io.litever.remind.features.alarms.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import vn.io.litever.designsystem.components.button.LiteVerButtonDefaults
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.features.alarms.R
import vn.io.litever.remind.features.alarms.ui.state.NextAlarmUiState

@Composable
fun NextAlarmHeader(
    state: NextAlarmUiState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = LiteverTheme.colors.tertiaryContainer,
        contentColor = LiteverTheme.colors.onTertiaryContainer,
        shape = LiteverTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(all = LiteverTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Alarm,
                contentDescription = null,
                modifier = Modifier.size(LiteVerButtonDefaults.IconSize)
            )
            Spacer(modifier = Modifier.width(LiteverTheme.spacing.small))
            Text(
                text = formatNextAlarmText(state),
                style = LiteverTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun NextAlarmHeaderPreview() {
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme {
        NextAlarmHeader(
            state = NextAlarmUiState.Remaining(0, 7, 30)
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun NextAlarmHeaderAllOffPreview() {
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme {
        NextAlarmHeader(
            state = NextAlarmUiState.AllOff
        )
    }
}











