package vn.io.litever.remind.app.provider

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import vn.io.litever.remind.app.R
import vn.io.litever.remind.core.common.util.AppIconManager
import vn.io.litever.remind.core.common.util.AppIconVariant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppIconManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppIconManager {

    override val defaultForegroundRes: Int = R.drawable.ic_launcher_foreground
    override val proGoldForegroundRes: Int = R.drawable.ic_launcher_fg_pro_gold_trim

    @Volatile
    private var pendingVariant: AppIconVariant? = null

    override fun scheduleAppIconChange(variant: AppIconVariant) {
        pendingVariant = if (isAliasCurrentlyEnabled(variant)) null else variant
    }

    override fun applyPendingIconChange() {
        val targetVariant = pendingVariant ?: return
        pendingVariant = null

        val pm = context.packageManager
        val aliases = mapOf(
            AppIconVariant.DEFAULT to ComponentName(context, ALIAS_DEFAULT),
            AppIconVariant.PRO_GOLD to ComponentName(context, ALIAS_PRO_GOLD),
        )

        val targetComponent = aliases[targetVariant] ?: return
        pm.setComponentEnabledSetting(
            targetComponent,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        aliases.forEach { (key, component) ->
            if (key != targetVariant) {
                pm.setComponentEnabledSetting(
                    component,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
        }
    }

    private fun isAliasCurrentlyEnabled(variant: AppIconVariant): Boolean {
        val aliasName = when (variant) {
            AppIconVariant.DEFAULT -> ALIAS_DEFAULT
            AppIconVariant.PRO_GOLD -> ALIAS_PRO_GOLD
        }
        val state = context.packageManager.getComponentEnabledSetting(ComponentName(context, aliasName))
        return when (variant) {
            AppIconVariant.DEFAULT -> state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
                state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
            AppIconVariant.PRO_GOLD -> state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }
    }

    companion object {
        private const val ALIAS_DEFAULT = "vn.io.litever.remind.app.MainActivityDefault"
        private const val ALIAS_PRO_GOLD = "vn.io.litever.remind.app.MainActivityProGold"
    }
}
