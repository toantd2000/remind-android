package vn.io.litever.remind.features.reminders.ui.components

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.brand.assets.LiteverMascotState
import vn.io.litever.remind.core.designsystem.components.ReMindMascotEmptyState
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.features.reminders.R

@Composable
fun ReminderEmptyState(
    onAddReminderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ReMindMascotEmptyState(
        mascot = LiteverMascotState.EMPTY,
        title = stringResource(R.string.no_reminders),
        description = stringResource(R.string.empty_reminders_description),
        actionLabel = stringResource(R.string.action_add_new_reminder),
        onActionClick = onAddReminderClick,
        modifier = modifier
    )
}

@Preview(
    name = "Reminder Empty State - Light",
    showBackground = true,
    device = Devices.PIXEL_7
)
@Composable
private fun ReminderEmptyStatePreview() {
    ReMindTheme {
        ReminderEmptyState(onAddReminderClick = {})
    }
}

@Preview(
    name = "Reminder Empty State - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = Devices.PIXEL_7,
    backgroundColor = 0xFF121212L
)
@Composable
private fun ReminderEmptyStateDarkPreview() {
    ReMindTheme(darkTheme = true) {
        ReminderEmptyState(onAddReminderClick = {})
    }
}
