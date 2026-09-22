package vn.io.litever.remind.core.ads.impl.ui

import android.graphics.Typeface
import android.text.TextUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import vn.io.litever.designsystem.theme.LiteverShapes
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.ads.api.AdPlacement
import vn.io.litever.remind.core.ads.impl.AdMobManagerImpl
import vn.io.litever.remind.core.designsystem.theme.neutralContainer
import vn.io.litever.remind.core.designsystem.theme.onNeutral
import kotlin.time.Duration.Companion.milliseconds
import com.google.android.gms.ads.nativead.NativeAdView as GmsNativeAdView

@Composable
internal fun AdMobNativeAdView(
    placement: AdPlacement,
    adManager: AdMobManagerImpl,
    modifier: Modifier = Modifier
) {
    var nativeAd by remember { mutableStateOf<NativeAd?>(adManager.cachedAds[placement]) }
    var adFailed by remember { mutableStateOf(false) }

    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(placement, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (isActive) {
                adManager.loadNativeAd(placement) { ad ->
                    if (ad != null) {
                        nativeAd = ad
                        adFailed = false
                    } else {
                        adFailed = true
                    }
                }
                delay(120_000L.milliseconds) // Tải lại sau mỗi 2 phút nếu người dùng vẫn đang ở màn hình
            }
        }
    }

    if (adFailed) return
    
    // Customize ad size based on placement to avoid UI duplication
    val isLarge = when (placement) {
        AdPlacement.MESSAGE_NATIVE -> true // Make alarm message ad larger to stand out
        else -> false                      // TodayScreen and AlarmList should use small/compact
    }
    
    val isFillSpace = false

    if (nativeAd != null) {
        AdNativeContainer(modifier = modifier, isFillSpace = isFillSpace) {
            NativeAdContent(nativeAd!!, isLarge = isLarge, isFillSpace = isFillSpace)
        }
    }
}

@Composable
private fun AdNativeContainer(
    modifier: Modifier = Modifier,
    isFillSpace: Boolean = false,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = if (isFillSpace) modifier.fillMaxSize().clip(LiteverTheme.shapes.large)
                   else modifier.fillMaxWidth().clip(LiteverTheme.shapes.large),
        shape = LiteverTheme.shapes.large,
        color = LiteverTheme.colors.surfaceContainerLow,
        content = content
    )
}

@Composable
private fun NativeAdContent(nativeAd: NativeAd, isLarge: Boolean = false, isFillSpace: Boolean = false) {
    val colorOnSurface = LiteverTheme.colors.onSurface
    val colorOnSurfaceVariant = LiteverTheme.colors.onSurfaceVariant
    val colorPrimary = LiteverTheme.colors.secondary
    val colorOnPrimary = LiteverTheme.colors.onSecondary

    val fontFamilyResolver = LocalFontFamilyResolver.current
    val typography = LiteverTheme.typography
    val titleFontFamily = typography.titleMedium.fontFamily
    val bodyFontFamily = typography.bodyMedium.fontFamily

    val headlineTypeface = remember(fontFamilyResolver, titleFontFamily) {
        fontFamilyResolver.resolve(
            fontFamily = titleFontFamily,
            fontWeight = FontWeight.Bold
        ).value as? Typeface
    }

    val bodyTypeface = remember(fontFamilyResolver, bodyFontFamily) {
        fontFamilyResolver.resolve(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Normal
        ).value as? Typeface
    }

    AndroidView(
        modifier = if (isFillSpace) Modifier.fillMaxSize() else Modifier.fillMaxWidth(),
        factory = { ctx ->
            val adView = GmsNativeAdView(ctx)
            
            val container = if (isLarge) {
                createLargeAdLayout(ctx, adView, isFillSpace, colorOnSurface, colorOnSurfaceVariant, colorPrimary, colorOnPrimary, headlineTypeface, bodyTypeface)
            } else {
                createSmallAdLayout(ctx, adView, colorOnSurface, colorOnSurfaceVariant, colorPrimary, headlineTypeface, bodyTypeface)
            }

            adView.addView(container)
            adView
        },
        update = { adView ->
            (adView.headlineView as TextView).apply {
                text = nativeAd.headline
                setTextColor(colorOnSurface.toArgb())
                headlineTypeface?.let { typeface = it }
            }

            (adView.bodyView as TextView).apply {
                text = nativeAd.body
                setTextColor(colorOnSurfaceVariant.toArgb())
                bodyTypeface?.let { typeface = it }
            }

            (adView.iconView as? ImageView)?.apply {
                val iconDrawable = nativeAd.icon?.drawable ?: nativeAd.images.firstOrNull()?.drawable
                if (iconDrawable != null) {
                    setImageDrawable(iconDrawable)
                    visibility = android.view.View.VISIBLE
                } else {
                    visibility = android.view.View.GONE
                }
            }

            (adView.callToActionView as Button).apply {
                text = nativeAd.callToAction
                headlineTypeface?.let { typeface = it }
                (background as? android.graphics.drawable.GradientDrawable)?.let { bg ->
                    bg.setColor(colorPrimary.toArgb())
                    setTextColor(colorOnPrimary.toArgb())
                }
            }

            adView.mediaView?.let { mv ->
                if (nativeAd.mediaContent != null) {
                    mv.mediaContent = nativeAd.mediaContent
                    mv.visibility = android.view.View.VISIBLE
                } else {
                    mv.visibility = android.view.View.GONE
                }
            }

            adView.setNativeAd(nativeAd)
        }
    )
}

