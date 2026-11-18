package vn.io.litever.remind.core.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import vn.io.litever.remind.core.common.billing.BillingLauncher
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.domain.repository.ProRepository
import vn.io.litever.remind.core.model.BillingPlanType
import vn.io.litever.remind.core.model.ProEntitlement
import vn.io.litever.remind.core.model.ProProduct
import vn.io.litever.remind.core.model.ProProductIds
import vn.io.litever.remind.core.model.PurchaseStatus
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PlayBillingManager"

@Singleton
class PlayBillingManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesDataSource: AlarmPreferencesDataSource
) : ProRepository, BillingLauncher {

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    override val isPro: Flow<Boolean> = preferencesDataSource.isProUser

    override val proEntitlement: Flow<ProEntitlement> = combine(
        preferencesDataSource.isProUser,
        preferencesDataSource.proProductId
    ) { isPro, productId ->
        ProEntitlement(isPro = isPro, activeProductId = productId)
    }

    private val _products = MutableStateFlow(defaultFallbackProducts())
    override val products: StateFlow<List<ProProduct>> = _products.asStateFlow()

    private val _purchaseStatus = MutableStateFlow<PurchaseStatus>(PurchaseStatus.Idle)
    override val purchaseStatus: StateFlow<PurchaseStatus> = _purchaseStatus.asStateFlow()

    private val productDetailsMap = ConcurrentHashMap<String, ProductDetails>()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (purchases != null) {
                    scope.launch {
                        handlePurchases(purchases, isRestore = false)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseStatus.value = PurchaseStatus.Cancelled
            }
            else -> {
                _purchaseStatus.value = PurchaseStatus.Error(
                    billingResult.debugMessage.ifBlank { "Billing error: ${billingResult.responseCode}" }
                )
            }
        }
    }

    private val billingClient: BillingClient by lazy {
        BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .enablePrepaidPlans()
                    .build()
            )
            .build()
    }

    override fun startConnection() {
        if (billingClient.isReady) {
            queryProductDetails()
            refreshPurchases()
            return
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "BillingClient connected")
                    queryProductDetails()
                    refreshPurchases()
                } else {
                    Log.w(TAG, "BillingClient setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "BillingClient disconnected")
            }
        })
    }

    private fun queryProductDetails() {
        val subsList = listOf(ProProductIds.SUB_MONTHLY, ProProductIds.SUB_ANNUAL).map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val inAppList = listOf(ProProductIds.IAP_LIFETIME).map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val subsParams = QueryProductDetailsParams.newBuilder().setProductList(subsList).build()
        val inAppParams = QueryProductDetailsParams.newBuilder().setProductList(inAppList).build()

        billingClient.queryProductDetailsAsync(subsParams) { result, subsDetails ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                subsDetails.forEach { productDetailsMap[it.productId] = it }
                updateExposedProducts()
            }
        }

        billingClient.queryProductDetailsAsync(inAppParams) { result, inAppDetails ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                inAppDetails.forEach { productDetailsMap[it.productId] = it }
                updateExposedProducts()
            }
        }
    }

    private fun updateExposedProducts() {
        val defaults = defaultFallbackProducts().associateBy { it.productId }
        val resolved = mutableListOf<ProProduct>()

        listOf(ProProductIds.SUB_MONTHLY, ProProductIds.SUB_ANNUAL, ProProductIds.IAP_LIFETIME).forEach { id ->
            val details = productDetailsMap[id]
            val fallback = defaults[id]!!
            if (details == null) {
                resolved.add(fallback)
            } else if (details.productType == BillingClient.ProductType.SUBS) {
                val offers = details.subscriptionOfferDetails.orEmpty()
                val trialOffer = offers.firstOrNull { offer ->
                    offer.pricingPhases.pricingPhaseList.any { it.priceAmountMicros == 0L }
                }
                val selectedOffer = trialOffer ?: offers.firstOrNull()
                val paidPhase = selectedOffer?.pricingPhases?.pricingPhaseList?.lastOrNull()
                resolved.add(
                    ProProduct(
                        productId = id,
                        planType = fallback.planType,
                        formattedPrice = paidPhase?.formattedPrice ?: fallback.formattedPrice,
                        offerToken = selectedOffer?.offerToken,
                        hasFreeTrial = trialOffer != null,
                        trialDays = if (trialOffer != null) 7 else 0
                    )
                )
            } else {
                val oneTimePrice = details.oneTimePurchaseOfferDetails?.formattedPrice
                resolved.add(
                    ProProduct(
                        productId = id,
                        planType = BillingPlanType.LIFETIME,
                        formattedPrice = oneTimePrice ?: fallback.formattedPrice,
                        offerToken = null,
                        hasFreeTrial = false
                    )
                )
            }
        }
        _products.value = resolved
    }

    override fun refreshPurchases() {
        if (!billingClient.isReady) return
        val subsParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        val inAppParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        scope.launch {
            val allActive = mutableListOf<Purchase>()
            billingClient.queryPurchasesAsync(subsParams) { result, subsPurchases ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    allActive.addAll(subsPurchases)
                }
                billingClient.queryPurchasesAsync(inAppParams) { inAppResult, inAppPurchases ->
                    if (inAppResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        allActive.addAll(inAppPurchases)
                    }
                    scope.launch {
                        handlePurchases(allActive, isRestore = true)
                    }
                }
            }
        }
    }

    private suspend fun handlePurchases(purchases: List<Purchase>, isRestore: Boolean) {
        val activePurchase = purchases.firstOrNull { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        if (activePurchase != null) {
            if (!activePurchase.isAcknowledged) {
                val ackParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(activePurchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(ackParams) { ackResult ->
                    Log.d(TAG, "Acknowledge result: ${ackResult.responseCode}")
                }
            }
            val productId = activePurchase.products.firstOrNull()
            preferencesDataSource.setProStatus(isPro = true, productId = productId)
            _purchaseStatus.value = PurchaseStatus.Success
        } else if (isRestore) {
            preferencesDataSource.setProStatus(isPro = false, productId = null)
        }
    }

    override fun launchPurchaseFlow(activity: Activity, productId: String, offerToken: String?) {
        val details = productDetailsMap[productId]
        if (details == null || !billingClient.isReady) {
            _purchaseStatus.value = PurchaseStatus.Error("Google Play Billing is not ready")
            return
        }
        _purchaseStatus.value = PurchaseStatus.Loading

        val productParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
        if (details.productType == BillingClient.ProductType.SUBS) {
            val token = offerToken ?: details.subscriptionOfferDetails?.firstOrNull()?.offerToken
            if (token != null) {
                productParamsBuilder.setOfferToken(token)
            }
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParamsBuilder.build()))
            .build()

        val result = billingClient.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _purchaseStatus.value = PurchaseStatus.Error(
                result.debugMessage.ifBlank { "Unable to launch billing flow" }
            )
        }
    }

    override fun restorePurchases() {
        _purchaseStatus.value = PurchaseStatus.Loading
        if (!billingClient.isReady) {
            startConnection()
            _purchaseStatus.value = PurchaseStatus.Idle
        } else {
            refreshPurchases()
        }
    }

    override fun clearPurchaseStatus() {
        _purchaseStatus.value = PurchaseStatus.Idle
    }

    override suspend fun setProEntitlement(isPro: Boolean, productId: String?) {
        preferencesDataSource.setProStatus(isPro = isPro, productId = productId)
    }

    private fun defaultFallbackProducts(): List<ProProduct> = listOf(
        ProProduct(
            productId = ProProductIds.SUB_MONTHLY,
            planType = BillingPlanType.MONTHLY,
            formattedPrice = "$0.99",
            hasFreeTrial = false
        ),
        ProProduct(
            productId = ProProductIds.SUB_ANNUAL,
            planType = BillingPlanType.ANNUAL,
            formattedPrice = "$6.99",
            hasFreeTrial = true,
            trialDays = 7
        ),
        ProProduct(
            productId = ProProductIds.IAP_LIFETIME,
            planType = BillingPlanType.LIFETIME,
            formattedPrice = "$14.99",
            hasFreeTrial = false
        )
    )
}
