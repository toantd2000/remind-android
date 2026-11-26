package vn.io.litever.remind.core.model

import java.io.Serializable

enum class BillingPlanType : Serializable {
    MONTHLY,
    ANNUAL,
    LIFETIME
}

data class ProProduct(
    val productId: String,
    val planType: BillingPlanType,
    val formattedPrice: String,
    val offerToken: String? = null,
    val hasFreeTrial: Boolean = false,
    val trialDays: Int = 0
) : Serializable

data class ProEntitlement(
    val isPro: Boolean = false,
    val activeProductId: String? = null
) : Serializable

sealed interface PurchaseStatus {
    data object Idle : PurchaseStatus
    data object Loading : PurchaseStatus
    data object Restoring : PurchaseStatus
    data object Success : PurchaseStatus
    data object Restored : PurchaseStatus
    data object NothingToRestore : PurchaseStatus
    data object Cancelled : PurchaseStatus
    data class Error(val message: String) : PurchaseStatus
}

object ProProductIds {
    const val SUB_MONTHLY = "remind_pro_monthly"
    const val SUB_ANNUAL = "remind_pro_annual"
    const val IAP_LIFETIME = "remind_pro_lifetime"
}
