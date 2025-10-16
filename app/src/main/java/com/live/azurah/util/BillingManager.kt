package com.live.azurah.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams
import com.google.gson.Gson
import com.live.azurah.listener.BillingUpdatesListener

class BillingManager(context: Context, private val listener: BillingUpdatesListener) {

    val billingClient = BillingClient.newBuilder(context)
        .setListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                listener.onPurchasesUpdated(purchases)
            }
        }
        .enablePendingPurchases()
        .build()

    fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // BillingClient is ready
                    Log.d("sdsdvsdvds","conectedddddddd")
                    queryAvailableProducts()
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d("sdsdvsdvds","disconecteddddd")
                // Try to reconnect later
            }
        })
    }

    private fun queryAvailableProducts() {
        val params = QueryProductDetailsParams.newBuilder()
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId("bible_monthly")
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        params.setProductList(productList)

        billingClient.queryProductDetailsAsync(params.build()) { billingResult, productDetailsList ->
            Log.d("sdsdvsdvds",Gson().toJson(productDetailsList))
            Log.d("sdsdvsdvds", "Billing result: ${billingResult.responseCode} - ${billingResult.debugMessage}")
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                listener.onProductDetailsFetched(productDetailsList)
            }
        }
    }

    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
        val offerToken = productDetails.subscriptionOfferDetails
            ?.firstOrNull() // You may want to filter for specific pricing phases
            ?.offerToken

        if (offerToken == null) {
            Log.e("BillingManager", "Offer token is missing for subscription product")
            return
        }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offerToken)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }
  /*  fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }*/
}
