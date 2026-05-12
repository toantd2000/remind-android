package vn.io.litever.remind.core.ads.impl.ui

import android.widget.Button
import android.widget.ImageView
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import vn.io.litever.designsystem.theme.LiteverShapes
import vn.io.litever.remind.core.ads.api.AdPlacement
import vn.io.litever.remind.core.ads.impl.AdMobManagerImpl
import com.google.android.gms.ads.nativead.NativeAdView as GmsNativeAdView

@Composable
internal fun AdMobNativeAdView(
    placement: AdPlacement,
    adManager: AdMobManagerImpl,
    modifier: Modifier = Modifier
) {
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var adFailed by remember { mutableStateOf(false) }

    val lifecycle = androidx.compose.ui.platform.LocalLifecycleOwner.current.lifecycle

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
                delay(120_000L) // Tải lại sau mỗi 2 phút nếu người dùng vẫn đang ở màn hình
            }
        }
    }

    if (adFailed) return

    val isLarge = false
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
        modifier = if (isFillSpace) modifier.fillMaxSize().clip(LiteverShapes.small)
                   else modifier.fillMaxWidth().clip(LiteverShapes.small),
        shape = LiteverShapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        content = content
    )
}

@Composable
private fun NativeAdContent(nativeAd: NativeAd, isLarge: Boolean = false, isFillSpace: Boolean = false) {
    val colorOnSurface = MaterialTheme.colorScheme.onSurface
    val colorOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val colorPrimary = MaterialTheme.colorScheme.primary

    AndroidView(
        modifier = if (isFillSpace) Modifier.fillMaxSize() else Modifier.fillMaxWidth(),
        factory = { ctx ->
            val adView = GmsNativeAdView(ctx)
            
            val container = if (isLarge) {
                createLargeAdLayout(ctx, adView, isFillSpace, colorOnSurface, colorOnSurfaceVariant, colorPrimary)
            } else {
                createSmallAdLayout(ctx, adView, colorOnSurface, colorOnSurfaceVariant, colorPrimary)
            }

            adView.addView(container)
            adView
        },
        update = { adView ->
            (adView.headlineView as TextView).apply {
                text = nativeAd.headline
                setTextColor(colorOnSurface.toArgb())
            }

            (adView.bodyView as TextView).apply {
                text = nativeAd.body
                setTextColor(colorOnSurfaceVariant.toArgb())
            }

            (adView.iconView as ImageView).apply {
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
                (background as? android.graphics.drawable.GradientDrawable)?.let { bg ->
                    if (isLarge) {
                        bg.setStroke((1f * context.resources.displayMetrics.density).toInt(), colorPrimary.toArgb())
                        bg.setColor(android.graphics.Color.TRANSPARENT)
                        setTextColor(colorPrimary.toArgb())
                    } else {
                        bg.setColor(colorPrimary.toArgb())
                        setTextColor(android.graphics.Color.WHITE)
                    }
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
    colorOnSurface: androidx.compose.ui.graphics.Color,
    colorOnSurfaceVariant: androidx.compose.ui.graphics.Color,
    colorPrimary: androidx.compose.ui.graphics.Color
): android.widget.LinearLayout {
    val container = android.widget.LinearLayout(ctx).apply {
        orientation = android.widget.LinearLayout.VERTICAL
        setPadding(16 * 3, 16 * 3, 16 * 3, 16 * 3)
        layoutParams = android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            if (isFillSpace) android.widget.FrameLayout.LayoutParams.MATCH_PARENT else android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
        )
        gravity = if (isFillSpace) android.view.Gravity.CENTER else android.view.Gravity.CENTER_VERTICAL
    }

    val topRow = android.widget.LinearLayout(ctx).apply {
        orientation = android.widget.LinearLayout.HORIZONTAL
        layoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        )
        gravity = android.view.Gravity.CENTER_VERTICAL
    }

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

    val headlineView = TextView(ctx).apply {
        textSize = 16f
        setTextColor(colorOnSurface.toArgb())
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        maxLines = 1
        ellipsize = android.text.TextUtils.TruncateAt.END
    }
    topRow.addView(headlineView)
    adView.headlineView = headlineView

    container.addView(topRow)

    val mediaView = com.google.android.gms.ads.nativead.MediaView(ctx).apply {
        layoutParams = if (isFillSpace) {
            android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        } else {
            android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                minimumHeight = (150 * ctx.resources.displayMetrics.density).toInt()
            }
        }.apply {
            topMargin = 12 * 3
            bottomMargin = 12 * 3
        }
        outlineProvider = roundedOutlineProvider
        clipToOutline = true
    }
    container.addView(mediaView)
    adView.mediaView = mediaView

    val bodyView = TextView(ctx).apply {
        textSize = 13f
        setTextColor(colorOnSurfaceVariant.toArgb())
        maxLines = 2
        ellipsize = android.text.TextUtils.TruncateAt.END
    }
    container.addView(bodyView)
    adView.bodyView = bodyView

    val ctaButton = Button(ctx, null, android.R.attr.buttonStyleSmall).apply {
        layoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 12 * 3
        }
        textSize = 14f
        isAllCaps = false
        val radius = 8f * ctx.resources.displayMetrics.density
        val strokeWidth = (1f * ctx.resources.displayMetrics.density).toInt()
        background = android.graphics.drawable.GradientDrawable().apply {
            setStroke(strokeWidth, colorPrimary.toArgb())
            setColor(android.graphics.Color.TRANSPARENT)
            cornerRadius = radius
        }
        setTextColor(colorPrimary.toArgb())
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
    colorPrimary: androidx.compose.ui.graphics.Color
): android.widget.LinearLayout {
    val container = android.widget.LinearLayout(ctx).apply {
        orientation = android.widget.LinearLayout.HORIZONTAL
        setPadding(16 * 3, 16 * 3, 16 * 3, 16 * 3)
        layoutParams = android.widget.FrameLayout.LayoutParams(
            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
        )
        gravity = android.view.Gravity.CENTER_VERTICAL
    }

    val iconView = ImageView(ctx).apply {
        layoutParams = android.widget.LinearLayout.LayoutParams(48 * 3, 48 * 3).apply {
            marginEnd = 16 * 3
        }
        scaleType = ImageView.ScaleType.FIT_CENTER
        outlineProvider = roundedOutlineProvider
        clipToOutline = true
    }
    container.addView(iconView)
    adView.iconView = iconView

    val contentColumn = android.widget.LinearLayout(ctx).apply {
        orientation = android.widget.LinearLayout.VERTICAL
        layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
    }

    val headlineView = TextView(ctx).apply {
        textSize = 15f
        setTextColor(colorOnSurface.toArgb())
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        maxLines = 1
        ellipsize = android.text.TextUtils.TruncateAt.END
    }
    contentColumn.addView(headlineView)
    adView.headlineView = headlineView

    val bodyView = TextView(ctx).apply {
        textSize = 13f
        setTextColor(colorOnSurfaceVariant.toArgb())
        maxLines = 2
        ellipsize = android.text.TextUtils.TruncateAt.END
        setPadding(0, 4 * 3, 0, 0)
    }
    contentColumn.addView(bodyView)
    adView.bodyView = bodyView

    container.addView(contentColumn)

    val ctaButton = Button(ctx, null, android.R.attr.buttonStyleSmall).apply {
        layoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            marginStart = 12 * 3
        }
        textSize = 12f
        isAllCaps = false
        val radius = 8f * ctx.resources.displayMetrics.density
        background = android.graphics.drawable.GradientDrawable().apply {
            setColor(colorPrimary.toArgb())
            cornerRadius = radius
        }
        setTextColor(android.graphics.Color.WHITE)
    }
    container.addView(ctaButton)
    adView.callToActionView = ctaButton

    return container
}
