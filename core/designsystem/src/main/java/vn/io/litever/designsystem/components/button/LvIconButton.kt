package vn.io.litever.designsystem.components.button

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme

/**
 * Standard opinionated IconButton for LiteVer applications.
 *
 * Enforces LiteVer squircle shape (6.dp) instead of Material 3's circular pill default,
 * supporting visual [LvButtonType]s (Filled, Tonal, Outlined, Text) and [LvSemantic] intents.
 *
 * @param onClick Called when the icon button is clicked.
 * @param modifier The modifier to be applied to the button.
 * @param type The visual archetype (Filled, Tonal, Outlined, Text).
 * @param semantic The color intent (Primary, Secondary, Success, Destructive, Warning).
 * @param enabled Controls the enabled state of the button.
 * @param shape Shape of the icon button, defaulting to [LiteVerButtonDefaults.shape] (6.dp).
 * @param colors Optional custom [IconButtonColors] override.
 * @param interactionSource The [MutableInteractionSource] representing the stream of interactions.
 * @param content The composable child content (typically an Icon).
 */
@Composable
fun LvIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: LvButtonType = LvButtonType.Text,
    semantic: LvSemantic = LvSemantic.Text,
    enabled: Boolean = true,
    shape: Shape = LiteVerButtonDefaults.shape,
    colors: IconButtonColors = LiteVerButtonDefaults.resolveIconButtonColors(type = type, semantic = semantic),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }

    when (type) {
        LvButtonType.Filled -> {
            FilledIconButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = shape,
                colors = colors,
                interactionSource = interactionSource,
                content = content
            )
        }
        LvButtonType.Tonal -> {
            FilledTonalIconButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = shape,
                colors = colors,
                interactionSource = interactionSource,
                content = content
            )
        }
        LvButtonType.Outlined -> {
            val border = LiteVerButtonDefaults.resolveBorder(type = type, semantic = semantic, enabled = enabled)
            OutlinedIconButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                shape = shape,
                colors = colors,
                border = border,
                interactionSource = interactionSource,
                content = content
            )
        }
        LvButtonType.Text -> {
            IconButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                colors = colors,
                shape = shape,
                interactionSource = interactionSource,
                content = content
            )
        }
    }
}

@Preview(showBackground = true, name = "LvIconButton Matrix - Light")
@Composable
fun LvIconButtonLightPreview() {
    ReMindTheme(darkTheme = false) {
        Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LvIconButton(onClick = {}, type = LvButtonType.Filled) {
                    Text("âœ“")
                }
                LvIconButton(onClick = {}, type = LvButtonType.Tonal, semantic = LvSemantic.Success) {
                    Text("âœ“")
                }
                LvIconButton(onClick = {}, type = LvButtonType.Outlined, semantic = LvSemantic.Destructive) {
                    Text("âœ•")
                }
                LvIconButton(onClick = {}, type = LvButtonType.Text, semantic = LvSemantic.Primary) {
                    Text("?")
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "LvIconButton Matrix - Dark")
@Composable
fun LvIconButtonDarkPreview() {
    ReMindTheme(darkTheme = true) {
        Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LvIconButton(onClick = {}, type = LvButtonType.Filled) {
                    Text("âœ“")
                }
                LvIconButton(onClick = {}, type = LvButtonType.Tonal, semantic = LvSemantic.Success) {
                    Text("âœ“")
                }
                LvIconButton(onClick = {}, type = LvButtonType.Outlined, semantic = LvSemantic.Destructive) {
                    Text("âœ•")
                }
                LvIconButton(onClick = {}, type = LvButtonType.Text, semantic = LvSemantic.Primary) {
                    Text("?")
                }
            }
        }
    }
}
