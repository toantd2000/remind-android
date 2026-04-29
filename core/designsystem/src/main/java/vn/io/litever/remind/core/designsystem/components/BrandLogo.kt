package vn.io.litever.remind.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import vn.io.litever.designsystem.components.LiteverLogo

@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 48.sp
) {
    LiteverLogo(
        modifier = modifier,
        fontSize = fontSize
    )
}
