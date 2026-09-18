package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.button.LvButtonType
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme

/**
 * A premium bottom bar for main actions.
 * @param modifier Modifier for the bar.
 * @param shape Shape of the bar container. Default is Rectangle for a seamless look.
 * @param windowInsets Window insets to apply as padding. Defaults to safe drawing horizontal and bottom insets (navigation bars + IME).
 * @param content Slot for buttons or other actions.
 */
@Composable
fun ReMindBottomBar(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    windowInsets: WindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = LiteverTheme.colors.surfaceContainerHigh,
        shape = shape
    ) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(windowInsets)
                .padding(
                    vertical = LiteverTheme.spacing.smallMedium,
                    horizontal = LiteverTheme.spacing.medium
                )
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.smallMedium),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReMindBottomBarPreview() {
    ReMindTheme {
        Column(verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)) {
            // Single button
            ReMindBottomBar {
                LvButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(LiteverTheme.spacing.doubleLarge)
                ) {
                    Text("Save Changes")
                }
            }

            // Dual buttons - Automatically match heights
            ReMindBottomBar {
                LvButton(
                    onClick = {},
                    type = LvButtonType.Outlined,
                    semantic = LvSemantic.Secondary,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                LvButton(
                    onClick = {},
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}
