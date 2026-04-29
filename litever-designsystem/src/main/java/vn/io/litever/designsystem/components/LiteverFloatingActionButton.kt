package vn.io.litever.designsystem.components

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import vn.io.litever.designsystem.theme.LiteverTheme

@Composable
fun LiteverFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = LiteverTheme.colors.primaryContainer,
        contentColor = LiteverTheme.colors.onPrimaryContainer,
        content = content
    )
}
