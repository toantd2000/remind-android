package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.remind.core.designsystem.icon.ReMindIcons
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing

/**
 * Category header for settings groups.
 */
@Composable
fun ReMindSettingsCategory(
    title: String,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        Box(
            modifier = Modifier
                .size(MaterialTheme.spacing.small)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        trailingContent?.invoke()
    }
}

/**
 * Container card grouping related settings items.
 */
@Composable
fun ReMindSettingsGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    headerTrailingContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = MaterialTheme.spacing
    ReMindGroupCard {
        Column(modifier = modifier.padding()) {
            if (title != null) {
                ReMindSettingsCategory(
                    title = title,
                    trailingContent = headerTrailingContent,
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
 * Standard settings row item with title, optional inline title badge, subtitle, icon,
 * status text, and trailing content.
 *
 * Placing badges inside [titleBadge] keeps them on the same row as [title] so [subtitle]
 * can span the full width of the item without being squeezed by [trailingContent].
 */
@Composable
fun ReMindSettingsItem(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    subtitle: String? = null,
    statusText: String? = null,
    statusColor: Color? = null,
    enabled: Boolean = true,
    alpha: Float = if (enabled) 1f else 0.38f,
    showChevron: Boolean = false,
    onClick: (() -> Unit)? = null,
    titleBadge: (@Composable () -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    val effectiveTrailing: (@Composable () -> Unit)? = when {
        trailingContent != null -> {
            {
                CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.copy(alpha = alpha)) {
                    trailingContent()
                }
            }
        }
        showChevron -> {
            {
                Icon(
                    imageVector = ReMindIcons.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f * alpha)
                )
            }
        }
        else -> null
    }

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
                    tint = (iconTint ?: MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = alpha)
                )
            }
        },
        trailingContent = effectiveTrailing,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                titleBadge?.invoke()
            }
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

/**
 * Subtle 0.5.dp hairline divider for separating items inside a [ReMindSettingsGroup].
 *
 * When [inset] is true (default), aligns with the leading edge of item text (`56.dp`)
 * so the leading icon column flows cleanly without heavy horizontal breaks.
 */
@Composable
fun ReMindSettingsDivider(
    modifier: Modifier = Modifier,
    inset: Boolean = true
) {
    HorizontalDivider(
        modifier = modifier.padding(
            start = if (inset) 56.dp else MaterialTheme.spacing.medium,
            end = MaterialTheme.spacing.medium
        ),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    )
}

@Preview(showBackground = true)
@Composable
private fun ReMindSettingsGroupPreview() {
    ReMindTheme {
        ReMindSettingsGroup(title = "Account & Security") {
            ReMindSettingsItem(
                title = "Notifications",
                subtitle = "Sound, vibration and alerts",
                icon = ReMindIcons.Notifications
            )
            ReMindSettingsItem(
                title = "Theme",
                subtitle = "Dark, light or system default",
                icon = ReMindIcons.Palette,
                statusText = "System"
            )
        }
    }
}
