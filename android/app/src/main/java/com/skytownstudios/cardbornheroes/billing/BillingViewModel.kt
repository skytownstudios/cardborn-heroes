package com.skytownstudios.cardbornheroes.billing

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.skytownstudios.cardbornheroes.MonetizationConfig

class BillingViewModel(app: Application) : AndroidViewModel(app), PurchasesUpdatedListener {
    private val client = BillingClient.newBuilder(app)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium

    private val _prices = MutableStateFlow<Map<String, String>>(emptyMap())
    val prices: StateFlow<Map<String, String>> = _prices

    init {
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProducts()
                    queryPurchases()
                }
            }
            override fun onBillingServiceDisconnected() {}
        })
    }

    private fun queryProducts() {
        val subs = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(MonetizationConfig.PREMIUM_MONTHLY)
                        .setProductType(BillingClient.ProductType.SUBS).build(),
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(MonetizationConfig.PREMIUM_YEARLY)
                        .setProductType(BillingClient.ProductType.SUBS).build(),
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(MonetizationConfig.REMOVE_ADS)
                        .setProductType(BillingClient.ProductType.INAPP).build(),
                )
            ).build()
        client.queryProductDetailsAsync(subs) { result, list ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _prices.value = list.associate { it.productId to (it.oneTimePurchaseOfferDetails?.formattedPrice ?: it.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: "") }
            }
        }
    }

    private fun queryPurchases() {
        client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        ) { r, purchases -> updatePremium(purchases) }
        client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { r, purchases -> updatePremium(purchases) }
    }

    private fun updatePremium(purchases: List<Purchase>) {
        val premium = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED &&
            (it.products.contains(MonetizationConfig.REMOVE_ADS) ||
             it.products.contains(MonetizationConfig.PREMIUM_MONTHLY) ||
             it.products.contains(MonetizationConfig.PREMIUM_YEARLY)) }
        _isPremium.value = premium
    }

    fun purchase(activity: Activity, productId: String) {
        val type = if (productId == MonetizationConfig.REMOVE_ADS) BillingClient.ProductType.INAPP else BillingClient.ProductType.SUBS
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(
                QueryProductDetailsParams.Product.newBuilder().setProductId(productId).setProductType(type).build()
            )).build()
        client.queryProductDetailsAsync(params) { result, details ->
            val detail = details.firstOrNull() ?: return@queryProductDetailsAsync
            val flow = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(detail).build()
            client.launchBillingFlow(activity, BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(flow)).build())
        }
    }

    fun restore() = queryPurchases()

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(it.purchaseToken).build()) {} }
            updatePremium(purchases)
        }
    }
}
