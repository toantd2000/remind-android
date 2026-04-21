package vn.io.litever.remind.features.reminder.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.io.litever.remind.core.designsystem.components.ReMindScaffold
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.features.reminder.viewmodel.ReminderRingingViewModel
import androidx.compose.ui.tooling.preview.Preview
import java.time.LocalTime

@Composable
fun ReminderMessageRoute(
    reminderId: Long,
    onFinish: () -> Unit,
    viewModel: ReminderRingingViewModel = hiltViewModel()
) {
    val reminder by viewModel.reminder.collectAsState()

    ReminderMessageScreen(
        reminder = reminder,
        onFinish = {
            viewModel.onFinishMessage()
            onFinish()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderMessageScreen(
    reminder: Reminder?,
    onFinish: () -> Unit
) {
    ReMindScaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val isMissed = reminder?.isMissed == true
            val titleText = if (isMissed) {
                stringResource(vn.io.litever.remind.features.reminder.R.string.missed_alarm_title)
            } else {
                stringResource(vn.io.litever.remind.core.designsystem.R.string.mission_complete)
            }
            val titleColor = if (isMissed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

            Text(
                text = titleText,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                color = titleColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!reminder?.label.isNullOrBlank()) {
                Text(
                    text = reminder?.label ?: "",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            }

            if (!reminder?.message.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = reminder?.message ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Text(
                    text = "OK",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReminderMessageScreenPreview() {
    ReMindTheme {
        ReminderMessageScreen(
            reminder = Reminder(
                id = 1,
                time = LocalTime.of(7, 30),
                label = "Wake up!",
                message = "Drink some water and start your day with a smile!"
            ),
            onFinish = {}
        )
    }
}
