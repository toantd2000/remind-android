package vn.io.litever.remind.features.reminders.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.designsystem.components.FeedbackStateType
import vn.io.litever.designsystem.components.FeedbackStateView
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.ReMindIllustration
import vn.io.litever.remind.core.designsystem.components.ReMindIllustrationType
import vn.io.litever.remind.core.designsystem.components.ReMindLogo
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.features.reminders.R

@Composable
fun ReminderEmptyState(
    onAddReminderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val descriptionPrefix = stringResource(R.string.empty_reminders_description)

    FeedbackStateView(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.no_reminders),
                style = LiteverTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
        },
        description = {
            Column(
                modifier = Modifier.wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = descriptionPrefix,
                    style = LiteverTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                ReMindLogo(fontSize = LiteverTheme.typography.bodyMedium.fontSize)
            }
        },
        illustration = {
            ReMindIllustration(type = ReMindIllustrationType.NO_REMINDER)
        },
        action = {
            LvButton(
                onClick = onAddReminderClick,
                semantic = LvSemantic.Primary
            ) {
                Text(
                    text = stringResource(R.string.action_add_new_reminder)
                )
            }
        }
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

