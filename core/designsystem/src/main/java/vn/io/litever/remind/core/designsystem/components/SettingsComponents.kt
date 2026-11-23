package vn.io.litever.remind.core.designsystem.components

import androidx.compose.material3.MaterialTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme

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
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .fillMaxWidth()
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
    val spacing = MaterialTheme.spacing
    ReMindGroupCard {
        Column(modifier = modifier.padding()) {
            if (title != null) {
                ReMindSettingsCategory(
                    title = title,
                    modifier = Modifier.padding(
                        top = spacing.medium,
                        bottom = spacing.extraSmall,
                        start = spacing.medium,
                        end = spacing.medium
                    )
                )
            }
            content()
        }
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
        supportingContent = subtitle?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
                )
            }
        },
        leadingContent = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(MaterialTheme.spacing.large),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
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
            .clip(MaterialTheme.shapes.large)
            .then(
                if (onClick != null) Modifier.clickable(
                    enabled = enabled,
                    onClick = onClick
                ) else Modifier
            )
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
            )
            if (statusText != null) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium,
                    color = (statusColor ?: MaterialTheme.colorScheme.primary).copy(alpha = alpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReMindSettingsGroupPreview() {
    ReMindTheme {
        ReMindSettingsGroup(title = "Account & Security") {
            ReMindSettingsItem(
                title = "Notifications",
                subtitle = "Sound, vibration and alerts",
                icon = ReMindIcons.Notifications,
                trailingContent = {
                    Icon(
                        imageVector = ReMindIcons.ChevronRight,
                        contentDescription = null
                    )
                }
            )
            ReMindSettingsItem(
                title = "Theme",
                subtitle = "Dark, light or system default",
                icon = ReMindIcons.Palette,
                statusText = "System",
                trailingContent = {
                    Icon(
                        imageVector = ReMindIcons.ChevronRight,
                        contentDescription = null
                    )
                }
            )
        }
    }
}

