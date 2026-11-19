package vn.io.litever.remind.core.ads.impl.ui

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.ads.api.AdPlacement
import vn.io.litever.remind.core.ads.impl.AdMobManagerImpl
import kotlin.time.Duration.Companion.milliseconds
import com.google.android.gms.ads.nativead.NativeAdView as GmsNativeAdView

/**
 * Compact 1-line Native Ad strip (~40dp) anchored near bottom navigation / action bars,
 * styled to match the app's surface without disrupting user interaction.
 */
@Composable
internal fun AdMobMicroAdStrip(
    placement: AdPlacement,
    adManager: AdMobManagerImpl,
    modifier: Modifier = Modifier
) {
    var nativeAd by remember { mutableStateOf<NativeAd?>(adManager.cachedAds[placement]) }
    var adFailed by remember { mutableStateOf(false) }

    val lifecycle = LocalLifecycleOwner.current.lifecycle

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
                delay(60_000L.milliseconds) // Refresh every 60s per AdMob banner/strip best practice
            }
        }
    }

    val currentAd = nativeAd
    if (adFailed || currentAd == null) return

    val colorOnSurface = LiteverTheme.colors.onSurface
    val colorOnSurfaceVariant = LiteverTheme.colors.onSurfaceVariant
    val colorPrimary = LiteverTheme.colors.primary
    val colorBadgeBg = LiteverTheme.colors.surfaceVariant
    val fallbackCta = stringResource(vn.io.litever.remind.core.ads.impl.R.string.ad_cta_open)

    val fontFamilyResolver = LocalFontFamilyResolver.current
    val titleFontFamily = LiteverTheme.typography.labelMedium.fontFamily
    val headlineTypeface = remember(fontFamilyResolver, titleFontFamily) {
        fontFamilyResolver.resolve(
            fontFamily = titleFontFamily,
            fontWeight = FontWeight.Medium
        ).value as? Typeface
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = LiteverTheme.colors.outlineVariant.copy(alpha = 0.4f)
            )
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                factory = { ctx ->
                    val adView = GmsNativeAdView(ctx)
                    val row = createMicroStripLayout(
                        ctx = ctx,
                        adView = adView,
                        colorOnSurface = colorOnSurface,
                        colorOnSurfaceVariant = colorOnSurfaceVariant,
                        colorPrimary = colorPrimary,
                        colorBadgeBg = colorBadgeBg,
                        headlineTypeface = headlineTypeface
                    )
                    adView.addView(row)
                    adView
                },
                update = { adView ->
                    (adView.headlineView as TextView).apply {
                        text = currentAd.headline ?: currentAd.body.orEmpty()
                        setTextColor(colorOnSurface.toArgb())
                        headlineTypeface?.let { typeface = it }
                    }

                    (adView.iconView as? ImageView)?.apply {
                        val iconDrawable = currentAd.icon?.drawable ?: currentAd.images.firstOrNull()?.drawable
                        if (iconDrawable != null) {
                            setImageDrawable(iconDrawable)
                            visibility = View.VISIBLE
                        } else {
                            visibility = View.GONE
                        }
                    }

                    (adView.callToActionView as TextView).apply {
                        val cta = currentAd.callToAction?.takeIf { it.isNotBlank() } ?: fallbackCta
                        text = "$cta ›"
                        setTextColor(colorPrimary.toArgb())
                        headlineTypeface?.let { typeface = it }
                    }

                    adView.setNativeAd(currentAd)
                }
            )
        }
    }
}

private fun createMicroStripLayout(
    ctx: android.content.Context,
    adView: GmsNativeAdView,
    colorOnSurface: Color,
    colorOnSurfaceVariant: Color,
    colorPrimary: Color,
    colorBadgeBg: Color,
    headlineTypeface: Typeface?
): LinearLayout {
    val density = ctx.resources.displayMetrics.density
    val container = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val padH = (12 * density).toInt()
        setPadding(padH, 0, padH, 0)
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
    }

    val adBadge = TextView(ctx).apply {
        text = "AD"
        textSize = 9f
        setTextColor(colorOnSurfaceVariant.toArgb())
        typeface = Typeface.DEFAULT_BOLD
        val padH = (5 * density).toInt()
        val padV = (1 * density).toInt()
        setPadding(padH, padV, padH, padV)
        background = GradientDrawable().apply {
            setColor(colorBadgeBg.toArgb())
            cornerRadius = 4f * density
        }
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            marginEnd = (8 * density).toInt()
        }
    }
    container.addView(adBadge)

    val iconView = ImageView(ctx).apply {
        val size = (20 * density).toInt()
        layoutParams = LinearLayout.LayoutParams(size, size).apply {
            marginEnd = (8 * density).toInt()
        }
        scaleType = ImageView.ScaleType.FIT_CENTER
        clipToOutline = true
    }
    container.addView(iconView)
    adView.iconView = iconView

    val headlineView = TextView(ctx).apply {
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginEnd = (8 * density).toInt()
        }
        textSize = 12f
        setTextColor(colorOnSurface.toArgb())
        headlineTypeface?.let { typeface = it }
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }
    container.addView(headlineView)
    adView.headlineView = headlineView

    val ctaView = TextView(ctx).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        textSize = 12f
        setTextColor(colorPrimary.toArgb())
        typeface = headlineTypeface ?: Typeface.DEFAULT_BOLD
        maxLines = 1
    }
    container.addView(ctaView)
    adView.callToActionView = ctaView

    return container
}
