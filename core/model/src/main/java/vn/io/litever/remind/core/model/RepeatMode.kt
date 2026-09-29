package vn.io.litever.remind.core.model

import java.io.Serializable

enum class RepeatMode : Serializable {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}
