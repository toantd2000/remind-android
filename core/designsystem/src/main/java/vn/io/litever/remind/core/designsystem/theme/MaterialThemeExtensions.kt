package vn.io.litever.remind.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import vn.io.litever.brand.tokens.LiteverSpacing
import vn.io.litever.brand.tokens.LiteverTokens

/**
 * Semantic and brand colors not included in standard Material 3 [androidx.compose.material3.ColorScheme].
 */
@Immutable
data class ReMindSemanticColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val brandLite: Color,
    val brandVer: Color,
)

/**
 * Spacing scale tokens from `litever-brand`, accessed via [MaterialTheme].
 */
val MaterialTheme.spacing: LiteverSpacing
    @Composable
    @ReadOnlyComposable
    get() = LiteverTokens.spacing

/**
 * Semantic and brand colors from `litever-brand`, accessed via [MaterialTheme].
 */
val MaterialTheme.semanticColors: ReMindSemanticColors
    @Composable
    @ReadOnlyComposable
    get() {
        val colors = LiteverTokens.colors
        return ReMindSemanticColors(
            success = colors.success,
            onSuccess = colors.onSuccess,
            successContainer = colors.successContainer,
            onSuccessContainer = colors.onSuccessContainer,
            warning = colors.warning,
            onWarning = colors.onWarning,
            warningContainer = colors.warningContainer,
            onWarningContainer = colors.onWarningContainer,
            brandLite = colors.brandLite,
            brandVer = colors.brandVer,
        )
    }
