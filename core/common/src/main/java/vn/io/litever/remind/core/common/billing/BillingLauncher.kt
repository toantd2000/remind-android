package vn.io.litever.remind.core.common.billing

import android.app.Activity

/**
 * Abstraction for launching Google Play Billing purchase flow from UI layers
 * without leaking Play Billing SDK dependencies into feature modules.
 */
interface BillingLauncher {
    fun launchPurchaseFlow(activity: Activity, productId: String, offerToken: String? = null)
    fun restorePurchases()
}
