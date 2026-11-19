package vn.io.litever.remind.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Bảng màu chuẩn nhận diện thương hiệu của ReMind App Icon (Orange Alarm Clock).
 * Khớp với `ic_launcher_foreground.xml`.
 */
object AppIconDefaults {
    val OuterBodyLight = Color(0xFFEA732D)
    val OuterBodyMid = Color(0xFFD9631E)
    val OuterBodyDark = Color(0xFFBD500F)
    val BellDark = Color(0xFFAE470B)
    val FootColor = Color(0xFFA8430A)
    val StemColor = Color(0xFF5C3E28)
    val LeafLight = Color(0xFF608B67)
    val LeafDark = Color(0xFF4B7252)

    val FaceCenter = Color(0xFFFFFDFB)
    val FaceEdge = Color(0xBFEADBCC)
    val WedgeFill = Color(0xFFFAF3ED)
    val WedgeStroke = Color(0xFFF3E9E0)

    val HourDotColor = Color(0xFFD9631E)
    val MinuteDotColor = Color(0xFFE38955)

    val MinuteHandColor = Color(0xFF3E2214)
    val HourHandColor = Color(0xFF1C1008)
    val SecondHandColor = Color(0xFFB44707)

    val ShadowHour = Color(0xFF220E03)
    val ShadowMinute = Color(0xFF381902)
    val ShadowSecond = Color(0xFF4A1C02)
}

