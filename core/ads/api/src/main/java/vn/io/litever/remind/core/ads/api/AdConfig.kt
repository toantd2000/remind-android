package vn.io.litever.remind.core.ads.api

import kotlinx.serialization.Serializable

@Serializable
data class AdConfig(
    val isAdsEnabled: Boolean = true,
    val placements: Map<AdPlacement, PlacementConfig> = emptyMap()
)

@Serializable
data class PlacementConfig(
    val enabled: Boolean = true,
    val adUnitId: String = "",
    val frequencyCapping: Int = 3,
    val intervalSeconds: Int = 60,
    val enableCache: Boolean = true
)
