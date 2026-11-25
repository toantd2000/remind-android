package vn.io.litever.remind.core.common.util

import androidx.annotation.DrawableRes

enum class AppIconVariant(val key: String) {
    DEFAULT("DEFAULT"),
    PRO_GOLD("PRO_GOLD");

    companion object {
        fun fromKey(key: String?): AppIconVariant =
            entries.firstOrNull { it.key == key } ?: DEFAULT
    }
}

interface AppIconManager {
    @get:DrawableRes
    val defaultForegroundRes: Int

    @get:DrawableRes
    val proGoldForegroundRes: Int

    /**
     * Schedules an app icon change to be applied when the app moves to background,
     * preventing Android/MIUI from forcibly closing the foreground task.
     */
    fun scheduleAppIconChange(variant: AppIconVariant)

    /**
     * Applies any pending app icon change safely while the app is stopping.
     */
    fun applyPendingIconChange()
}
