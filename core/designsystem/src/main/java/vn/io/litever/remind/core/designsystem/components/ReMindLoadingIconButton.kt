package vn.io.litever.remind.core.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.theme.LiteverTheme

/**
 * A standard IconButton that automatically transitions between an [icon] and a loading spinner.
 *
 * @param onClick Called when the button is clicked.
 * @param icon The icon to display when not loading.
 * @param loading Whether the button should display a loading indicator.
 * @param modifier The [Modifier] to be applied to this button.
 * @param contentDescription The content description for the icon.
 * @param enabled Whether the button is enabled. Note that it is automatically disabled when [loading] is true.
 */
@Composable
fun ReMindLoadingIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    loading: Boolean,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !loading
    ) {
        AnimatedContent(
            targetState = loading,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
            },
            label = "LoadingIconButtonAnimation"
        ) { isLoading ->
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = LiteverTheme.colors.primary
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription
                )
            }
        }
    }
}
