package com.live.azurah.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.activity.MyPostsActivity
import com.live.azurah.adapter.MyPostAdapter
import com.live.azurah.databinding.FragmentPostBinding
import com.live.azurah.model.PostResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import com.live.azurah.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostFragment(var id: String = "") : Fragment(), Observer<Resource<Any>> {
    private lateinit var binding: FragmentPostBinding
    private var postAdapter: MyPostAdapter? = null
    private var list = ArrayList<PostResponse.Body.Data>()

    //    private val loaderDialog by lazy { LoaderDialog(requireActivity()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var currentPage = 1
    private var totalPageCount = 0
    private var resetPage = true
    private var isApiRunning = false
    private var profileType = -1
    private var isFollowByMe = 0
    private lateinit var sharedViewModel: SharedViewModel
    private var res: PostResponse.Body? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        sharedViewModel.profile.observe(viewLifecycleOwner) {
            it?.let {
                setAccountPrivacy()
            }
        }

        setAdapter()

        binding.rvPosts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    if (currentPage <= totalPageCount && !isApiRunning) {
                        resetPage = false
                        getPosts()
                    }
                }
            }
        })
    }

    private fun setAdapter() {
        postAdapter = MyPostAdapter(requireContext(), list)
        binding.rvPosts.adapter = postAdapter

        postAdapter?.onClickListener = { pos, model ->
            startActivity(Intent(requireContext(), MyPostsActivity::class.java).apply {
                putExtra("type", "1")
                putExtra("list", list)
                putExtra("scrollPos", pos)
                putExtra("currentPage", currentPage)
                putExtra("totalPage", totalPageCount)
                putExtra("otherUserId", id)
            })
        }
    }

    private fun getPosts() {
        isApiRunning = true
        if (resetPage) {
            currentPage = 1
        }
        val map = HashMap<String, String>()
        map["page"] = currentPage.toString()
        map["limit"] = "10"
        if (id.isEmpty()) {
            map["user_id"] = getPreference("id", "")
//            binding.tvNoDataFound.text = "Share your first post and let others get to know you!"

        } else {
            map["user_id"] = id
//            binding.tvNoDataFound.text = "This user hasn’t posted anything yet."
        }
        viewModel.getPostList(map, requireActivity()).observe(viewLifecycleOwner, this)
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
                isApiRunning = false
                LoaderDialog.dismiss()
                when (value.data) {
                    is PostResponse -> {
                        with(binding) {
                            res = value.data.body
                            if (resetPage) {
                                list.clear()
                            }
                            list.addAll(res?.data ?: ArrayList())
                            postAdapter?.notifyDataSetChanged()

                            setAccountPrivacy()

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


    private fun setAccountPrivacy() {
        profileType = sharedViewModel.profile.value?.profileType ?: -1
        isFollowByMe = sharedViewModel.profile.value?.isFollowByMe ?: -1
        Log.d("ASFfdssdg", profileType.toString())
        Log.d("ASFfdssdg", isFollowByMe.toString())

        if (getPreference("id", "") == sharedViewModel.profile.value?.id.toString()) {
            if (list.isNotEmpty()) {
                currentPage = (res?.current_page ?: 0) + 1
                totalPageCount = res?.total_pages ?: 0
                binding.tvNoDataFound.gone()
                binding.rvPosts.visible()

            } else {
                binding.tvNoDataFound.visible()
                binding.tvNoDataFound.text = buildString {
                    append("You haven't posted anything yet.")
                }
            }
        } else {
            if (profileType != -1) {
                if (profileType == 1 && isFollowByMe != 1) {
                    binding.tvNoDataFound.visible()
                    binding.rvPosts.gone()
                    binding.tvNoDataFound.text = buildString {
                        append("This account is private.")
                    }
                } else {
                    if (list.isNotEmpty()) {
                        currentPage = (res?.current_page ?: 0) + 1
                        totalPageCount = res?.total_pages ?: 0
                        binding.tvNoDataFound.gone()
                        binding.rvPosts.visible()

                    } else {
                        binding.tvNoDataFound.visible()
                        if (id.isEmpty()) {
                            binding.tvNoDataFound.text = buildString {
                                append("You haven't posted anything yet.")
                            }
                        } else {
                            binding.tvNoDataFound.text = buildString {
                                append("This user hasn’t posted anything yet.")
                            }
                        }
                    }
                }
            } else {
                binding.tvNoDataFound.gone()
                binding.rvPosts.gone()
            }
        }


    }

    override fun onResume() {
        super.onResume()
        resetPage = true
        getPosts()
    }
}