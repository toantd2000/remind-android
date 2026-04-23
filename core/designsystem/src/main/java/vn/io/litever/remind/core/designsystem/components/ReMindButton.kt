package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun ReMindButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.medium,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        content = { content() }
    )
}

@Composable
fun ReMindOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.medium,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    content: @Composable () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        content = { content() }
    )
}

@Composable
fun ReMindTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.medium,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    content: @Composable () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        shape = shape,
        colors = colors,
        content = { content() }
    )
}
