package vn.io.litever.remind.core.model

import java.io.Serializable

data class WifiTrigger(
    val ssid: String,
    val bssid: String? = null,
    val triggerType: WifiTriggerType = WifiTriggerType.ON_CONNECT,
    val timeWindow: ReminderTimeWindow? = null
) : Serializable
