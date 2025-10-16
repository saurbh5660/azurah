package com.live.azurah.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.adapter.EventUpcomingAdapter
import com.live.azurah.databinding.ActivityBookmarkEventBinding
import com.live.azurah.model.EventListResponse
import com.live.azurah.model.EventResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

@AndroidEntryPoint
class BookmarkEventActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivityBookmarkEventBinding
    private var from = ""
    private var title = ""
    private var id = ""
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private val list = ArrayList<EventListResponse.Body.RecommendedEvents.Data>()
    private var eventAdapter: EventUpcomingAdapter? = null
    private var search = ""
    private var showDialog = false
    private var currentPage = 1
    private var totalPageCount = 0
    private var resetPage = false
    private var isApiRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarkEventBinding.inflate(layoutInflater)
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

        from = intent.getStringExtra("from") ?: ""
        title = intent.getStringExtra("title") ?: ""
        id = intent.getStringExtra("id") ?: "0"

        showDialog = true
        resetPage = true
        if(from != "3"){
            showDialog = false
            binding.shimmerLayout.visible()
            binding.shimmerLayout.startShimmer()
            getEvents()
        }else{
            binding.tvNoDataFound.visible()
        }
        when (from) {
            "0" -> {
                binding.tvBookmark.text = "Upcoming Events"
            }
            "1" -> {
                binding.tvBookmark.text = "Popular Events"
            }
            "2" -> {
                binding.tvBookmark.text = "Recommendations for You"
            }

            else -> {
                binding.tvBookmark.text = title
            }
        }


        initListener()
        setAdapter()
    }

    private fun getEvents() {
        var type = ""
        when (from) {
            "0" -> {
                type = "1"
            }

            "1" -> {
                type = "2"
            }

            "2" -> {
                type = "3"
            }
        }
        isApiRunning = true
        if (resetPage){
            currentPage = 1
        }
        val map = HashMap<String, String>()
        if (type.isNotEmpty()){
            map["type"] = type
        }
        map["page"] = currentPage.toString()
        map["limit"] = "20"
        map["category_id"] = id
        map["search_string"] = search

        viewModel.eventList2(map, this).observe(this, this)
    }

    private fun initListener() {
        with(binding) {

            rvCurrentEvents.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!recyclerView.canScrollVertically(1)) {
                        if (currentPage <= totalPageCount && !isApiRunning) {
                            resetPage = false
                            showDialog = false
                            getEvents()
                        }
                    }
                }
            })

            swipeRefreshLayout.setOnRefreshListener {
                resetPage = true
                showDialog = false
                getEvents()
                swipeRefreshLayout.isRefreshing = true
            }


            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            ivCross.setOnClickListener {
                etSearch.setText("")
            }
            etSearch.addTextChangedListener(object : TextWatcher {
                var delay : Long = 1000
                var timer = Timer()
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    timer.cancel()
                    timer.purge()
                    if (s.toString().isNotBlank()) {
                        ivCross.visibility = View.VISIBLE
                    } else {
                        ivCross.visibility = View.GONE
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    timer = Timer()
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            lifecycleScope.launch(Dispatchers.Main){
                                search = s.toString().trim()
                                resetPage = true
                                showDialog = false
                                binding.shimmerLayout.visible()
                                binding.tvNoDataFound.gone()
                                binding.shimmerLayout.startShimmer()
                                getEvents()
                            }
                        }
                    }, delay)
                }

            })
        }
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
                isApiRunning = false
               LoaderDialog.dismiss()
                binding.swipeRefreshLayout.isRefreshing = false
                when (value.data) {
                    is EventResponse -> {
                        binding.shimmerLayout.gone()
                        binding.shimmerLayout.stopShimmer()
                        if (resetPage) {
                            list.clear()
                        }
                        value.data.body?.getData?.let {
                            val allList = it.data?.map {
                                EventListResponse.Body.RecommendedEvents.Data(
                                    image = it?.image ?: "",
                                    title = it?.title ?:"",
                                    id = it?.id,
                                    price = it?.price.toString(),
                                    startDate = it?.startDate.toString(),
                                    startTime = it?.startTime.toString(),
                                    endTime = it?.endTime.toString(),
                                    endDate = it?.endDate.toString(),
                                    location = it?.location ?: ""
                                )
                            }
                            if (allList != null) {
                                list.addAll(allList)
                            }
                        }
                        if (list.isNotEmpty()){
                            binding.tvNoDataFound.gone()
                            currentPage = (value.data.body?.getData?.currentPage ?: 0) + 1
                        }else{
                            binding.tvNoDataFound.visible()
                        }
                        Log.d("fsfsdfd",list.size.toString())
                        eventAdapter?.notifyDataSetChanged()
                        totalPageCount = value.data.body?.getData?.totalPages ?: 0

                    }
                }
            }
            Status.LOADING -> {
                if (showDialog){
                    LoaderDialog.show(this)
                }
            }
            Status.ERROR -> {
                isApiRunning = false
               LoaderDialog.dismiss()
                binding.swipeRefreshLayout.isRefreshing = false
                showCustomSnackbar(this, binding.root, value.message.toString())
                binding.shimmerLayout.gone()
                binding.shimmerLayout.stopShimmer()
            }
        }
    }

    private fun setAdapter() {
        eventAdapter = EventUpcomingAdapter(this, list)
        binding.rvCurrentEvents.adapter = eventAdapter
    }

}
