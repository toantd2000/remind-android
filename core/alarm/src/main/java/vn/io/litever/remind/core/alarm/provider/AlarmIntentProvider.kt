package vn.io.litever.remind.core.alarm.provider

import android.content.Intent

interface AlarmIntentProvider {
    fun createRingingIntent(alarmId: Long): Intent
}










