package vn.io.litever.remind.core.analytics.di

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import vn.io.litever.remind.core.analytics.AnalyticsLogger
import vn.io.litever.remind.core.analytics.FirebaseAnalyticsLogger
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsBindsModule {

    @Binds
    @Singleton
    abstract fun bindAnalyticsLogger(
        firebaseAnalyticsLogger: FirebaseAnalyticsLogger
    ): AnalyticsLogger
}

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsProvidesModule {

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(
        @ApplicationContext context: Context
    ): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }
}
