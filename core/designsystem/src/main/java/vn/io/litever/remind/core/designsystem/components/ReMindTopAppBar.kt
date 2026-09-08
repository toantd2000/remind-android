package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import vn.io.litever.designsystem.components.LiteverNavigationIconType
import vn.io.litever.designsystem.components.LiteverTopAppBar

/**
 * Project-level TopAppBar wrapper extending [LiteverTopAppBar].
 * Provides convenient [onBackClick] support while respecting Litever M3 standards.
 * Defaults [navigationIconType] to null when [onBackClick] is null.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReMindTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    navigationIconType: LiteverNavigationIconType = LiteverNavigationIconType.Back,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets
) {
    if (onBackClick != null) {
        LiteverTopAppBar(
            title = title,
            modifier = modifier,
            subtitle = subtitle,
            navigationIconType = navigationIconType,
            onNavigationClick = onBackClick,
            actions = actions,
            windowInsets = windowInsets
        )
    } else {
        LiteverTopAppBar(
            title = title,
            modifier = modifier,
            subtitle = subtitle,
            actions = actions,
            windowInsets = windowInsets
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReMindTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets
) {
    LiteverTopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        windowInsets = windowInsets
    )
}
