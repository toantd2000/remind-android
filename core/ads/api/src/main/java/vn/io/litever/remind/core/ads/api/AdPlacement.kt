package vn.io.litever.remind.core.ads.api

import kotlinx.serialization.Serializable

@Serializable
enum class AdPlacement {
    @Deprecated("Legacy placement key kept for RemoteConfig JSON deserialization safety")
    REMIND_NATIVE,
    @Deprecated("Legacy placement key kept for RemoteConfig JSON deserialization safety")
    MESSAGE_NATIVE,
    EXIT_NATIVE,
    SUPPORT_REWARDED,
    ALARM_LIST_NATIVE,
    SAVE_ALARM_INTERSTITIAL,
    EDIT_BOTTOM_STRIP,
    SETTINGS_BOTTOM_STRIP
}
