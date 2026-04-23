package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsCategory(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
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
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = subtitle?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = statusColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
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
fun SettingsSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtitle: String? = null,
) {
    SettingsItem(
        title = title,
        icon = icon,
        subtitle = subtitle,
        trailingContent = {
            ReMindSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        },
        onClick = { onCheckedChange(!checked) },
        modifier = modifier
    )
}

@Composable
fun ReMindSettingsGroup(
    title: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        if (title != null) {
            SettingsCategory(
                title = title,
                modifier = Modifier.padding(horizontal = 4.dp) // Thêm một chút padding để chữ không sát mép card
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
            content = content
        )
    }
}
