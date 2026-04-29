package vn.io.litever.designsystem.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

/**
 * A custom TextField wrapper for the Litever application.
 * Follows Material 3 design and includes a built-in clear button.
 */
@Composable
fun LiteverTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    onClearClick: (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.large,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        shape = shape,
        trailingIcon = if (onClearClick != null && value.isNotEmpty()) {
            {
                IconButton(onClick = onClearClick) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        contentDescription = "Clear"
                    )
                }
            }
        } else null,
        colors = OutlinedTextFieldDefaults.colors()
    )
}
