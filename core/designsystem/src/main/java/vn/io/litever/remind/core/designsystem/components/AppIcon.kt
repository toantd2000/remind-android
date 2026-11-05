package vn.io.litever.remind.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Bảng màu chuẩn nhận diện thương hiệu của ReMind App Icon (Orange Palette).
 */
object AppIconDefaults {
    val SurfaceContainerLow = Color(0xFFFAEBE0)  // @color/icon_surface_container_low
    val ClockFaceColor = Color(0xFF50453A)       // @color/icon_surface_container_high (Nền mặt đồng hồ)
    val RaysColor = Color(0xFFFCB974)            // @color/icon_rays (12 tia nắng)
    val ClockBorderColor = Color(0xFF372F28)     // @color/icon_clock_border (Viền đồng hồ & viền tâm)
    val HourHandColor = Color(0xFFFEDCBE)        // @color/icon_hour_hand (Kim giờ)
    val MinuteHandColor = Color(0xFFDBE8B5)      // @color/icon_minute_hand (Kim phút)
    val CenterDotColor = Color(0xFF855317)       // @color/icon_center_dot (Ruột tâm đồng hồ)
}

/**
 * Composable hiển thị Logo Icon của ứng dụng ReMind trực tiếp bằng Canvas,
 * là Single Source of Truth cho cả trạng thái tĩnh lẫn chuyển động.
 *
 * @param modifier Modifier tùy chỉnh layout
 * @param size Kích thước icon (mặc định 88.dp)
 * @param animated Bật hiệu ứng chuyển động (tia nắng bật ra và kim xoay nhẹ)
 * @param contentDescription Mô tả trợ năng
 */
@Composable
fun AppIcon(
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    animated: Boolean = false,
    contentDescription: String? = "ReMind"
) {
    val semanticsModifier = if (contentDescription != null) {
        modifier.semantics { this.contentDescription = contentDescription }
    } else {
        modifier
    }

    if (animated) {
        val isPreview = LocalInspectionMode.current
        val rayProgress = remember { Animatable(if (isPreview) 1f else 0f) }
        val handProgress = remember { Animatable(if (isPreview) 1f else 0f) }

        LaunchedEffect(Unit) {
            if (!isPreview) {
                // 1. Các tia nắng bật ra trước với độ nảy (Spring bounce)
                launch {
                    rayProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = 0.55f,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                }

                // 2. Kim đồng hồ xoay một chút (bắt đầu sau khi tia bắt đầu bung ra 120ms)
                delay(120)
                handProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 600,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }

        Canvas(modifier = semanticsModifier.size(size)) {
            drawAppIcon(
                rayProgress = rayProgress.value,
                handProgress = handProgress.value
            )
        }
    } else {
        // Trạng thái tĩnh: vẽ trực tiếp Canvas ở trạng thái hoàn chỉnh (progress = 1f), không tốn chi phí animation
        Canvas(modifier = semanticsModifier.size(size)) {
            drawAppIcon(
                rayProgress = 1f,
                handProgress = 1f
            )
        }
    }
}

/**
 * Hàm vẽ Canvas dùng chung cho cả trạng thái tĩnh (progress = 1f) và trạng thái animate.
 */
private fun DrawScope.drawAppIcon(
    rayProgress: Float,
    handProgress: Float
) {
    val s = this.size.minDimension / 512f
    val center = Offset(this.size.width / 2f, this.size.height / 2f)

    // 1. TIA NẮNG MẶT TRỜI (12 tia đồng đều dài 12px từ R=126 đến R=138, màu #FCB974)
    val rayAlpha = (rayProgress * 2f).coerceIn(0f, 1f)
    if (rayAlpha > 0f) {
        val innerR = 126f * s
        val outerR = (126f + 12f * rayProgress) * s

        for (i in 0 until 12) {
            val angle = i * 30f
            rotate(angle, pivot = center) {
                drawLine(
                    color = AppIconDefaults.RaysColor.copy(alpha = rayAlpha),
                    start = Offset(center.x, center.y - innerR),
                    end = Offset(center.x, center.y - outerR),
                    strokeWidth = 16f * s,
                    cap = StrokeCap.Round
                )
            }
        }
    }

    // 2. MẶT & VIỀN ĐỒNG HỒ (Nền mặt: #50453A, Viền: #372F28 R=102, stroke=18)
    drawCircle(
        color = AppIconDefaults.ClockFaceColor,
        radius = 102f * s,
        center = center
    )
    drawCircle(
        color = AppIconDefaults.ClockBorderColor,
        radius = 102f * s,
        center = center,
        style = Stroke(width = 18f * s)
    )

    // 3. KIM ĐỒNG HỒ
    val pHand = handProgress

    // Kim giờ (Màu #FEDCBE, từ tâm 256,256 đến 211,282. Xoay nhẹ -12° về 0°)
    val hourOffset = -12f * (1f - pHand)
    rotate(hourOffset, pivot = center) {
        drawLine(
            color = AppIconDefaults.HourHandColor,
            start = center,
            end = Offset(center.x - 45f * s, center.y + 26f * s),
            strokeWidth = 18f * s,
            cap = StrokeCap.Round
        )
    }

    // Kim phút (Màu #DBE8B5, từ tâm 256,256 đến 306,206. Xoay -36° về 0°)
    val minuteOffset = -36f * (1f - pHand)
    rotate(minuteOffset, pivot = center) {
        drawLine(
            color = AppIconDefaults.MinuteHandColor,
            start = center,
            end = Offset(center.x + 50f * s, center.y - 50f * s),
            strokeWidth = 18f * s,
            cap = StrokeCap.Round
        )
    }

    // 4. CHẤM TÂM ĐỒNG HỒ (Ruột: #855317, Viền: #372F28, R=16, stroke=4)
    val centerRadius = 16f * s
    drawCircle(
        color = AppIconDefaults.CenterDotColor,
        radius = centerRadius,
        center = center
    )
    drawCircle(
        color = AppIconDefaults.ClockBorderColor,
        radius = centerRadius,
        center = center,
        style = Stroke(width = 4f * s)
    )
}

@Preview(showBackground = true)
@Composable
private fun AppIconStaticPreview() {
    AppIcon(size = 96.dp, animated = false)
}

@Preview(showBackground = true)
@Composable
private fun AppIconAnimatedPreview() {
    AppIcon(size = 96.dp, animated = true)
}

@Preview(showBackground = true, backgroundColor = 0xFF19120C)
@Composable
private fun AppIconDarkPreview() {
    AppIcon(size = 96.dp, animated = true)
}
