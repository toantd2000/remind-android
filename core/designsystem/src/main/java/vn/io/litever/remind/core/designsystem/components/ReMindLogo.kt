package vn.io.litever.remind.core.designsystem.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme

@Composable
fun ReMindLogo(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 24.sp,
) {
    val reColor = LiteverTheme.colors.onSurfaceVariant
    val mindColor = LiteverTheme.colors.primary

    Text(
        text = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = reColor,
                    fontWeight = FontWeight.Light,
                )
            ) {
                append("Re")
            }
            withStyle(
                style = SpanStyle(
                    color = mindColor,
                    fontWeight = FontWeight.Bold,
                )
            ) {
                append("Mind")
            }
        },
        style = LiteverTheme.typography.displayLarge.copy(fontSize = fontSize),
        letterSpacing = 0.sp,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun ReMindLogoPreview() {
    ReMindTheme {
        ReMindLogo()
    }
}











