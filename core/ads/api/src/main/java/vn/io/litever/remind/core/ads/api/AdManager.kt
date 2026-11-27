package vn.io.litever.remind.core.ads.api

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.StateFlow

interface AdManager {
    fun adState(placement: AdPlacement): StateFlow<AdState>

    fun initialize()
    fun loadAd(placement: AdPlacement)
    fun showAd(
        activity: Activity,
        placement: AdPlacement,
        onRewardEarned: () -> Unit = {},
        onAdDismissed: () -> Unit
    )
    fun isAdLoaded(placement: AdPlacement): Boolean

    @Composable
    fun MicroAdStrip(placement: AdPlacement, modifier: Modifier)
}
