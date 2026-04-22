package vn.io.litever.remind.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import vn.io.litever.remind.core.designsystem.R
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme

@Composable
fun BrandingSplashScreen(
    onFinished: () -> Unit = {}
) {
    val isPreview = LocalInspectionMode.current
    var visible by remember { mutableStateOf(isPreview) }

    LaunchedEffect(Unit) {
        if (!isPreview) {
            delay(300) // Small delay before animation
            visible = true
        }
        delay(2000) // Show for 2 seconds
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        val content = @Composable {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // App Logo (Large)
                ReMindLogo(fontSize = 36.sp)

                Spacer(modifier = Modifier.height(4.dp))

                // Slogan
                Text(
                    text = stringResource(R.string.app_slogan),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(64.dp))

                // Brand Attribution
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "by ",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                    BrandLogo(fontSize = 16.sp)
                }
            }
        }

        if (isPreview) {
            content()
        } else {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1000)),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                content()
            }
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_7, showSystemUi = true)
@Composable
fun BrandingSplashScreenPreview() {
    ReMindTheme {
        BrandingSplashScreen()
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_7, showSystemUi = true)
@Composable
fun BrandingSplashScreenDarkPreview() {
    ReMindTheme(darkTheme = true) {
        BrandingSplashScreen()
    }
}
