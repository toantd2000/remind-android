package vn.io.litever.remind.core.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.domain.repository.AlarmRepository
import vn.io.litever.remind.core.domain.scheduler.AlarmScheduler
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var alarmRepository: AlarmRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()
            scope.launch {
                try {
                    val alarms = alarmRepository.getAllAlarms().first()
                    val enabledAlarms = alarms.filter { it.isEnabled }
                    
                    enabledAlarms.forEach { alarm ->
                        val snoozeTime = alarm.snoozeNextTriggerTime
                        if (snoozeTime != null && snoozeTime > System.currentTimeMillis()) {
                            // Reschedule active snooze
                            alarmScheduler.scheduleSnooze(alarm, snoozeTime)
                        } else {
                            // Reschedule regular occurrence (also handles past snoozes)
                            alarmScheduler.schedule(alarm)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}










