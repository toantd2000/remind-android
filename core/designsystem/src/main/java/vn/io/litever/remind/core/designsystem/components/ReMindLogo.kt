package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import vn.io.litever.remind.core.designsystem.theme.logo_remind_dark
import vn.io.litever.remind.core.designsystem.theme.logo_remind_light
import vn.io.litever.remind.core.designsystem.theme.logo_up_dark
import vn.io.litever.remind.core.designsystem.theme.logo_up_light

import vn.io.litever.remind.core.designsystem.theme.InterFontFamily

@Composable
fun ReMindLogo(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 24.sp,
) {
    val isDark = isSystemInDarkTheme()
    val upColor = if (isDark) logo_up_dark else logo_up_light
    val remindColor = if (isDark) logo_remind_dark else logo_remind_light

    Text(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(
                fontWeight = FontWeight.Light,
                color = upColor
            )) {
                append("Re")
            }
            withStyle(style = SpanStyle(
                fontWeight = FontWeight.ExtraBold,
                color = remindColor
            )) {
                append("Mind")
            }
        },
        fontSize = fontSize,
        fontFamily = InterFontFamily,
        letterSpacing = 0.sp,
        modifier = modifier
    )
}










