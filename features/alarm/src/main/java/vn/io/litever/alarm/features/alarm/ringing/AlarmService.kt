package vn.io.litever.alarm.features.alarm.ringing

import android.app.Service
import android.content.Intent
import android.os.IBinder

class AlarmService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // TODO: Rung máy, phát nhạc và hiện Foreground Notification
        return START_STICKY
    }
}
