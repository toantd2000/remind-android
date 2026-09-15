package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import vn.io.litever.designsystem.theme.LiteverTheme

@Composable
fun ReMindGroupCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        shape = LiteverTheme.shapes.large,
        color = LiteverTheme.colors.surfaceContainerLow,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = LiteverTheme.spacing.medium)
    ) {
        content()
    }
}
