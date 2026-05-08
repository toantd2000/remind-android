package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import vn.io.litever.remind.core.model.ReminderResponse

@Composable
fun ReminderInfoView(
    reminder: ReminderResponse?,
    modifier: Modifier = Modifier
) {
    if (reminder == null) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Left Accent Bar (Bookmark effect for the entire block)
            Box(
                modifier = Modifier
                    .matchParentSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp)
                    .padding(vertical = 8.dp)
            ) {
                reminder.messages.forEachIndexed { index, msg ->
                    Text(
                        text = msg,
                        modifier = Modifier.padding(start = 12.dp, end = 16.dp)
                            .padding(vertical = 8.dp),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    if (index < reminder.messages.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
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
