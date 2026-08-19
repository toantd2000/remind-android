package vn.io.litever.remind.core.alarm

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import vn.io.litever.remind.core.alarm.service.ReminderMonitorService
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.model.ReminderTriggerType
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler
) {

    suspend fun sync() {
        val nowMillis = System.currentTimeMillis()
        val reminders = reminderRepository.getAllReminders().first()

        var hasActiveLocationOrWifi = false

        for (reminder in reminders) {
            if (!reminder.isEnabled || reminder.isCompleted) continue

            when (reminder.triggerType) {
                ReminderTriggerType.TIME -> {
                    val snoozeTime = reminder.snoozeNextTriggerTime
                    if (snoozeTime != null && snoozeTime > nowMillis) {
                        reminderScheduler.scheduleSnooze(reminder, snoozeTime)
                    } else {
                        val timeTrigger = reminder.timeTrigger
                        if (timeTrigger != null) {
                            val nextOccurrence = timeTrigger.getNextOccurrence(LocalDateTime.now())
                            val nextEpoch = nextOccurrence.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            if (nextEpoch > nowMillis) {
                                reminderScheduler.schedule(reminder)
                            } else if (timeTrigger.repeatDays.isEmpty()) {
                                reminderRepository.updateReminder(reminder.copy(isEnabled = false, isCompleted = true))
                            }
                        }
                    }
                }
                ReminderTriggerType.LOCATION, ReminderTriggerType.WIFI -> {
                    hasActiveLocationOrWifi = true
                }
            }
        }

        if (hasActiveLocationOrWifi) {
            try {
                ReminderMonitorService.sync(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                ReminderMonitorService.stop(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
