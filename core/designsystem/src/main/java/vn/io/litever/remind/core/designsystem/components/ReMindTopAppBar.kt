package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import vn.io.litever.designsystem.components.LiteverTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReMindTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    LiteverTopAppBar(
        title = title,
        modifier = modifier,
        onBackClick = onBackClick,
        actions = actions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainReMindTopAppBar(
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    LiteverTopAppBar(
        titleContent = { ReMindLogo() },
        modifier = modifier,
        actions = actions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReMindTopAppBar(
    titleContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    LiteverTopAppBar(
        titleContent = titleContent,
        modifier = modifier,
        onBackClick = onBackClick,
        actions = actions
    )
}
