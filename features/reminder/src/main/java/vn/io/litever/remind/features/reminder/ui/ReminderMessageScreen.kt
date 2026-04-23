package vn.io.litever.remind.features.reminder.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlarmOff
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Star
import vn.io.litever.remind.core.designsystem.components.ReMindScaffold
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.features.reminder.viewmodel.ReminderRingingViewModel
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.remind.core.designsystem.components.ReMindButton
import java.time.LocalTime
import java.util.Locale

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

@Composable
fun ReminderMessageScreen(
    reminder: Reminder?,
    onFinish: () -> Unit
) {
    BackHandler { }
    ReMindScaffold { padding ->
        val isMissed = reminder?.isMissed == true
        val statusColor = if (isMissed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        val icon = if (isMissed) Icons.Rounded.AlarmOff else Icons.Rounded.NotificationsActive
        val statusTitle = if (isMissed) {
            stringResource(vn.io.litever.remind.features.reminder.R.string.missed_alarm_title)
        } else {
            stringResource(vn.io.litever.remind.features.reminder.R.string.reminder_summary_title)
        }

        // Subtle gradient background based on status
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            statusColor.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Header Section
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = statusColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = statusTitle,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = statusColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Main Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, 
                    statusColor.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Time with small AM/PM
                    val displayTime = reminder?.time ?: LocalTime.now()
                    val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm")
                    val amPmFormatter = java.time.format.DateTimeFormatter.ofPattern("a")
                    
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = displayTime.format(timeFormatter),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-1).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = displayTime.format(amPmFormatter).uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (!reminder?.label.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = reminder?.label ?: "",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (!reminder?.message.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = reminder?.message ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Future Extras Placeholder (e.g. Weather)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Have a wonderful day ahead!",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            ReMindButton(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(vn.io.litever.remind.features.reminder.R.string.reminder_message_dismiss),
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
                label = "Morning Yoga",
                message = "Time to stretch and start your day!"
            ),
            onFinish = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReminderMessageMissedScreenPreview() {
    ReMindTheme(darkTheme = true) {
        ReminderMessageScreen(
            reminder = Reminder(
                id = 2,
                time = LocalTime.of(8, 0),
                label = "Important Meeting",
                message = "You missed this important reminder.",
                isMissed = true
            ),
            onFinish = {}
        )
    }
}
