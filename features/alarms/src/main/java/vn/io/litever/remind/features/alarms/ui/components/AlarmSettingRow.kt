package vn.io.litever.remind.features.alarms.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.remind.core.designsystem.components.ReMindSettingIcon
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme

/**
 * Reusable setting row component for feature:alarms using Material 3 [ListItem].
 *
 * Unifies the layout structure across setting items:
 * [Leading Icon (`ReMindSettingIcon`)] - [Title & Subtitle Column] - [Trailing Content / Chevron]
 */
@Composable
fun AlarmSettingRow(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    iconSelected: Boolean = false,
    onIconClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = {
        if (onClick != null) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
    },
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = subtitle?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        leadingContent = {
            ReMindSettingIcon(
                imageVector = icon,
                selected = iconSelected,
                enabled = enabled,
                onClick = onIconClick,
            )
        },
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .then(
                if ((onClick != null) && enabled) Modifier.clickable(onClick = onClick)
                else Modifier,
            ),
    )
}

@Preview(showBackground = true)
@Composable
private fun AlarmSettingRowPreview() {
    ReMindTheme {
        Column {
            AlarmSettingRow(
                title = "Snooze",
                subtitle = "5 minutes, 3 times",
                icon = Icons.Rounded.Snooze,
                onClick = {},
            )
        }
    }
}
