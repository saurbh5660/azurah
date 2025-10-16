package com.live.azurah.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import com.live.azurah.R
import com.live.azurah.adapter.NotificationAdapter
import com.live.azurah.databinding.ActivityNotificationBinding
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.DetailResponse
import com.live.azurah.model.InterestModel
import com.live.azurah.model.NotificationListingResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.formatCount
import com.live.azurah.util.getPreference
import com.live.azurah.util.getRelativeTime
import com.live.azurah.util.gone
import com.live.azurah.util.loadImage
import com.live.azurah.util.setupSeeMoreText
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivityNotificationBinding
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var notificationList = ArrayList<NotificationListingResponse.Body.Data>()
    private var notificationAdapter: NotificationAdapter? = null
    private var isNotification = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.white)
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
        isNotification = intent.getStringExtra("isNotification") ?: ""
        initListener()
        setAdapter()
        getNotification()
    }

    private fun getNotification() {
        viewModel.getNotification(this).observe(this, this)
    }

    private fun initListener() {
        with(binding) {
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            onBackPressedDispatcher.addCallback(
                this@NotificationActivity,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (isNotification == "1") {
                            startActivity(Intent(this@NotificationActivity,HomeActivity::class.java))
                            finishAffinity()
                        } else {
                            finish()
                        }
                    }
                }
            )

            /*   ivCross.setOnClickListener {
                   etSearch.setText("")
               }
               etSearch.addTextChangedListener(object: TextWatcher {
                   override fun beforeTextChanged(
                       s: CharSequence?,
                       start: Int,
                       count: Int,
                       after: Int
                   ) {

                   }

                   override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                       if (s.toString().isNotBlank()){
                           ivCross.visibility = View.VISIBLE
                       }else{
                           ivCross.visibility = View.GONE
                       }
                   }

                   override fun afterTextChanged(s: Editable?) {

                   }

               })*/
        }
    }

    private fun setAdapter() {
        /*   list.add(InterestModel(false, icon = R.drawable.unselected_heart, name = "Rudiey03 liked your post"))
           list.add(InterestModel(false, icon = R.drawable.comment_icon, name = "Mustafa_12 commented on your post"))
           list.add(InterestModel(false, icon = R.drawable.prayer_icon,name="Sadik1091 sent a prayer"))
           list.add(InterestModel(false, icon = R.drawable.comment_icon, name = "Hamide1234 commented on your post"))
           list.add(InterestModel(false, icon = R.drawable.comment_icon, name = "Sadik1091 commented on your post"))
           list.add(InterestModel(false, icon = R.drawable.prayer_icon, name = "Sasdik1091 sent a prayer"))
           list.add(InterestModel(false, icon = R.drawable.prayer_icon, name = "Kalsoom_123 sent a prayer"))
           list.add(InterestModel(false, icon = R.drawable.comment_icon, name = "Hamid_Mir commented on your post"))*/
        notificationAdapter = NotificationAdapter(this, notificationList)
        binding.rvNotification.adapter = notificationAdapter

        notificationAdapter?.acceptRejectListener = { pos, model, status ->
            val map = HashMap<String, String>()
            if (status == 1){
                map["follow_by"] =  model.notification_sender?.id.toString()
                map["follow_to"] = getPreference("id", "")
            }else{
                map["follow_by"] = getPreference("id", "")
                map["follow_to"] = model.notification_sender?.id.toString()
            }
            map["status"] = status.toString()
            map["notification_id"] = model.id.toString()
            viewModel.followUnfollow(map, this).observe(this) { value ->
                when (value.status) {
                    Status.SUCCESS -> {
                       LoaderDialog.dismiss()
                        with(binding) {
                           /* when (status) {
                                1 -> {
                                    showCustomSnackbar(
                                        this@NotificationActivity,
                                        binding.root,
                                        "Request Accepted Successfully."
                                    )
                                }

                                else-> {
                                    showCustomSnackbar(
                                        this@NotificationActivity,
                                        binding.root,
                                        "Request Rejected."
                                    )
                                }
                            }*/
                            notificationList.removeAt(pos)
                            notificationAdapter?.notifyItemRemoved(pos)

                            if (notificationList.isEmpty()){
                                binding.tvNoDataFound.visible()
                            }else{
                                binding.tvNoDataFound.gone()
                            }

                        }

                    }

                    Status.LOADING -> {
                        LoaderDialog.show(this)
                    }

                    Status.ERROR -> {
                       LoaderDialog.dismiss()

                        showCustomSnackbar(
                            this,
                            binding.root,
                            value.message.toString()
                        )
                    }
                }
            }
        }
    }


    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is NotificationListingResponse -> {
                        val res = value.data.body
                        notificationList.clear()
                        notificationList.addAll(res?.data ?: ArrayList())
                        notificationAdapter?.notifyDataSetChanged()

                        if (notificationList.isEmpty()){
                            binding.tvNoDataFound.visible()
                        }else{
                            binding.tvNoDataFound.gone()
                        }
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

    private fun followUnfollowApi(status: String, otherUserId: String) {


    }

}