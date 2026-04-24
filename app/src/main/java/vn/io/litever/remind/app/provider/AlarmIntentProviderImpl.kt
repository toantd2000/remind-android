package vn.io.litever.remind.app.provider

import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.io.litever.remind.app.MainActivity
import vn.io.litever.remind.core.alarm.provider.AlarmIntentProvider
import javax.inject.Inject

class AlarmIntentProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmIntentProvider {
    override fun createRingingIntent(alarmId: Long): Intent {
        return Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("app://remind/ring/$alarmId")
        }
    }
}










