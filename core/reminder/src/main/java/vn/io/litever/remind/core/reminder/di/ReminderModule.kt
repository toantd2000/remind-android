package vn.io.litever.remind.core.reminder.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.io.litever.remind.core.reminder.ReminderSchedulerImpl
import vn.io.litever.alarm.core.domain.scheduler.AlarmScheduler

@Module
@InstallIn(SingletonComponent::class)
abstract class ReminderModule {
    @Binds
    abstract fun bindReminderScheduler(impl: ReminderSchedulerImpl): AlarmScheduler
    
    @Binds
    abstract fun bindReminderController(impl: vn.io.litever.remind.core.reminder.service.ReminderControllerImpl): vn.io.litever.alarm.core.domain.scheduler.AlarmController
}
