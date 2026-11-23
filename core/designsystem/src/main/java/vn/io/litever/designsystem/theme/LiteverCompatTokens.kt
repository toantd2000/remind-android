package vn.io.litever.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import vn.io.litever.brand.tokens.LiteverTokens
import vn.io.litever.brand.tokens.asLiteverColors as brandAsLiteverColors
import vn.io.litever.brand.tokens.asMaterial3 as brandAsMaterial3
import vn.io.litever.brand.tokens.defaultLiteverTypography as brandDefaultTypography
import vn.io.litever.brand.tokens.LiteverShapes as BrandShapes
import vn.io.litever.brand.tokens.LocalLiteverColors as BrandLocalColors
import vn.io.litever.brand.tokens.LocalLiteverSpacing as BrandLocalSpacing
import vn.io.litever.brand.tokens.LocalLiteverTypography as BrandLocalTypography

typealias LiteverColors = vn.io.litever.brand.tokens.LiteverColors
typealias LiteverTypography = vn.io.litever.brand.tokens.LiteverTypography
typealias LiteverSpacing = vn.io.litever.brand.tokens.LiteverSpacing
typealias LiteverThemeColor = vn.io.litever.brand.tokens.LiteverThemeColor

val LocalLiteverColors get() = BrandLocalColors
val LocalLiteverTypography get() = BrandLocalTypography
val LocalLiteverSpacing get() = BrandLocalSpacing
val defaultLiteverTypography get() = brandDefaultTypography
val LiteverShapes: Shapes get() = BrandShapes

fun LiteverColors.asMaterial3(): ColorScheme = brandAsMaterial3()

fun ColorScheme.asLiteverColors(isLight: Boolean = true): LiteverColors =
    brandAsLiteverColors(isLight = isLight)

/**
 * Compatibility accessor delegating to [LiteverTokens] from `litever-brand`.
 * Kept so existing feature modules continue compiling without mass-rewriting imports.
 */
object LiteverTheme {
    val colors: LiteverColors
        @Composable
        @ReadOnlyComposable
        get() = LiteverTokens.colors

    val typography: LiteverTypography
        @Composable
        @ReadOnlyComposable
        get() = LiteverTokens.typography

    val spacing: LiteverSpacing
        @Composable
        @ReadOnlyComposable
        get() = LiteverTokens.spacing

    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = LiteverTokens.shapes
}
