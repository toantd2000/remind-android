package vn.io.litever.remind.core.alarm.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.io.litever.remind.core.alarm.AlarmRingManager

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AlarmRingManagerEntryPoint {
    fun alarmRingManager(): AlarmRingManager
}










