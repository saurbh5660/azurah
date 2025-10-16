package com.live.azurah.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayoutMediator
import com.live.azurah.R
import com.live.azurah.adapter.HomeSearchViewPagerAdapter
import com.live.azurah.adapter.RecentSearchAdapter
import com.live.azurah.databinding.FragmentSearchHomeBinding
import com.live.azurah.model.BlockResposne
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.HomeSearchResposne
import com.live.azurah.model.InterestModel
import com.live.azurah.model.PostResponse
import com.live.azurah.model.RecentSearchResposne
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
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
class SearchHomeFragment : Fragment(), Observer<Resource<Any>> {
    private val tabList = arrayOf("Posts" to R.drawable.tag_user, "Users" to R.drawable.post_icon)

//    private val loaderDialog by lazy { LoaderDialog(requireContext()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var search = ""
    private var showDialog = false
    private var currentPage = 1
    private var totalPageCount = 0
    private var userCurrentPage = 1
    private var userTotalPageCount = 0
    private var resetPage = false
    private var isApiRunning = false
    private var list = ArrayList<PostResponse.Body.Data>()
    private var blockList = ArrayList<BlockResposne.Body.Data>()
    private var recentSearch = ArrayList<RecentSearchResposne.Body.Data>()
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var binding: FragmentSearchHomeBinding
    private lateinit var homeSearchViewPagerAdapter: HomeSearchViewPagerAdapter
    private lateinit var recentSearchAdapter : RecentSearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        initListener()

        setAdapter()
        setViewPagerAdapter()
        TabLayoutMediator(binding.tbLayout, binding.viewPager) { tab, position ->
            tab.text = tabList[position].first
        }.attach()

