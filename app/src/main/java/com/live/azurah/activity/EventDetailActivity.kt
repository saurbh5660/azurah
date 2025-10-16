package com.live.azurah.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.live.azurah.R
import com.live.azurah.databinding.ActivityEventDetailBinding
import com.live.azurah.model.AddBookmarkResponse
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.EventDetailResponse
import com.live.azurah.model.EventListResponse
import com.live.azurah.model.EventResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.formatDateRange
import com.live.azurah.util.formatSpecifiedDate
import com.live.azurah.util.formatStartEndRange
import com.live.azurah.util.formatStartEndTimeRange
import com.live.azurah.util.gone
import com.live.azurah.util.loadImage
import com.live.azurah.util.openUrlInBrowser
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EventDetailActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivityEventDetailBinding
    private var id = ""
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var res: EventDetailResponse.Body? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
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
        id = intent.getStringExtra("id") ?: ""
        initListener()
        viewEvent()
    }

    private fun viewEvent() {
        viewModel.viewEvent(id, this).observe(this, this)
    }

    private fun initListener() {
        with(binding){
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            btnBuyTicket.setOnClickListener {
                openUrlInBrowser(this@EventDetailActivity,res?.websiteUrl.toString())
               /* val map = HashMap<String, String>()
                map["event_id"] = id
                map["status"] = "1"
                viewModel.eventBuyTicket(map, this@EventDetailActivity).observe(this@EventDetailActivity) { value ->
                    when (value.status) {
                        Status.SUCCESS -> {
                           LoaderDialog.dismiss()
                            when (value.data) {
                                is CommonResponse -> {
                                    showCustomSnackbar(this@EventDetailActivity,binding.root,value.data.message.toString())
                                    lifecycleScope.launch {
                                        delay(1000)
                                        onBackPressedDispatcher.onBackPressed()
                                    }
                                }
                            }
                        }

                        Status.LOADING -> {
                            LoaderDialog.show(this)
                        }

                        Status.ERROR -> {
                           LoaderDialog.dismiss()
                            showCustomSnackbar(this@EventDetailActivity, binding.root, value.message.toString())
                        }
                    }
                }*/
            }
            btnBookmarkEvent.setOnClickListener {
                val map = HashMap<String, String>()
                map["event_id"] =id
                if (res?.isBookmark == 1) {
                    map["status"] = "0"
                } else {
                    map["status"] = "1"
                }
                viewModel.eventBookmark(map, this@EventDetailActivity).observe(this@EventDetailActivity) { value ->
                    when (value.status) {
                        Status.SUCCESS -> {
                           LoaderDialog.dismiss()
                            when (value.data) {
                                is AddBookmarkResponse -> {
                                    val res = value.data.body
                                    if (res?.isBooking == 1){
                                        btnBookmarkEvent.setImageResource(R.drawable.selected_bookmark_icon)
                                    }else{
                                        btnBookmarkEvent.setImageResource(R.drawable.bookmark_icon)
                                    }
                                }
                            }
                        }

                        Status.LOADING -> {
                            LoaderDialog.show(this@EventDetailActivity)
                        }

                        Status.ERROR -> {
                           LoaderDialog.dismiss()
                            showCustomSnackbar(this@EventDetailActivity, binding.root, value.message.toString())
                        }
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
                    is EventDetailResponse -> {
                         res = value.data.body
                        binding.nsViewDetail.visible()
                        res?.let {
                            with(binding){
                                ivComm.loadImage(ApiConstants.IMAGE_BASE_URL+res?.image)
                                tvCommunityForum.text = res?.title
                                tvLoc.text = res?.location
                                tvDate.text = formatStartEndRange(res?.startDate+","+res?.startTime+" 44 "+res?.endDate+","+res?.endTime)
                                tvClock.text = formatStartEndTimeRange(res?.startDate+","+res?.startTime+" 44 "+res?.endDate+","+res?.endTime)
                                Log.d("dvdsggdfg",res?.startDate+","+res?.startTime+"-"+res?.endDate+","+res?.endTime)

                                tvName.text = res?.description
                                btnViewGroup.text = "£"+res?.price
                                if (res?.organizer?.lastName.toString().trim().uppercase() == "ADMIN"){
                                    tvOrganizerName.text = res?.organizer?.firstName ?: ""
                                }else{
                                    tvOrganizerName.text = buildString {
                                        append(res?.organizer?.firstName ?: "")
                                        append(" ")
                                        append(res?.organizer?.lastName ?: "")
                                    }
                                }
                                Log.d("fdsfdsg",ApiConstants.IMAGE_BASE_URL+res?.organizer?.image)
                                ivUserImage.loadImage(ApiConstants.IMAGE_BASE_URL+res?.organizer?.image)
                                if (res?.isBookmark == 1){
                                    btnBookmarkEvent.setImageResource(R.drawable.selected_bookmark_icon)
                                }else{
                                    btnBookmarkEvent.setImageResource(R.drawable.bookmark_icon)
                                }
                            }
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
}