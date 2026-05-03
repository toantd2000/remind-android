package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView as GmsNativeAdView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import vn.io.litever.remind.core.designsystem.R
import dagger.hilt.EntryPoints
import vn.io.litever.remind.core.common.ads.NativeAdManagerEntryPoint



@Composable
fun NativeAdView(
    adId: String?,
    modifier: Modifier = Modifier
) {
    if (adId.isNullOrBlank()) return

    val context = LocalContext.current
    val adManager = remember(context) {
        EntryPoints.get(
            context.applicationContext,
            NativeAdManagerEntryPoint::class.java
        ).nativeAdManager()
    }

    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var adFailed by remember { mutableStateOf(false) }

    LaunchedEffect(adId) {
        adManager.loadAd(adId) { ad ->
            if (ad != null) {
                nativeAd = ad
            } else {
                adFailed = true
            }
        }
    }

    if (adFailed) return


    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        if (nativeAd != null) {
            NativeAdContent(nativeAd!!)
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun NativeAdContent(nativeAd: NativeAd) {
    val colorOnSurface = MaterialTheme.colorScheme.onSurface
    val colorOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val colorPrimary = MaterialTheme.colorScheme.primary

    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { ctx ->
            val adView = GmsNativeAdView(ctx)
            val container = android.widget.LinearLayout(ctx).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                setPadding(16 * 3, 16 * 3, 16 * 3, 16 * 3) // Convert dp to px roughly by *3
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = android.view.Gravity.CENTER_VERTICAL
            }

            // Icon
            val iconView = ImageView(ctx).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(48 * 3, 48 * 3).apply {
                    marginEnd = 16 * 3
                }
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            container.addView(iconView)
            adView.iconView = iconView

            // Content Column
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

            // CTA Button
            val ctaButton = Button(ctx, null, android.R.attr.buttonStyleSmall).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = 12 * 3
                }
                textSize = 12f
                isAllCaps = false
                setBackgroundColor(colorPrimary.toArgb())
                setTextColor(android.graphics.Color.WHITE)
            }
            container.addView(ctaButton)
            adView.callToActionView = ctaButton

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
                if (nativeAd.icon != null) {
                    setImageDrawable(nativeAd.icon?.drawable)
                    visibility = android.view.View.VISIBLE
                } else {
                    visibility = android.view.View.GONE
                }
            }

            (adView.callToActionView as Button).apply {
                text = nativeAd.callToAction
                setBackgroundColor(colorPrimary.toArgb())
            }

            adView.setNativeAd(nativeAd)
        }
    )
}

