package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.R
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme

@Composable
fun LiteVerLogo(
    modifier: Modifier = Modifier,
    style: TextStyle? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    showIcon: Boolean = false,
    iconSize: Dp = 24.dp
) {
    if (showIcon) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small),
            modifier = modifier
        ) {
            Image(
                painter = painterResource(id = R.drawable.litever_studio_tight),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(iconSize)
            )
            LiteVerLogoText(style = style, fontSize = fontSize)
        }
    } else {
        LiteVerLogoText(modifier = modifier, style = style, fontSize = fontSize)
    }
}

@Composable
private fun LiteVerLogoText(
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
                    fontWeight = FontWeight.SemiBold,
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
        LiteVerLogo(showIcon = true)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF19120C)
@Composable
private fun LiteVerLogoDarkPreview() {
    ReMindTheme(darkTheme = true) {
        LiteVerLogo(showIcon = true)
    }
}
