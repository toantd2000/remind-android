package vn.io.litever.remind.core.reminder.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.io.litever.remind.core.reminder.ReminderSchedulerImpl
import vn.io.litever.remind.core.domain.scheduler.ReminderScheduler
import vn.io.litever.remind.core.domain.scheduler.ReminderController
import vn.io.litever.remind.core.reminder.service.ReminderControllerImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class ReminderModule {
    @Binds
    abstract fun bindReminderScheduler(impl: ReminderSchedulerImpl): ReminderScheduler
    
    @Binds
    abstract fun bindReminderController(impl: ReminderControllerImpl): ReminderController
}
