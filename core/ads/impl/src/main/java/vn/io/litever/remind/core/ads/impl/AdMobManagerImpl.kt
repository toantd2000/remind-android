package vn.io.litever.remind.core.ads.impl

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.ads.api.AdConfig
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

private const val TAG = "AdMobManagerImpl"

@Singleton
class AdMobManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remoteAdConfigFetcher: RemoteAdConfigFetcher,
    private val preferencesDataSource: AlarmPreferencesDataSource
) : AdManager {

    private val _adState = MutableStateFlow<AdState>(AdState.Idle)
    override val adState: StateFlow<AdState> = _adState.asStateFlow()

    internal val cachedAds = ConcurrentHashMap<AdPlacement, NativeAd>()
    private val loadingIds = ConcurrentHashMap.newKeySet<AdPlacement>()
    private val lastLoadTime = ConcurrentHashMap<AdPlacement, Long>()
    private val showTimestamps = ConcurrentHashMap<AdPlacement, MutableList<Long>>()

    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var adsDisabledUntilValue = 0L
    private var isProUserValue = false

    init {
        CoroutineScope(Dispatchers.IO).launch {
            preferencesDataSource.adsDisabledUntil.collect { timestamp ->
                adsDisabledUntilValue = timestamp
                if (System.currentTimeMillis() < timestamp) {
                    cachedAds.clear()
                    interstitialAd = null
                }
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            preferencesDataSource.isProUser.collect { isPro ->
                isProUserValue = isPro
                if (isPro) {
                    cachedAds.clear()
                    interstitialAd = null
                }
            }
        }
    }

    private fun resolvePlacementConfig(config: AdConfig, placement: AdPlacement): PlacementConfig {
        return config.placements[placement] ?: when (placement) {
            AdPlacement.SUPPORT_REWARDED -> PlacementConfig(
                enabled = true,
                adUnitId = "ca-app-pub-3940256099942544/5224354917",
                frequencyCapping = 0,
                intervalSeconds = 0
            )
            @Suppress("DEPRECATION")
            AdPlacement.REMIND_NATIVE,
            AdPlacement.MESSAGE_NATIVE,
            AdPlacement.EXIT_NATIVE,
            AdPlacement.ALARM_LIST_NATIVE,
            AdPlacement.EDIT_BOTTOM_STRIP,
            AdPlacement.SETTINGS_BOTTOM_STRIP,
            AdPlacement.MISSION_TOP_STRIP -> PlacementConfig(
                enabled = true,
                adUnitId = "ca-app-pub-3940256099942544/2247696110"
            )
            AdPlacement.SAVE_ALARM_INTERSTITIAL -> PlacementConfig(
                enabled = true,
                adUnitId = "ca-app-pub-3940256099942544/1033173712",
                frequencyCapping = 1,
                intervalSeconds = 1800 // Ethical cap: at most 1 interstitial per 30 minutes when saving alarms
            )
        }
    }

    private fun isFrequencyCapped(placement: AdPlacement, config: PlacementConfig): Boolean {
        if (config.frequencyCapping <= 0 || config.intervalSeconds <= 0) return false
        val now = System.currentTimeMillis()
        val timestamps = showTimestamps[placement] ?: return false
        val threshold = now - (config.intervalSeconds * 1000L)
        val recentShows = timestamps.count { it > threshold }
        android.util.Log.d(TAG, "Frequency check for $placement: $recentShows/${config.frequencyCapping} (in ${config.intervalSeconds}s)")
        return recentShows >= config.frequencyCapping
    }

    private fun isAdFreeActive(): Boolean {
        return isProUserValue || System.currentTimeMillis() < adsDisabledUntilValue
    }

    override fun initialize() {
        MobileAds.initialize(context) {}
        remoteAdConfigFetcher.fetchConfig()
        // Preload interstitial ad cho màn hình lưu báo thức nếu không trong trạng thái ad-free
        if (!isAdFreeActive()) {
            loadAd(AdPlacement.SAVE_ALARM_INTERSTITIAL)
        }
    }

    @android.annotation.SuppressLint("MissingPermission")
    override fun loadAd(placement: AdPlacement) {
        android.util.Log.d(TAG, "loadAd: requested for placement $placement")
        if (placement != AdPlacement.SUPPORT_REWARDED && isAdFreeActive()) {
            android.util.Log.d(TAG, "loadAd: skipped due to ad-free active")
            _adState.value = AdState.Failed("Ads are currently disabled (supporter reward active)")
            return
        }

        val config = remoteAdConfigFetcher.getConfig()
        if (placement != AdPlacement.SUPPORT_REWARDED && !config.isAdsEnabled) {
            android.util.Log.d(TAG, "loadAd: skipped because global ads are disabled")
            _adState.value = AdState.Failed("Global ads disabled in remote config")
            return
        }

        val placementConfig = resolvePlacementConfig(config, placement)

        if (!placementConfig.enabled) {
            android.util.Log.d(TAG, "loadAd: skipped because placement is disabled in config")
            _adState.value = AdState.Failed("Placement disabled in remote config")
            return
        }

        if (isFrequencyCapped(placement, placementConfig)) {
            android.util.Log.d(TAG, "loadAd: skipped due to frequency capping")
            _adState.value = AdState.Failed("Frequency capping reached")
            return
        }

        val currentTime = System.currentTimeMillis()
        val lastTime = lastLoadTime[placement] ?: 0L
        val cacheDurationMs = placementConfig.intervalSeconds * 1000L
        val isLoaded = when (placement) {
            AdPlacement.SUPPORT_REWARDED -> rewardedAd != null
            AdPlacement.SAVE_ALARM_INTERSTITIAL -> interstitialAd != null
            else -> false
        }

        if (isLoaded) {
            if (!placementConfig.enableCache || cacheDurationMs <= 0L || (currentTime - lastTime) < cacheDurationMs) {
                android.util.Log.d(TAG, "loadAd: already loaded and valid cache, skipping")
                _adState.value = AdState.Loaded
                return
            } else {
                android.util.Log.d(TAG, "loadAd: cache expired, clearing old ad")
                if (placement == AdPlacement.SUPPORT_REWARDED) rewardedAd = null
                if (placement == AdPlacement.SAVE_ALARM_INTERSTITIAL) interstitialAd = null
            }
        }

        if (placement == AdPlacement.SUPPORT_REWARDED) {
            if (DeviceUtils.isEmulator()) {
                _adState.value = AdState.Failed("Rewarded ads are disabled on emulators")
                return
            }

            val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
            val adId = if (isDebug) {
                "ca-app-pub-3940256099942544/5224354917" // Official Google test rewarded ID in debug builds
            } else {
                placementConfig.adUnitId.ifBlank { "ca-app-pub-3940256099942544/5224354917" }
            }
            val adRequest = AdRequest.Builder().build()

            RewardedAd.load(context, adId, adRequest, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    android.util.Log.e(TAG, "loadAd(Rewarded): failed $adError")
                    rewardedAd = null
                    _adState.value = AdState.Failed(adError.message)
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    android.util.Log.d(TAG, "loadAd(Rewarded): loaded")
                    rewardedAd = ad
                    lastLoadTime[placement] = System.currentTimeMillis()
                    _adState.value = AdState.Loaded
                }
            })
        } else if (placement == AdPlacement.SAVE_ALARM_INTERSTITIAL) {
            if (DeviceUtils.isEmulator()) {
                _adState.value = AdState.Failed("Interstitial ads are disabled on emulators")
                return
            }

            val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
            val adId = if (isDebug) {
                "ca-app-pub-3940256099942544/1033173712"
            } else {
                placementConfig.adUnitId.ifBlank { "ca-app-pub-3940256099942544/1033173712" }
            }
            val adRequest = AdRequest.Builder().build()

            InterstitialAd.load(context, adId, adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    android.util.Log.e(TAG, "loadAd(Interstitial): failed $adError")
                    interstitialAd = null
                    _adState.value = AdState.Failed(adError.message)
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    android.util.Log.d(TAG, "loadAd(Interstitial): loaded")
                    interstitialAd = ad
                    lastLoadTime[placement] = System.currentTimeMillis()
                    _adState.value = AdState.Loaded
                }
            })
        } else {
            _adState.value = AdState.Idle
        }
    }

    override fun showAd(activity: Activity, placement: AdPlacement, onAdDismissed: () -> Unit) {
        android.util.Log.d(TAG, "showAd: requested for $placement")

        val config = remoteAdConfigFetcher.getConfig()
        if (placement != AdPlacement.SUPPORT_REWARDED && !config.isAdsEnabled) {
            onAdDismissed()
            return
        }

        val placementConfig = resolvePlacementConfig(config, placement)
        if (!placementConfig.enabled || isFrequencyCapped(placement, placementConfig)) {
            android.util.Log.d(TAG, "showAd: skipped due to disabled or frequency capping")
            onAdDismissed()
            return
        }

        fun recordShow() {
            showTimestamps.getOrPut(placement) { mutableListOf() }.add(System.currentTimeMillis())
        }

        if (placement != AdPlacement.SUPPORT_REWARDED && isAdFreeActive()) {
            android.util.Log.d(TAG, "showAd: skipped because isAdFreeActive() is true")
            onAdDismissed()
            return
        }

        if (placement == AdPlacement.SUPPORT_REWARDED) {
            val ad = rewardedAd
            if (ad != null) {
                recordShow()
                android.util.Log.d(TAG, "showAd(Rewarded): showing")
                ad.show(activity, OnUserEarnedRewardListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        // Grant ad-free supporter status (1h on debug, 24h on release)
                        val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
                        val duration = if (isDebug) 1 * 60 * 60 * 1000L else 24 * 60 * 60 * 1000L
                        preferencesDataSource.setAdsDisabledUntil(System.currentTimeMillis() + duration)
                        cachedAds.clear()
                        interstitialAd = null
                    }
                })
                rewardedAd = null
                _adState.value = AdState.Idle
                onAdDismissed()
            } else {
                android.util.Log.d(TAG, "showAd(Rewarded): ad is null, skipping")
                onAdDismissed()
            }
        } else if (placement == AdPlacement.SAVE_ALARM_INTERSTITIAL) {
            val ad = interstitialAd
            if (ad != null) {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                        _adState.value = AdState.Idle
                        loadAd(AdPlacement.SAVE_ALARM_INTERSTITIAL)
                        onAdDismissed()
                    }
                    override fun onAdFailedToShowFullScreenContent(error: AdError) {
                        interstitialAd = null
                        _adState.value = AdState.Idle
                        loadAd(AdPlacement.SAVE_ALARM_INTERSTITIAL)
                        onAdDismissed()
                    }
                }
                recordShow()
                android.util.Log.d(TAG, "showAd(Interstitial): showing")
                ad.show(activity)
            } else {
                android.util.Log.d(TAG, "showAd(Interstitial): ad is null, skipping")
                loadAd(AdPlacement.SAVE_ALARM_INTERSTITIAL)
                onAdDismissed()
            }
        } else {
            onAdDismissed()
        }
    }

    override fun isAdLoaded(placement: AdPlacement): Boolean {
        if (placement != AdPlacement.SUPPORT_REWARDED && isAdFreeActive()) {
            return false
        }
        return when (placement) {
            AdPlacement.SUPPORT_REWARDED -> rewardedAd != null
            AdPlacement.SAVE_ALARM_INTERSTITIAL -> interstitialAd != null
            else -> cachedAds.containsKey(placement)
        }
    }

    @Composable
    override fun NativeAdView(placement: AdPlacement, modifier: Modifier) {
        val adsDisabledUntil by preferencesDataSource.adsDisabledUntil.collectAsState(initial = 0L)
        val isPro by preferencesDataSource.isProUser.collectAsState(initial = false)
        val isAdFree = remember(adsDisabledUntil, isPro) {
            isPro || System.currentTimeMillis() < adsDisabledUntil
        }
        if (isAdFree) return
        AdMobNativeAdView(placement = placement, adManager = this, modifier = modifier)
    }

    @Composable
    override fun MicroAdStrip(placement: AdPlacement, modifier: Modifier) {
        val adsDisabledUntil by preferencesDataSource.adsDisabledUntil.collectAsState(initial = 0L)
        val isPro by preferencesDataSource.isProUser.collectAsState(initial = false)
        val isAdFree = remember(adsDisabledUntil, isPro) {
            isPro || System.currentTimeMillis() < adsDisabledUntil
        }
        if (isAdFree) return
        vn.io.litever.remind.core.ads.impl.ui.AdMobMicroAdStrip(
            placement = placement,
            adManager = this,
            modifier = modifier
        )
    }

    // internal method for AdMobNativeAdView to load native ads
    @android.annotation.SuppressLint("MissingPermission")
    internal fun loadNativeAd(placement: AdPlacement, onComplete: (NativeAd?) -> Unit) {
        android.util.Log.d(TAG, "loadNativeAd: requested for $placement")

        if (isAdFreeActive()) {
            cachedAds.remove(placement)
            onComplete(null)
            return
        }

        val config = remoteAdConfigFetcher.getConfig()
        if (!config.isAdsEnabled) {
            onComplete(null)
            return
        }

        val placementConfig = resolvePlacementConfig(config, placement)
        if (!placementConfig.enabled) {
            onComplete(null)
            return
        }

        val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val adId = if (isDebug) {
            "ca-app-pub-3940256099942544/2247696110" // Official Google test native ID in debug builds
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

        if (isFrequencyCapped(placement, placementConfig)) {
            android.util.Log.d(TAG, "loadNativeAd: skipped due to frequency capping")
            onComplete(null)
            return
        }

        if (placementConfig.enableCache) {
            val currentTime = System.currentTimeMillis()
            val cachedAd = cachedAds[placement]
            val lastTime = lastLoadTime[placement] ?: 0L

            val cacheDurationMs = placementConfig.intervalSeconds * 1000L
            if (cachedAd != null && (currentTime - lastTime) < cacheDurationMs) {
                android.util.Log.d(TAG, "loadNativeAd: using valid cache for $placement")
                onComplete(cachedAd)
                return
            } else if (cachedAd != null) {
                android.util.Log.d(TAG, "loadNativeAd: cache expired for $placement, clearing")
                cachedAd.destroy()
                cachedAds.remove(placement)
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
                android.util.Log.d(TAG, "loadNativeAd: loaded successfully for $placement")
                showTimestamps.getOrPut(placement) { mutableListOf() }.add(System.currentTimeMillis())
                loadingIds.remove(placement)
                onComplete(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    android.util.Log.e(TAG, "loadNativeAd: failed to load for $placement")
                    loadingIds.remove(placement)
                    onComplete(null)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        android.util.Log.d(TAG, "loadNativeAd: sending ad request for $placement")
        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun clearCache() {
        cachedAds.values.forEach { it.destroy() }
        cachedAds.clear()
        lastLoadTime.clear()
        loadingIds.clear()
    }
}
