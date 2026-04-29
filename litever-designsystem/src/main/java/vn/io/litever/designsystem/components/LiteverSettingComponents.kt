package vn.io.litever.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.designsystem.theme.LiteverShapes

@Composable
fun LiteverSettingsCategory(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = LiteverTheme.typography.labelLarge,
        color = LiteverTheme.colors.primary,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@Composable
fun LiteverSettingsItem(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtitle: String? = null,
    statusText: String? = null,
    statusColor: Color? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = LiteverTheme.typography.bodyLarge
            )
        },
        supportingContent = subtitle?.let {
            {
                Text(
                    text = it,
                    style = LiteverTheme.typography.bodyMedium
                )
            }
        },
        leadingContent = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (statusText != null) {
                    Text(
                        text = statusText,
                        style = LiteverTheme.typography.bodyMedium,
                        color = statusColor ?: LiteverTheme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                trailingContent?.invoke()
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    )
}

@Composable
fun LiteverSettingsSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtitle: String? = null,
) {
    LiteverSettingsItem(
        title = title,
        icon = icon,
        subtitle = subtitle,
        trailingContent = {
            LiteverSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        },
        onClick = { onCheckedChange(!checked) },
        modifier = modifier
    )
}

@Composable
fun LiteverSettingsGroup(
    title: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        if (title != null) {
            LiteverSettingsCategory(
                title = title,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = LiteverShapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.3f)
            ),
            border = BorderStroke(1.dp, LiteverTheme.colors.outlineVariant.copy(alpha = 0.2f)),
            content = content
        )
    }
}
