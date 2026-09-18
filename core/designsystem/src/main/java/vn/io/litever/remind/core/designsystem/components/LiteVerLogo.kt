package vn.io.litever.remind.core.designsystem.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme

@Composable
fun LiteVerLogo(
    modifier: Modifier = Modifier,
    style: TextStyle? = null,
    fontSize: TextUnit = TextUnit.Unspecified
) {
    val baseStyle = style ?: LiteverTheme.typography.titleLarge
    val finalStyle = if (fontSize != TextUnit.Unspecified) {
        baseStyle.copy(fontSize = fontSize)
    } else {
        baseStyle
    }

    Text(
        text = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = LiteverTheme.colors.brandLite,
                    fontWeight = FontWeight.ExtraLight,
                )
            ) {
                append("Lite")
            }
            withStyle(
                style = SpanStyle(
                    color = LiteverTheme.colors.brandVer,
                    fontWeight = FontWeight.ExtraBold,
                )
            ) {
                append("Ver.")
            }
        },
        style = finalStyle,
        letterSpacing = 0.sp,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun LiteVerLogoPreview() {
    ReMindTheme {
        LiteVerLogo()
    }
}

@Preview(showBackground = true)
@Composable
private fun LiteVerLogoDarkPreview() {
    ReMindTheme(darkTheme = true) {
        LiteVerLogo()
    }
}
