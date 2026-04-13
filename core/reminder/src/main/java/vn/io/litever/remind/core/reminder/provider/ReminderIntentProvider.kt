package vn.io.litever.remind.core.reminder.provider

import android.content.Intent

interface ReminderIntentProvider {
    fun createRingingIntent(reminderId: Long): Intent
}
