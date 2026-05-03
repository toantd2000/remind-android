package vn.io.litever.remind.core.common.ads

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NativeAdManagerEntryPoint {
    fun nativeAdManager(): NativeAdManager
}
