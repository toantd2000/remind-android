package vn.io.litever.alarm.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.io.litever.alarm.app.provider.AlarmIntentProviderImpl
import vn.io.litever.alarm.core.alarms.provider.AlarmIntentProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    abstract fun bindAlarmIntentProvider(impl: AlarmIntentProviderImpl): AlarmIntentProvider
}
