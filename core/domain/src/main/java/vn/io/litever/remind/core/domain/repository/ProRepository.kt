package vn.io.litever.remind.core.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import vn.io.litever.remind.core.model.ProEntitlement
import vn.io.litever.remind.core.model.ProProduct
import vn.io.litever.remind.core.model.PurchaseStatus

interface ProRepository {
    val isPro: Flow<Boolean>
    val proEntitlement: Flow<ProEntitlement>
    val products: StateFlow<List<ProProduct>>
    val purchaseStatus: StateFlow<PurchaseStatus>

    fun startConnection()
    fun refreshPurchases()
    fun clearPurchaseStatus()
    suspend fun setProEntitlement(isPro: Boolean, productId: String?)
}
