package com.live.azurah.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.adapter.SuggestionCompleteAdapter
import com.live.azurah.databinding.FragmentSuggetionForYouBinding
import com.live.azurah.model.CommunityForumResponse
import com.live.azurah.model.PostCommentListResposne
import com.live.azurah.model.PostLikesResposne
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Status
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SuggetionForYouFragment : Fragment() {
    private lateinit var binding: FragmentSuggetionForYouBinding
    private val suggestionList = ArrayList<PostLikesResposne.Body.Data>()
//    private val loaderDialog by lazy { LoaderDialog(requireActivity()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var suggestionAdapter : SuggestionCompleteAdapter? = null
    private var showDialog = false
    private var currentPage = 1
    private var totalPageCount = 0
    private var resetPage = false
    private var isApiRunning = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSuggetionForYouBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter()
        initListener()
        showDialog = false
        binding.shimmerLayout.visible()
        binding.shimmerLayout.startShimmer()
        resetPage = true
        getSuggestionList()
    }

    private fun setAdapter() {
        suggestionAdapter = SuggestionCompleteAdapter(requireContext(), 0, suggestionList)
        binding.rvSuggestions.adapter = suggestionAdapter

        suggestionAdapter?.removeSuggestionListener = {
            if (suggestionList.isEmpty()) {
                binding.tvNoDataFound.visible()
            } else {
                binding.tvNoDataFound.gone()
            }
        }

        suggestionAdapter?.followUnfollowListener = { _, model, _ ->
            val map = HashMap<String, String>()
            map["follow_by"] = getPreference("id", "")
            map["follow_to"] = model.id.toString()
            map["status"] = model.isFollowByMe.toString()
            viewModel.followUnfollow(map, requireActivity()).observe(viewLifecycleOwner) { value ->
                when (value.status) {
                    Status.SUCCESS -> {
                    }

                    Status.LOADING -> {
                    }

                    Status.ERROR -> {
                        showCustomSnackbar(
                            requireActivity(),
                            binding.root,
                            value.message.toString()
                        )
                    }
                }
            }
        }
    }

    private fun getSuggestionList() {
        isApiRunning = true
        if (resetPage){
            currentPage = 1
        }
        val map = HashMap<String, String>()
        map["page"] = currentPage.toString()
        map["limit"] = "20"
        viewModel.suggestionList(map, requireActivity()).observe(viewLifecycleOwner) { value ->
            when (value.status) {
                Status.SUCCESS -> {
                    isApiRunning = false
                   LoaderDialog.dismiss()
                    binding.swipeRefreshLayout.isRefreshing = false
                    when (value.data) {
                        is PostLikesResposne -> {
                            binding.shimmerLayout.gone()
                            binding.clSongWeek.visible()
                            binding.shimmerLayout.stopShimmer()
                            val res = value.data.body
                            if (resetPage) {
                                suggestionList.clear()
                            }
                            suggestionList.addAll(res?.data ?: ArrayList())
                            suggestionAdapter?.notifyDataSetChanged()

                             if (suggestionList.isEmpty()) {
                                 binding.tvNoDataFound.visible()
                             } else {
                                 binding.tvNoDataFound.gone()
                                 currentPage = (value.data.body?.current_page?:0) + 1
                             }
                            totalPageCount = value.data.body?.total_pages ?: 0

                        }
                    }
                }

                Status.LOADING -> {
                    if (showDialog){
                        LoaderDialog.show(requireActivity())
                    }
                }

                Status.ERROR -> {
                    isApiRunning = false
                   LoaderDialog.dismiss()
                    binding.swipeRefreshLayout.isRefreshing = false
                    showCustomSnackbar(requireActivity(), binding.root, value.message.toString())
                    binding.shimmerLayout.gone()
                    binding.clSongWeek.gone()
                    binding.shimmerLayout.stopShimmer()
                }
            }
        }

    }

    private fun initListener() {
        with(binding) {
            rvSuggestions.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!recyclerView.canScrollVertically(1)) {
                        if (currentPage <= totalPageCount && !isApiRunning) {
                            resetPage = false
                            showDialog = false
                            getSuggestionList()
                        }
                    }
                }
            })

            swipeRefreshLayout.setOnRefreshListener {
                resetPage = true
                showDialog = false
                getSuggestionList()
                swipeRefreshLayout.isRefreshing = true
            }

            backIcon.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }


}