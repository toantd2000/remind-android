package vn.io.litever.alarm.core.alarms.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.io.litever.alarm.core.alarms.AlarmRingManager

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AlarmRingManagerEntryPoint {
    fun alarmRingManager(): AlarmRingManager
}
