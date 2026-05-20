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
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.OnUserEarnedRewardListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.ads.api.AdManager
import vn.io.litever.remind.core.ads.api.AdPlacement
import vn.io.litever.remind.core.ads.api.AdState
import vn.io.litever.remind.core.ads.api.PlacementConfig
import vn.io.litever.remind.core.ads.impl.ui.AdMobNativeAdView
import vn.io.litever.remind.core.common.util.DeviceUtils
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdMobManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remoteAdConfigFetcher: RemoteAdConfigFetcher,
    private val preferencesDataSource: AlarmPreferencesDataSource
) : AdManager {

    private val _adState = MutableStateFlow<AdState>(AdState.Idle)
    override val adState: StateFlow<AdState> = _adState.asStateFlow()

    private val cachedAds = ConcurrentHashMap<AdPlacement, NativeAd>()
    private val loadingIds = ConcurrentHashMap.newKeySet<AdPlacement>()
    private val lastLoadTime = ConcurrentHashMap<AdPlacement, Long>()

    private var rewardedAd: RewardedAd? = null
    private var adsDisabledUntilValue = 0L

    init {
        CoroutineScope(Dispatchers.IO).launch {
            preferencesDataSource.adsDisabledUntil.collect { timestamp ->
                adsDisabledUntilValue = timestamp
            }
        }
    }

    private fun isAdFreeActive(): Boolean {
        return System.currentTimeMillis() < adsDisabledUntilValue
    }

    override fun initialize() {
        MobileAds.initialize(context) {}
        remoteAdConfigFetcher.fetchConfig()
    }

    @android.annotation.SuppressLint("MissingPermission")
    override fun loadAd(placement: AdPlacement) {
        if (isAdFreeActive()) {
            _adState.value = AdState.Failed("Ads are currently disabled (supporter reward active)")
            return
        }

        val config = remoteAdConfigFetcher.getConfig()
        if (!config.isAdsEnabled) {
            _adState.value = AdState.Failed("Ads are disabled globally")
            return
        }
        val placementConfig = config.placements[placement] ?: when (placement) {
            AdPlacement.SUPPORT_REWARDED -> PlacementConfig(enabled = true, adUnitId = "ca-app-pub-3940256099942544/5224354917")
            AdPlacement.REMIND_NATIVE -> PlacementConfig(enabled = true, adUnitId = "ca-app-pub-3940256099942544/2247696110")
            AdPlacement.MESSAGE_NATIVE -> PlacementConfig(enabled = true, adUnitId = "ca-app-pub-3940256099942544/2247696110")
            AdPlacement.EXIT_NATIVE -> PlacementConfig(enabled = true, adUnitId = "ca-app-pub-3940256099942544/2247696110")
        }
        if (!placementConfig.enabled) {
            _adState.value = AdState.Failed("Placement $placement is disabled")
            return
        }
        
        _adState.value = AdState.Loading
        
        if (placement == AdPlacement.SUPPORT_REWARDED) {
            if (DeviceUtils.isEmulator()) {
                _adState.value = AdState.Failed("Rewarded ads are disabled on emulators")
                return
            }
            
            if (placementConfig.enableCache) {
                val cachedAd = rewardedAd
                val lastTime = lastLoadTime[placement] ?: 0L
                val currentTime = System.currentTimeMillis()
                val cacheDurationMs = placementConfig.intervalSeconds * 1000L
                
                if (cachedAd != null && (currentTime - lastTime) < cacheDurationMs) {
                    _adState.value = AdState.Loaded
                    return
                }
            }
            
            val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
            val adId = if (isDebug) {
                "ca-app-pub-3940256099942544/5224354917" // Always use official Google test rewarded ID in debug builds
            } else {
                placementConfig.adUnitId.ifBlank { "ca-app-pub-3940256099942544/5224354917" }
            }
            val adRequest = AdRequest.Builder().build()
            
            RewardedAd.load(context, adId, adRequest, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                    _adState.value = AdState.Failed(adError.message)
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    lastLoadTime[placement] = System.currentTimeMillis()
                    _adState.value = AdState.Loaded
                }
            })
        } else {
            _adState.value = AdState.Idle
        }
    }

    override fun showAd(activity: Activity, placement: AdPlacement, onAdDismissed: () -> Unit) {
        if (placement == AdPlacement.SUPPORT_REWARDED) {
            val ad = rewardedAd
            if (ad != null) {
                ad.show(activity, OnUserEarnedRewardListener { rewardItem ->
                    CoroutineScope(Dispatchers.IO).launch {
                        // Grant ad-free supporter status (30s on debug, 24h on release)
                        val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
                        val duration = if (isDebug) 30 * 1000L else 24 * 60 * 60 * 1000L
                        preferencesDataSource.setAdsDisabledUntil(System.currentTimeMillis() + duration)
                    }
                })
                rewardedAd = null
                _adState.value = AdState.Idle
                onAdDismissed()
            } else {
                // Fallback to calling screen simulation trigger
                onAdDismissed()
            }
        } else {
            onAdDismissed()
        }
    }

    override fun isAdLoaded(placement: AdPlacement): Boolean {
        return when (placement) {
            AdPlacement.SUPPORT_REWARDED -> rewardedAd != null
            else -> cachedAds.containsKey(placement)
        }
    }

    @Composable
    override fun NativeAdView(placement: AdPlacement, modifier: Modifier) {
        if (isAdFreeActive()) return
        AdMobNativeAdView(placement = placement, adManager = this, modifier = modifier)
    }

    // internal method for AdMobNativeAdView to load native ads
    @android.annotation.SuppressLint("MissingPermission")
    internal fun loadNativeAd(placement: AdPlacement, onComplete: (NativeAd?) -> Unit) {
        if (isAdFreeActive()) {
            onComplete(null)
            return
        }

        val config = remoteAdConfigFetcher.getConfig()
        if (!config.isAdsEnabled) {
            onComplete(null)
            return
        }
        
        val placementConfig = config.placements[placement] ?: when (placement) {
            AdPlacement.SUPPORT_REWARDED -> PlacementConfig(enabled = true, adUnitId = "ca-app-pub-3940256099942544/5224354917")
            AdPlacement.REMIND_NATIVE -> PlacementConfig(enabled = true, adUnitId = "ca-app-pub-3940256099942544/2247696110")
            AdPlacement.MESSAGE_NATIVE -> PlacementConfig(enabled = true, adUnitId = "ca-app-pub-3940256099942544/2247696110")
            AdPlacement.EXIT_NATIVE -> PlacementConfig(enabled = true, adUnitId = "ca-app-pub-3940256099942544/2247696110")
        }
        if (!placementConfig.enabled) {
            onComplete(null)
            return
        }
        
        val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val adId = if (isDebug) {
            "ca-app-pub-3940256099942544/2247696110" // Always use official Google test native ID in debug builds
        } else {
            placementConfig.adUnitId
        }
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
