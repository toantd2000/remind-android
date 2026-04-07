package vn.io.litever.alarm.core.domain.scheduler

import vn.io.litever.alarm.core.model.Alarm

interface AlarmScheduler {
    fun schedule(alarm: Alarm)
    fun cancel(alarm: Alarm)

    companion object {
        const val ACTION_TRIGGER_ALARM = "vn.io.litever.alarm.ACTION_TRIGGER_ALARM"
        const val EXTRA_ALARM_ID = "ALARM_ID"
    }
}
