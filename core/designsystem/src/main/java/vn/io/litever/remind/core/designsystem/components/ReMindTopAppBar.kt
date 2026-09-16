package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.designsystem.components.button.LvIconButton
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.R
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme

/**
 * Navigation icon types for [ReMindTopAppBar].
 */
enum class ReMindNavigationIconType {
    Back, Close, Menu
}

/**
 * Backward compatibility alias for [ReMindNavigationIconType].
 */
typealias LiteverNavigationIconType = ReMindNavigationIconType

/**
 * Project-level TopAppBar wrapper extending Material 3 [TopAppBar].
 * Provides convenient [onBackClick] support while respecting Litever M3 standards.
 * Defaults navigation icon to null when [onBackClick] is null.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReMindTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    navigationIconType: ReMindNavigationIconType = ReMindNavigationIconType.Back,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = LiteverTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = LiteverTheme.typography.bodyMedium,
                        color = LiteverTheme.colors.onSurfaceVariant
                    )
                }
            }
        },
        modifier = modifier,
        navigationIcon = {
            if (onBackClick != null) {
                val icon = when (navigationIconType) {
                    ReMindNavigationIconType.Back -> Icons.AutoMirrored.Rounded.ArrowBack
                    ReMindNavigationIconType.Close -> Icons.Rounded.Close
                    ReMindNavigationIconType.Menu -> Icons.Rounded.Menu
                }
                val contentDesc = when (navigationIconType) {
                    ReMindNavigationIconType.Back -> stringResource(R.string.action_back)
                    ReMindNavigationIconType.Close -> "Close"
                    ReMindNavigationIconType.Menu -> "Menu"
                }
                LvIconButton(
                    onClick = onBackClick,
                    semantic = LvSemantic.Neutral
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDesc
                    )
                }
            }
        },
        actions = actions,
        windowInsets = windowInsets,
        colors = colors,
        scrollBehavior = scrollBehavior
    )
}

/**
 * Overload of [ReMindTopAppBar] accepting a custom [title] composable slot.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReMindTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        windowInsets = windowInsets,
        colors = colors,
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun ReMindTopAppBarPreview() {
    ReMindTheme {
        Column {
            ReMindTopAppBar(
                title = "Title",
                subtitle = "Subtitle",
                onBackClick = {},
                actions = {
                    LvIconButton(
                        onClick = {},
                        semantic = LvSemantic.Neutral
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                }
            )
            ReMindTopAppBar(
                title = "Settings",
                onBackClick = {}
            )
        }
    }
}
