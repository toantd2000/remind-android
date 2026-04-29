package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import vn.io.litever.designsystem.components.*

@Composable
fun SettingsCategory(
    title: String,
    modifier: Modifier = Modifier
) {
    LiteverSettingsCategory(title = title, modifier = modifier)
}

@Composable
fun SettingsItem(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtitle: String? = null,
    statusText: String? = null,
    statusColor: Color? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    LiteverSettingsItem(
        title = title,
        modifier = modifier,
        icon = icon,
        subtitle = subtitle,
        statusText = statusText,
        statusColor = statusColor,
        onClick = onClick,
        trailingContent = trailingContent
    )
}

@Composable
fun SettingsSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtitle: String? = null,
) {
    LiteverSettingsSwitchItem(
        title = title,
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        icon = icon,
        subtitle = subtitle
    )
}

@Composable
fun ReMindSettingsGroup(
    title: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    LiteverSettingsGroup(title = title, modifier = modifier, content = content)
}
