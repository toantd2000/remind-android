package vn.io.litever.remind.features.reminders.ui.components
import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.ReminderTimeWindow
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.ReminderMonthDay
import vn.io.litever.remind.features.reminders.R
import vn.io.litever.remind.features.reminders.viewmodel.ReminderRepeatInterval
import java.time.LocalDate
import java.time.LocalTime
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTriggerSection(
    uiState: vn.io.litever.remind.features.reminders.viewmodel.ReminderEditUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = LiteverTheme.shapes.large,
        color = LiteverTheme.colors.surfaceContainerLow,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(LiteverTheme.spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.section_trigger_type),
                    style = LiteverTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LiteverTheme.colors.onSurface
                )
                Text(
                    text = stringResource(R.string.action_edit),
                    style = LiteverTheme.typography.labelMedium,
                    color = LiteverTheme.colors.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (uiState.triggerType) {
                ReminderTriggerType.FIXED_TIME -> {
                    val timesSummary = if (uiState.fixedTimes.isNotEmpty()) {
                        uiState.fixedTimes.joinToString(", ")
                    } else {
                        uiState.time.toString()
                    }
                    Text(
                        text = "Time: $timesSummary (${uiState.repeatInterval})",
                        style = LiteverTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = LiteverTheme.colors.onSurface
                    )
                    if (uiState.repeatInterval == ReminderRepeatInterval.MONTHLY && uiState.monthlyDays.isNotEmpty()) {
                        Text(
                            text = "Days of month: ${uiState.monthlyDays.joinToString(", ")}",
                            style = LiteverTheme.typography.bodySmall,
                            color = LiteverTheme.colors.onSurfaceVariant
                        )
                    }
                    if (uiState.repeatInterval == ReminderRepeatInterval.YEARLY && uiState.yearlyDates.isNotEmpty()) {
                        Text(
                            text = "Dates: ${uiState.yearlyDates.joinToString(", ") { "${it.day}/${it.month}" }}",
                            style = LiteverTheme.typography.bodySmall,
                            color = LiteverTheme.colors.onSurfaceVariant
                        )
                    }
                }
                ReminderTriggerType.INTERVAL -> {
                    val intervalText = uiState.intervalMinutes?.let { "Every $it minutes" } ?: "Interval not set"
                    Text(
                        text = intervalText,
                        style = LiteverTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = LiteverTheme.colors.onSurface
                    )
                    val windowSummary = if (uiState.timeWindows.isNotEmpty()) {
                        uiState.timeWindows.joinToString("; ") { "${it.startTime} - ${it.endTime}" }
                    } else {
                        stringResource(R.string.time_window_24_7_desc)
                    }
                    Text(
                        text = "Active: $windowSummary",
                        style = LiteverTheme.typography.bodySmall,
                        color = LiteverTheme.colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}