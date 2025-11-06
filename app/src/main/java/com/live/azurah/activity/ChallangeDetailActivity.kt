package com.live.azurah.activity

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.view.animation.ScaleAnimation
import androidx.activity.viewModels
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.live.azurah.R
import com.live.azurah.adapter.ShopSliderAdapter
import com.live.azurah.adapter.ViewPagerAdapter
import com.live.azurah.databinding.ActivityChallangeDetailBinding
import com.live.azurah.databinding.ConfirmationDialogBinding
import com.live.azurah.fragment.AdviceFragment
import com.live.azurah.fragment.OverviewFragment
import com.live.azurah.model.BibleQuestViewModel
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.ShopBannerModel
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.isExpired
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import com.live.azurah.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChallangeDetailActivity : AppCompatActivity(), Observer<Resource<Any>> {
    lateinit var binding: ActivityChallangeDetailBinding
    private val list = arrayOf("Overview", "Advice")
    private lateinit var fragmentList: MutableList<Fragment>
    private lateinit var viewpagerAdapter: ViewPagerAdapter
    private var from = ""
    private var id = ""
    private var isPremium = ""
    private var subscribed = false
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private lateinit var sharedViewModel: SharedViewModel
    private var model = BibleQuestViewModel.Body()
    private lateinit var receiver: BroadcastReceiver
    private lateinit var billingClient: BillingClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChallangeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.white)
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
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

        binding.viewPager.isNestedScrollingEnabled = false
        binding.viewPager.isUserInputEnabled = false
        binding.clVerseDay.isNestedScrollingEnabled = true;
        binding.tbLayout.isNestedScrollingEnabled = true;
        from = intent.getStringExtra("from") ?: ""
        id = intent.getStringExtra("id") ?: ""
        initListener()
        initData()
        initFragment()
        TabLayoutMediator(binding.tbLayout, binding.viewPager) { tab, position ->
            tab.text = list[position]

        }.attach()

        binding.shimmerLayout.visible()
        binding.shimmerLayout.startShimmer()

        receiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                if (p1?.action == "Shop") {
                    Log.d("sdfdsgfdsgdg", "dsfds")

                    getDetail()
                }
            }
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter("Shop"))
    }

    private fun getDetail() {
        viewModel.getBibleView(id, this).observe(this, this)
    }

    private fun initListener() {
        with(binding) {
            btnRestart.setOnClickListener {
                if (isPremium == "1") {
                    if (subscribed) {
                        confirmationDialog()
                    } else {
                        startActivity(
                            Intent(
                                this@ChallangeDetailActivity,
                                SubscriptionActivity::class.java
                            )
                        )
                    }
                } else {
                    confirmationDialog()
                }

            }
            btnStartChallange.setOnClickListener {
                if (isPremium == "1") {
                    if (subscribed) {
                        startActivity(
                            Intent(
                                this@ChallangeDetailActivity,
                                MarkChallangeActivity::class.java
                            ).apply {
                                putExtra("from", from)
                                putExtra("id", id)
                            })
                    } else {
                        startActivity(
                            Intent(
                                this@ChallangeDetailActivity,
                                SubscriptionActivity::class.java
                            )
                        )
                    }
                } else {
                    startActivity(
                        Intent(
                            this@ChallangeDetailActivity,
                            MarkChallangeActivity::class.java
                        ).apply {
                            putExtra("from", from)
                            putExtra("id", id)
                        })
                }

            }
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun initData() {
        fragmentList = ArrayList()
        fragmentList.add(OverviewFragment())
        fragmentList.add(AdviceFragment())
    }

    private fun initFragment() {
        viewpagerAdapter = ViewPagerAdapter(fragmentList, this)
        binding.viewPager.adapter = viewpagerAdapter

    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
                binding.shimmerLayout.gone()
                binding.shimmerLayout.stopShimmer()
                binding.btnStartChallange.visible()
                binding.clLayout.visible()
               LoaderDialog.dismiss()
                when (value.data) {
                    is BibleQuestViewModel -> {
                        val res = value.data.body
                        model = value.data.body ?: BibleQuestViewModel.Body()
                        with(binding) {
                            tvTitle.text = model.title ?: ""
                            val imagesList =
                                res?.bibleQuestImages?.map { it?.image ?: "" } as ArrayList

                            val modelList = res.bibleQuestImages.map {
                                ShopBannerModel(
                                    image = it?.image,
                                    id = it?.id,
                                    type = 0
                                )
                            } as ArrayList<ShopBannerModel>

                            isPremium = model.isPremium ?: ""
                            rvChallangeImages.adapter =
                                ShopSliderAdapter(this@ChallangeDetailActivity, modelList)
                            TabLayoutMediator(tabLayout, rvChallangeImages) { tab, position ->
                                tab.setIcon(R.drawable.dot_unselected)
                            }.attach()

                            if (modelList.size > 1) {
                                tabLayout.visible()
                            } else {
                                tabLayout.gone()
                            }

                            tabLayout.addOnTabSelectedListener(object :
                                TabLayout.OnTabSelectedListener {
                                override fun onTabSelected(tab: TabLayout.Tab) {
                                    tab.setIcon(R.drawable.dot_selected)
                                    val scaleUp = ScaleAnimation(
                                        1.0f, 1.0f,
                                        1.0f, 1.0f,
                                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f
                                    ).apply {
                                        duration = 200
                                        fillAfter = true
                                    }
                                    tab.view.startAnimation(scaleUp)
                                }

                                override fun onTabUnselected(tab: TabLayout.Tab) {
                                    tab.setIcon(R.drawable.dot_unselected)
                                    val scaleDown = ScaleAnimation(
                                        1.0f, 1.0f,  // Start and end X scale
                                        1.0f, 1.0f,  // Start and end Y scale
                                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                                        ScaleAnimation.RELATIVE_TO_SELF, 0.5f
                                    ).apply {
                                        duration = 200
                                        fillAfter = true
                                    }
                                    tab.view.startAnimation(scaleDown)
                                }

                                override fun onTabReselected(tab: TabLayout.Tab) {
                                }
                            })

                            Log.d("fjsdbjdfkgfg", res.bibleVerse.toString().trim())

//                            val cleanedText = res.bibleVerse.toString().trim().replace("&nbsp;", " ")
                            val cleanedText = res.bibleVerse
                                .toString()
                                .replace("&nbsp;", " ")
                                .replace("<p>", "")
                                .replace("</p>", "")
                                .replace("<br>", "")
                                .replace("<br/>", "")
                                .replace("<div>", "")
                                .replace("</div>", "")
                                .trim()
                            tvMessage.text = HtmlCompat.fromHtml(
                                cleanedText,
                                HtmlCompat.FROM_HTML_MODE_COMPACT
                            )
                            tvVerse.text = res.bibleVersion ?: ""
                            sharedViewModel.setChallengeData(res)
                            if (res.isChallengeStarted == 0 && res.isChallengeCompleted == 0) {
                                binding.btnStartChallange.visible()
                                binding.btnRestart.gone()
                                binding.btnStartChallange.text =
                                    getString(R.string.start_challenge_1)
                            } else if ((res.isChallengeStarted
                                    ?: 0) > 0 && res.isChallengeCompleted == 0
                            ) {
                                binding.btnStartChallange.visible()
                                binding.btnRestart.visible()
                                binding.btnStartChallange.text =
                                    getString(R.string.resume_challenge)

                            } else if ((res.isChallengeStarted
                                    ?: 0) > 0 && res.isChallengeCompleted == 1
                            ) {
                                binding.btnStartChallange.gone()
                                binding.btnRestart.visible()
                                /* binding.btnStartChallange.text =
                                     getString(R.string.restart_challenge_1)*/
                            }
                        }
                    }
                }
            }

            Status.LOADING -> {
//                LoaderDialog.show(this)
            }

            Status.ERROR -> {
                binding.shimmerLayout.gone()
                binding.shimmerLayout.stopShimmer()
                binding.btnStartChallange.gone()
                binding.clLayout.gone()
               LoaderDialog.dismiss()
                showCustomSnackbar(this, binding.root, value.message.toString())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getDetail()
        checkSubscriptionStatus()

    }

    private fun confirmationDialog() {
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val confirmationBinding = ConfirmationDialogBinding.inflate(layoutInflater)
        customDialog.setContentView(confirmationBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        confirmationBinding.tvNo.setOnClickListener {
            customDialog.dismiss()
        }

        confirmationBinding.tvYes.setOnClickListener {
            customDialog.dismiss()
            markAsRestart()
        }
        customDialog.show()
    }

    private fun markAsRestart() {
        val map = HashMap<String, String>()
        map["user_id"] = getPreference("id", "")
        map["bible_quest_id"] = id

        viewModel.markAsRestart(map, this).observe(this) { value ->
            when (value.status) {
                Status.SUCCESS -> {
                   LoaderDialog.dismiss()
                    when (value.data) {
                        is CommonResponse -> {
                            startActivity(
                                Intent(
                                    this@ChallangeDetailActivity,
                                    MarkChallangeActivity::class.java
                                ).apply {
                                    putExtra("from", from)
                                    putExtra("id", id)
                                })
                        }
                    }
                }

                Status.LOADING -> {
                    LoaderDialog.show(this)
                }

                Status.ERROR -> {
                   LoaderDialog.dismiss()
                    showCustomSnackbar(this, binding.root, value.message.toString())
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    private fun checkSubscriptionStatus() {
        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener { billingResult, purchases ->
                // You usually don't need to handle this here for checking past purchases
            }
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryActivePurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry logic if needed
            }
        })
    }

    private fun queryActivePurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val isSubscribed = purchasesList.any { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isExpired()
                }

                subscribed = isSubscribed
            }
        }
    }




}