package vn.io.litever.alarm.core.alarms.provider

import android.content.Intent

interface AlarmIntentProvider {
    fun createRingingIntent(alarmId: Long): Intent
}
