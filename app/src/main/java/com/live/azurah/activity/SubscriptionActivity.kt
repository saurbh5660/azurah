package com.live.azurah.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.live.azurah.R
import com.live.azurah.databinding.ActivitySubscriptionBinding
import com.live.azurah.fragment.ViewMoreFragment
import com.live.azurah.listener.BillingUpdatesListener
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.util.BillingManager
import com.live.azurah.util.showCustomSnackbar
import kotlinx.coroutines.launch

class SubscriptionActivity : AppCompatActivity(), BillingUpdatesListener {
    private lateinit var binding: ActivitySubscriptionBinding
    private lateinit var billingManager: BillingManager
    private var monthlyProduct: ProductDetails? = null
    //   private val loaderDialog by lazy { LoaderDialog(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.white)
        window.navigationBarColor = getColor(R.color.white)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
              val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            view.updatePadding(
                left = systemBars.left,
                bottom = systemBars.bottom,
                right = systemBars.right,
                top = systemBars.top
            )
            insets
        }
        initListener()
        billingManager = BillingManager(this, this)
        billingManager.startConnection()
//        LoaderDialog.show(this)
    }

    private fun initListener() {
        with(binding) {
            setSpannableText(this@SubscriptionActivity, tvTerms)
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            btnMoreInfo.setOnClickListener {
                replaceViewFragment(ViewMoreFragment())
            }
            btnEdit1.setOnClickListener {
                monthlyProduct?.let { it1 ->
                    billingManager.launchBillingFlow(this@SubscriptionActivity, it1)
                } ?: showCustomSnackbar(this@SubscriptionActivity, it, "Product not loaded.")
            }
        }
    }

    private fun setSpannableText(context: Context, textView: TextView) {
        val fullText =
            "By subscribing, you agree to our Terms & Conditions and Privacy Policy. Your subscription renews automatically unless cancelled at least 24 hours before the end of the current billing period."

        val spannable = SpannableString(fullText)

        val termsText = "Terms & Conditions"
        val privacyText = "Privacy Policy"

        val termsStart = fullText.indexOf(termsText)
        val termsEnd = termsStart + termsText.length
        val privacyStart = fullText.indexOf(privacyText)
        val privacyEnd = privacyStart + privacyText.length

        val whiteColor = ContextCompat.getColor(context, R.color.white)

        val termsClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                context.startActivity(Intent(context, ContentActivity::class.java).apply {
                    putExtra("type", 0)
                })
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = whiteColor
            }
        }

        val privacyClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                context.startActivity(Intent(context, ContentActivity::class.java).apply {
                    putExtra("type", 1)
                })
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = whiteColor
            }
        }

        spannable.setSpan(termsClickable, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(
            privacyClickable,
            privacyStart,
            privacyEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.text = spannable
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = ContextCompat.getColor(context, android.R.color.transparent)
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
                        this@SubscriptionActivity,
                        binding.root,
                        "Subscription purchased successfully."
                    )
                    finish()
                }
            }
        }
    }


    private fun replaceViewFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(binding.clMainSubscription.id, fragment).addToBackStack(null).commit()
    }
}

