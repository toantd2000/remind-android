package vn.io.litever.remind.core.designsystem.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import vn.io.litever.designsystem.theme.LiteverTheme

@Composable
fun ReMindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    colorPalette: String = "DEFAULT",
    content: @Composable () -> Unit
) {
    val isDynamic = colorPalette == "DYNAMIC" || dynamicColor
    
    val colors = if (colorPalette == "SIMPLE") {
        null
    } else {
        if (darkTheme) remindDarkColors else remindLightColors
    }
    
    LiteverTheme(
        colors = if (isDynamic) null else colors,
        darkTheme = darkTheme,
        dynamicColor = isDynamic,
        content = content
    )
}











