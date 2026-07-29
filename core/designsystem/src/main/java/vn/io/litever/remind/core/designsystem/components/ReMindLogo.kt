package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

import vn.io.litever.designsystem.theme.LiteverFontFamily
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.onSurfaceVariantDark
import vn.io.litever.remind.core.designsystem.theme.onSurfaceVariantLight
import vn.io.litever.remind.core.designsystem.theme.primaryDark
import vn.io.litever.remind.core.designsystem.theme.primaryLight

@Composable
fun ReMindLogo(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 24.sp,
) {
    val isDark = isSystemInDarkTheme()
    val upColor = if (isDark) onSurfaceVariantDark else onSurfaceVariantLight
    val remindColor = if (isDark) primaryDark else primaryLight

    Text(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(
                fontWeight = FontWeight.Light,
                color = upColor,
                letterSpacing = 0.05.em
            )) {
                append("Re")
            }
            withStyle(style = SpanStyle(
                fontWeight = FontWeight.ExtraBold,
                color = remindColor,
                letterSpacing = 0.em
            )) {
                append("Mind")
            }
        },
        fontSize = fontSize,
        fontFamily = LiteverFontFamily,
        letterSpacing = 0.sp,
        modifier = modifier
    )
}











