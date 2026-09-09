package vn.io.litever.remind.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import vn.io.litever.designsystem.theme.LiteverColors
import vn.io.litever.designsystem.theme.LiteverShapes
import vn.io.litever.designsystem.theme.LiteverSpacing
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.designsystem.theme.LiteverTypography
import vn.io.litever.designsystem.theme.defaultLiteverTypography

/**
 * ReMind application theme composable that configures and delegates to [LiteverTheme].
 *
 * It bridges ReMind's brand color schemes ([remindLightColors], [remindDarkColors]) to Litever v2.0.0's
 * lean architecture, providing native Jetpack Compose Material 3 MaterialTheme alongside Litever design tokens
 * ([LiteverTheme.colors], [LiteverTheme.typography], [LiteverTheme.spacing], and [LiteverTheme.shapes]).
 *
 * @param darkTheme Whether dark theme should be applied. Defaults to system dark theme state.
 * @param dynamicColor Whether dynamic system coloring (Android 12+) should be used.
 * @param colorPalette Color palette selection: "REMIND" (default), "LITEVER", or "DYNAMIC".
 * @param colorScheme Optional custom Material 3 [ColorScheme]. If supplied, it takes precedence for MaterialTheme.
 * @param colors Optional custom [LiteverColors] container. If supplied, it takes precedence for Litever token locals.
 * @param typography Typography specifications, defaulting to [defaultLiteverTypography].
 * @param spacing Spacing scale tokens, defaulting to [LiteverSpacing].
 * @param shapes Corner shape definitions, defaulting to [LiteverShapes].
 * @param content Composable child hierarchy.
 */
@Composable
fun ReMindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    colorPalette: String = "REMIND",
    colorScheme: ColorScheme? = null,
    colors: LiteverColors? = null,
    typography: LiteverTypography = defaultLiteverTypography,
    spacing: LiteverSpacing = LiteverSpacing(),
    shapes: Shapes = LiteverShapes,
    content: @Composable () -> Unit
) {
    val isDynamic = colorPalette == "DYNAMIC" || dynamicColor

    val resolvedColors: LiteverColors? = when {
        colors != null -> colors
        colorScheme != null -> null
        isDynamic -> null
        colorPalette == "LITEVER" -> null
        darkTheme -> remindDarkColors
        else -> remindLightColors
    }

    LiteverTheme(
        colorScheme = colorScheme,
        colors = resolvedColors,
        typography = typography,
        spacing = spacing,
        shapes = shapes,
        darkTheme = darkTheme,
        dynamicColor = isDynamic,
        content = content
    )
}
