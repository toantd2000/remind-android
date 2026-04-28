package vn.io.litever.remind.core.model

data class MissedAlarm(
    val id: Long = 0,
    val alarmId: Long,
    val alarmLabel: String,
    val scheduledTime: Long,
    val missedTime: Long = System.currentTimeMillis(),
    val reason: MissedReason
)

enum class MissedReason {
    POWER_OFF,
    PERMISSION_MISSING,
    TIMEOUT,
    UNKNOWN
}
