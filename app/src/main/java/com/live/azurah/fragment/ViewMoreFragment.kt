package com.live.azurah.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.live.azurah.databinding.FragmentViewMoreBinding
import com.live.azurah.listener.BillingUpdatesListener
import com.live.azurah.util.BillingManager
import com.live.azurah.util.showCustomSnackbar
import kotlinx.coroutines.launch

class ViewMoreFragment : Fragment(), BillingUpdatesListener {
    private lateinit var binding : FragmentViewMoreBinding
    private lateinit var billingManager: BillingManager
    private var monthlyProduct: ProductDetails? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewMoreBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        billingManager = BillingManager(requireContext(), this)
        billingManager.startConnection()

        initListener()
    }


    private fun initListener() {
        with(binding) {
            backIcon.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }
            btnEdit1.setOnClickListener {
                monthlyProduct?.let { it1 ->
                    billingManager.launchBillingFlow(requireActivity(), it1)
                } ?: showCustomSnackbar(requireActivity(), it, "Product not loaded.")
            }
        }
    }


    override fun onPurchasesUpdated(purchases: List<Purchase>) {
        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                acknowledgePurchase(purchase)
            }
        }
    }

    override fun onProductDetailsFetched(productDetailsList: List<ProductDetails>) {
        monthlyProduct = productDetailsList.firstOrNull()

        monthlyProduct?.let {
            val price = it.subscriptionOfferDetails
                ?.firstOrNull()
                ?.pricingPhases
                ?.pricingPhaseList
                ?.firstOrNull()
                ?.formattedPrice ?: "N/A"

            /* binding.txtPriceMonthly.text = buildString {
                 append("Only ")
                 append(price)
                 append(" a month")

             }*/
//            binding.txtPriceMonthly.visible()
//           LoaderDialog.dismiss()
        }
    }


    private fun acknowledgePurchase(purchase: Purchase) {
        if (purchase.isAcknowledged) return

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingManager.billingClient.acknowledgePurchase(params) { result ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                lifecycleScope.launch {
                    showCustomSnackbar(
                        requireActivity(),
                        binding.root,
                        "Subscription purchased successfully."
                    )
                  requireActivity().finish()
                }
            }
        }
    }
}