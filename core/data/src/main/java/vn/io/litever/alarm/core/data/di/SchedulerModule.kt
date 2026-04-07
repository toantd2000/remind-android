package vn.io.litever.alarm.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.io.litever.alarm.core.data.scheduler.AlarmSchedulerImpl
import vn.io.litever.alarm.core.domain.scheduler.AlarmScheduler

@Module
@InstallIn(SingletonComponent::class)
abstract class SchedulerModule {
    @Binds
    abstract fun bindAlarmScheduler(impl: AlarmSchedulerImpl): AlarmScheduler
}
