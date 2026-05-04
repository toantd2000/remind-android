package vn.io.litever.remind.core.domain.scheduler

import vn.io.litever.remind.core.model.Alarm

interface AlarmScheduler {
    fun schedule(alarm: Alarm)
    fun scheduleSnooze(alarm: Alarm, triggerTime: Long)
    fun cancel(alarm: Alarm)
    fun cancelSnooze(alarm: Alarm)

    companion object {
        const val ACTION_TRIGGER_ALARM = "vn.io.litever.remind.ACTION_TRIGGER_ALARM"
        const val ACTION_TRIGGER_SNOOZE = "vn.io.litever.remind.ACTION_TRIGGER_SNOOZE"
        const val EXTRA_ALARM_ID = "ALARM_ID"
        const val EXTRA_IS_SNOOZE = "IS_SNOOZE"
    }
}
