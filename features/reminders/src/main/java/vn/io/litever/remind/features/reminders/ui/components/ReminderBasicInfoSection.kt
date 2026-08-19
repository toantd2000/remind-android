package vn.io.litever.remind.features.reminders.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverOutlinedCard
import vn.io.litever.designsystem.components.LiteverTextField
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.features.reminders.R

@Composable
fun ReminderBasicInfoSection(
    title: String,
    onTitleChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    showTitleError: Boolean,
    modifier: Modifier = Modifier
) {
    LiteverOutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.section_basic_info),
                style = LiteverTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LiteverTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            LiteverTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.reminder_title_placeholder)) },
                isError = showTitleError,
                supportingText = if (showTitleError) {
                    {
                        Text(
                            text = stringResource(R.string.reminder_title_required),
                            color = LiteverTheme.colors.error
                        )
                    }
                } else null,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            LiteverTextField(
                value = notes,
                onValueChange = onNotesChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.reminder_notes_placeholder)) },
                minLines = 3,
                maxLines = 5,
                singleLine = false
            )
        }
    }
}
