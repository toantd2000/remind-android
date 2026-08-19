package vn.io.litever.remind.core.alarm.di

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    companion object {
        @Provides
        @Singleton
        fun provideLocationManager(@ApplicationContext context: Context): LocationManager {
            return context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }

        @Provides
        @Singleton
        fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager {
            return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        }

        @Provides
        @Singleton
        fun provideWifiManager(@ApplicationContext context: Context): WifiManager {
            return context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        }
    }
}
