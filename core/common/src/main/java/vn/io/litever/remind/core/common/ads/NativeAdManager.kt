package vn.io.litever.remind.core.common.ads

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import vn.io.litever.remind.core.common.util.DeviceUtils
import java.util.concurrent.ConcurrentHashMap

@Singleton
class NativeAdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cachedAds = ConcurrentHashMap<String, NativeAd>()
    private val loadingIds = ConcurrentHashMap.newKeySet<String>()
    private val lastLoadTime = ConcurrentHashMap<String, Long>()

    // Cache duration: 2 minutes
    private val CACHE_DURATION_MS = 2 * 60 * 1000L

    fun loadAd(adId: String, onComplete: (NativeAd?) -> Unit) {
        val currentTime = System.currentTimeMillis()
        val cachedAd = cachedAds[adId]
        val lastTime = lastLoadTime[adId] ?: 0L

        // Skip loading on emulators
        if (DeviceUtils.isEmulator()) {
            onComplete(null)
            return
        }

        // If we have a cached ad and it's still fresh, use it
        if (cachedAd != null && (currentTime - lastTime) < CACHE_DURATION_MS) {
            onComplete(cachedAd)
            return
        }

        // Avoid multiple simultaneous loads for the same ID
        if (loadingIds.contains(adId)) return

        loadingIds.add(adId)
        
        val adLoader = AdLoader.Builder(context, adId)
            .forNativeAd { ad ->
                // Clean up old ad if exists
                cachedAds[adId]?.destroy()
                
                cachedAds[adId] = ad
                lastLoadTime[adId] = System.currentTimeMillis()
                loadingIds.remove(adId)
                onComplete(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    loadingIds.remove(adId)
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