private val roundedOutlineProvider = object : android.view.ViewOutlineProvider() {
    override fun getOutline(view: android.view.View, outline: android.graphics.Outline) {
        val radius = 8f * view.context.resources.displayMetrics.density
        outline.setRoundRect(0, 0, view.width, view.height, radius)
    }
}

private fun createLargeAdLayout(
    ctx: android.content.Context,
    adView: GmsNativeAdView,
    isFillSpace: Boolean,
    colorOnSurface: Color,
    colorOnSurfaceVariant: Color,
    colorPrimary: Color,
    colorOnPrimary: Color,
    headlineTypeface: Typeface?,
    bodyTypeface: Typeface?
): LinearLayout {
    val container = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(16 * 3, 16 * 3, 16 * 3, 16 * 3)
        layoutParams = android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            if (isFillSpace) android.widget.FrameLayout.LayoutParams.MATCH_PARENT else android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
        )
        gravity = if (isFillSpace) android.view.Gravity.CENTER else android.view.Gravity.CENTER_VERTICAL
    }

    val topRow = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        gravity = android.view.Gravity.CENTER_VERTICAL
    }

    // Icon nhỏ (Tạm ẩn theo yêu cầu thiết kế gọn gàng cho AlarmMessageScreen)
    /*
    val iconView = ImageView(ctx).apply {
        layoutParams = android.widget.LinearLayout.LayoutParams(40 * 3, 40 * 3).apply {
            marginEnd = 12 * 3
        }
        scaleType = ImageView.ScaleType.FIT_CENTER
        outlineProvider = roundedOutlineProvider
        clipToOutline = true
    }
    topRow.addView(iconView)
    adView.iconView = iconView
    */

    val headlineView = TextView(ctx).apply {
        textSize = 18f // Tăng chữ to hơn
        setTextColor(colorOnSurface.toArgb())
        typeface = headlineTypeface ?: Typeface.DEFAULT_BOLD
        maxLines = 2 // Đổi từ 1 sang 2
        ellipsize = TextUtils.TruncateAt.END
    }
    topRow.addView(headlineView)
    adView.headlineView = headlineView

    container.addView(topRow)

    val mediaView = com.google.android.gms.ads.nativead.MediaView(ctx).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (120 * ctx.resources.displayMetrics.density).toInt() // Cố định chiều cao 120dp
        ).apply {
            topMargin = 12 * 3
            bottomMargin = 12 * 3
        }
        outlineProvider = roundedOutlineProvider
        clipToOutline = true
    }
    container.addView(mediaView)
    adView.mediaView = mediaView

    val bodyView = TextView(ctx).apply {
        textSize = 14f // To hơn một chút
        setTextColor(colorOnSurfaceVariant.toArgb())
        bodyTypeface?.let { typeface = it }
        maxLines = 3 // Đổi từ 2 sang 3
        ellipsize = TextUtils.TruncateAt.END
    }
    container.addView(bodyView)
    adView.bodyView = bodyView

    val ctaButton = Button(ctx).apply {
        layoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT // Trả về bằng button default
        ).apply {
            topMargin = 12 * 3
        }
        textSize = 14f
        isAllCaps = false
        headlineTypeface?.let { typeface = it }
        val radius = 8f * ctx.resources.displayMetrics.density
        background = android.graphics.drawable.GradientDrawable().apply {
            setColor(colorPrimary.toArgb())
            cornerRadius = radius
        }
        setTextColor(colorOnPrimary.toArgb())
    }
    container.addView(ctaButton)
    adView.callToActionView = ctaButton

    return container
}

