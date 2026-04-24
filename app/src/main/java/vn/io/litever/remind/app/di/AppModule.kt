package vn.io.litever.remind.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.io.litever.remind.app.provider.AlarmIntentProviderImpl
import vn.io.litever.remind.core.alarm.provider.AlarmIntentProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    abstract fun bindAlarmIntentProvider(impl: AlarmIntentProviderImpl): AlarmIntentProvider
}










