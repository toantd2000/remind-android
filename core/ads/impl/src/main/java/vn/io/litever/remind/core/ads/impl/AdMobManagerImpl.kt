package vn.io.litever.remind.core.ads.impl

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import vn.io.litever.remind.core.ads.api.AdManager
import vn.io.litever.remind.core.ads.api.AdPlacement
import vn.io.litever.remind.core.ads.api.AdState
import vn.io.litever.remind.core.ads.impl.ui.AdMobNativeAdView
import vn.io.litever.remind.core.common.util.DeviceUtils
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdMobManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remoteAdConfigFetcher: RemoteAdConfigFetcher
) : AdManager {

    private val _adState = MutableStateFlow<AdState>(AdState.Idle)
    override val adState: StateFlow<AdState> = _adState.asStateFlow()

    private val cachedAds = ConcurrentHashMap<AdPlacement, NativeAd>()
    private val loadingIds = ConcurrentHashMap.newKeySet<AdPlacement>()
    private val lastLoadTime = ConcurrentHashMap<AdPlacement, Long>()

    override fun initialize() {
        MobileAds.initialize(context) {}
        remoteAdConfigFetcher.fetchConfig()
    }

    override fun loadAd(placement: AdPlacement) {
        val config = remoteAdConfigFetcher.getConfig()
        if (!config.isAdsEnabled) {
            _adState.value = AdState.Failed("Ads are disabled globally")
            return
        }
        val placementConfig = config.placements[placement]
        if (placementConfig == null || !placementConfig.enabled) {
            _adState.value = AdState.Failed("Placement $placement is disabled")
            return
        }
        
        _adState.value = AdState.Loading
        // TODO: Implement AdMob load logic here for other ad types
    }

    override fun showAd(activity: Activity, placement: AdPlacement, onAdDismissed: () -> Unit) {
        // TODO: Implement AdMob show logic here
        onAdDismissed()
    }

    @Composable
    override fun NativeAdView(placement: AdPlacement, modifier: Modifier) {
        AdMobNativeAdView(placement = placement, adManager = this, modifier = modifier)
    }

    // internal method for AdMobNativeAdView to load native ads
    internal fun loadNativeAd(placement: AdPlacement, onComplete: (NativeAd?) -> Unit) {
        val config = remoteAdConfigFetcher.getConfig()
        if (!config.isAdsEnabled) {
            onComplete(null)
            return
        }
        
        val placementConfig = config.placements[placement]
        if (placementConfig == null || !placementConfig.enabled) {
            onComplete(null)
            return
        }
        
        val adId = placementConfig.adUnitId
        if (adId.isBlank()) {
            onComplete(null)
            return
        }

        if (DeviceUtils.isEmulator()) {
            onComplete(null)
            return
        }

        if (placementConfig.enableCache) {
            val currentTime = System.currentTimeMillis()
            val cachedAd = cachedAds[placement]
            val lastTime = lastLoadTime[placement] ?: 0L

            val cacheDurationMs = placementConfig.intervalSeconds * 1000L
            if (cachedAd != null && (currentTime - lastTime) < cacheDurationMs) {
                onComplete(cachedAd)
                return
            }
        }

        if (loadingIds.contains(placement)) return

        loadingIds.add(placement)
        
        val adLoader = AdLoader.Builder(context, adId)
            .forNativeAd { ad ->
                if (placementConfig.enableCache) {
                    cachedAds[placement]?.destroy()
                    cachedAds[placement] = ad
                    lastLoadTime[placement] = System.currentTimeMillis()
                }
                loadingIds.remove(placement)
                onComplete(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    loadingIds.remove(placement)
                    onComplete(null)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun clearCache() {
        cachedAds.values.forEach { it.destroy() }
        cachedAds.clear()
        lastLoadTime.clear()
        loadingIds.clear()
    }
}
