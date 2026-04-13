package vn.io.litever.alarm.app.provider

import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.io.litever.alarm.app.MainActivity
import vn.io.litever.remind.core.reminder.provider.ReminderIntentProvider
import javax.inject.Inject

class ReminderIntentProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ReminderIntentProvider {
    override fun createRingingIntent(alarmId: Long): Intent {
        return Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("app://alarm/ring/$alarmId")
        }
    }
}
