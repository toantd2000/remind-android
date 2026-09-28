package vn.io.litever.remind.features.reminders.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.designsystem.components.LiteverTextField
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.features.reminders.R

@Composable
fun ReminderBasicInfoSection(
    title: String,
    onTitleChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    showTitleError: Boolean,
    modifier: Modifier = Modifier,
    titleFocusRequester: FocusRequester? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = LiteverTheme.colors.surfaceContainerLow,
        shape = LiteverTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LiteverTheme.spacing.medium)
        ) {
            Text(
                text = stringResource(R.string.section_basic_info),
                style = LiteverTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LiteverTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.height(LiteverTheme.spacing.smallMedium))

            val titleFieldModifier = if (titleFocusRequester != null) {
                Modifier.fillMaxWidth().focusRequester(titleFocusRequester)
            } else {
                Modifier.fillMaxWidth()
            }

            LiteverTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = titleFieldModifier,
                label = stringResource(R.string.reminder_title_placeholder),
                singleLine = true,
                isError = showTitleError,
                supportingText = {
                    if (showTitleError) {
                        Text(text = stringResource(R.string.reminder_title_required))
                    }
                }
            )

            Spacer(modifier = Modifier.height(LiteverTheme.spacing.small))

            LiteverTextField(
                value = notes,
                onValueChange = onNotesChange,
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.reminder_notes_placeholder),
                minLines = 3,
                maxLines = 5,
                singleLine = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderBasicInfoSectionPreview() {
    ReMindTheme {
        ReminderBasicInfoSection(
            title = "Buy groceries",
            onTitleChange = {},
            notes = "Milk, Eggs, Bread, Cheese",
            onNotesChange = {},
            showTitleError = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReminderBasicInfoSectionErrorPreview() {
    ReMindTheme {
        ReminderBasicInfoSection(
            title = "",
            onTitleChange = {},
            notes = "",
            onNotesChange = {},
            showTitleError = true
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReminderBasicInfoSectionDarkPreview() {
    ReMindTheme(darkTheme = true) {
        ReminderBasicInfoSection(
            title = "Buy groceries",
            onTitleChange = {},
            notes = "Milk, Eggs, Bread, Cheese",
            onNotesChange = {},
            showTitleError = false
        )
    }
}

