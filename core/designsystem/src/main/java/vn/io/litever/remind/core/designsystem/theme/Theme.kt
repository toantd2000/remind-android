package vn.io.litever.remind.core.designsystem.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat


import vn.io.litever.designsystem.theme.LiteverTheme

@Composable
fun ReMindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    colorPalette: String = "DEFAULT",
    content: @Composable () -> Unit
) {
    val isDynamic = colorPalette == "DYNAMIC" || dynamicColor
    
    val colors = if (darkTheme) remindDarkColors else remindLightColors
    
    LiteverTheme(
        colors = if (isDynamic) null else colors,
        darkTheme = darkTheme,
        dynamicColor = isDynamic,
        content = content
    )
}