/**
 * Composable hiển thị Logo Icon của ứng dụng ReMind trực tiếp bằng Canvas,
 * là Single Source of Truth cho cả trạng thái tĩnh lẫn chuyển động.
 *
 * Tất cả state animation chỉ được đọc trong pha Draw (`drawWithCache`) để tránh
 * recompose ở mỗi frame, đồng thời cache sẵn Path & Brush để không cấp phát bộ nhớ khi vẽ.
 *
 * @param modifier Modifier tùy chỉnh layout
 * @param size Kích thước icon (mặc định 88.dp)
 * @param animated Bật hiệu ứng chuyển động (chuông rung nhẹ, lá cam đung đưa, múi cam hiện dần và kim xoay)
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
        val wedgeProgress = remember { Animatable(if (isPreview) 1f else 0f) }
        val handProgress = remember { Animatable(if (isPreview) 1f else 0f) }
        val secondHandProgress = remember { Animatable(if (isPreview) 1f else 0f) }
        val leafSway = remember { Animatable(if (isPreview) 0f else -14f) }
        val bellRingProgress = remember { Animatable(if (isPreview) 1f else 0f) }
        val highlightProgress = remember { Animatable(if (isPreview) 1f else 0f) }

        LaunchedEffect(Unit) {
            if (!isPreview) {
                // Chờ 1 nhịp ngắn để Activity & theme ổn định sau khi launch
                delay(180)

                // 1. Múi cam bên trong mặt đồng hồ bung sáng êm ái theo vòng
                launch {
                    wedgeProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 850,
                            easing = FastOutSlowInEasing
                        )
                    )
                }

                // 2. Lá cam trên đỉnh đung đưa mềm mại
                launch {
                    delay(120)
                    leafSway.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(
                            dampingRatio = 0.55f,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                }

                // 3. Hai tai chuông rung nhẹ liên tục theo sóng sin tắt dần
                launch {
                    delay(140)
                    bellRingProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 900,
                            easing = LinearOutSlowInEasing
                        )
                    )
                }

                // 4. Kim giờ & kim phút xoay từ từ vào vị trí 7:00
                launch {
                    delay(100)
                    handProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 950,
                            easing = FastOutSlowInEasing
                        )
                    )
                }

                // 5. Kim giây lướt mượt vào vị trí
                launch {
                    delay(200)
                    secondHandProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 950,
                            easing = FastOutSlowInEasing
                        )
                    )
                }

                // 6. Điểm sáng thủy tinh hiện dần khi mặt đồng hồ ổn định
                launch {
                    delay(500)
                    highlightProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing
                        )
                    )
                }
            }
        }

        AppIconCanvas(
            modifier = semanticsModifier.size(size),
            wedgeProgressProvider = { wedgeProgress.value },
            handProgressProvider = { handProgress.value },
            secondHandProgressProvider = { secondHandProgress.value },
            leafSwayProvider = { leafSway.value },
            bellJiggleProvider = {
                val p = bellRingProgress.value
                (sin(p * 3.0 * PI) * (1f - p)).toFloat()
            },
            highlightProgressProvider = { highlightProgress.value }
        )
    } else {
        AppIconCanvas(
            modifier = semanticsModifier.size(size),
            wedgeProgressProvider = { 1f },
            handProgressProvider = { 1f },
            secondHandProgressProvider = { 1f },
            leafSwayProvider = { 0f },
            bellJiggleProvider = { 0f },
            highlightProgressProvider = { 1f }
        )
    }
}

@Composable
private fun AppIconCanvas(
    modifier: Modifier,
    wedgeProgressProvider: () -> Float,
    handProgressProvider: () -> Float,
    secondHandProgressProvider: () -> Float,
    leafSwayProvider: () -> Float,
    bellJiggleProvider: () -> Float,
    highlightProgressProvider: () -> Float
) {
    Spacer(
        modifier = modifier.drawWithCache {
            val s = size.minDimension / 390f
            val canvasCenter = Offset(size.width / 2f, size.height / 2f)

            fun pt(x: Float, y: Float): Offset = Offset(
                x = canvasCenter.x + (x - 256f) * s,
                y = canvasCenter.y + (y - 228f) * s
            )

            val clockCenter = pt(256f, 256f)

            // Cache Path & Brush theo kích thước Canvas (chỉ tính 1 lần)
            val leftBellPath = Path().apply {
                val p0 = pt(152f, 126f)
                moveTo(p0.x, p0.y)
                val c1 = pt(124f, 152f); val c2 = pt(114f, 186f); val e1 = pt(116f, 200f)
                cubicTo(c1.x, c1.y, c2.x, c2.y, e1.x, e1.y)
                val c3 = pt(128f, 202f); val c4 = pt(164f, 192f); val e2 = pt(188f, 166f)
                cubicTo(c3.x, c3.y, c4.x, c4.y, e2.x, e2.y)
                val c5 = pt(200f, 152f); val c6 = pt(166f, 114f)
                cubicTo(c5.x, c5.y, c6.x, c6.y, p0.x, p0.y)
                close()
            }
            val leftBellBrush = Brush.linearGradient(
                colors = listOf(AppIconDefaults.OuterBodyMid, AppIconDefaults.BellDark),
                start = pt(115.75f, 123.69f),
                end = pt(190.52f, 200.25f)
            )
            val leftBellPivot = pt(176f, 174f)

            val rightBellPath = Path().apply {
                val p0 = pt(360f, 126f)
                moveTo(p0.x, p0.y)
                val c1 = pt(388f, 152f); val c2 = pt(398f, 186f); val e1 = pt(396f, 200f)
                cubicTo(c1.x, c1.y, c2.x, c2.y, e1.x, e1.y)
                val c3 = pt(384f, 202f); val c4 = pt(348f, 192f); val e2 = pt(324f, 166f)
                cubicTo(c3.x, c3.y, c4.x, c4.y, e2.x, e2.y)
                val c5 = pt(312f, 152f); val c6 = pt(346f, 114f)
                cubicTo(c5.x, c5.y, c6.x, c6.y, p0.x, p0.y)
                close()
            }
            val rightBellBrush = Brush.linearGradient(
                colors = listOf(AppIconDefaults.OuterBodyMid, AppIconDefaults.BellDark),
                start = pt(321.48f, 123.69f),
                end = pt(396.25f, 200.25f)
            )
            val rightBellPivot = pt(336f, 174f)

            val stemTopLeft = pt(246f, 66f)
            val stemSize = Size(20f * s, 34f * s)
            val stemCorner = CornerRadius(10f * s, 10f * s)

            val leafPath = Path().apply {
                val p0 = pt(264f, 76f)
                moveTo(p0.x, p0.y)
                val c1 = pt(304f, 60f); val c2 = pt(326f, 80f); val e1 = pt(322f, 100f)
                cubicTo(c1.x, c1.y, c2.x, c2.y, e1.x, e1.y)
                val c3 = pt(304f, 106f); val c4 = pt(270f, 100f)
                cubicTo(c3.x, c3.y, c4.x, c4.y, p0.x, p0.y)
                close()
            }
            val leafBrush = Brush.linearGradient(
                colors = listOf(AppIconDefaults.LeafLight, AppIconDefaults.LeafDark),
                start = pt(264f, 70.19f),
                end = pt(322.47f, 102.16f)
            )
            val leafPivot = pt(264f, 82f)

            val footSize = Size(24f * s, 36f * s)
            val footRadius = CornerRadius(12f * s, 12f * s)
            val leftFootCenter = pt(170f, 378f)
            val leftFootTopLeft = Offset(
                leftFootCenter.x - footSize.width / 2f,
                leftFootCenter.y - footSize.height / 2f
            )
            val rightFootCenter = pt(342f, 378f)
            val rightFootTopLeft = Offset(
                rightFootCenter.x - footSize.width / 2f,
                rightFootCenter.y - footSize.height / 2f
            )

            val bodyBrush = Brush.linearGradient(
                colorStops = arrayOf(
                    0f to AppIconDefaults.OuterBodyLight,
                    0.5f to AppIconDefaults.OuterBodyMid,
                    1f to AppIconDefaults.OuterBodyDark
                ),
                start = pt(104f, 104f),
                end = pt(408f, 408f)
            )
            val faceBrush = Brush.radialGradient(
                colorStops = arrayOf(
                    0f to AppIconDefaults.FaceCenter,
                    0.85f to AppIconDefaults.FaceCenter,
                    1f to AppIconDefaults.FaceEdge
                ),
                center = pt(244.96f, 244.96f),
                radius = 171.12f * s
            )

            val rIn = 30f * s
            val rOut = 110f * s
            val halfInAngle = 5.7 * (PI / 180.0)
            val halfOutAngle = 9.5 * (PI / 180.0)
            val wedgeRectOut = Rect(
                left = clockCenter.x - rOut,
                top = clockCenter.y - rOut,
                right = clockCenter.x + rOut,
                bottom = clockCenter.y + rOut
            )
            val wedgePath = Path().apply {
                val baseAngle = -PI / 2.0 + 15.0 * (PI / 180.0)
                val inStartX = clockCenter.x + (rIn * cos(baseAngle - halfInAngle)).toFloat()
                val inStartY = clockCenter.y + (rIn * sin(baseAngle - halfInAngle)).toFloat()
                val outStartX = clockCenter.x + (rOut * cos(baseAngle - halfOutAngle)).toFloat()
                val outStartY = clockCenter.y + (rOut * sin(baseAngle - halfOutAngle)).toFloat()
                val inEndX = clockCenter.x + (rIn * cos(baseAngle + halfInAngle)).toFloat()
                val inEndY = clockCenter.y + (rIn * sin(baseAngle + halfInAngle)).toFloat()

                moveTo(inStartX, inStartY)
                lineTo(outStartX, outStartY)
                arcTo(
                    rect = wedgeRectOut,
                    startAngleDegrees = -75f - 9.5f,
                    sweepAngleDegrees = 19f,
                    forceMoveTo = false
                )
                lineTo(inEndX, inEndY)
                close()
            }
            val wedgeStroke = Stroke(width = 1.8f * s)

            // Cache sẵn tọa độ 60 chấm phút/giờ
            val dotOffsets = Array(60) { i ->
                val angleRad = i * 6.0 * (PI / 180.0)
                Offset(
                    x = clockCenter.x + (126f * s * sin(angleRad)).toFloat(),
                    y = clockCenter.y - (126f * s * cos(angleRad)).toFloat()
                )
            }
            val minuteDotColor = AppIconDefaults.MinuteDotColor.copy(alpha = 0.88f)
            val hourDotRadius = 4.5f * s
            val minuteDotRadius = 1.9f * s

            val shadowOffset = Offset(2.5f * s, 4.5f * s)
            val shadowCenter = clockCenter + shadowOffset
            val minuteEnd = pt(256f, 160f)
            val hourEnd = pt(219f, 320.1f)
            val secondStart = pt(237f, 240f)
            val secondEnd = pt(328.8f, 321.6f)
            val highlightCenter = pt(330f, 182f)

            onDrawBehind {
                val bellJiggle = bellJiggleProvider()
                val leafSwayDeg = leafSwayProvider()
                val wedgeProgress = wedgeProgressProvider()
                val handProgress = handProgressProvider()
                val secondHandProgress = secondHandProgressProvider()
                val highlightProgress = highlightProgressProvider()

                // 1. HAI TAI CHUÔNG BÁO THỨC
                rotate(degrees = -bellJiggle * 6f, pivot = leftBellPivot) {
                    drawPath(path = leftBellPath, brush = leftBellBrush)
                }
                rotate(degrees = bellJiggle * 6f, pivot = rightBellPivot) {
                    drawPath(path = rightBellPath, brush = rightBellBrush)
                }

                // 2. CUỐNG CAM & LÁ XANH TRÊN ĐỈNH
                drawRoundRect(
                    color = AppIconDefaults.StemColor,
                    topLeft = stemTopLeft,
                    size = stemSize,
                    cornerRadius = stemCorner
                )
                rotate(degrees = leafSwayDeg, pivot = leafPivot) {
                    drawPath(path = leafPath, brush = leafBrush)
                }

                // 3. HAI CHÂN ĐẾ DƯỚI ĐÁY
                rotate(degrees = 25f, pivot = leftFootCenter) {
                    drawRoundRect(
                        color = AppIconDefaults.FootColor,
                        topLeft = leftFootTopLeft,
                        size = footSize,
                        cornerRadius = footRadius
                    )
                }
                rotate(degrees = -25f, pivot = rightFootCenter) {
                    drawRoundRect(
                        color = AppIconDefaults.FootColor,
                        topLeft = rightFootTopLeft,
                        size = footSize,
                        cornerRadius = footRadius
                    )
                }

                // 4. VÒNG VỎ CAM & MẶT ĐỒNG HỒ
                drawCircle(brush = bodyBrush, radius = 152f * s, center = clockCenter)
                drawCircle(brush = faceBrush, radius = 138f * s, center = clockCenter)

                // 5. 12 MÚI CAM BÊN TRONG MẶT ĐỒNG HỒ
                for (i in 0 until 12) {
                    val itemProgress = ((wedgeProgress * 12f - i * 0.45f) / 6.5f).coerceIn(0f, 1f)
                    if (itemProgress > 0f) {
                        rotate(degrees = i * 30f, pivot = clockCenter) {
                            drawPath(
                                path = wedgePath,
                                color = AppIconDefaults.WedgeFill.copy(alpha = 0.38f * itemProgress)
                            )
                            drawPath(
                                path = wedgePath,
                                color = AppIconDefaults.WedgeStroke.copy(alpha = 0.38f * itemProgress),
                                style = wedgeStroke
                            )
                        }
                    }
                }

                // 6. 60 VẠCH PHÚT & 12 CHẤM GIỜ
                for (i in 0 until 60) {
                    if (i % 5 == 0) {
                        drawCircle(
                            color = AppIconDefaults.HourDotColor,
                            radius = hourDotRadius,
                            center = dotOffsets[i]
                        )
                    } else {
                        drawCircle(
                            color = minuteDotColor,
                            radius = minuteDotRadius,
                            center = dotOffsets[i]
                        )
                    }
                }

                // 7. KIM ĐỒNG HỒ (KÈM BÓNG ĐỔ MỀM)
                val minuteRotation = -42f * (1f - handProgress)
                val hourRotation = -18f * (1f - handProgress)
                val secondRotation = -72f * (1f - secondHandProgress)

                rotate(degrees = minuteRotation, pivot = shadowCenter) {
                    drawLine(
                        color = AppIconDefaults.ShadowMinute.copy(alpha = 0.28f),
                        start = shadowCenter,
                        end = minuteEnd + shadowOffset,
                        strokeWidth = 7.5f * s,
                        cap = StrokeCap.Round
                    )
                }
                rotate(degrees = hourRotation, pivot = shadowCenter) {
                    drawLine(
                        color = AppIconDefaults.ShadowHour.copy(alpha = 0.28f),
                        start = shadowCenter,
                        end = hourEnd + shadowOffset,
                        strokeWidth = 11.5f * s,
                        cap = StrokeCap.Round
                    )
                }
                rotate(degrees = secondRotation, pivot = shadowCenter) {
                    drawLine(
                        color = AppIconDefaults.ShadowSecond.copy(alpha = 0.28f),
                        start = secondStart + shadowOffset,
                        end = secondEnd + shadowOffset,
                        strokeWidth = 2.4f * s,
                        cap = StrokeCap.Round
                    )
                }
                drawCircle(
                    color = AppIconDefaults.ShadowHour.copy(alpha = 0.28f),
                    radius = 10.5f * s,
                    center = shadowCenter
                )

                rotate(degrees = minuteRotation, pivot = clockCenter) {
                    drawLine(
                        color = AppIconDefaults.MinuteHandColor,
                        start = clockCenter,
                        end = minuteEnd,
                        strokeWidth = 7.5f * s,
                        cap = StrokeCap.Round
                    )
                }
                rotate(degrees = hourRotation, pivot = clockCenter) {
                    drawLine(
                        color = AppIconDefaults.HourHandColor,
                        start = clockCenter,
                        end = hourEnd,
                        strokeWidth = 11.5f * s,
                        cap = StrokeCap.Round
                    )
                }
                rotate(degrees = secondRotation, pivot = clockCenter) {
                    drawLine(
                        color = AppIconDefaults.SecondHandColor,
                        start = secondStart,
                        end = secondEnd,
                        strokeWidth = 2.4f * s,
                        cap = StrokeCap.Round
                    )
                }

                // 8. TÂM ĐỒNG HỒ ĐA LỚP
                drawCircle(color = AppIconDefaults.HourHandColor, radius = 10f * s, center = clockCenter)
                drawCircle(color = AppIconDefaults.MinuteHandColor, radius = 6.6f * s, center = clockCenter)
                drawCircle(color = AppIconDefaults.OuterBodyMid, radius = 3.4f * s, center = clockCenter)
                drawCircle(color = Color.White, radius = 1.3f * s, center = clockCenter)

                // 9. ĐIỂM SÁNG PHẢN CHIẾU MẶT KÍNH
                if (highlightProgress > 0f) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.82f * highlightProgress),
                        radius = 7.5f * s * highlightProgress,
                        center = highlightCenter
                    )
                }
            }
        }
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
