package vn.io.litever.remind.core.alarm.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.io.litever.remind.core.alarm.AlarmSchedulerImpl
import vn.io.litever.remind.core.domain.scheduler.AlarmScheduler
import vn.io.litever.remind.core.domain.scheduler.AlarmController
import vn.io.litever.remind.core.alarm.service.AlarmControllerImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class AlarmModule {
    @Binds
    abstract fun bindAlarmScheduler(impl: AlarmSchedulerImpl): AlarmScheduler
    
    @Binds
    abstract fun bindAlarmController(impl: AlarmControllerImpl): AlarmController
}










