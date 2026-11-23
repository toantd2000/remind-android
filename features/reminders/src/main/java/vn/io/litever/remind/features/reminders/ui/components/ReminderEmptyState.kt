package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.material3.MaterialTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.brand.assets.LiteverMascotState
import vn.io.litever.remind.core.designsystem.components.ReMindLogo
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.features.reminders.R

@Composable
fun ReminderEmptyState(
    onAddReminderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val descriptionPrefix = stringResource(R.string.empty_reminders_description)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = LiteverMascotState.EMPTY.drawableRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(160.dp)
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.mediumLarge))
        Text(
            text = stringResource(R.string.no_reminders),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
        Column(
            modifier = Modifier.wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = descriptionPrefix,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            ReMindLogo(fontSize = MaterialTheme.typography.bodyMedium.fontSize)
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        Button(onClick = onAddReminderClick) {
            Text(text = stringResource(R.string.action_add_new_reminder))
        }
    }
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