        getRecentSearch()

    }

    private fun setViewPagerAdapter() {
        homeSearchViewPagerAdapter = HomeSearchViewPagerAdapter(requireActivity(), 2)
        binding.viewPager.adapter = homeSearchViewPagerAdapter
    }

    private fun initListener() {
        with(binding) {

            sharedViewModel.recyclerScroll.observe(viewLifecycleOwner) {
                if (binding.viewPager.currentItem == 0) {
                    if (currentPage <= totalPageCount && !isApiRunning) {
                        resetPage = false
                        showDialog = false
                        getPostUsers()
                    }
                } else {
                    if (userCurrentPage <= userTotalPageCount && !isApiRunning) {
                        resetPage = false
                        showDialog = false
                        getPostUsers()
                    }
                }


            }

            backIcon.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }

            ivCross.setOnClickListener {
                etSearch.setText("")
            }

            tvViewAll.setOnClickListener {
                deleteSearch("")
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
                                if (s.toString().isNotBlank()){
                                    binding.clRecentSearches.gone()
                                    search = s.toString().trim()
                                    showDialog = true
                                    resetPage = true
                                    getPostUsers()
                                }
                            }
                        }
                    }, delay)
                }

            })
        }
    }

    private fun getRecentSearch(){
        val map = HashMap<String, String>()
        map["page"] = "1"
        map["limit"] = "10"
        viewModel.getRecentSearch(map,requireActivity()).observe(viewLifecycleOwner){value->
            when (value.status) {
                Status.SUCCESS -> {
                    isApiRunning = false
                   LoaderDialog.dismiss()
                    when (value.data) {
                        is RecentSearchResposne -> {
                            with(binding) {
                                val res = value.data.body
                                recentSearch.clear()
                                recentSearch.addAll(res?.data ?: ArrayList())
                                recentSearchAdapter.notifyDataSetChanged()
                                if (recentSearch.isNotEmpty()){
                                    binding.clRecentSearches.visible()
                                    binding.clTab.gone()
                                }
                            }
                        }
                    }
                }

                Status.LOADING -> {
                   LoaderDialog.dismiss()
                }

                Status.ERROR -> {
                    isApiRunning = false
                   LoaderDialog.dismiss()
                    showCustomSnackbar(requireActivity(), binding.root, value.message.toString())
                }
            }
        }
    }

    private fun getPostUsers() {
        isApiRunning = true
        if (resetPage) {
            currentPage = 1
            userCurrentPage = 1
        }
        val map = HashMap<String, String>()
        if (binding.viewPager.currentItem == 0) {
            map["page"] = currentPage.toString()
        } else {
            map["page"] = userCurrentPage.toString()
        }
        map["limit"] = "25"
        map["search_string"] = search
        viewModel.getHomeSearch(map, requireActivity()).observe(viewLifecycleOwner, this)
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
                isApiRunning = false
               LoaderDialog.dismiss()
                when (value.data) {
                    is HomeSearchResposne -> {
                        with(binding) {
                            binding.clTab.visible()
                            val res = value.data.body?.posts
                            val users = value.data.body?.users
                            val newMappedList = res?.data?.map {
                                PostResponse.Body.Data(
                                    id = it.id,
                                    comment_count = it.comment_count,
                                    created = it.created,
                                    created_at = it.created_at,
                                    description = it.description,
                                    is_bookmark = it.is_bookmark,
                                    is_like = it.is_like,
                                    like_count = it.like_count,
                                    post_images = it.post_images?.map {
                                        PostResponse.Body.Data.PostImage(
                                            id = it?.id,
                                            image = it?.image,
                                            image_thumb = it?.image_thumb,
                                            post_id = it?.post_id,
                                            type = it?.type
                                        )
                                    },
                                    user_id = it.user_id,
                                    user = PostResponse.Body.Data.User(
                                        first_name = it.user?.first_name,
                                        last_name = it.user?.last_name,
                                        username = it.user?.username,
                                        id = it.user?.id,
                                        image = it.user?.image,
                                        image_thumb = it.user?.image_thumb,
                                    )
                                )
                            }
                            if (resetPage) {
                                list.clear()
                                blockList.clear()
                            }

                            val newUserMappedList = users?.data?.map {
                                BlockResposne.Body.Data(
                                    blockToUser = BlockResposne.Body.Data.BlockToUser(
                                        id = it.id,
                                        firstName = it.first_name,
                                        lastName = it.last_name,
                                        username = it.username,
                                        image = it.image,
                                        isFollowByMe = it.isFollowByMe,
                                        isFollowByOther = it.isFollowByOther,
                                        profile_type = it.profile_type
                                    )
                                )
                            }

                            blockList.addAll(newUserMappedList ?: ArrayList())
                            list.addAll(newMappedList ?: ArrayList())
                            if (list.isNotEmpty()) {
                                currentPage = (res?.current_page ?: 0) + 1
                                totalPageCount = res?.total_pages ?: 0
                            }

                            if (blockList.isNotEmpty()) {
                                userCurrentPage = (users?.current_page ?: 0) + 1
                                userTotalPageCount = users?.total_pages ?: 0
                            }
                            sharedViewModel.setPostSearchData(list)
                            sharedViewModel.setUserSearchData(blockList)
                            sharedViewModel.setSearchPair(Triple(search, currentPage, totalPageCount))
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
            }
        }
    }

    private fun setAdapter() {
        recentSearchAdapter = RecentSearchAdapter(requireContext(), recentSearch)
        binding.rvRecentSearch.adapter = recentSearchAdapter

        recentSearchAdapter.searchListener = {pos: Int ->
            binding.etSearch.setText(recentSearch[pos].search_string)
        }

        recentSearchAdapter.deleteListener = {pos: Int ->
            deleteSearch(recentSearch[pos].id.toString(),pos)
        }

    }

    private fun deleteSearch(id:String,pos :Int = -1){
        val  map = HashMap<String,String>()
        if (id.isNotEmpty()){
            map["type"] = "1"
            map["id"] = id
        }else{
            map["type"] = "2"
            map["id"] = "0"
        }

        viewModel.deleteRecentSearch(map,requireActivity()).observe(viewLifecycleOwner){value->
            when (value.status) {
                Status.SUCCESS -> {
                    isApiRunning = false
                   LoaderDialog.dismiss()
                    when (value.data) {
                        is CommonResponse -> {
                           if (pos != -1){
                               recentSearch.removeAt(pos)
                               recentSearchAdapter.notifyItemRemoved(pos)

                           }else{
                               recentSearch.clear()
                               recentSearchAdapter.notifyDataSetChanged()
                           }

                            if (recentSearch.isEmpty()){
                                binding.clRecentSearches.gone()
                            }
                        }
                    }
                }

                Status.LOADING -> {
                    LoaderDialog.show(requireActivity())
                }

                Status.ERROR -> {
                    isApiRunning = false
                   LoaderDialog.dismiss()
                    showCustomSnackbar(requireActivity(), binding.root, value.message.toString())
                }
            }
        }
    }

}