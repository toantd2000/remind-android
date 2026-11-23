package vn.io.litever.remind.features.reminders.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
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
    ReMindSettingsGroup(
        title = stringResource(R.string.section_basic_info),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Field
            Column {
                Text(
                    text = stringResource(R.string.reminder_title_label),
                    style = LiteverTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (showTitleError) LiteverTheme.colors.error else LiteverTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val textFieldModifier = if (titleFocusRequester != null) {
                        Modifier.weight(1f).focusRequester(titleFocusRequester)
                    } else {
                        Modifier.weight(1f)
                    }

                    val titleState = rememberTextFieldState(initialText = title)
                    LaunchedEffect(title) {
                        if (titleState.text.toString() != title) {
                            titleState.setTextAndPlaceCursorAtEnd(title)
                        }
                    }
                    LaunchedEffect(titleState) {
                        snapshotFlow { titleState.text.toString() }.collect {
                            if (it != title) onTitleChange(it)
                        }
                    }

                    BasicTextField(
                        state = titleState,
                        modifier = textFieldModifier,
                        textStyle = LiteverTheme.typography.bodyLarge.copy(
                            color = LiteverTheme.colors.onSurface
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        lineLimits = TextFieldLineLimits.SingleLine,
                        cursorBrush = SolidColor(LiteverTheme.colors.primary),
                        decorator = TextFieldDecorator { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (titleState.text.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.reminder_title_placeholder),
                                        style = LiteverTheme.typography.bodyLarge.copy(
                                            color = LiteverTheme.colors.outlineVariant
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    if (title.isNotEmpty()) {
                        IconButton(
                            onClick = { onTitleChange("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = ReMindIcons.Close,
                                contentDescription = stringResource(R.string.action_clear_title),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = ReMindIcons.NewLabel,
                            contentDescription = null,
                            tint = LiteverTheme.colors.outlineVariant
                        )
                    }
                }

                if (showTitleError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.reminder_title_required),
                        style = LiteverTheme.typography.bodySmall,
                        color = LiteverTheme.colors.error
                    )
                }
            }

            HorizontalDivider(
                color = LiteverTheme.colors.outlineVariant.copy(alpha = 0.3f)
            )

            // Notes Field
            Column {
                Text(
                    text = stringResource(R.string.reminder_notes_label),
                    style = LiteverTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = LiteverTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    val notesState = rememberTextFieldState(initialText = notes)
                    LaunchedEffect(notes) {
                        if (notesState.text.toString() != notes) {
                            notesState.setTextAndPlaceCursorAtEnd(notes)
                        }
                    }
                    LaunchedEffect(notesState) {
                        snapshotFlow { notesState.text.toString() }.collect {
                            if (it != notes) onNotesChange(it)
                        }
                    }

                    BasicTextField(
                        state = notesState,
                        modifier = Modifier.weight(1f),
                        textStyle = LiteverTheme.typography.bodyLarge.copy(
                            color = LiteverTheme.colors.onSurface
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Default
                        ),
                        lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 4),
                        cursorBrush = SolidColor(LiteverTheme.colors.primary),
                        decorator = TextFieldDecorator { innerTextField ->
                            Box(contentAlignment = Alignment.TopStart) {
                                if (notesState.text.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.reminder_notes_placeholder),
                                        style = LiteverTheme.typography.bodyLarge.copy(
                                            color = LiteverTheme.colors.outlineVariant
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    if (notes.isNotEmpty()) {
                        IconButton(
                            onClick = { onNotesChange("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = ReMindIcons.Close,
                                contentDescription = stringResource(R.string.action_clear_notes),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = ReMindIcons.EditNote,
                            contentDescription = null,
                            tint = LiteverTheme.colors.outlineVariant,
                            modifier = Modifier
                                .size(24.dp)
                                .padding(top = 2.dp)
                        )
                    }
                }
            }
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
