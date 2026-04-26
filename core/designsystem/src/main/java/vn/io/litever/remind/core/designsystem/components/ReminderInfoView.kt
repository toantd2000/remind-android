package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import vn.io.litever.remind.core.model.ReminderResponse

@Composable
fun ReminderInfoView(
    reminder: ReminderResponse?,
    modifier: Modifier = Modifier
) {
    if (reminder == null) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        reminder.messages.forEach { msg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                )
            ) {
                Text(
                    text = msg,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ReminderInfoViewPreview() {
    val mockReminder = ReminderResponse(
        messages = listOf(
            "Đừng quên mang theo tài liệu họp lúc 9h sáng nhé!",
            "Uống thuốc sau khi ăn sáng."
        ),
        adConfig = vn.io.litever.remind.core.model.AdConfig(enableAds = false)
    )
    vn.io.litever.remind.core.designsystem.theme.ReMindTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ReminderInfoView(reminder = mockReminder)
        }
    }
}
