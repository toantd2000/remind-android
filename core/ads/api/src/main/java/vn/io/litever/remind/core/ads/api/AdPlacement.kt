package vn.io.litever.remind.core.ads.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AdPlacement {
    REMIND_NATIVE,
    MESSAGE_NATIVE,
    EXIT_NATIVE,
    SUPPORT_REWARDED
}
