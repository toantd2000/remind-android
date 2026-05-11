package vn.io.litever.remind.core.ads.api

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.StateFlow

interface AdManager {
    val adState: StateFlow<AdState>

    fun initialize()
    fun loadAd(placement: AdPlacement)
    fun showAd(activity: Activity, placement: AdPlacement, onAdDismissed: () -> Unit)

    @Composable
    fun NativeAdView(placement: AdPlacement, modifier: Modifier)
}
