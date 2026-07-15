package vn.io.litever.remind.core.designsystem.theme

import vn.io.litever.designsystem.theme.darkLiteverColors
import vn.io.litever.designsystem.theme.lightLiteverColors

// Logo Colors (App Specific)
val logo_up_light = TailwindColors.Zinc.c500
val logo_remind_light = TailwindColors.Amber.c500

val logo_up_dark = TailwindColors.Zinc.c400
val logo_remind_dark = TailwindColors.Amber.c400

// ReMind Specific Colors (Amber, Sky, Zinc)
val remindLightColors = lightLiteverColors(
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

)

val remindDarkColors = darkLiteverColors(
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
)