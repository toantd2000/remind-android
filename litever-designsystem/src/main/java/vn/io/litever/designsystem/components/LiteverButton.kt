package vn.io.litever.designsystem.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.designsystem.theme.LiteverShapes

@Composable
fun LiteverButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = LiteverShapes.medium,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = LiteverTheme.colors.primary,
        contentColor = LiteverTheme.colors.onPrimary
    ),
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
fun LiteverOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = LiteverShapes.medium,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(
        contentColor = LiteverTheme.colors.primary
    ),
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
fun LiteverTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = LiteverShapes.medium,
    colors: ButtonColors = ButtonDefaults.textButtonColors(
        contentColor = LiteverTheme.colors.primary
    ),
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
