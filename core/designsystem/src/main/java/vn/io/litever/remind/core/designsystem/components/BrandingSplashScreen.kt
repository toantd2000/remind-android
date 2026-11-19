package vn.io.litever.remind.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.designsystem.R
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun BrandingSplashScreen(
    onFinished: () -> Unit = {}
) {
    val isPreview = LocalInspectionMode.current

    // Dùng Animatable + graphicsLayer lambda để chạy hoàn toàn trên GPU RenderNode,
    // không recompose layout tree trong suốt quá trình animation.
    val logoAlpha = remember { Animatable(if (isPreview) 1f else 0f) }
    val logoScale = remember { Animatable(if (isPreview) 1f else 0.88f) }
    val glowProgress = remember { Animatable(if (isPreview) 1f else 0f) }
    val wordmarkProgress = remember { Animatable(if (isPreview) 1f else 0f) }
    val attributionAlpha = remember { Animatable(if (isPreview) 1f else 0f) }

    LaunchedEffect(Unit) {
        if (!isPreview) {
            // Chờ nhịp khởi tạo Activity/Theme ổn định để tránh drop frame đầu tiên
            delay(180.milliseconds)

            launch {
                logoAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
                )
            }
            launch {
                logoScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
                )
            }
            launch {
                glowProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
                )
            }

            delay(260.milliseconds)
            launch {
                wordmarkProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                )
            }

            delay(220.milliseconds)
            launch {
                attributionAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
                )
            }

            delay(1300.milliseconds)
        }
        onFinished()
    }

    val haloColor = LiteverTheme.colors.primary
    val slideOffsetPx = with(LocalDensity.current) { 12.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LiteverTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        // Main Logo Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = logoAlpha.value
                    scaleX = logoScale.value
                    scaleY = logoScale.value
                }
            ) {
                // Warm Citrus Halo behind the Orange Alarm Clock logo
                Canvas(modifier = Modifier.size(232.dp)) {
                    val gp = glowProgress.value
                    if (gp > 0f) {
                        val radius = (this.size.minDimension / 2f) * (0.65f + 0.35f * gp)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    haloColor.copy(alpha = 0.16f * gp),
                                    haloColor.copy(alpha = 0.05f * gp),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = radius
                            ),
                            radius = radius,
                            center = center
                        )
                    }
                }

                AppIcon(
                    size = 184.dp,
                    animated = true
                )
            }

            Spacer(modifier = Modifier.height(LiteverTheme.spacing.medium))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    val wp = wordmarkProgress.value
                    alpha = wp
                    translationY = (1f - wp) * slideOffsetPx
                }
            ) {
                ReMindLogo(style = LiteverTheme.typography.titleLarge)

                Spacer(modifier = Modifier.height(LiteverTheme.spacing.small))

                Text(
                    text = stringResource(R.string.app_slogan),
                    style = LiteverTheme.typography.titleMedium,
                    color = LiteverTheme.colors.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.5.sp
                )
            }
        }

        // Bottom Brand Attribution
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = LiteverTheme.spacing.tripleLarge)
                .graphicsLayer {
                    alpha = attributionAlpha.value
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "from",
                style = LiteverTheme.typography.labelMedium,
                color = LiteverTheme.colors.outline.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = LiteverTheme.spacing.extraSmall),
                letterSpacing = 1.sp
            )
            LiteVerLogo(
                fontSize = 20.sp,
                showIcon = true,
                iconSize = 24.dp
            )
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
