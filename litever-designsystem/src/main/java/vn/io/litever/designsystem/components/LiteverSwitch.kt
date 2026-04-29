package vn.io.litever.designsystem.components

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import vn.io.litever.designsystem.theme.LiteverTheme

@Composable
fun LiteverSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = LiteverTheme.colors.primary,
            checkedTrackColor = LiteverTheme.colors.primaryContainer,
            uncheckedThumbColor = LiteverTheme.colors.onSurfaceVariant,
            uncheckedTrackColor = LiteverTheme.colors.surfaceVariant
        )
    )
}
