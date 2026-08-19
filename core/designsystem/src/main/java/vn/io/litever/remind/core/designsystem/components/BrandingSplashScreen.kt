package vn.io.litever.remind.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
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
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun BrandingSplashScreen(
    onFinished: () -> Unit = {}
) {
    val isPreview = LocalInspectionMode.current
    var visible by remember { mutableStateOf(isPreview) }

    LaunchedEffect(Unit) {
        if (!isPreview) {
            delay(100.milliseconds) // Small delay before animation
            visible = true
            delay(900.milliseconds) // Wait for animations to finish
        }
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LiteverTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        val scale by animateFloatAsState(
            targetValue = if (visible) 1f else 0.8f,
            animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
            label = "logoScale"
        )

        // Main Logo Content
        AnimatedVisibility(
            visible = isPreview || visible,
            enter = fadeIn(tween(600)),
            exit = fadeOut(tween(400))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(scaleX = scale, scaleY = scale)
            ) {
                // App Logo
                ReMindLogo(fontSize = 42.sp)

                Spacer(modifier = Modifier.height(8.dp))

                // Slogan
                Text(
                    text = stringResource(R.string.app_slogan),
                    style = LiteverTheme.typography.titleMedium,
                    color = LiteverTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.5.sp
                )
            }
        }

        // Bottom Brand Attribution
        AnimatedVisibility(
            visible = isPreview || visible,
            enter = fadeIn(tween(500, delayMillis = 200)),
            exit = fadeOut(tween(400)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .padding(bottom = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "from",
                    style = LiteverTheme.typography.labelMedium,
                    color = LiteverTheme.colors.outline.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 4.dp),
                    letterSpacing = 1.sp
                )
                BrandLogo(fontSize = 20.sp)
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