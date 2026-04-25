package vn.io.litever.remind.core.domain.scheduler

interface AlarmController {
    suspend fun dismissAlarm(alarmId: Long? = null)
    suspend fun snoozeAlarm(alarmId: Long? = null)
    suspend fun markAsMissed(alarmId: Long? = null)
    suspend fun cancelSnooze(alarmId: Long)
}










