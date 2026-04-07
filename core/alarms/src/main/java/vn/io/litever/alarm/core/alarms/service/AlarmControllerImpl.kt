package vn.io.litever.alarm.core.alarms.service

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.io.litever.alarm.core.domain.scheduler.AlarmController
import javax.inject.Inject

class AlarmControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmController {

    override fun dismissAlarm() {
        // Tắt service an toàn mà UI không cần biết đến AlarmService
        val intent = Intent(context, AlarmService::class.java)
        context.stopService(intent)
    }

    override fun snoozeAlarm() {
        // Tắt service và có thể gọi tới AlarmScheduler để lên lịch 5p sau
        val intent = Intent(context, AlarmService::class.java)
        context.stopService(intent)
    }
}
