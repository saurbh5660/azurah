package com.live.azurah.listener

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase

interface BillingUpdatesListener {
    fun onPurchasesUpdated(purchases: List<Purchase>)
    fun onProductDetailsFetched(productDetailsList: List<ProductDetails>)
}