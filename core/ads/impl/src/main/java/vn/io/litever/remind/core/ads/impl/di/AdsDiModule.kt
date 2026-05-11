package vn.io.litever.remind.core.ads.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.io.litever.remind.core.ads.api.AdManager
import vn.io.litever.remind.core.ads.impl.AdMobManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdsDiModule {

    @Binds
    @Singleton
    abstract fun bindAdManager(
        adMobManagerImpl: AdMobManagerImpl
    ): AdManager
}
