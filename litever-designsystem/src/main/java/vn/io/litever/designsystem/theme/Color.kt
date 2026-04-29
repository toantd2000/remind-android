package vn.io.litever.designsystem.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

class LiteverColors(
    primary: Color,
    onPrimary: Color,
    primaryContainer: Color,
    onPrimaryContainer: Color,
    inversePrimary: Color,
    secondary: Color,
    onSecondary: Color,
    secondaryContainer: Color,
    onSecondaryContainer: Color,
    tertiary: Color,
    onTertiary: Color,
    tertiaryContainer: Color,
    onTertiaryContainer: Color,
    background: Color,
    onBackground: Color,
    surface: Color,
    onSurface: Color,
    surfaceVariant: Color,
    onSurfaceVariant: Color,
    surfaceTint: Color,
    inverseSurface: Color,
    inverseOnSurface: Color,
    error: Color,
    onError: Color,
    errorContainer: Color,
    onErrorContainer: Color,
    outline: Color,
    outlineVariant: Color,
    scrim: Color,
    surfaceBright: Color,
    surfaceDim: Color,
    surfaceContainerLowest: Color,
    surfaceContainerLow: Color,
    surfaceContainer: Color,
    surfaceContainerHigh: Color,
    surfaceContainerHighest: Color,
    isLight: Boolean
) {
    var primary by mutableStateOf(primary)
        private set
    var onPrimary by mutableStateOf(onPrimary)
        private set
    var primaryContainer by mutableStateOf(primaryContainer)
        private set
    var onPrimaryContainer by mutableStateOf(onPrimaryContainer)
        private set
    var inversePrimary by mutableStateOf(inversePrimary)
        private set
    var secondary by mutableStateOf(secondary)
        private set
    var onSecondary by mutableStateOf(onSecondary)
        private set
    var secondaryContainer by mutableStateOf(secondaryContainer)
        private set
    var onSecondaryContainer by mutableStateOf(onSecondaryContainer)
        private set
    var tertiary by mutableStateOf(tertiary)
        private set
    var onTertiary by mutableStateOf(onTertiary)
        private set
    var tertiaryContainer by mutableStateOf(tertiaryContainer)
        private set
    var onTertiaryContainer by mutableStateOf(onTertiaryContainer)
        private set
    var background by mutableStateOf(background)
        private set
    var onBackground by mutableStateOf(onBackground)
        private set
    var surface by mutableStateOf(surface)
        private set
    var onSurface by mutableStateOf(onSurface)
        private set
    var surfaceVariant by mutableStateOf(surfaceVariant)
        private set
    var onSurfaceVariant by mutableStateOf(onSurfaceVariant)
        private set
    var surfaceTint by mutableStateOf(surfaceTint)
        private set
    var inverseSurface by mutableStateOf(inverseSurface)
        private set
    var inverseOnSurface by mutableStateOf(inverseOnSurface)
        private set
    var error by mutableStateOf(error)
        private set
    var onError by mutableStateOf(onError)
        private set
    var errorContainer by mutableStateOf(errorContainer)
        private set
    var onErrorContainer by mutableStateOf(onErrorContainer)
        private set
    var outline by mutableStateOf(outline)
        private set
    var outlineVariant by mutableStateOf(outlineVariant)
        private set
    var scrim by mutableStateOf(scrim)
        private set
    var surfaceBright by mutableStateOf(surfaceBright)
        private set
    var surfaceDim by mutableStateOf(surfaceDim)
        private set
    var surfaceContainerLowest by mutableStateOf(surfaceContainerLowest)
        private set
    var surfaceContainerLow by mutableStateOf(surfaceContainerLow)
        private set
    var surfaceContainer by mutableStateOf(surfaceContainer)
        private set
    var surfaceContainerHigh by mutableStateOf(surfaceContainerHigh)
        private set
    var surfaceContainerHighest by mutableStateOf(surfaceContainerHighest)
        private set
    var isLight by mutableStateOf(isLight)
        private set

    fun updateColorsFrom(other: LiteverColors) {
        primary = other.primary
        onPrimary = other.onPrimary
        primaryContainer = other.primaryContainer
        onPrimaryContainer = other.onPrimaryContainer
        inversePrimary = other.inversePrimary
        secondary = other.secondary
        onSecondary = other.onSecondary
        secondaryContainer = other.secondaryContainer
        onSecondaryContainer = other.onSecondaryContainer
        tertiary = other.tertiary
        onTertiary = other.onTertiary
        tertiaryContainer = other.tertiaryContainer
        onTertiaryContainer = other.onTertiaryContainer
        background = other.background
        onBackground = other.onBackground
        surface = other.surface
        onSurface = other.onSurface
        surfaceVariant = other.surfaceVariant
        onSurfaceVariant = other.onSurfaceVariant
        surfaceTint = other.surfaceTint
        inverseSurface = other.inverseSurface
        inverseOnSurface = other.inverseOnSurface
        error = other.error
        onError = other.onError
        errorContainer = other.errorContainer
        onErrorContainer = other.onErrorContainer
        outline = other.outline
        outlineVariant = other.outlineVariant
        scrim = other.scrim
        surfaceBright = other.surfaceBright
        surfaceDim = other.surfaceDim
        surfaceContainerLowest = other.surfaceContainerLowest
        surfaceContainerLow = other.surfaceContainerLow
        surfaceContainer = other.surfaceContainer
        surfaceContainerHigh = other.surfaceContainerHigh
        surfaceContainerHighest = other.surfaceContainerHighest
        isLight = other.isLight
    }

    fun copy(): LiteverColors = LiteverColors(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        inversePrimary = inversePrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceTint = surfaceTint,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        outline = outline,
        outlineVariant = outlineVariant,
        scrim = scrim,
        surfaceBright = surfaceBright,
        surfaceDim = surfaceDim,
        surfaceContainerLowest = surfaceContainerLowest,
        surfaceContainerLow = surfaceContainerLow,
        surfaceContainer = surfaceContainer,
        surfaceContainerHigh = surfaceContainerHigh,
        surfaceContainerHighest = surfaceContainerHighest,
        isLight = isLight
    )
}

