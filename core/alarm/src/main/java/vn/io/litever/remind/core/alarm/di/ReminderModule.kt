package vn.io.litever.remind.core.alarm.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.io.litever.remind.core.alarm.alert.ReminderAlertManager
import vn.io.litever.remind.core.alarm.alert.ReminderAlertManagerImpl
import vn.io.litever.remind.core.alarm.reminder.ReminderSchedulerImpl
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReminderModule {

    @Binds
    @Singleton
    abstract fun bindReminderScheduler(impl: ReminderSchedulerImpl): ReminderScheduler

    @Binds
    @Singleton
    abstract fun bindReminderAlertManager(impl: ReminderAlertManagerImpl): ReminderAlertManager
}
