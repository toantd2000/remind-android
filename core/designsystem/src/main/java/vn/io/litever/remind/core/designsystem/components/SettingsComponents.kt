package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.theme.LiteverTheme

/**
 * Category header for settings groups.
 */
@Composable
fun ReMindSettingsCategory(
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
            .padding(vertical = LiteverTheme.spacing.small)
    )
}

/**
 * Container card grouping related settings items.
 */
@Composable
fun ReMindSettingsGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = LiteverTheme.spacing
    Column(modifier = modifier.padding(horizontal = spacing.medium, vertical = spacing.small)) {
        if (title != null) {
            ReMindSettingsCategory(
                title = title,
                modifier = Modifier.padding(horizontal = spacing.extraSmall)
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = LiteverTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = LiteverTheme.colors.surfaceVariant.copy(alpha = 0.3f)
            ),
            border = BorderStroke(1.dp, LiteverTheme.colors.outlineVariant.copy(alpha = 0.2f)),
            content = content
        )
    }
}

/**
 * Standard settings row item with title, optional subtitle, icon, status text, and trailing content.
 */
@Composable
fun ReMindSettingsItem(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtitle: String? = null,
    statusText: String? = null,
    statusColor: Color? = null,
    enabled: Boolean = true,
    alpha: Float = if (enabled) 1f else 0.38f,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = {
            Column {
                Text(
                    text = title,
                    style = LiteverTheme.typography.bodyLarge,
                    color = LiteverTheme.colors.onSurface.copy(alpha = alpha)
                )
                if (statusText != null) {
                    Text(
                        text = statusText,
                        style = LiteverTheme.typography.labelMedium,
                        color = (statusColor ?: LiteverTheme.colors.primary).copy(alpha = alpha),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        supportingContent = subtitle?.let {
            {
                Text(
                    text = it,
                    style = LiteverTheme.typography.bodyMedium,
                    color = LiteverTheme.colors.onSurfaceVariant.copy(alpha = alpha)
                )
            }
        },
        leadingContent = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(LiteverTheme.spacing.large),
                    tint = LiteverTheme.colors.onSurfaceVariant.copy(alpha = alpha)
                )
            }
        },
        trailingContent = {
            CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.copy(alpha = alpha)) {
                trailingContent?.invoke()
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier)
    )
}

// Backward-compatibility aliases
@Composable
fun LiteverSettingsGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) = ReMindSettingsGroup(modifier = modifier, title = title, content = content)

@Composable
fun LiteverSettingsItem(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtitle: String? = null,
    statusText: String? = null,
    statusColor: Color? = null,
    enabled: Boolean = true,
    alpha: Float = if (enabled) 1f else 0.38f,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) = ReMindSettingsItem(
    title = title,
    modifier = modifier,
    icon = icon,
    subtitle = subtitle,
    statusText = statusText,
    statusColor = statusColor,
    enabled = enabled,
    alpha = alpha,
    onClick = onClick,
    trailingContent = trailingContent
)

@Composable
fun LiteverSettingsCategory(
    title: String,
    modifier: Modifier = Modifier
) = ReMindSettingsCategory(title = title, modifier = modifier)
