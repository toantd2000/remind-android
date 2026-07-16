package vn.io.litever.remind.app

import android.app.Application

import dagger.hilt.android.HiltAndroidApp

import com.google.firebase.analytics.FirebaseAnalytics
import vn.io.litever.remind.app.BuildConfig

@HiltAndroidApp
class ReMindApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        if (BuildConfig.DEBUG) {
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false)
        }
    }
}











