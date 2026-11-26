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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.brand.tokens.LiteverTokens
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsDivider
import vn.io.litever.remind.core.designsystem.components.ReMindSettingsGroup
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
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
                .padding(
                    start = MaterialTheme.spacing.medium,
                    end = MaterialTheme.spacing.medium,
                    top = MaterialTheme.spacing.small,
                    bottom = MaterialTheme.spacing.medium
                ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            // Title Field
            Column {
                Text(
                    text = stringResource(R.string.reminder_title_label),
                    style = LiteverTokens.typography.labelSmallEmphasized,
                    color = if (showTitleError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
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
                        textStyle = LiteverTokens.typography.titleMediumEmphasized.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        lineLimits = TextFieldLineLimits.SingleLine,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorator = TextFieldDecorator { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (titleState.text.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.reminder_title_placeholder),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
                                modifier = Modifier.size(MaterialTheme.spacing.mediumLarge)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = ReMindIcons.NewLabel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                if (showTitleError) {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
                    Text(
                        text = stringResource(R.string.reminder_title_required),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            ReMindSettingsDivider(
                inset = false,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.none)
            )

            // Notes Field
            Column {
                Text(
                    text = stringResource(R.string.reminder_notes_label),
                    style = LiteverTokens.typography.labelSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
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
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Default
                        ),
                        lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 4),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorator = TextFieldDecorator { innerTextField ->
                            Box(contentAlignment = Alignment.TopStart) {
                                if (notesState.text.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.reminder_notes_placeholder),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
                                modifier = Modifier.size(MaterialTheme.spacing.mediumLarge)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = ReMindIcons.EditNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(MaterialTheme.spacing.large)
                                .padding(top = MaterialTheme.spacing.tiny)
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
