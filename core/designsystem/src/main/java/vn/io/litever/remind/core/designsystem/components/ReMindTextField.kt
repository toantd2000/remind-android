package vn.io.litever.remind.core.designsystem.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import vn.io.litever.designsystem.components.LiteverTextField

@Composable
fun ReMindTextField(
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
    LiteverTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        onClearClick = onClearClick,
        shape = shape
    )
}
