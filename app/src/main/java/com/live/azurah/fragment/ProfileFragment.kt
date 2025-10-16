package com.live.azurah.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import com.live.azurah.R
import com.live.azurah.activity.AddPostActivity
import com.live.azurah.activity.EditProfileActivity
import com.live.azurah.activity.HomeActivity
import com.live.azurah.activity.LoginActivity
import com.live.azurah.adapter.ViewPagerAdapter
import com.live.azurah.databinding.FragmentProfileBinding
import com.live.azurah.model.ProfileResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.clearPreferences
import com.live.azurah.util.gone
import com.live.azurah.util.loadImage
import com.live.azurah.util.removeExtraSpaces
import com.live.azurah.util.savePreference
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.showCustomToast
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import com.live.azurah.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment(), Observer<Resource<Any>> {
    private lateinit var binding: FragmentProfileBinding
    private val list = arrayOf("About Me" to R.drawable.tag_user,"Posts" to R.drawable.post_icon)
    private lateinit var fragmentList: MutableList<Fragment>
    private lateinit var viewpagerAdapter: ViewPagerAdapter
    private var type = false
//    private val loaderDialog by lazy { LoaderDialog(requireContext()) }
    private val viewModel by viewModels<CommonViewModel>()
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewPager.isUserInputEnabled = false
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        binding.shimmerLayout.visible()
        binding.coodinatorLayout.gone()
        binding.shimmerLayout.startShimmer()
        initListener()
        initData()
        initFragment()

        TabLayoutMediator(binding.tbLayout, binding.viewPager) { tab, position ->
            tab.text = list[position].first
        }.attach()

    }

    private fun getProfile(){
        viewModel.getProfile(requireActivity()).observe(viewLifecycleOwner,this)
    }

    private fun initListener() {
        with(binding){
            btnEdit.setOnClickListener {
                startActivity(Intent(requireContext(),EditProfileActivity::class.java))
            }

            btnAdd.setOnClickListener {
                startActivity(Intent(requireContext(),AddPostActivity::class.java))
            }

            clPost.setOnClickListener {
               binding.viewPager.currentItem = 1
            }
            clChristianName.setOnClickListener {
                if (type){
                    type = false
                    tvType.visibility = View.GONE
                }else{
                    type = true
                    tvType.visibility = View.VISIBLE
                }
            }

            clFollowers.setOnClickListener {

                val fragment= FollowFollowingFragment().apply {
                    arguments = Bundle().apply {
                        putString("from","0")
                    }
                }
                (requireActivity() as HomeActivity).replaceShortFragment(fragment)
            }
            clFollowing.setOnClickListener {

                val fragment= FollowFollowingFragment().apply {
                    arguments = Bundle().apply {
                        putString("from","1")
                    }
                }
                (requireActivity() as HomeActivity).replaceShortFragment(fragment)
            }

        }
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is ProfileResponse -> {
                        val res = value.data.body
                        binding.shimmerLayout.stopShimmer()
                        binding.coodinatorLayout.visible()
                        binding.shimmerLayout.gone()
                        binding.coodinatorLayout.visible()
                        sharedViewModel.setProfileData(res)
                        if (res?.is_block_by_admin == "1"){
                            showCustomToast(requireContext(),"Your account has been blocked by the admin.")
                            clearPreferences()
                            startActivity(Intent(requireContext(), LoginActivity::class.java))
                            requireActivity().finishAffinity()
                        }
                        with(binding){
                            savePreference("image",res?.image ?: "")
                            rvProfileBackground.loadImage(ApiConstants.IMAGE_BASE_URL+res?.coverImage)
                            ivProfile.loadImage(ApiConstants.IMAGE_BASE_URL+res?.image,R.drawable.profile_icon)
                            when(res?.christianJourney){
                                "Interested/New Christian"->{
                                    ivType.setImageResource(R.drawable.unselected_christian_icon)
                                }
                                "Taking the Next Steps"->{
                                    ivType.setImageResource(R.drawable.selected_christian_icon)
                                }
                                "Delving Deeper"->{
                                    ivType.setImageResource(R.drawable.deeper_icon)
                                }
                            }
                            tvType.text = res?.christianJourney
                            savePreference("displayNamePreference",res?.displayNamePreference ?: 1)

                            if (res?.displayNamePreference == 1){
                                tvName.text = buildString {
                                    append(res.firstName)
                                }
                            }else{
                                tvName.text = buildString {
                                    append(res?.firstName)
                                    append(" ")
                                    append(res?.lastName)
                                }
                            }

                            if (res?.profileType == 2) {
                                savePreference("isProfileType", "2")

                            } else if (res?.profileType == 1) {
                                savePreference("isProfileType", "1")
                            }

                            tvUserName.text = buildString {
                                append("@")
                                append(res?.username ?: "")
                            }
                            tvDescription.text = removeExtraSpaces(res?.bio ?: "")
                            tvPosts.text = res?.postCount
                            tvFollowers.text = res?.followerCount
                            tvFollowing.text = res?.followingCount

                            if (res?.bio.isNullOrEmpty()){
                                tvDescription.gone()
                            }else{
                                tvDescription.visible()
                            }
                        }
                    }
                }
            }
            Status.LOADING -> {
               LoaderDialog.dismiss()
            }

            Status.ERROR -> {
               LoaderDialog.dismiss()
                showCustomSnackbar(requireActivity(),binding.root, value.message.toString())

            }
        }
    }

    private fun initData() {
        fragmentList = ArrayList()
        fragmentList.add(AboutFragment())
        fragmentList.add(PostFragment())
    }
    private fun initFragment() {
        viewpagerAdapter = ViewPagerAdapter(fragmentList, requireActivity())
        binding.viewPager.adapter = viewpagerAdapter
    }

    override fun onResume() {
        super.onResume()
        getProfile()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.shimmerLayout.stopShimmer()
    }


}