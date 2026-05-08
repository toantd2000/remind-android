package vn.io.litever.remind.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReminderResponse(
    @SerialName("messages") val messages: List<String>,
    @SerialName("ad_config") val adConfig: AdConfig,
    @SerialName("metadata") val metadata: ReminderMetadata? = null,
    @SerialName("ai_status") val aiStatus: String = "completed"
)

@Serializable
data class AdConfig(
    @SerialName("enable_ads") val enableAds: Boolean,
    @SerialName("native_id") val nativeId: String? = null
)

@Serializable
data class ReminderMetadata(
    @SerialName("date") val date: String,
    @SerialName("is_holiday") val isHoliday: Boolean
)
