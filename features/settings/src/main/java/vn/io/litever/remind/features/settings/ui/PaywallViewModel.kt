package vn.io.litever.remind.features.settings.ui

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.common.billing.BillingLauncher
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.domain.repository.ProRepository
import vn.io.litever.remind.core.model.BillingPlanType
import vn.io.litever.remind.core.model.ProProduct
import vn.io.litever.remind.core.model.PurchaseStatus
import javax.inject.Inject

data class PaywallUiState(
    val isPro: Boolean = false,
    val products: List<ProProduct> = emptyList(),
    val selectedPlan: BillingPlanType = BillingPlanType.ANNUAL,
    val purchaseStatus: PurchaseStatus = PurchaseStatus.Idle
)

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val proRepository: ProRepository,
    private val billingLauncher: BillingLauncher,
    private val preferencesDataSource: AlarmPreferencesDataSource
) : ViewModel() {

    private val _selectedPlan = MutableStateFlow(BillingPlanType.ANNUAL)

    val uiState: StateFlow<PaywallUiState> = combine(
        proRepository.isPro,
        proRepository.products,
        _selectedPlan,
        proRepository.purchaseStatus
    ) { isPro, products, selectedPlan, status ->
        PaywallUiState(
            isPro = isPro,
            products = products,
            selectedPlan = selectedPlan,
            purchaseStatus = status
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PaywallUiState()
    )

    fun selectPlan(plan: BillingPlanType) {
        if (plan == BillingPlanType.MONTHLY || plan == BillingPlanType.ANNUAL) {
            _selectedPlan.value = plan
        }
    }

    /**
     * Launches the Google Play Billing purchase flow for the selected plan.
     * @return true if the product was found and flow was launched, false if products are not yet loaded.
     */
    fun purchaseSelectedPlan(activity: Activity): Boolean {
        val state = uiState.value
        val product = state.products.firstOrNull { it.planType == state.selectedPlan } ?: return false
        billingLauncher.launchPurchaseFlow(
            activity = activity,
            productId = product.productId,
            offerToken = product.offerToken
        )
        return true
    }

    fun restorePurchases() {
        billingLauncher.restorePurchases()
    }

    fun clearPurchaseStatus() {
        proRepository.clearPurchaseStatus()
    }

    fun grant24hAdFreeReward(context: Context) {
        viewModelScope.launch {
            val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
            val duration = if (isDebug) 1 * 60 * 60 * 1000L else 24 * 60 * 60 * 1000L
            preferencesDataSource.setAdsDisabledUntil(System.currentTimeMillis() + duration)
        }
    }
}
