package vn.io.litever.alarm.app.provider

import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.io.litever.alarm.app.MainActivity
import vn.io.litever.alarm.core.alarms.provider.AlarmIntentProvider
import javax.inject.Inject

class AlarmIntentProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmIntentProvider {
    override fun createRingingIntent(alarmId: Long): Intent {
        // TƯỜNG MINH LỚP ACTIVITY TRỰC TIẾP
        return Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("app://alarm/ring/$alarmId")
        }
    }
}
