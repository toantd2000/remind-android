package vn.io.litever.remind.core.ads.impl

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.serialization.json.Json
import vn.io.litever.remind.core.ads.api.AdConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteAdConfigFetcher @Inject constructor() {
    
    private var cachedConfig = AdConfig()
    
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    fun fetchConfig() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        
        // Parse initially if already have values
        parseConfig(remoteConfig.getString("ads_config_v1"))
        
        // Fetch new values
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                parseConfig(remoteConfig.getString("ads_config_v1"))
            }
        }
    }

    private fun parseConfig(jsonString: String) {
        if (jsonString.isBlank()) return
        
        try {
            cachedConfig = jsonParser.decodeFromString<AdConfig>(jsonString)
            Log.d("AdConfig", "Successfully parsed AdConfig from Remote Config")
        } catch (e: Exception) {
            Log.e("AdConfig", "Failed to parse AdConfig from Remote Config", e)
        }
    }

    fun getConfig(): AdConfig {
        return cachedConfig
    }
}
