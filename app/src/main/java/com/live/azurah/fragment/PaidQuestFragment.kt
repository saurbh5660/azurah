package com.live.azurah.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.activity.ChallangeActivity
import com.live.azurah.adapter.ChallangeItemAdapter
import com.live.azurah.databinding.FragmentPaidQuestBinding
import com.live.azurah.model.BibleQuestListResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Status
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import com.live.azurah.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PaidQuestFragment : Fragment() {
  private lateinit var binding: FragmentPaidQuestBinding
//    private val loaderDialog by lazy { LoaderDialog(requireActivity()) }
    private val viewModel by viewModels<CommonViewModel>()
    private lateinit var sharedViewModel: SharedViewModel
    private var categoryId = "0"
    private var currentPage = 1
    private var totalPageCount = 0
    private var resetPage = false
    private var isApiRunning = false
    private var showDialog = true
    private var challangeItemAdapter: ChallangeItemAdapter? = null
    private var bibleQuestList = ArrayList<BibleQuestListResponse.Body.Data>()
    private var search = ""
    private var isResume = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPaidQuestBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        sharedViewModel.getCategoryId.observe(viewLifecycleOwner) {
            if (it.first.isNotEmpty()) {
                categoryId = it.first
                if (isResume){
                    showDialog = true
                    resetPage = true
                    getBibleList()
                }

            }
        }

        sharedViewModel.search.observe(viewLifecycleOwner){
            search = it
            if (isResume){
                showDialog = true
                resetPage = true
                getBibleList()

            }
        }

        binding.rvChallange.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollVertically(1)) {
                    if (currentPage <= totalPageCount && !isApiRunning) {
                        resetPage = false
                        showDialog = false
                        getBibleList()
                    }
                }
            }
        })
    }

    private fun setAdapter() {
        challangeItemAdapter = ChallangeItemAdapter(requireContext(),bibleQuestList)
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
        map["category_id"] = categoryId
        map["is_premium"] = "1"
        viewModel.getBibleQuestList(map, requireActivity()).observe(viewLifecycleOwner) { value ->
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

                            with(requireActivity() as ChallangeActivity){
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
                }

                Status.LOADING -> {
                    if (showDialog) {
                        LoaderDialog.show(requireActivity())
                    }
                }

                Status.ERROR -> {
                    isApiRunning = false
                   LoaderDialog.dismiss()
                    showCustomSnackbar(requireActivity(), binding.root, value.message.toString())
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
        isResume = true
        binding.shimmerLayout.startShimmer()
        binding.shimmerLayout.visible()
        binding.rvChallange.gone()
        binding.tvNoDataFound.gone()
        val data = sharedViewModel.getCategoryId.value
        categoryId = data?.first ?: ""
        if (categoryId.isEmpty()){
            categoryId = "0"
        }
        getBibleList()
    }

    override fun onPause() {
        super.onPause()
        isResume = false
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.shimmerLayout.startShimmer()

    }
}