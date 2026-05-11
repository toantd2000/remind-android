package vn.io.litever.remind.core.ads.api

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAdManager = staticCompositionLocalOf<AdManager> {
    error("No AdManager provided")
}
