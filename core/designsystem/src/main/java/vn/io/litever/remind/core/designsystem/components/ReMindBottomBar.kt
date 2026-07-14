package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.designsystem.components.LiteverButton
import vn.io.litever.designsystem.components.LiteverOutlinedButton
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
        modifier = modifier
            .fillMaxWidth(),
        color = LiteverTheme.colors.surfaceContainer,
        tonalElevation = 1.dp,
        shadowElevation = 4.dp,
        shape = shape
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReMindBottomBarPreview() {
    ReMindTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Single button
            ReMindBottomBar {
                LiteverButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Save Changes")
                }
            }

            // Dual buttons - Automatically match heights
            ReMindBottomBar {
                LiteverOutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                LiteverButton(
                    onClick = {},
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}
