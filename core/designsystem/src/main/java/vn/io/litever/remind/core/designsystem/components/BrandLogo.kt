package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import vn.io.litever.remind.core.designsystem.theme.brandLite
import vn.io.litever.remind.core.designsystem.theme.brandVer

@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 48.sp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = brandLite,
                        fontWeight = FontWeight.Light,
                        fontSize = fontSize
                    )
                ) {
                    append("Lite")
                }
                withStyle(
                    style = SpanStyle(
                        color = brandVer,
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSize
                    )
                ) {
                    append("Ver.")
                }
            }
        )
    }
}
