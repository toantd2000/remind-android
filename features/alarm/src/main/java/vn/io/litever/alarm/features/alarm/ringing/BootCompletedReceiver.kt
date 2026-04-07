package vn.io.litever.alarm.features.alarm.ringing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import vn.io.litever.alarm.core.domain.scheduler.AlarmScheduler
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // TODO: Lọc tất cả báo thức có isEnabled = true từ DB và gọi alarmScheduler.schedule()
        }
    }
}
