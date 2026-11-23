package vn.io.litever.remind.core.designsystem.theme

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import vn.io.litever.brand.tokens.LiteverColors
import vn.io.litever.brand.tokens.LiteverShapes
import vn.io.litever.brand.tokens.LiteverSpacing
import vn.io.litever.brand.tokens.LiteverTypography
import vn.io.litever.brand.tokens.LocalLiteverColors
import vn.io.litever.brand.tokens.LocalLiteverSpacing
import vn.io.litever.brand.tokens.LocalLiteverTypography
import vn.io.litever.brand.tokens.asLiteverColors
import vn.io.litever.brand.tokens.asMaterial3
import vn.io.litever.brand.tokens.defaultLiteverTypography
import vn.io.litever.brand.tokens.palettes.blackDarkColorScheme
import vn.io.litever.brand.tokens.palettes.blackLightColorScheme
import vn.io.litever.brand.tokens.palettes.blueDarkColorScheme
import vn.io.litever.brand.tokens.palettes.blueLightColorScheme
import vn.io.litever.brand.tokens.palettes.greenDarkColorScheme
import vn.io.litever.brand.tokens.palettes.greenLightColorScheme
import vn.io.litever.brand.tokens.palettes.indigoDarkColorScheme
import vn.io.litever.brand.tokens.palettes.indigoLightColorScheme
import vn.io.litever.brand.tokens.palettes.orangeDarkColorScheme
import vn.io.litever.brand.tokens.palettes.orangeLightColorScheme
import vn.io.litever.brand.tokens.palettes.redDarkColorScheme
import vn.io.litever.brand.tokens.palettes.redLightColorScheme
import vn.io.litever.brand.tokens.palettes.violetDarkColorScheme
import vn.io.litever.brand.tokens.palettes.violetLightColorScheme
import vn.io.litever.brand.tokens.palettes.yellowDarkColorScheme
import vn.io.litever.brand.tokens.palettes.yellowLightColorScheme

/**
 * App-owned theme entry point for ReMind.
 *
 * Directly configures [MaterialTheme] and provides Litever token [CompositionLocal]s
 * without coupling to an external theme wrapper composable. Consumes only raw color palettes,
 * bundled fonts, and spacing/shape tokens from the shared brand resource kit.
 *
 * @param darkTheme Whether dark theme should be applied. Defaults to system dark theme state.
 * @param dynamicColor Whether dynamic system coloring (Android 12+) should be used.
 * @param colorPalette Color palette selection: "BLACK", "RED", "ORANGE", "YELLOW", "GREEN", "BLUE", "INDIGO", "VIOLET", or "DYNAMIC".
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
    val context = LocalContext.current
    val isDynamic = colorPalette == "DYNAMIC" || dynamicColor

    val predefinedColorScheme = when (colorPalette) {
        "BLACK" -> if (darkTheme) blackDarkColorScheme else blackLightColorScheme
        "RED" -> if (darkTheme) redDarkColorScheme else redLightColorScheme
        "ORANGE" -> if (darkTheme) orangeDarkColorScheme else orangeLightColorScheme
        "YELLOW" -> if (darkTheme) yellowDarkColorScheme else yellowLightColorScheme
        "GREEN" -> if (darkTheme) greenDarkColorScheme else greenLightColorScheme
        "BLUE" -> if (darkTheme) blueDarkColorScheme else blueLightColorScheme
        "INDIGO" -> if (darkTheme) indigoDarkColorScheme else indigoLightColorScheme
        "VIOLET" -> if (darkTheme) violetDarkColorScheme else violetLightColorScheme
        else -> if (darkTheme) blackDarkColorScheme else blackLightColorScheme
    }

    val effectiveColorScheme: ColorScheme = when {
        colorScheme != null -> colorScheme
        colors != null -> colors.asMaterial3()
        isDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> predefinedColorScheme
    }

    val effectiveLiteverColors: LiteverColors = colors
        ?: effectiveColorScheme.asLiteverColors(isLight = !darkTheme)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                window.statusBarColor = android.graphics.Color.TRANSPARENT

                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    val currentConfiguration = LocalConfiguration.current
    val themedConfiguration = remember(currentConfiguration, darkTheme) {
        Configuration(currentConfiguration).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                if (darkTheme) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
        }
    }

    CompositionLocalProvider(
        LocalConfiguration provides themedConfiguration,
        LocalLiteverColors provides effectiveLiteverColors,
        LocalLiteverTypography provides typography,
        LocalLiteverSpacing provides spacing
    ) {
        MaterialTheme(
            colorScheme = effectiveColorScheme,
            typography = typography.asMaterial3(),
            shapes = shapes,
            content = content
        )
    }
}
