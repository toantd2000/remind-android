package vn.io.litever.remind.core.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.domain.repository.AlarmRepository
import vn.io.litever.remind.core.domain.repository.MissedAlarmRepository
import vn.io.litever.remind.core.model.MissedAlarm
import vn.io.litever.remind.core.model.MissedReason
import vn.io.litever.remind.core.domain.scheduler.AlarmScheduler
import vn.io.litever.remind.core.alarm.service.AlarmService
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var alarmRepository: AlarmRepository
    
    @Inject
    lateinit var missedAlarmRepository: MissedAlarmRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AlarmScheduler.ACTION_TRIGGER_ALARM) {
            val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
            if (alarmId != -1L) {
                // Check notification permission on Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        recordMissed(alarmId, MissedReason.PERMISSION_MISSING)
                    }
                }
                
                val serviceIntent = Intent(context, AlarmService::class.java).apply {
                    putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
                    putExtra(AlarmScheduler.EXTRA_IS_SNOOZE, intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_SNOOZE, false))
                }
                
                try {
                    context.startForegroundService(serviceIntent)
                } catch (e: Exception) {
                    recordMissed(alarmId, MissedReason.PERMISSION_MISSING)
                }
            }
        }
    }

    private fun recordMissed(alarmId: Long, reason: MissedReason) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val alarm = alarmRepository.getAlarmById(alarmId)
                missedAlarmRepository.insertMissedAlarm(
                    MissedAlarm(
                        alarmId = alarmId,
                        alarmLabel = alarm?.label ?: "",
                        scheduledTime = System.currentTimeMillis(),
                        reason = reason
                    )
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}










