package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Menu
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
import vn.io.litever.designsystem.components.button.LvIconButton
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.R

/**
 * Navigation icon types for [LvTopAppBar].
 */
enum class LvNavigationIconType {
    Back, Close, Menu
}

/**
 * Backward compatibility alias for [LvNavigationIconType].
 */
typealias ReMindNavigationIconType = LvNavigationIconType

/**
 * Custom TopAppBar component in `:core:designsystem` extending Material 3 [TopAppBar].
 * Supports title, navigation icon (`Back`, `Close`, `Menu`), and action slots.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LvTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    navigationIconType: LvNavigationIconType = LvNavigationIconType.Back,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = LiteverTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (onBackClick != null) {
                val icon = when (navigationIconType) {
                    LvNavigationIconType.Back -> Icons.AutoMirrored.Rounded.ArrowBack
                    LvNavigationIconType.Close -> Icons.Rounded.Close
                    LvNavigationIconType.Menu -> Icons.Rounded.Menu
                }
                val contentDesc = when (navigationIconType) {
                    LvNavigationIconType.Back -> stringResource(R.string.action_back)
                    LvNavigationIconType.Close -> "Close"
                    LvNavigationIconType.Menu -> "Menu"
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
 * Overload of [LvTopAppBar] accepting a custom [title] composable slot.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LvTopAppBar(
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
