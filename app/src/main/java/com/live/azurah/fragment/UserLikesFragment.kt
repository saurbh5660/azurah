package com.live.azurah.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.adapter.SuggestionCompleteAdapter
import com.live.azurah.databinding.FragmentUserLikesBinding
import com.live.azurah.model.PostLikesResposne
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserLikesFragment : Fragment(), Observer<Resource<Any>> {
    private lateinit var binding: FragmentUserLikesBinding
    private val likesList = ArrayList<PostLikesResposne.Body.Data>()
//    private val loaderDialog by lazy { LoaderDialog(requireActivity()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var postId = ""
    private var from = ""
    private var likesAdapter : SuggestionCompleteAdapter? = null
    private var showDialog = false
    private var currentPage = 1
    private var totalPageCount = 0
    private var resetPage = false
    private var isApiRunning = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserLikesBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postId = arguments?.getString("postId") ?: ""
        from = arguments?.getString("from") ?: ""
        initListener()
        setAdapter()
        showDialog = true
        resetPage = true
        getLikes()
    }

    private fun setAdapter(){
        likesAdapter = SuggestionCompleteAdapter(requireContext(),1,likesList)
        binding.rvSuggestions.adapter = likesAdapter
    }

    private fun getLikes(){
        isApiRunning = true
        if (resetPage){
            currentPage = 1
        }
        val map = HashMap<String, String>()
        map["page"] = currentPage.toString()
        map["limit"] = "20"
        if (from == "community"){
            map["community_forum_id"] = postId
            viewModel.getCommunityLikeList(map,requireActivity()).observe(viewLifecycleOwner,this)

        }else if(from == "prayer"){
            map["prayer_id"] = postId
            viewModel.getPrayerLikeList(map,requireActivity()).observe(viewLifecycleOwner,this)

        }else if(from == "prayer_praise"){
            map["prayer_id"] = postId
            viewModel.getPraiseLikeList(map,requireActivity()).observe(viewLifecycleOwner,this)

        }else if(from == "testimony_praise"){
            map["testimony_id"] = postId
            viewModel.getTestimonyPraiseList(map,requireActivity()).observe(viewLifecycleOwner,this)

        }else if(from == "testimony"){
            map["testimony_id"] = postId
            viewModel.getTestimonyLikeList(map,requireActivity()).observe(viewLifecycleOwner,this)

        }else{
            map["post_id"] = postId
            viewModel.getPostLikes(map,requireActivity()).observe(viewLifecycleOwner,this)
        }
    }


    private fun initListener() {
        with(binding){

            rvSuggestions.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (!recyclerView.canScrollVertically(1)) {
                        if (currentPage <= totalPageCount && !isApiRunning) {
                            resetPage = false
                            showDialog = false
                            getLikes()
                        }
                    }
                }
            })

            swipeRefreshLayout.setOnRefreshListener {
                resetPage = true
                showDialog = false
                getLikes()
                swipeRefreshLayout.isRefreshing = true
            }

            backIcon.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
                isApiRunning = false
               LoaderDialog.dismiss()
                binding.swipeRefreshLayout.isRefreshing = false
                when (value.data) {
                    is PostLikesResposne -> {
                        with(binding){
                            val res = value.data.body
                            if (resetPage) {
                                likesList.clear()
                            }
                            likesList.addAll(res?.data ?: ArrayList())
                            likesAdapter?.notifyDataSetChanged()

                            if (likesList.isEmpty()){
                                binding.tvNoDataFound.visible()
                            }else{
                                binding.tvNoDataFound.gone()
                                currentPage = (value.data.body?.current_page?:0) + 1
                            }
                            totalPageCount = value.data.body?.total_pages ?: 0
                        }
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
            }
        }
    }

}