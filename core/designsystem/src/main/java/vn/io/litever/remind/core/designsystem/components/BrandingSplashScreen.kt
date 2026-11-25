package vn.io.litever.remind.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
import vn.io.litever.remind.core.designsystem.R
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.designsystem.theme.spacing
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun BrandingSplashScreen(
    isPro: Boolean = false,
    onFinished: () -> Unit = {}
) {
    val isPreview = LocalInspectionMode.current

    // Dùng Animatable + graphicsLayer lambda để chạy hoàn toàn trên GPU RenderNode,
    // không recompose layout tree trong suốt quá trình animation.
    val logoAlpha = remember { Animatable(if (isPreview) 1f else 0f) }
    val logoScale = remember { Animatable(if (isPreview) 1f else 0.88f) }
    val glowProgress = remember { Animatable(if (isPreview) 1f else 0f) }
    val sparkleProgress = remember { Animatable(if (isPreview) 1f else 0f) }
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
            launch {
                delay(180.milliseconds)
                sparkleProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
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

    val haloColor = MaterialTheme.colorScheme.primary
    val proGoldBright = Color(0xFFF7D774)
    val proGoldRich = Color(0xFFE59E1A)
    val slideOffsetPx = with(LocalDensity.current) { 12.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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
                // Halo behind logo (+ radiant golden burst & sparkles when Pro)
                Canvas(modifier = Modifier.size(if (isPro) 268.dp else 232.dp)) {
                    val gp = glowProgress.value
                    if (gp > 0f) {
                        val radius = (this.size.minDimension / 2f) * (0.65f + 0.35f * gp)
                        if (isPro) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        proGoldBright.copy(alpha = 0.34f * gp),
                                        proGoldRich.copy(alpha = 0.14f * gp),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = radius
                                ),
                                radius = radius,
                                center = center
                            )

                            // Soft expanding golden ring pulse
                            val ringRadius = (this.size.minDimension * 0.34f) + (this.size.minDimension * 0.14f * gp)
                            val ringAlpha = (1f - gp) * 0.45f
                            if (ringAlpha > 0.01f) {
                                drawCircle(
                                    color = proGoldBright.copy(alpha = ringAlpha),
                                    radius = ringRadius,
                                    center = center,
                                    style = Stroke(width = 2.dp.toPx())
                                )
                            }
                        } else {
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

                    // Pro 4-point sparkles matching ic_launcher_fg_pro_gold_trim
                    val sp = sparkleProgress.value
                    if (isPro && sp > 0f) {
                        val shimmerScale = 0.85f + 0.2f * sin(sp * Math.PI).toFloat()
                        val w = this.size.width
                        val h = this.size.height

                        // Primary sparkle (top-right)
                        drawFourPointSparkle(
                            center = Offset(w * 0.78f, h * 0.22f),
                            radius = 15.dp.toPx() * sp * shimmerScale,
                            color = proGoldRich.copy(alpha = (0.95f * sp).coerceIn(0f, 1f)),
                            rotationDegrees = (1f - sp) * 28f
                        )
                        // Secondary sparkle (right)
                        drawFourPointSparkle(
                            center = Offset(w * 0.85f, h * 0.32f),
                            radius = 8.dp.toPx() * sp,
                            color = proGoldBright.copy(alpha = (0.9f * sp).coerceIn(0f, 1f)),
                            rotationDegrees = -(1f - sp) * 20f
                        )
                        // Balancing subtle sparkle (bottom-left)
                        drawFourPointSparkle(
                            center = Offset(w * 0.20f, h * 0.72f),
                            radius = 10.dp.toPx() * sp * shimmerScale,
                            color = proGoldBright.copy(alpha = (0.75f * sp).coerceIn(0f, 1f)),
                            rotationDegrees = (1f - sp) * 22f
                        )
                    }
                }

                AppIcon(
                    size = 184.dp,
                    animated = true
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    val wp = wordmarkProgress.value
                    alpha = wp
                    translationY = (1f - wp) * slideOffsetPx
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    ReMindLogo(style = MaterialTheme.typography.titleLarge)
                    if (isPro) {
                        ReMindPlanBadge(isPro = true)
                    }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                Text(
                    text = stringResource(R.string.app_slogan),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                .padding(bottom = MaterialTheme.spacing.tripleLarge)
                .graphicsLayer {
                    alpha = attributionAlpha.value
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "from",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = MaterialTheme.spacing.extraSmall),
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

private fun DrawScope.drawFourPointSparkle(
    center: Offset,
    radius: Float,
    color: Color,
    rotationDegrees: Float = 0f
) {
    if (radius <= 0.5f) return
    val inner = radius * 0.22f
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x + inner * 0.4f, center.y - inner * 0.4f, center.x + radius, center.y)
        quadraticTo(center.x + inner * 0.4f, center.y + inner * 0.4f, center.x, center.y + radius)
        quadraticTo(center.x - inner * 0.4f, center.y + inner * 0.4f, center.x - radius, center.y)
        quadraticTo(center.x - inner * 0.4f, center.y - inner * 0.4f, center.x, center.y - radius)
        close()
    }
    rotate(degrees = rotationDegrees, pivot = center) {
        drawPath(path = path, color = color)
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_7, showSystemUi = true)
@Composable
fun BrandingSplashScreenPreview() {
    ReMindTheme {
        BrandingSplashScreen()
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_7, showSystemUi = true, name = "Pro Splash - Light")
@Composable
fun BrandingSplashScreenProPreview() {
    ReMindTheme {
        BrandingSplashScreen(isPro = true)
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_7, showSystemUi = true, name = "Pro Splash - Dark")
@Composable
fun BrandingSplashScreenDarkPreview() {
    ReMindTheme(darkTheme = true) {
        BrandingSplashScreen(isPro = true)
    }
}
