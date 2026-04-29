package vn.io.litever.remind.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import vn.io.litever.designsystem.components.LiteverSwitch

@Composable
fun ReMindSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier
) {
    LiteverSwitch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier
    )
}
