package vn.io.litever.remind.core.designsystem.theme

import vn.io.litever.designsystem.theme.LiteverColors
import vn.io.litever.designsystem.theme.TailwindColors

// Logo Colors (App Specific)
val logo_up_light = TailwindColors.Zinc.c500
val logo_remind_light = TailwindColors.Amber.c500

val logo_up_dark = TailwindColors.Zinc.c400
val logo_remind_dark = TailwindColors.Amber.c400

// ReMind Specific Colors (Amber, Sky, Zinc)
val remindLightColors = LiteverColors(
    primary = TailwindColors.Amber.c500,
    onPrimary = TailwindColors.White,
    primaryContainer = TailwindColors.Amber.c100,
    onPrimaryContainer = TailwindColors.Amber.c900,
    inversePrimary = TailwindColors.Amber.c400,

    secondary = TailwindColors.Sky.c500,
    onSecondary = TailwindColors.White,
    secondaryContainer = TailwindColors.Sky.c100,
    onSecondaryContainer = TailwindColors.Sky.c900,

    tertiary = TailwindColors.Zinc.c500,
    onTertiary = TailwindColors.White,
    tertiaryContainer = TailwindColors.Zinc.c100,
    onTertiaryContainer = TailwindColors.Zinc.c900,

    background = TailwindColors.Zinc.c50,
    onBackground = TailwindColors.Zinc.c900,
    surface = TailwindColors.Zinc.c50,
    onSurface = TailwindColors.Zinc.c900,
    surfaceVariant = TailwindColors.Zinc.c200,
    onSurfaceVariant = TailwindColors.Zinc.c700,
    surfaceTint = TailwindColors.Amber.c500,
    inverseSurface = TailwindColors.Zinc.c800,
    inverseOnSurface = TailwindColors.Zinc.c50,

    error = TailwindColors.Red.c500,
    onError = TailwindColors.White,
    errorContainer = TailwindColors.Red.c100,
    onErrorContainer = TailwindColors.Red.c900,

    outline = TailwindColors.Zinc.c400,
    outlineVariant = TailwindColors.Zinc.c300,
    scrim = TailwindColors.Black,

    surfaceBright = TailwindColors.Zinc.c50,
    surfaceDim = TailwindColors.Zinc.c200,
    surfaceContainerLowest = TailwindColors.White,
    surfaceContainerLow = TailwindColors.Zinc.c50,
    surfaceContainer = TailwindColors.Zinc.c100,
    surfaceContainerHigh = TailwindColors.Zinc.c200,
    surfaceContainerHighest = TailwindColors.Zinc.c300,
    isLight = true
)

val remindDarkColors = LiteverColors(
    primary = TailwindColors.Amber.c400,
    onPrimary = TailwindColors.Amber.c900,
    primaryContainer = TailwindColors.Amber.c800,
    onPrimaryContainer = TailwindColors.Amber.c100,
    inversePrimary = TailwindColors.Amber.c500,

    secondary = TailwindColors.Sky.c400,
    onSecondary = TailwindColors.Sky.c900,
    secondaryContainer = TailwindColors.Sky.c800,
    onSecondaryContainer = TailwindColors.Sky.c100,

    tertiary = TailwindColors.Zinc.c400,
    onTertiary = TailwindColors.Zinc.c900,
    tertiaryContainer = TailwindColors.Zinc.c800,
    onTertiaryContainer = TailwindColors.Zinc.c100,

    background = TailwindColors.Zinc.c900,
    onBackground = TailwindColors.Zinc.c50,
    surface = TailwindColors.Zinc.c900,
    onSurface = TailwindColors.Zinc.c50,
    surfaceVariant = TailwindColors.Zinc.c700,
    onSurfaceVariant = TailwindColors.Zinc.c300,
    surfaceTint = TailwindColors.Amber.c400,
    inverseSurface = TailwindColors.Zinc.c200,
    inverseOnSurface = TailwindColors.Zinc.c900,

    error = TailwindColors.Red.c400,
    onError = TailwindColors.Red.c900,
    errorContainer = TailwindColors.Red.c800,
    onErrorContainer = TailwindColors.Red.c100,

    outline = TailwindColors.Zinc.c500,
    outlineVariant = TailwindColors.Zinc.c600,
    scrim = TailwindColors.Black,

    surfaceBright = TailwindColors.Zinc.c800,
    surfaceDim = TailwindColors.Zinc.c900,
    surfaceContainerLowest = TailwindColors.Black,
    surfaceContainerLow = TailwindColors.Zinc.c900,
    surfaceContainer = TailwindColors.Zinc.c800,
    surfaceContainerHigh = TailwindColors.Zinc.c700,
    surfaceContainerHighest = TailwindColors.Zinc.c600,
    isLight = false
)