// Default Light Palette
val liteverLightColors = LiteverColors(
    primary = Color(0xFF825513),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDDB8),
    onPrimaryContainer = Color(0xFF653E00),
    inversePrimary = Color(0xFFF8BB71),
    secondary = Color(0xFF715A41),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFCDDBD),
    onSecondaryContainer = Color(0xFF57432B),
    tertiary = Color(0xFF54643D),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD7E9B9),
    onTertiaryContainer = Color(0xFF3D4C28),
    background = Color(0xFFFFF8F4),
    onBackground = Color(0xFF211A13),
    surface = Color(0xFFFFF8F4),
    onSurface = Color(0xFF211A13),
    surfaceVariant = Color(0xFFF1E0D0),
    onSurfaceVariant = Color(0xFF504539),
    surfaceTint = Color(0xFF825513),
    inverseSurface = Color(0xFF372F27),
    inverseOnSurface = Color(0xFFFCEEE2),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    outline = Color(0xFF827568),
    outlineVariant = Color(0xFFD4C4B5),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFFFF8F4),
    surfaceDim = Color(0xFFE5D8CC),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFF1E5),
    surfaceContainer = Color(0xFFF9ECE0),
    surfaceContainerHigh = Color(0xFFF3E6DA),
    surfaceContainerHighest = Color(0xFFEEE0D4),
    isLight = true
)

// Default Dark Palette
val liteverDarkColors = LiteverColors(
    primary = Color(0xFFF8BB71),
    onPrimary = Color(0xFF472A00),
    primaryContainer = Color(0xFF653E00),
    onPrimaryContainer = Color(0xFFFFDDB8),
    inversePrimary = Color(0xFF825513),
    secondary = Color(0xFFDFC2A2),
    onSecondary = Color(0xFF3F2D17),
    secondaryContainer = Color(0xFF57432B),
    onSecondaryContainer = Color(0xFFFCDDBD),
    tertiary = Color(0xFFBBCD9E),
    onTertiary = Color(0xFF273513),
    tertiaryContainer = Color(0xFF3D4C28),
    onTertiaryContainer = Color(0xFFD7E9B9),
    background = Color(0xFF18120C),
    onBackground = Color(0xFFEEE0D4),
    surface = Color(0xFF18120C),
    onSurface = Color(0xFFEEE0D4),
    surfaceVariant = Color(0xFF504539),
    onSurfaceVariant = Color(0xFFD4C4B5),
    surfaceTint = Color(0xFFF8BB71),
    inverseSurface = Color(0xFFEEE0D4),
    inverseOnSurface = Color(0xFF372F27),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF9C8E80),
    outlineVariant = Color(0xFF504539),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF403830),
    surfaceDim = Color(0xFF18120C),
    surfaceContainerLowest = Color(0xFF130D07),
    surfaceContainerLow = Color(0xFF211A13),
    surfaceContainer = Color(0xFF251E17),
    surfaceContainerHigh = Color(0xFF302921),
    surfaceContainerHighest = Color(0xFF3B332B),
    isLight = false
)

val LocalLiteverColors = staticCompositionLocalOf<LiteverColors> {
    error("No LiteverColors provided")
}

// Brand Colors
val brandLite = Color(0xFF6B7280)
val brandVer = Color(0xFF2563EB)
