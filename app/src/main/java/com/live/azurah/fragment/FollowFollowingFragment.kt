package com.live.azurah.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.adapter.FollowUnfollowAdapter
import com.live.azurah.adapter.FollowUnfollowListAdapter
import com.live.azurah.databinding.FragmentFollowFollowing2Binding
import com.live.azurah.model.FollowFollowingResponse
import com.live.azurah.model.InterestModel
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FollowFollowingFragment : Fragment(), FollowUnfollowAdapter.ClickListener,
    Observer<Resource<Any>> {
    private lateinit var binding: FragmentFollowFollowing2Binding
    private var list = ArrayList<InterestModel>()
    private var followUnfollowAdapter: FollowUnfollowListAdapter? = null
    private var userList = ArrayList<FollowFollowingResponse.Body.Data>()
    private var type = "0"
//    private val loaderDialog by lazy { LoaderDialog(requireActivity()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var currentPage = 1
    private var totalPageCount = 0
    private var resetPage = true
    private var showDialog = true
    private var isApiRunning = false
    private var selectedType = 2
    private var id = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFollowFollowing2Binding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        type = arguments?.getString("from", "0") ?: "0"
        id = arguments?.getString("id", "") ?: ""

        if (type == "0") {
            list.add(InterestModel(true, "Followers"))
            list.add(InterestModel(false, "Following"))
            selectedType = 2

        } else {
            list.add(InterestModel(false, "Followers"))
            list.add(InterestModel(true, "Following"))
            selectedType = 1
        }
        setAdapter(userList)
        initListener()
        resetPage = true
        showDialog = true
        getList()
    }

    private fun initListener() {
        with(binding) {

            rvFollow.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!recyclerView.canScrollVertically(1)) {
                        if (currentPage <= totalPageCount && !isApiRunning) {
                            resetPage = false
                            showDialog = false
                            getList()
                        }
                    }
                }
            })

            backIcon.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }
            ivCross.setOnClickListener {
                etSearch.setText("")
            }
            etSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().isNotBlank()) {
                        ivCross.visibility = View.VISIBLE
                    } else {
                        ivCross.visibility = View.GONE
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    if (s.toString().isNotBlank()) {
                        if (selectedType == 1) {
                            val newList = userList.filter {
                                it.follow_to_user?.username.toString().contains(s.toString())
                            } as ArrayList
                            setAdapter(newList)
                        } else {
                            val newList = userList.filter {
                                it.follow_by_user?.username.toString().contains(s.toString())
                            } as ArrayList
                            setAdapter(newList)
                        }
                    } else {
                        setAdapter(userList)
                    }

                }

            })
        }
    }

    fun setAdapter(newList: java.util.ArrayList<FollowFollowingResponse.Body.Data>) {
        val adapter = FollowUnfollowAdapter(requireContext(), list, this)
        binding.rvFollowType.adapter = adapter

        followUnfollowAdapter =
            FollowUnfollowListAdapter(requireContext(), newList, selectedType = selectedType)
        binding.rvFollow.adapter = followUnfollowAdapter

        if (newList.isNotEmpty()) {
            binding.tvNoDataFound.gone()
        } else {
            if (selectedType == 1) {
                binding.tvNoDataFound.text =
                    buildString {
                        append("Your followers will appear here. Keep sharing and engaging to connect with others!")
                    }
            } else {
                binding.tvNoDataFound.text =
                    buildString {
                        append("The people you follow will be listed here.")
                    }
            }
            binding.tvNoDataFound.visible()
        }

        followUnfollowAdapter?.followUnfollowListener = { _, model, view ->
            val map = HashMap<String, String>()
            map["follow_by"] = getPreference("id", "")
            if (getPreference("id", "") == model.follow_by_user?.id.toString()) {
                map["follow_to"] = model.follow_to_user?.id.toString()
            } else {
                map["follow_to"] = model.follow_by_user?.id.toString()
            }
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

    private fun getList() {
        isApiRunning = true
        if (resetPage) {
            currentPage = 1
        }
        val map = HashMap<String, String>()
        map["page"] = currentPage.toString()
        map["limit"] = "10"
        map["type"] = selectedType.toString()
        if (id.isEmpty()) {
            map["user_id"] = getPreference("id", "")
        } else {
            map["user_id"] = id
        }
        viewModel.userFollowFollowingList(map, requireActivity()).observe(viewLifecycleOwner, this)
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
                isApiRunning = false
               LoaderDialog.dismiss()
                when (value.data) {
                    is FollowFollowingResponse -> {
                        with(binding) {
                            val res = value.data.body
                            if (resetPage) {
                                userList.clear()
                            }
                            userList.addAll(res?.data ?: ArrayList())
                            followUnfollowAdapter?.notifyDataSetChanged()
                            if (userList.isNotEmpty()) {
                                currentPage = (res?.current_page ?: 0) + 1
                                totalPageCount = res?.total_pages ?: 0
                                binding.tvNoDataFound.gone()
                            } else {
                                if (selectedType == 1) {
                                    tvNoDataFound.text =
                                        "Your followers will appear here. Keep sharing and engaging to connect with others!"
                                } else {
                                    tvNoDataFound.text =
                                        "The people you follow will be listed here."
                                }
                                binding.tvNoDataFound.visible()
                            }
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


    override fun onClick(position: Int) {
        selectedType = if (position == 0) 2 else 1
        resetPage = true
        showDialog = true
        followUnfollowAdapter?.updateSelectedType(selectedType)
        getList()
    }

}