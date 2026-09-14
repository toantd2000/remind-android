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

import vn.io.litever.designsystem.theme.LiteverThemeColor

/**
 * ReMind application theme composable that configures and delegates to [LiteverTheme].
 *
 * Defaults to the RED palette from Litever Design System, supporting dynamic theming across
 * all 7 predefined Litever color palettes (RED, ORANGE, YELLOW, GREEN, BLUE, INDIGO, VIOLET)
 * and Android 12+ wallpaper dynamic coloring.
 *
 * @param darkTheme Whether dark theme should be applied. Defaults to system dark theme state.
 * @param dynamicColor Whether dynamic system coloring (Android 12+) should be used.
 * @param colorPalette Color palette selection: "RED" (default), "ORANGE", "YELLOW", "GREEN", "BLUE", "INDIGO", "VIOLET", or "DYNAMIC".
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
    colorPalette: String = "RED",
    colorScheme: ColorScheme? = null,
    colors: LiteverColors? = null,
    typography: LiteverTypography = defaultLiteverTypography,
    spacing: LiteverSpacing = LiteverSpacing(),
    shapes: Shapes = LiteverShapes,
    content: @Composable () -> Unit
) {
    val isDynamic = colorPalette == "DYNAMIC" || dynamicColor

    val resolvedThemeColor = when (colorPalette) {
        "RED" -> LiteverThemeColor.RED
        "ORANGE" -> LiteverThemeColor.ORANGE
        "YELLOW" -> LiteverThemeColor.YELLOW
        "GREEN" -> LiteverThemeColor.GREEN
        "BLUE" -> LiteverThemeColor.BLUE
        "INDIGO" -> LiteverThemeColor.INDIGO
        "VIOLET" -> LiteverThemeColor.VIOLET
        else -> LiteverThemeColor.RED
    }

    LiteverTheme(
        themeColor = resolvedThemeColor,
        colorScheme = colorScheme,
        colors = colors,
        typography = typography,
        spacing = spacing,
        shapes = shapes,
        darkTheme = darkTheme,
        dynamicColor = isDynamic,
        content = content
    )
}
