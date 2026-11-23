package vn.io.litever.remind.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import vn.io.litever.brand.tokens.LiteverTokens

// ==========================================
// COLOR SCHEME EXTENSION PROPERTIES (SEMANTIC)
// ==========================================
val ColorScheme.warning: Color
    @Composable
    @ReadOnlyComposable
    get() = LiteverTokens.colors.warning

val ColorScheme.onWarning: Color
    @Composable
    @ReadOnlyComposable
    get() = LiteverTokens.colors.onWarning

val ColorScheme.warningContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = LiteverTokens.colors.warningContainer

val ColorScheme.onWarningContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = LiteverTokens.colors.onWarningContainer

val ColorScheme.success: Color
    @Composable
    @ReadOnlyComposable
    get() = LiteverTokens.colors.success

val ColorScheme.onSuccess: Color
    @Composable
    @ReadOnlyComposable
    get() = LiteverTokens.colors.onSuccess

val ColorScheme.successContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = LiteverTokens.colors.successContainer

val ColorScheme.onSuccessContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = LiteverTokens.colors.onSuccessContainer