private fun createSmallAdLayout(
    ctx: android.content.Context,
    adView: GmsNativeAdView,
    colorOnSurface: androidx.compose.ui.graphics.Color,
    colorOnSurfaceVariant: androidx.compose.ui.graphics.Color,
    colorPrimary: Color,
    headlineTypeface: Typeface?,
    bodyTypeface: Typeface?
): LinearLayout {
    val density = ctx.resources.displayMetrics.density
    val container = android.widget.LinearLayout(ctx).apply {
        orientation = android.widget.LinearLayout.VERTICAL
        val pad = (12 * density).toInt()
        setPadding(pad, pad, pad, pad)
        layoutParams = android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
        )
    }

    // --- HÀNG 1: ICON + HEADLINE ---
    val topRow = android.widget.LinearLayout(ctx).apply {
        orientation = android.widget.LinearLayout.HORIZONTAL
        layoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        )
        gravity = android.view.Gravity.CENTER_VERTICAL
    }

    val iconView = ImageView(ctx).apply {
        val size = (24 * density).toInt() // Vuông tầm 24dp tương đối với 2 dòng chữ
        layoutParams = android.widget.LinearLayout.LayoutParams(size, size).apply {
            marginEnd = (8 * density).toInt()
        }
        scaleType = ImageView.ScaleType.FIT_CENTER
        outlineProvider = roundedOutlineProvider
        clipToOutline = true
    }
    topRow.addView(iconView)
    adView.iconView = iconView

    val headlineView = TextView(ctx).apply {
        layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        textSize = 15f
        setTextColor(colorOnSurface.toArgb())
        typeface = headlineTypeface ?: android.graphics.Typeface.DEFAULT_BOLD
        maxLines = 2 // Đổi thành max 2 dòng
        ellipsize = android.text.TextUtils.TruncateAt.END
    }
    topRow.addView(headlineView)
    adView.headlineView = headlineView

    container.addView(topRow)

    // --- HÀNG 2: MÔ TẢ + NÚT CTA ---
    val bottomRow = android.widget.LinearLayout(ctx).apply {
        orientation = android.widget.LinearLayout.HORIZONTAL
        layoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = (6 * density).toInt()
        }
        gravity = android.view.Gravity.CENTER_VERTICAL
    }

    val bodyView = TextView(ctx).apply {
        layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginEnd = (12 * density).toInt()
        }
        textSize = 13f
        setTextColor(colorOnSurfaceVariant.toArgb())
        bodyTypeface?.let { typeface = it }
        maxLines = 2
        ellipsize = android.text.TextUtils.TruncateAt.END
    }
    bottomRow.addView(bodyView)
    adView.bodyView = bodyView

    val ctaButton = Button(ctx, null, android.R.attr.buttonStyleSmall).apply {
        layoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            (32 * density).toInt() // Giảm chiều cao xuống 32dp
        )
        textSize = 11f 
        val padH = (12 * density).toInt()
        setPadding(padH, 0, padH, 0)
        
        isAllCaps = false
        headlineTypeface?.let { typeface = it }
        val radius = 8f * density
        background = android.graphics.drawable.GradientDrawable().apply {
            setColor(colorPrimary.toArgb())
            cornerRadius = radius
        }
        setTextColor(android.graphics.Color.WHITE)
    }
    bottomRow.addView(ctaButton)
    adView.callToActionView = ctaButton

    container.addView(bottomRow)

    return container
}
