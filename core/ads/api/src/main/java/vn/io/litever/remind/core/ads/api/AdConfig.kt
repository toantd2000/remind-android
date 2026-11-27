package vn.io.litever.remind.core.ads.api

import kotlinx.serialization.Serializable

@Serializable
data class AdConfig(
    val isAdsEnabled: Boolean = true,
    val placements: Map<String, PlacementConfig> = emptyMap()
) {
    fun placementConfig(placement: AdPlacement): PlacementConfig? {
        return placements[placement.name] ?: when (placement) {
            // Backward compatibility with legacy Remote Config key name
            AdPlacement.MAIN_BOTTOM_STRIP -> placements["SETTINGS_BOTTOM_STRIP"]
            else -> null
        }
    }
}

@Serializable
data class PlacementConfig(
    val enabled: Boolean = true,
    val adUnitId: String = "",
    val frequencyCapping: Int = 3,
    val intervalSeconds: Int = 60,
    val enableCache: Boolean = true
)
