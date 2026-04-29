package vn.io.litever.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.designsystem.theme.brandLite
import vn.io.litever.designsystem.theme.brandVer

/**
 * The official Litever brand logo component.
 */
@Composable
fun LiteverLogo(
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

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun LiteverLogoPreview() {
    LiteverTheme(darkTheme = false) {
        Surface {
            LiteverLogo(modifier = Modifier.padding(16.dp))
        }
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun LiteverLogoDarkPreview() {
    LiteverTheme(darkTheme = true) {
        Surface {
            LiteverLogo(modifier = Modifier.padding(16.dp))
        }
    }
}
