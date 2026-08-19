package vn.io.litever.remind.core.model

import java.io.Serializable

enum class WifiTriggerType : Serializable {
    ON_CONNECT,
    ON_DISCONNECT,
    WHILE_CONNECTED
}
