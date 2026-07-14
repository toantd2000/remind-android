package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.LiteverFilledTonalIconButton

@Composable
fun ReMindSettingIcon(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val finalModifier = modifier.size(44.dp)

    if (onClick != null && enabled) {
        LiteverFilledTonalIconButton(
            onClick = onClick,
            modifier = finalModifier,
            shape = MaterialTheme.shapes.medium,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = containerColor,
                contentColor = contentColor
            )
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    } else {
        Box(
            modifier = finalModifier
                .clip(MaterialTheme.shapes.medium)
                .background(if (enabled) containerColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (enabled) contentColor else contentColor.copy(alpha = 0.38f)
            )
        }
    }
}
