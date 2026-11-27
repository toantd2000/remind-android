package vn.io.litever.remind.core.ads.impl

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vn.io.litever.remind.core.ads.api.AdConfig
import vn.io.litever.remind.core.ads.api.AdManager
import vn.io.litever.remind.core.ads.api.AdPlacement
import vn.io.litever.remind.core.ads.api.AdState
import vn.io.litever.remind.core.ads.api.PlacementConfig
import vn.io.litever.remind.core.ads.impl.ui.AdMobMicroAdStrip
import vn.io.litever.remind.core.common.util.DeviceUtils
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AdMobManagerImpl"

private const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
private const val TEST_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

@Singleton
class AdMobManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val remoteAdConfigFetcher: RemoteAdConfigFetcher,
    private val preferencesDataSource: AlarmPreferencesDataSource
) : AdManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainHandler = Handler(Looper.getMainLooper())

    private fun runOnMainThread(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }

    private val placementStates = ConcurrentHashMap<AdPlacement, MutableStateFlow<AdState>>()

    internal val cachedAds = ConcurrentHashMap<AdPlacement, NativeAd>()
    private val loadingIds = ConcurrentHashMap.newKeySet<AdPlacement>()
    private val lastLoadTime = ConcurrentHashMap<AdPlacement, Long>()
    private val showTimestamps = ConcurrentHashMap<AdPlacement, MutableList<Long>>()
    private val timestampLock = Any()

    @Volatile
    private var rewardedAd: RewardedAd? = null

    @Volatile
    private var interstitialAd: InterstitialAd? = null

    @Volatile
    private var adsDisabledUntilValue = 0L

    @Volatile
    private var isProUserValue = false

    private val isDebugBuild: Boolean
        get() = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    init {
        scope.launch {
            combine(
                preferencesDataSource.isProUser,
                preferencesDataSource.adsDisabledUntil
            ) { isPro, disabledUntil ->
                isPro to disabledUntil
            }.collect { (isPro, disabledUntil) ->
                isProUserValue = isPro
                adsDisabledUntilValue = disabledUntil
                if (isPro || System.currentTimeMillis() < disabledUntil) {
                    clearAdFreeIncompatibleCache()
                }
            }
        }
    }

    override fun adState(placement: AdPlacement): StateFlow<AdState> {
        return stateFlowFor(placement).asStateFlow()
    }

    private fun stateFlowFor(placement: AdPlacement): MutableStateFlow<AdState> {
        return placementStates.getOrPut(placement) { MutableStateFlow(AdState.Idle) }
    }

    private fun resolvePlacementConfig(config: AdConfig, placement: AdPlacement): PlacementConfig {
        return config.placementConfig(placement) ?: when (placement) {
            AdPlacement.SUPPORT_REWARDED -> PlacementConfig(
                enabled = true,
                adUnitId = "",
                frequencyCapping = 0,
                intervalSeconds = 0,
                enableCache = true
            )
            AdPlacement.SAVE_ALARM_INTERSTITIAL -> PlacementConfig(
                enabled = true,
                adUnitId = "",
                frequencyCapping = 1,
                intervalSeconds = 1800, // Ethical cap: at most 1 interstitial per 30 minutes
                enableCache = true
            )
            AdPlacement.MAIN_BOTTOM_STRIP,
            AdPlacement.EDIT_BOTTOM_STRIP,
            AdPlacement.MISSION_TOP_STRIP -> PlacementConfig(
                enabled = true,
                adUnitId = "",
                frequencyCapping = 0,
                intervalSeconds = 60,
                enableCache = true
            )
        }
    }

    private fun resolveAdUnitId(placement: AdPlacement, placementConfig: PlacementConfig): String {
        if (isDebugBuild) {
            return when (placement) {
                AdPlacement.SUPPORT_REWARDED -> TEST_REWARDED_AD_UNIT_ID
                AdPlacement.SAVE_ALARM_INTERSTITIAL -> TEST_INTERSTITIAL_AD_UNIT_ID
                AdPlacement.MAIN_BOTTOM_STRIP,
                AdPlacement.EDIT_BOTTOM_STRIP,
                AdPlacement.MISSION_TOP_STRIP -> TEST_NATIVE_AD_UNIT_ID
            }
        }
        // Release builds must strictly use Remote Config IDs; never serve Google test IDs in production.
        return placementConfig.adUnitId.trim()
    }

    private fun isFrequencyCapped(placement: AdPlacement, config: PlacementConfig): Boolean {
        if (config.frequencyCapping <= 0 || config.intervalSeconds <= 0) return false
        val now = System.currentTimeMillis()
        val threshold = now - (config.intervalSeconds * 1000L)
        synchronized(timestampLock) {
            val timestamps = showTimestamps[placement] ?: return false
            timestamps.removeAll { it <= threshold }
            val recentShows = timestamps.size
            Log.d(TAG, "Frequency check for $placement: $recentShows/${config.frequencyCapping} (in ${config.intervalSeconds}s)")
            return recentShows >= config.frequencyCapping
        }
    }

    private fun recordShow(placement: AdPlacement) {
        synchronized(timestampLock) {
            showTimestamps.getOrPut(placement) { mutableListOf() }.add(System.currentTimeMillis())
        }
    }

    private fun isAdFreeActive(): Boolean {
        return isProUserValue || System.currentTimeMillis() < adsDisabledUntilValue
    }

    private fun clearAdFreeIncompatibleCache() {
        val adsToDestroy = cachedAds.values.toList()
        cachedAds.clear()
        interstitialAd = null
        stateFlowFor(AdPlacement.SAVE_ALARM_INTERSTITIAL).value = AdState.Idle
        if (adsToDestroy.isNotEmpty()) {
            runOnMainThread { adsToDestroy.forEach { it.destroy() } }
        }
    }

    override fun initialize() {
        MobileAds.initialize(context) {}
        remoteAdConfigFetcher.fetchConfig()
        // Wait for initial Pro / Ad-Free preference values before preloading interstitials
        scope.launch {
            val isPro = preferencesDataSource.isProUser.first()
            val disabledUntil = preferencesDataSource.adsDisabledUntil.first()
            isProUserValue = isPro
            adsDisabledUntilValue = disabledUntil
            if (!isPro && System.currentTimeMillis() >= disabledUntil) {
                withContext(Dispatchers.Main) {
                    loadAd(AdPlacement.SAVE_ALARM_INTERSTITIAL)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun loadAd(placement: AdPlacement) {
        Log.d(TAG, "loadAd: requested for placement $placement")
        val stateFlow = stateFlowFor(placement)

        if (placement != AdPlacement.SUPPORT_REWARDED && isAdFreeActive()) {
            Log.d(TAG, "loadAd: skipped due to ad-free active")
            stateFlow.value = AdState.Failed("Ads are currently disabled (Pro or supporter reward active)")
            return
        }

        val config = remoteAdConfigFetcher.getConfig()
        if (placement != AdPlacement.SUPPORT_REWARDED && !config.isAdsEnabled) {
            Log.d(TAG, "loadAd: skipped because global ads are disabled")
            stateFlow.value = AdState.Failed("Global ads disabled in remote config")
            return
        }

        val placementConfig = resolvePlacementConfig(config, placement)
        if (!placementConfig.enabled) {
            Log.d(TAG, "loadAd: skipped because placement is disabled in config")
            stateFlow.value = AdState.Failed("Placement disabled in remote config")
            return
        }

        if (isFrequencyCapped(placement, placementConfig)) {
            Log.d(TAG, "loadAd: skipped due to frequency capping")
            stateFlow.value = AdState.Failed("Frequency capping reached")
            return
        }

        val currentTime = System.currentTimeMillis()
        val lastTime = lastLoadTime[placement] ?: 0L
        val cacheDurationMs = placementConfig.intervalSeconds * 1000L
        val isLoaded = when (placement) {
            AdPlacement.SUPPORT_REWARDED -> rewardedAd != null
            AdPlacement.SAVE_ALARM_INTERSTITIAL -> interstitialAd != null
            AdPlacement.MAIN_BOTTOM_STRIP,
            AdPlacement.EDIT_BOTTOM_STRIP,
            AdPlacement.MISSION_TOP_STRIP -> cachedAds.containsKey(placement)
        }

        if (isLoaded) {
            if (!placementConfig.enableCache || cacheDurationMs <= 0L || (currentTime - lastTime) < cacheDurationMs) {
                Log.d(TAG, "loadAd: already loaded and valid cache, skipping")
                stateFlow.value = AdState.Loaded
                return
            } else {
                Log.d(TAG, "loadAd: cache expired, clearing old ad")
                if (placement == AdPlacement.SUPPORT_REWARDED) rewardedAd = null
                if (placement == AdPlacement.SAVE_ALARM_INTERSTITIAL) interstitialAd = null
            }
        }

        if (DeviceUtils.isEmulator()) {
            stateFlow.value = AdState.Failed("Ads are disabled on emulators")
            return
        }

        val adId = resolveAdUnitId(placement, placementConfig)
        if (adId.isBlank()) {
            Log.w(TAG, "loadAd: missing adUnitId for $placement")
            stateFlow.value = AdState.Failed("Missing ad unit ID")
            return
        }

        if (!loadingIds.add(placement)) {
            Log.d(TAG, "loadAd: already loading $placement")
            return
        }

        stateFlow.value = AdState.Loading
        val adRequest = AdRequest.Builder().build()

        when (placement) {
            AdPlacement.SUPPORT_REWARDED -> {
                runOnMainThread {
                    RewardedAd.load(context, adId, adRequest, object : RewardedAdLoadCallback() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.e(TAG, "loadAd(Rewarded): failed $adError")
                            loadingIds.remove(placement)
                            rewardedAd = null
                            stateFlow.value = AdState.Failed(adError.message)
                        }

                        override fun onAdLoaded(ad: RewardedAd) {
                            Log.d(TAG, "loadAd(Rewarded): loaded")
                            loadingIds.remove(placement)
                            rewardedAd = ad
                            lastLoadTime[placement] = System.currentTimeMillis()
                            stateFlow.value = AdState.Loaded
                        }
                    })
                }
            }

            AdPlacement.SAVE_ALARM_INTERSTITIAL -> {
                runOnMainThread {
                    InterstitialAd.load(context, adId, adRequest, object : InterstitialAdLoadCallback() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.e(TAG, "loadAd(Interstitial): failed $adError")
                            loadingIds.remove(placement)
                            interstitialAd = null
                            stateFlow.value = AdState.Failed(adError.message)
                        }

                        override fun onAdLoaded(ad: InterstitialAd) {
                            Log.d(TAG, "loadAd(Interstitial): loaded")
                            loadingIds.remove(placement)
                            interstitialAd = ad
                            lastLoadTime[placement] = System.currentTimeMillis()
                            stateFlow.value = AdState.Loaded
                        }
                    })
                }
            }

            AdPlacement.MAIN_BOTTOM_STRIP,
            AdPlacement.EDIT_BOTTOM_STRIP,
            AdPlacement.MISSION_TOP_STRIP -> {
                loadingIds.remove(placement)
                stateFlow.value = AdState.Idle
            }
        }
    }

    override fun showAd(
        activity: Activity,
        placement: AdPlacement,
        onRewardEarned: () -> Unit,
        onAdDismissed: () -> Unit
    ) {
        Log.d(TAG, "showAd: requested for $placement")
        val stateFlow = stateFlowFor(placement)

        val config = remoteAdConfigFetcher.getConfig()
        if (placement != AdPlacement.SUPPORT_REWARDED && !config.isAdsEnabled) {
            onAdDismissed()
            return
        }

        val placementConfig = resolvePlacementConfig(config, placement)
        if (!placementConfig.enabled || isFrequencyCapped(placement, placementConfig)) {
            Log.d(TAG, "showAd: skipped due to disabled or frequency capping")
            onAdDismissed()
            return
        }

        if (placement != AdPlacement.SUPPORT_REWARDED && isAdFreeActive()) {
            Log.d(TAG, "showAd: skipped because isAdFreeActive() is true")
            onAdDismissed()
            return
        }

        when (placement) {
            AdPlacement.SUPPORT_REWARDED -> {
                val ad = rewardedAd
                if (ad != null) {
                    var rewardEarned = false
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            rewardedAd = null
                            stateFlow.value = AdState.Idle
                            if (rewardEarned) {
                                onRewardEarned()
                            }
                            onAdDismissed()
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            Log.e(TAG, "showAd(Rewarded): failed to show $error")
                            rewardedAd = null
                            stateFlow.value = AdState.Idle
                            onAdDismissed()
                        }
                    }
                    recordShow(placement)
                    Log.d(TAG, "showAd(Rewarded): showing")
                    ad.show(activity) {
                        rewardEarned = true
                        scope.launch {
                            // Grant ad-free supporter status (1h on debug, 24h on release)
                            val duration = if (isDebugBuild) 1 * 60 * 60 * 1000L else 24 * 60 * 60 * 1000L
                            preferencesDataSource.setAdsDisabledUntil(System.currentTimeMillis() + duration)
                            clearAdFreeIncompatibleCache()
                        }
                    }
                } else {
                    Log.d(TAG, "showAd(Rewarded): ad is null, skipping")
                    onAdDismissed()
                }
            }

            AdPlacement.SAVE_ALARM_INTERSTITIAL -> {
                val ad = interstitialAd
                if (ad != null) {
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            interstitialAd = null
                            stateFlow.value = AdState.Idle
                            onAdDismissed()
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            Log.e(TAG, "showAd(Interstitial): failed to show $error")
                            interstitialAd = null
                            stateFlow.value = AdState.Idle
                            onAdDismissed()
                        }
                    }
                    recordShow(placement)
                    Log.d(TAG, "showAd(Interstitial): showing")
                    ad.show(activity)
                } else {
                    Log.d(TAG, "showAd(Interstitial): ad is null, skipping")
                    onAdDismissed()
                }
            }

            AdPlacement.MAIN_BOTTOM_STRIP,
            AdPlacement.EDIT_BOTTOM_STRIP,
            AdPlacement.MISSION_TOP_STRIP -> {
                onAdDismissed()
            }
        }
    }

    override fun isAdLoaded(placement: AdPlacement): Boolean {
        if (placement != AdPlacement.SUPPORT_REWARDED && isAdFreeActive()) {
            return false
        }
        return when (placement) {
            AdPlacement.SUPPORT_REWARDED -> rewardedAd != null
            AdPlacement.SAVE_ALARM_INTERSTITIAL -> interstitialAd != null
            AdPlacement.MAIN_BOTTOM_STRIP,
            AdPlacement.EDIT_BOTTOM_STRIP,
            AdPlacement.MISSION_TOP_STRIP -> cachedAds.containsKey(placement)
        }
    }

    @Composable
    override fun MicroAdStrip(placement: AdPlacement, modifier: Modifier) {
        val adsDisabledUntil by preferencesDataSource.adsDisabledUntil.collectAsState(initial = 0L)
        val isPro by preferencesDataSource.isProUser.collectAsState(initial = false)
        val isAdFree = remember(adsDisabledUntil, isPro) {
            isPro || System.currentTimeMillis() < adsDisabledUntil
        }
        if (isAdFree) return
        AdMobMicroAdStrip(
            placement = placement,
            adManager = this,
            modifier = modifier
        )
    }

    @SuppressLint("MissingPermission")
    internal fun loadNativeAd(placement: AdPlacement, onComplete: (NativeAd?) -> Unit) {
        Log.d(TAG, "loadNativeAd: requested for $placement")

        if (isAdFreeActive()) {
            cachedAds.remove(placement)?.destroy()
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

        if (DeviceUtils.isEmulator()) {
            onComplete(null)
            return
        }

        val adId = resolveAdUnitId(placement, placementConfig)
        if (adId.isBlank()) {
            onComplete(null)
            return
        }

        if (isFrequencyCapped(placement, placementConfig)) {
            Log.d(TAG, "loadNativeAd: skipped due to frequency capping")
            onComplete(null)
            return
        }

        if (placementConfig.enableCache) {
            val currentTime = System.currentTimeMillis()
            val cachedAd = cachedAds[placement]
            val lastTime = lastLoadTime[placement] ?: 0L
            val cacheDurationMs = placementConfig.intervalSeconds * 1000L

            if (cachedAd != null && (cacheDurationMs <= 0L || (currentTime - lastTime) < cacheDurationMs)) {
                Log.d(TAG, "loadNativeAd: using valid cache for $placement")
                onComplete(cachedAd)
                return
            } else if (cachedAd != null) {
                Log.d(TAG, "loadNativeAd: cache expired for $placement, clearing")
                cachedAds.remove(placement)?.destroy()
            }
        }

        if (!loadingIds.add(placement)) return

        val adLoader = AdLoader.Builder(context, adId)
            .forNativeAd { ad ->
                if (isAdFreeActive()) {
                    ad.destroy()
                    loadingIds.remove(placement)
                    onComplete(null)
                    return@forNativeAd
                }
                if (placementConfig.enableCache) {
                    cachedAds.put(placement, ad)?.destroy()
                    lastLoadTime[placement] = System.currentTimeMillis()
                }
                Log.d(TAG, "loadNativeAd: loaded successfully for $placement")
                recordShow(placement)
                loadingIds.remove(placement)
                onComplete(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "loadNativeAd: failed to load for $placement: ${error.message}")
                    loadingIds.remove(placement)
                    onComplete(null)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        Log.d(TAG, "loadNativeAd: sending ad request for $placement")
        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun clearCache() {
        val adsToDestroy = cachedAds.values.toList()
        cachedAds.clear()
        lastLoadTime.clear()
        loadingIds.clear()
        if (adsToDestroy.isNotEmpty()) {
            runOnMainThread { adsToDestroy.forEach { it.destroy() } }
        }
    }
}
