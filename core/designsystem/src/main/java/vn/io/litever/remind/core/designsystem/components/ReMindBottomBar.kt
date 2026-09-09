package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.designsystem.components.LiteVerButtonDefaults
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme

/**
 * A premium bottom bar for main actions.
 * @param modifier Modifier for the bar.
 * @param shape Shape of the bar container. Default is Rectangle for a seamless look.
 * @param content Slot for buttons or other actions.
 */
@Composable
fun ReMindBottomBar(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = LiteverTheme.colors.surfaceContainer,
        shape = shape
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(
                    horizontal = LiteverTheme.spacing.large,
                    vertical = LiteverTheme.spacing.medium
                )
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium),
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
                Button(
                    onClick = {},
                    shape = LiteVerButtonDefaults.shape,
                    colors = LiteVerButtonDefaults.primaryColors(),
                    modifier = Modifier.fillMaxWidth().height(LiteverTheme.spacing.doubleLarge)
                ) {
                    Text("Save Changes")
                }
            }

            // Dual buttons - Automatically match heights
            ReMindBottomBar {
                OutlinedButton(
                    onClick = {},
                    shape = LiteVerButtonDefaults.shape,
                    colors = LiteVerButtonDefaults.outlinedColors(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {},
                    shape = LiteVerButtonDefaults.shape,
                    colors = LiteVerButtonDefaults.primaryColors(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}
