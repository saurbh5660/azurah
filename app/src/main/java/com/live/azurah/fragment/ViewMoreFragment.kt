package com.live.azurah.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.live.azurah.databinding.FragmentViewMoreBinding
import com.live.azurah.listener.BillingUpdatesListener
import com.live.azurah.model.CommonResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.BillingManager
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.getValue

@AndroidEntryPoint
class ViewMoreFragment : Fragment(), BillingUpdatesListener,Observer<Resource<Any>>  {
    private lateinit var binding : FragmentViewMoreBinding
    private lateinit var billingManager: BillingManager
    private var monthlyProduct: ProductDetails? = null
    private val viewModel by viewModels<CommonViewModel>()

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
                    val startDate = formatDate(purchase.purchaseTime)
                    val endDate = formatDate(getSubscriptionExpiryDate(purchase))

                    showCustomSnackbar(
                        requireContext(),
                        binding.root,
                        "Subscription purchased successfully."
                    )
                    requireActivity().finish()
//                    updateSubscription(startDate, endDate,purchase.purchaseToken)
                }
            }
        }
    }

    private fun getSubscriptionExpiryDate(purchase: Purchase): Long {
        return try {
            val purchaseTime = purchase.purchaseTime
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = purchaseTime
            calendar.add(Calendar.MONTH, 1) // Assuming 1 month subscription
            calendar.timeInMillis
        } catch (e: Exception) {
            purchase.purchaseTime + (30L * 24 * 60 * 60 * 1000)
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
                LoaderDialog.dismiss()
                when (value.data) {
                    is CommonResponse -> {
                        showCustomSnackbar(
                            requireContext(),
                            binding.root,
                            "Subscription purchased successfully."
                        )
                       requireActivity().finish()
                    }
                }
            }

            Status.LOADING -> {
                LoaderDialog.show(requireActivity())
            }

            Status.ERROR -> {
                LoaderDialog.dismiss()
                showCustomSnackbar(requireActivity(),binding.root, value.message.toString())
            }
        }
    }

    private fun updateSubscription(startDate: String, endDate: String, purchaseToken: String){
        val map = HashMap<String,String>()
        map["start_date"] = startDate
        map["end_date"] = endDate
        map["purchase_token"] = purchaseToken

        viewModel.updateSubscription(map,requireActivity()).observe(viewLifecycleOwner,this)

    }
}