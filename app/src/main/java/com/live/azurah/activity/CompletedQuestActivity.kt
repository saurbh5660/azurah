package com.live.azurah.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.live.azurah.R
import com.live.azurah.adapter.ChallangeItemAdapter
import com.live.azurah.databinding.ActivityCompletedQuestBinding
import com.live.azurah.model.BibleQuestListResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Status
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import com.live.azurah.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

@AndroidEntryPoint
class CompletedQuestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCompletedQuestBinding
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var currentPage = 1
    private var totalPageCount = 0
    private var resetPage = false
    private var isApiRunning = false
    private var showDialog = true
    private var challangeItemAdapter: ChallangeItemAdapter? = null
    private var bibleQuestList = ArrayList<BibleQuestListResponse.Body.Data>()
    private var search = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompletedQuestBinding.inflate(layoutInflater)
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

        initListener()
        setAdapter()
    }

    private fun initListener() {
        with(binding){
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            ivCross.setOnClickListener {
                etSearch.setText("")
            }

            etSearch.addTextChangedListener(object: TextWatcher {
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
                    if (s.toString().isNotBlank()){
                        ivCross.visibility = View.VISIBLE
                    }else{
                        ivCross.visibility = View.GONE
                    }
                }
                override fun afterTextChanged(s: Editable?) {
                    timer = Timer()
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            lifecycleScope.launch(Dispatchers.Main){
                                search = s.toString().trim()
                                showDialog = true
                                getBibleList()
                            }
                        }
                    }, delay)
                }
            })
        }
    }


    private fun setAdapter() {
        challangeItemAdapter = ChallangeItemAdapter(this,bibleQuestList)
        binding.rvChallange.adapter = challangeItemAdapter
    }

    private fun getBibleList() {
        isApiRunning = true
        if (resetPage) {
            currentPage = 1
        }
        val map = HashMap<String, String>()
        map["page"] = currentPage.toString()
        map["limit"] = "10"
        map["category_id"] = "0"
//        map["is_premium"] = "0"
        map["search_string"] = search
        map["is_completed"] = "1"

        viewModel.getBibleQuestList(map, this).observe(this) { value ->
            when (value.status) {
                Status.SUCCESS -> {
                    isApiRunning = false
                   LoaderDialog.dismiss()
                    when (value.data) {
                        is BibleQuestListResponse -> {
                            binding.shimmerLayout.stopShimmer()
                            binding.rvChallange.visible()
                            binding.shimmerLayout.gone()
                            val res = value.data.body
                            if (resetPage) {
                                bibleQuestList.clear()
                            }
                            bibleQuestList.addAll(res?.data ?: ArrayList())
                            challangeItemAdapter?.notifyDataSetChanged()

                            if (bibleQuestList.isEmpty()) {
                                binding.tvNoDataFound.visible()
                            } else {
                                binding.tvNoDataFound.gone()
                                currentPage = (res?.currentPage ?: 0) + 1
                            }
                            totalPageCount = res?.totalPages ?: 0

                                binding.progressBar.max = res?.total_challenges ?: 0
                                binding.progressBar.progress = res?.total_completed_challenges ?: 0
                                binding.tvCha.text = buildString {
                                    append(res?.total_completed_challenges ?: 0)
                                    append("/")
                                    append(res?.total_challenges ?: 0)
                                }
                        }
                    }
                }

                Status.LOADING -> {
                    if (showDialog) {
                        LoaderDialog.show(this)
                    }
                }

                Status.ERROR -> {
                    isApiRunning = false
                   LoaderDialog.dismiss()
                    showCustomSnackbar(this, binding.root, value.message.toString())
                    binding.shimmerLayout.stopShimmer()
                    binding.rvChallange.gone()
                    binding.shimmerLayout.gone()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showDialog = false
        resetPage = true
        binding.shimmerLayout.visible()
        binding.rvChallange.gone()
        binding.tvNoDataFound.gone()
        getBibleList()
        binding.shimmerLayout.startShimmer()
    }


    override fun onDestroy() {
        super.onDestroy()
        binding.shimmerLayout.startShimmer()
    }
}