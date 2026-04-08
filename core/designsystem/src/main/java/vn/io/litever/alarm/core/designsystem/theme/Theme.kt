package vn.io.litever.alarm.core.designsystem.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    secondary = NeonPurple,
    tertiary = Purple80,
    background = MidnightBlack,
    surface = SoftGraphite,
    onPrimary = MidnightBlack,
    onSecondary = PureWhite,
    onBackground = PureWhite,
    onSurface = PureWhite
)

private val LightColorScheme = lightColorScheme(
    primary = DeepIndigo,
    secondary = Purple40,
    tertiary = Pink40,
    background = PureWhite,
    surface = Color(0xFFF5F5F7),
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onBackground = DeepIndigo,
    onSurface = DeepIndigo
)

private val OceanLightColorScheme = lightColorScheme(
    primary = OceanBlueLight,
    secondary = OceanTeal,
    background = PureWhite,
    surface = Color(0xFFF0F8FF),
    onPrimary = PureWhite,
    onBackground = OceanBlueDark,
    onSurface = OceanBlueDark
)

private val OceanDarkColorScheme = darkColorScheme(
    primary = OceanBlueLight,
    secondary = OceanTeal,
    background = MidnightBlack,
    surface = OceanBlueDark,
    onPrimary = MidnightBlack,
    onBackground = PureWhite,
    onSurface = PureWhite
)

private val SunsetLightColorScheme = lightColorScheme(
    primary = SunsetOrangeLight,
    secondary = SunsetYellow,
    background = PureWhite,
    surface = Color(0xFFFFF3E0),
    onPrimary = PureWhite,
    onBackground = SunsetOrangeDark,
    onSurface = SunsetOrangeDark
)

private val SunsetDarkColorScheme = darkColorScheme(
    primary = SunsetOrangeLight,
    secondary = SunsetYellow,
    background = MidnightBlack,
    surface = SunsetOrangeDark,
    onPrimary = MidnightBlack,
    onBackground = PureWhite,
    onSurface = PureWhite
)

private val ForestLightColorScheme = lightColorScheme(
    primary = ForestGreenLight,
    secondary = ForestOlive,
    background = PureWhite,
    surface = Color(0xFFE8F5E9),
    onPrimary = PureWhite,
    onBackground = ForestGreenDark,
    onSurface = ForestGreenDark
)

private val ForestDarkColorScheme = darkColorScheme(
    primary = ForestGreenLight,
    secondary = ForestOlive,
    background = MidnightBlack,
    surface = ForestGreenDark,
    onPrimary = MidnightBlack,
    onBackground = PureWhite,
    onSurface = PureWhite
)

@Composable
fun AlarmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    colorPalette: String = "DYNAMIC",
    content: @Composable () -> Unit
) {
    val isDynamic = colorPalette == "DYNAMIC" || dynamicColor
    val colorScheme = when {
        isDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        colorPalette == "OCEAN" -> if (darkTheme) OceanDarkColorScheme else OceanLightColorScheme
        colorPalette == "SUNSET" -> if (darkTheme) SunsetDarkColorScheme else SunsetLightColorScheme
        colorPalette == "FOREST" -> if (darkTheme) ForestDarkColorScheme else ForestLightColorScheme
        else -> if (darkTheme) DarkColorScheme else LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
