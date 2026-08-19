package vn.io.litever.remind.core.model

import java.io.Serializable

enum class ReminderTriggerType : Serializable {
    TIME,
    LOCATION,
    WIFI
}
