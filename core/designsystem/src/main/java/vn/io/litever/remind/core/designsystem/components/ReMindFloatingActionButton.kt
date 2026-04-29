package vn.io.litever.remind.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import vn.io.litever.designsystem.components.LiteverFloatingActionButton

@Composable
fun ReMindFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    LiteverFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        content = content
    )
}
