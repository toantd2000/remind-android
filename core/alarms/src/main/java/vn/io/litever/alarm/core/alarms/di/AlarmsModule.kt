package vn.io.litever.alarm.core.alarms.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.io.litever.alarm.core.alarms.AlarmSchedulerImpl
import vn.io.litever.alarm.core.domain.scheduler.AlarmScheduler

@Module
@InstallIn(SingletonComponent::class)
abstract class AlarmsModule {
    @Binds
    abstract fun bindAlarmScheduler(impl: AlarmSchedulerImpl): AlarmScheduler
    
    @Binds
    abstract fun bindAlarmController(impl: vn.io.litever.alarm.core.alarms.service.AlarmControllerImpl): vn.io.litever.alarm.core.domain.scheduler.AlarmController
}
