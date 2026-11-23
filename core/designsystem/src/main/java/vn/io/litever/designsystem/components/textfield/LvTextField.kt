package vn.io.litever.designsystem.components.textfield

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme

import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.designsystem.components.core.LvSemantic

/**
 * Standard opinionated TextField for LiteVer applications.
 *
 * Automatically manages LiteVer shape (6.dp), active semantic colors,
 * error state rendering, and supporting error text across both [LvTextFieldType.Outlined]
 * and [LvTextFieldType.Filled] styles.
 *
 * @param value The text string to be shown in the text field.
 * @param onValueChange The callback that is triggered when the input service updates the text.
 * @param modifier The [Modifier] to be applied to this text field.
 * @param type Visual styling variant ([LvTextFieldType.Outlined] or [LvTextFieldType.Filled]).
 * @param label Optional label text displayed inside or above the text field.
 * @param placeholder Optional placeholder text displayed when the text field is empty.
 * @param leadingIcon Optional leading icon.
 * @param trailingIcon Optional trailing icon.
 * @param errorMessage If provided and not null, automatically marks field as error and displays supporting text.
 * @param semantic Visual intent color for active borders/indicators and cursor when not in error state.
 * @param singleLine When set to true, this text field becomes a single horizontally scrolling text field.
 * @param maxLines The maximum height in terms of maximum number of visible lines.
 * @param enabled Controls the enabled state of the text field.
 * @param readOnly Controls the editable state of the text field.
 */
@Composable
fun LvTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    type: LvTextFieldType = LvTextFieldType.Outlined,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    errorMessage: String? = null,
    semantic: LvSemantic = LvSemantic.Text,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    shape: Shape = LiteVerTextFieldDefaults.shape,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource? = null
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val isError = errorMessage != null

    val activeColor = when {
        isError -> MaterialTheme.colorScheme.error
        semantic == LvSemantic.Primary -> MaterialTheme.colorScheme.primary
        semantic == LvSemantic.Secondary -> MaterialTheme.colorScheme.secondary
        semantic == LvSemantic.Tertiary -> MaterialTheme.colorScheme.tertiary
        semantic == LvSemantic.Text -> MaterialTheme.colorScheme.onSurface
        semantic == LvSemantic.Success -> LiteverTheme.colors.success
        semantic == LvSemantic.Destructive -> MaterialTheme.colorScheme.error
        semantic == LvSemantic.Warning -> LiteverTheme.colors.warning
        else -> MaterialTheme.colorScheme.primary
    }

    when (type) {
        LvTextFieldType.Outlined -> {
            val colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = activeColor,
                unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                cursorColor = activeColor,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorCursorColor = MaterialTheme.colorScheme.error
            )

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier,
                enabled = enabled,
                readOnly = readOnly,
                label = label?.let { { Text(it) } },
                placeholder = placeholder?.let { { Text(it) } },
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                supportingText = errorMessage?.let {
                    { Text(text = it, color = MaterialTheme.colorScheme.error) }
                },
                isError = isError,
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                singleLine = singleLine,
                maxLines = maxLines,
                shape = shape,
                colors = colors,
                interactionSource = interactionSource
            )
        }
        LvTextFieldType.Filled -> {
            val colors: TextFieldColors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f),
                focusedIndicatorColor = activeColor,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = activeColor,
                errorIndicatorColor = MaterialTheme.colorScheme.error,
                errorCursorColor = MaterialTheme.colorScheme.error
            )

            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier,
                enabled = enabled,
                readOnly = readOnly,
                label = label?.let { { Text(it) } },
                placeholder = placeholder?.let { { Text(it) } },
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
                supportingText = errorMessage?.let {
                    { Text(text = it, color = MaterialTheme.colorScheme.error) }
                },
                isError = isError,
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                singleLine = singleLine,
                maxLines = maxLines,
                shape = shape,
                colors = colors,
                interactionSource = interactionSource
            )
        }
    }
}

@Preview(showBackground = true, name = "LvTextField - Light")
@Composable
fun LvTextFieldLightPreview() {
    ReMindTheme(darkTheme = false) {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LvTextField(
                    value = "Outlined style",
                    onValueChange = {},
                    type = LvTextFieldType.Outlined,
                    label = "Outlined Text Field",
                    placeholder = "Enter text..."
                )
                LvTextField(
                    value = "Filled style",
                    onValueChange = {},
                    type = LvTextFieldType.Filled,
                    label = "Filled Text Field",
                    placeholder = "Enter text..."
                )
                LvTextField(
                    value = "Invalid input",
                    onValueChange = {},
                    label = "Email",
                    errorMessage = "Invalid email format"
                )
                LvTextField(
                    value = "Tertiary value",
                    onValueChange = {},
                    label = "Tertiary",
                    semantic = LvSemantic.Tertiary
                )
                LvTextField(
                    value = "Text value",
                    onValueChange = {},
                    label = "Text",
                    semantic = LvSemantic.Text
                )
                LvTextField(
                    value = "Success value",
                    onValueChange = {},
                    label = "Code Verified",
                    semantic = LvSemantic.Success
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "LvTextField - Dark")
@Composable
fun LvTextFieldDarkPreview() {
    ReMindTheme(darkTheme = true) {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LvTextField(
                    value = "Dark mode outlined",
                    onValueChange = {},
                    type = LvTextFieldType.Outlined,
                    label = "Username"
                )
                LvTextField(
                    value = "Dark mode filled",
                    onValueChange = {},
                    type = LvTextFieldType.Filled,
                    label = "Password"
                )
                LvTextField(
                    value = "Error state",
                    onValueChange = {},
                    label = "Email",
                    errorMessage = "Invalid email format"
                )
            }
        }
    }
}
