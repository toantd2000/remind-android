package vn.io.litever.designsystem.components.button

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme

/**
 * High-level opinionated Button for LiteVer applications.
 *
 * Enforces LiteVer squircle shape (6.dp) and provides seamless styling across
 * visual [LvButtonType]s and [LvSemantic] intents without repetitive parameter passing.
 * Under the hood, it utilizes standard Material 3 [Button] preserving gestures, ripple, and accessibility.
 *
 * @param onClick Called when the button is clicked.
 * @param modifier The modifier to be applied to the button.
 * @param type The visual styling archetype (Filled, Outlined, Text, Tonal).
 * @param semantic The color intent (Primary, Secondary, Success, Destructive, Warning).
 * @param enabled Controls the enabled state of the button.
 * @param shape Shape of the button, defaulting to [LiteVerButtonDefaults.shape].
 * @param colors Optional custom [ButtonColors] override.
 * @param contentPadding Custom padding within the button.
 * @param interactionSource The [MutableInteractionSource] representing the stream of interactions.
 * @param content The composable child content (icon, text).
 */
@Composable
fun LvButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: LvButtonType = LvButtonType.Filled,
    semantic: LvSemantic = LvSemantic.Primary,
    enabled: Boolean = true,
    shape: Shape = LiteVerButtonDefaults.shape,
    colors: ButtonColors = LiteVerButtonDefaults.resolveColors(type = type, semantic = semantic),
    contentPadding: PaddingValues = when (type) {
        LvButtonType.Text -> LiteVerButtonDefaults.TextButtonContentPadding
        else -> LiteVerButtonDefaults.ContentPadding
    },
    elevation: ButtonElevation? = when (type) {
        LvButtonType.Text, LvButtonType.Outlined -> null
        else -> ButtonDefaults.buttonElevation()
    },
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = LiteVerButtonDefaults.resolveBorder(type = type, semantic = semantic, enabled = enabled),
        contentPadding = contentPadding,
        interactionSource = interactionSource
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.copy(
                fontWeight = FontWeight.Bold
            )
        ) {
            content()
        }
    }
}

@Preview(showBackground = true, name = "LvButton Matrix - Light")
@Composable
fun LvButtonLightPreview() {
    ReMindTheme(darkTheme = false) {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LvButton(onClick = {}) { Text("Primary Filled") }
                LvButton(onClick = {}, semantic = LvSemantic.Success) { Text("Success Filled") }
                LvButton(onClick = {}, type = LvButtonType.Outlined, semantic = LvSemantic.Destructive) { Text("Destructive Outlined") }
                LvButton(onClick = {}, type = LvButtonType.Tonal, semantic = LvSemantic.Warning) { Text("Warning Tonal") }
                LvButton(onClick = {}, type = LvButtonType.Text, semantic = LvSemantic.Secondary) { Text("Secondary Text") }
                LvButton(onClick = {}, enabled = false) { Text("Disabled Button") }
            }
        }
    }
}

@Preview(showBackground = true, name = "LvButton Matrix - Dark")
@Composable
fun LvButtonDarkPreview() {
    ReMindTheme(darkTheme = true) {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LvButton(onClick = {}) { Text("Primary Filled") }
                LvButton(onClick = {}, semantic = LvSemantic.Success) { Text("Success Filled") }
                LvButton(onClick = {}, type = LvButtonType.Outlined, semantic = LvSemantic.Destructive) { Text("Destructive Outlined") }
                LvButton(onClick = {}, type = LvButtonType.Tonal, semantic = LvSemantic.Warning) { Text("Warning Tonal") }
                LvButton(onClick = {}, type = LvButtonType.Text, semantic = LvSemantic.Secondary) { Text("Secondary Text") }
            }
        }
    }
}
