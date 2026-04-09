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


private val BlueLightColorScheme = lightColorScheme(
    primary = Color(0xFF34618E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD0E4FF),
    onPrimaryContainer = Color(0xFF174974),
    secondary = Color(0xFF525F70),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD6E4F7),
    onSecondaryContainer = Color(0xFF3B4857),
    tertiary = Color(0xFF6A5779),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF1DAFF),
    onTertiaryContainer = Color(0xFF524060),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFFF8F9FF),
    onBackground = Color(0xFF191C20),
    surface = Color(0xFFF8F9FF),
    onSurface = Color(0xFF191C20),
)

private val BlueDarkColorScheme = darkColorScheme(
    primary = Color(0xFF9FCAFC),
    onPrimary = Color(0xFF003257),
    primaryContainer = Color(0xFF174974),
    onPrimaryContainer = Color(0xFFD0E4FF),
    secondary = Color(0xFFBAC8DB),
    onSecondary = Color(0xFF253140),
    secondaryContainer = Color(0xFF3B4857),
    onSecondaryContainer = Color(0xFFD6E4F7),
    tertiary = Color(0xFFD6BEE5),
    onTertiary = Color(0xFF3A2948),
    tertiaryContainer = Color(0xFF524060),
    onTertiaryContainer = Color(0xFFF1DAFF),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFF101418),
    onBackground = Color(0xFFE1E2E8),
    surface = Color(0xFF101418),
    onSurface = Color(0xFFE1E2E8),
)

private val PurpleLightColorScheme = lightColorScheme(
    primary = Color(0xFF64558F),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8DDFF),
    onPrimaryContainer = Color(0xFF4C3E76),
    secondary = Color(0xFF615B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF494458),
    tertiary = Color(0xFF7D5261),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD9E3),
    onTertiaryContainer = Color(0xFF633B49),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFFFDF7FF),
    onBackground = Color(0xFF1C1B20),
    surface = Color(0xFFFDF7FF),
    onSurface = Color(0xFF1C1B20),
)

private val PurpleDarkColorScheme = darkColorScheme(
    primary = Color(0xFFCEBDFE),
    onPrimary = Color(0xFF35275D),
    primaryContainer = Color(0xFF4C3E76),
    onPrimaryContainer = Color(0xFFE8DDFF),
    secondary = Color(0xFFCBC3DC),
    onSecondary = Color(0xFF332E41),
    secondaryContainer = Color(0xFF494458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C9),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B49),
    onTertiaryContainer = Color(0xFFFFD9E3),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFF141218),
    onBackground = Color(0xFFE6E1E9),
    surface = Color(0xFF141218),
    onSurface = Color(0xFFE6E1E9),
)

private val GreenLightColorScheme = lightColorScheme(
    primary = Color(0xFF1B6B51),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA6F2D2),
    onPrimaryContainer = Color(0xFF00513B),
    secondary = Color(0xFF4C6359),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCEE9DB),
    onSecondaryContainer = Color(0xFF354B41),
    tertiary = Color(0xFF3E6374),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC2E8FD),
    onTertiaryContainer = Color(0xFF264B5C),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFFF5FBF5),
    onBackground = Color(0xFF171D1A),
    surface = Color(0xFFF5FBF5),
    onSurface = Color(0xFF171D1A),
)

private val GreenDarkColorScheme = darkColorScheme(
    primary = Color(0xFF8AD6B6),
    onPrimary = Color(0xFF003828),
    primaryContainer = Color(0xFF00513B),
    onPrimaryContainer = Color(0xFFA6F2D2),
    secondary = Color(0xFFB3CCBF),
    onSecondary = Color(0xFF1E352C),
    secondaryContainer = Color(0xFF354B41),
    onSecondaryContainer = Color(0xFFCEE9DB),
    tertiary = Color(0xFFA6CCE0),
    onTertiary = Color(0xFF093544),
    tertiaryContainer = Color(0xFF264B5C),
    onTertiaryContainer = Color(0xFFC2E8FD),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFF0F1512),
    onBackground = Color(0xFFDEE4DF),
    surface = Color(0xFF0F1512),
    onSurface = Color(0xFFDEE4DF),
)

private val OrangeLightColorScheme = lightColorScheme(
    primary = Color(0xFF8C4E29),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDBCA),
    onPrimaryContainer = Color(0xFF6F3813),
    secondary = Color(0xFF765848),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBCA),
    onSecondaryContainer = Color(0xFF5C4132),
    tertiary = Color(0xFF636032),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEAE5AB),
    onTertiaryContainer = Color(0xFF4B481D),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFFFFF8F6),
    onBackground = Color(0xFF221A15),
    surface = Color(0xFFFFF8F6),
    onSurface = Color(0xFF221A15),
)

private val OrangeDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB68E),
    onPrimary = Color(0xFF532201),
    primaryContainer = Color(0xFF6F3813),
    onPrimaryContainer = Color(0xFFFFDBCA),
    secondary = Color(0xFFE6BEAA),
    onSecondary = Color(0xFF432B1D),
    secondaryContainer = Color(0xFF5C4132),
    onSecondaryContainer = Color(0xFFFFDBCA),
    tertiary = Color(0xFFCDC991),
    onTertiary = Color(0xFF343208),
    tertiaryContainer = Color(0xFF4B481D),
    onTertiaryContainer = Color(0xFFEAE5AB),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFF1A120D),
    onBackground = Color(0xFFF0DFD7),
    surface = Color(0xFF1A120D),
    onSurface = Color(0xFFF0DFD7),
)
private val IndigoBlueLightColorScheme = lightColorScheme(
    primary = Color(0xFF505B92),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDDE1FF),
    onPrimaryContainer = Color(0xFF384379),
    secondary = Color(0xFF5A5D72),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDFE1F9),
    onSecondaryContainer = Color(0xFF434659),
    tertiary = Color(0xFF76546E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD7F2),
    onTertiaryContainer = Color(0xFF5C3C55),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFFFBF8FF),
    onBackground = Color(0xFF1B1B21),
    surface = Color(0xFFFBF8FF),
    onSurface = Color(0xFF1B1B21),
)

private val IndigoBlueDarkColorScheme = darkColorScheme(
    primary = Color(0xFFB9C3FF),
    onPrimary = Color(0xFF212C61),
    primaryContainer = Color(0xFF384379),
    onPrimaryContainer = Color(0xFFDDE1FF),
    secondary = Color(0xFFC3C5DD),
    onSecondary = Color(0xFF2C2F42),
    secondaryContainer = Color(0xFF434659),
    onSecondaryContainer = Color(0xFFDFE1F9),
    tertiary = Color(0xFFE5BAD8),
    onTertiary = Color(0xFF44263E),
    tertiaryContainer = Color(0xFF5C3C55),
    onTertiaryContainer = Color(0xFFFFD7F2),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFF121318),
    onBackground = Color(0xFFE3E1E9),
    surface = Color(0xFF121318),
    onSurface = Color(0xFFE3E1E9),
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
        colorPalette == "BLUE" -> if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
        colorPalette == "PURPLE" -> if (darkTheme) PurpleDarkColorScheme else PurpleLightColorScheme
        colorPalette == "GREEN" -> if (darkTheme) GreenDarkColorScheme else GreenLightColorScheme
        colorPalette == "ORANGE" -> if (darkTheme) OrangeDarkColorScheme else OrangeLightColorScheme
        colorPalette == "TEAL" -> if (darkTheme) IndigoBlueDarkColorScheme else IndigoBlueLightColorScheme
        else -> if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
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
