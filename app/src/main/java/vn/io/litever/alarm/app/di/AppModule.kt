package vn.io.litever.alarm.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.io.litever.alarm.app.provider.ReminderIntentProviderImpl
import vn.io.litever.remind.core.reminder.provider.ReminderIntentProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    abstract fun bindReminderIntentProvider(impl: ReminderIntentProviderImpl): ReminderIntentProvider
}
