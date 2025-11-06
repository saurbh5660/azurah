package com.live.azurah.activity

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayoutMediator
import com.live.azurah.R
import com.live.azurah.adapter.ViewPagerAdapter
import com.live.azurah.databinding.ActivityOtherUserProfileBinding
import com.live.azurah.databinding.MenuReportBinding
import com.live.azurah.databinding.MessageDialogBinding
import com.live.azurah.databinding.SureDialogBinding
import com.live.azurah.fragment.AboutFragment
import com.live.azurah.fragment.FollowFollowingFragment
import com.live.azurah.fragment.PostFragment
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.ProfileResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.loadImage
import com.live.azurah.util.removeExtraSpaces
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import com.live.azurah.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OtherUserProfileActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivityOtherUserProfileBinding
    private val list = arrayOf("About User" to R.drawable.tag_user, "Posts" to R.drawable.post_icon)
    private lateinit var fragmentList: MutableList<Fragment>
    private lateinit var viewpagerAdapter: ViewPagerAdapter
    private var type = false
    private var isFollowed = true
    private var from = 0
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private lateinit var sharedViewModel: SharedViewModel
    private var id = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtherUserProfileBinding.inflate(layoutInflater)
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
        id = intent.getStringExtra("user_id") ?: ""
        binding.viewPager.isUserInputEnabled = false
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        handleDeepLink(intent)
        binding.clOtherLayout.gone()
        binding.shimmerLayout.visible()
        binding.shimmerLayout.startShimmer()


        getProfile()

        initListener()
        initData()
        initFragment()

        TabLayoutMediator(binding.tbLayout, binding.viewPager) { tab, position ->
            tab.text = list[position].first
        }.attach()
    }

    private fun getProfile() {
        viewModel.otherUserProfile(id, this).observe(this, this)
    }

    private fun initData() {
        fragmentList = ArrayList()
        fragmentList.add(AboutFragment(1))
        fragmentList.add(PostFragment(id))
    }

    private fun initFragment() {
        viewpagerAdapter = ViewPagerAdapter(fragmentList, this)
        binding.viewPager.adapter = viewpagerAdapter
    }

    private fun initListener() {
        with(binding) {

            clPost.setOnClickListener {
                binding.viewPager.currentItem = 1
            }

            ivShare.setOnClickListener {
                setPopUpWindow(it)
            }

            ivChat.setOnClickListener {
                val profileData = sharedViewModel.profile.value
                if (profileData?.isFollowByMe != 1 && profileData?.isFollowByMe != 0) {
                    messageDialog()
                } else {
                    Log.d("dsfdsfds", sharedViewModel.profile.value?.id.toString())
                    startActivity(
                        Intent(
                            this@OtherUserProfileActivity,
                            ChatActivity::class.java
                        ).apply {
                            putExtra("uid2", sharedViewModel.profile.value?.id.toString())
                            putExtra("username", sharedViewModel.profile.value?.username.toString())
                            putExtra(
                                "name",
                                sharedViewModel.profile.value?.firstName + " " + sharedViewModel.profile.value?.lastName.toString()
                            )
                            putExtra("image", sharedViewModel.profile.value?.image.toString())
                        })
                }
            }

            clFollowers.setOnClickListener {
                val model = sharedViewModel.profile.value
                val fragment = FollowFollowingFragment().apply {
                    arguments = Bundle().apply {
                        putString("from", "0")
                        putString("id", model?.id.toString())
                    }
                }
                replaceFragment(fragment)
            }
            clFollowing.setOnClickListener {
                val model = sharedViewModel.profile.value
                val fragment = FollowFollowingFragment().apply {
                    arguments = Bundle().apply {
                        putString("from", "1")
                        putString("id", model?.id.toString())
                    }
                }
                replaceFragment(fragment)
            }

            clChristianName.setOnClickListener {
                if (type) {
                    type = false
                    tvType.visibility = View.GONE
                } else {
                    type = true
                    tvType.visibility = View.VISIBLE
                }
            }

            btnPost.setOnClickListener {
                val profileData = sharedViewModel.profile.value
                var status = ""
                val followStatus = profileData?.isFollowByMe ?: 4
                if (profileData?.profileType == 1) {
                    if (followStatus == 1) {
                        sureDialog("3")
                        return@setOnClickListener
                    } else {
                        status = "0"
                    }
                    if (followStatus == 0) {
                        sureRemoveRequestDialog("3")
                        return@setOnClickListener
                    }

                } else {
                    if (followStatus == 1) {
                        sureDialog("3")
                        return@setOnClickListener
                    } else {
                        status = "1"
                    }
                }
                followUnfollowApi(status)

            }

            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun followUnfollowApi(status: String) {
        val profileData = sharedViewModel.profile.value
        val map = HashMap<String, String>()
        map["follow_by"] = getPreference("id", "")
        map["follow_to"] = profileData?.id.toString()
        map["status"] = status
        viewModel.followUnfollow(map, this@OtherUserProfileActivity).observe(this) { value ->
            when (value.status) {
                Status.SUCCESS -> {
                   LoaderDialog.dismiss()
                    sharedViewModel.profile.value?.isFollowByMe = status.toInt()
                    with(binding) {
                        when (status) {
                            "0" -> {
                                btnPost.text = "Requested"
                                btnPost.backgroundTintList = ContextCompat.getColorStateList(
                                    this@OtherUserProfileActivity,
                                    R.color.blue
                                )
                                btnPost.setTextColor(
                                    ContextCompat.getColorStateList(
                                        this@OtherUserProfileActivity,
                                        R.color.white
                                    )
                                )
                                binding.ivChat.imageTintList = ContextCompat.getColorStateList(
                                    this@OtherUserProfileActivity,
                                    R.color.black
                                )
                            }

                            "1" -> {
                                binding.btnPost.text = "Following"
                                binding.btnPost.backgroundTintList =
                                    ContextCompat.getColorStateList(
                                        this@OtherUserProfileActivity,
                                        R.color.blue
                                    )
                                binding.ivChat.imageTintList = ContextCompat.getColorStateList(
                                    this@OtherUserProfileActivity,
                                    R.color.black
                                )
                                binding.btnPost.setTextColor(
                                    ContextCompat.getColorStateList(
                                        this@OtherUserProfileActivity,
                                        R.color.white
                                    )
                                )
                            }

                            else -> {
                                if (profileData?.profileType == 1) {
                                    if (profileData.isFollowByOther == 1) {
                                        binding.btnPost.text = "Follow back"
                                    } else {
                                        binding.btnPost.text = "Request"
                                    }
                                } else {
                                    if (profileData?.isFollowByOther == 1) {
                                        binding.btnPost.text = "Follow back"
                                    } else {
                                        binding.btnPost.text = "Follow"
                                    }
                                }
                                binding.btnPost.backgroundTintList =
                                    ContextCompat.getColorStateList(
                                        this@OtherUserProfileActivity,
                                        R.color.blue
                                    )
                                binding.ivChat.imageTintList = ContextCompat.getColorStateList(
                                    this@OtherUserProfileActivity,
                                    R.color.grey
                                )
                                binding.btnPost.setTextColor(
                                    ContextCompat.getColorStateList(
                                        this@OtherUserProfileActivity,
                                        R.color.white
                                    )
                                )
                                if (profileData?.profileType == 1 && profileData.isFollowByOther == 1) {
                                    removeUsernameDialog("3")
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

                    showCustomSnackbar(
                        this,
                        binding.root,
                        value.message.toString()
                    )
                }
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
                        sharedViewModel.setProfileData(res)
                        binding.shimmerLayout.stopShimmer()
                        binding.clOtherLayout.visible()
                        binding.shimmerLayout.gone()
                        with(binding) {
                            rvProfileBackground.loadImage(
                                ApiConstants.IMAGE_BASE_URL + res?.coverImage
                            )
                            ivProfile.loadImage(
                                ApiConstants.IMAGE_BASE_URL + res?.image,
                                R.drawable.profile_icon
                            )

                            when (res?.christianJourney) {
                                "Interested/New Christian" -> {
                                    ivType.setImageResource(R.drawable.unselected_christian_icon)
                                }

                                "Taking the Next Steps" -> {
                                    ivType.setImageResource(R.drawable.selected_christian_icon)
                                }

                                "Delving Deeper" -> {
                                    ivType.setImageResource(R.drawable.deeper_icon)
                                }
                            }

                            tvType.text = res?.christianJourney
                            if (res?.displayNamePreference == 1) {
                                tvName.text = buildString {
                                    append(res.firstName)
                                }
                            } else {
                                tvName.text = buildString {
                                    append(res?.firstName)
                                    append(" ")
                                    append(res?.lastName)
                                }
                            }
                            tvUserName.text = buildString {
                                append("@")
                                append(res?.username ?: "")
                            }
                            tvDescription.text = removeExtraSpaces(res?.bio ?: "")
                            tvPosts.text = res?.postCount

                          /*  *//** static **//*
                            if (res?.username == "carissa") {
                                tvFollowers.text = "390"
                                tvFollowing.text = "275"
                            } else if (res?.username == "stefany") {
                                tvFollowers.text = "225"
                                tvFollowing.text = "165"
                            } else {
                                tvFollowers.text = res?.followerCount
                                tvFollowing.text = res?.followingCount
                            }*/


                             tvFollowers.text = res?.followerCount
                             tvFollowing.text = res?.followingCount

                            if (res?.bio.isNullOrEmpty()) {
                                tvDescription.gone()
                            } else {
                                tvDescription.visible()
                            }

                            setFollowStatus()

                        }
                    }
                }
            }

            Status.LOADING -> {
               LoaderDialog.dismiss()
            }

            Status.ERROR -> {
               LoaderDialog.dismiss()
                showCustomSnackbar(this, binding.root, value.message.toString())

            }
        }
    }

    private fun setFollowStatus() {
        val res = sharedViewModel.profile.value

        with(binding) {
            when (res?.isFollowByMe) {
                0 -> {
                    btnPost.text = "Requested"
                    btnPost.backgroundTintList = ContextCompat.getColorStateList(
                        this@OtherUserProfileActivity,
                        R.color.blue
                    )
                    btnPost.setTextColor(
                        ContextCompat.getColorStateList(
                            this@OtherUserProfileActivity,
                            R.color.white
                        )
                    )
                    binding.ivChat.imageTintList = ContextCompat.getColorStateList(
                        this@OtherUserProfileActivity,
                        R.color.black
                    )
                }

                1 -> {
                    binding.btnPost.text = "Following"
                    binding.btnPost.backgroundTintList =
                        ContextCompat.getColorStateList(
                            this@OtherUserProfileActivity,
                            R.color.blue
                        )
                    binding.ivChat.imageTintList = ContextCompat.getColorStateList(
                        this@OtherUserProfileActivity,
                        R.color.black
                    )
                    binding.btnPost.setTextColor(
                        ContextCompat.getColorStateList(
                            this@OtherUserProfileActivity,
                            R.color.white
                        )
                    )
                }

                else -> {
                    if (res?.profileType == 1) {
                        if (res.isFollowByOther == 1) {
                            binding.btnPost.text = "Follow back"
                        } else {
                            binding.btnPost.text = "Request"
                        }

                    } else {
                        if (res?.isFollowByOther == 1) {
                            binding.btnPost.text = "Follow back"
                        } else {
                            binding.btnPost.text = "Follow"
                        }
                    }
                    binding.btnPost.backgroundTintList =
                        ContextCompat.getColorStateList(
                            this@OtherUserProfileActivity,
                            R.color.blue
                        )
                    binding.ivChat.imageTintList = ContextCompat.getColorStateList(
                        this@OtherUserProfileActivity,
                        R.color.grey
                    )
                    binding.btnPost.setTextColor(
                        ContextCompat.getColorStateList(
                            this@OtherUserProfileActivity,
                            R.color.white
                        )
                    )
                }
            }
        }
    }

    private fun messageDialog() {
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val confirmationBinding = MessageDialogBinding.inflate(layoutInflater)
        customDialog.setContentView(confirmationBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        confirmationBinding.tvYes.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()
    }

    private fun sureRemoveRequestDialog(status: String) {
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val confirmationBinding = SureDialogBinding.inflate(layoutInflater)
        customDialog.setContentView(confirmationBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val username = sharedViewModel.profile.value?.username ?: ""

        with(confirmationBinding) {
            tvUsernameTaken.text = buildString {
                append("Are you sure you want to remove the follow request for @")
                append(username)
                append("?")
            }
        }
        confirmationBinding.tvYes.setOnClickListener {
            customDialog.dismiss()
            followUnfollowApi(status)
        }

        confirmationBinding.tvNo.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()
    }

    private fun sureDialog(status: String) {
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val confirmationBinding = SureDialogBinding.inflate(layoutInflater)
        customDialog.setContentView(confirmationBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val username = sharedViewModel.profile.value?.username ?: ""

        with(confirmationBinding) {
            tvUsernameTaken.text = buildString {
                append("Are you sure you want to unfollow @")
                append(username)
                append("?")
            }
        }
        confirmationBinding.tvYes.setOnClickListener {
            customDialog.dismiss()
            followUnfollowApi(status)
        }

        confirmationBinding.tvNo.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()
    }

    private fun removeUsernameDialog(status: String) {
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val confirmationBinding = SureDialogBinding.inflate(layoutInflater)
        customDialog.setContentView(confirmationBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        confirmationBinding.tvMsg.visibility = View.GONE
        val username = sharedViewModel.profile.value?.username ?: ""
        confirmationBinding.tvUsernameTaken.text = buildString {
            append("Do you want to remove @")
            append(username)
            append(" as a follower?")
        }
        confirmationBinding.tvYes.setOnClickListener {
            customDialog.dismiss()
            val profileData = sharedViewModel.profile.value
            val map = HashMap<String, String>()
            map["follow_by"] = profileData?.id.toString()
            map["follow_to"] = getPreference("id", "")
            map["status"] = status
            viewModel.followUnfollow(map, this@OtherUserProfileActivity).observe(this) { value ->
                when (value.status) {
                    Status.SUCCESS -> {
                       LoaderDialog.dismiss()
                        sharedViewModel.profile.value?.isFollowByOther = status.toInt()
                        setFollowStatus()
                        showCustomSnackbar(
                            this,
                            binding.root,
                            "@${username} has been removed as a follower."
                        )
                    }

                    Status.LOADING -> {
                        LoaderDialog.show(this)
                    }

                    Status.ERROR -> {
                       LoaderDialog.dismiss()

                        showCustomSnackbar(
                            this,
                            binding.root,
                            value.message.toString()
                        )
                    }
                }
            }

        }
        confirmationBinding.tvNo.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()
    }

    private fun setPopUpWindow(view1: View) {
        val menuBinding = MenuReportBinding.inflate(layoutInflater)

        val myPopupWindow = PopupWindow(
            menuBinding.root,
            550,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        with(menuBinding) {
            ivUser.setImageResource(R.drawable.report_users_icon)
            ivReportUser.setImageResource(R.drawable.report_prompt)
            ivBlockUser.setImageResource(R.drawable.block_user_icon)

            tvNotDone.text = "Report User"
            tvHaveDone.text = "Report Prompt"
            tvBlockUser.text = "Block User"

            tvHaveDone.setOnClickListener {
                myPopupWindow.dismiss()
                val model = sharedViewModel.profile.value

                startActivity(
                    Intent(
                        this@OtherUserProfileActivity,
                        ReportPromptActivity::class.java
                    ).apply {
                        putExtra("id", model?.id.toString())
                        putExtra("username", model?.username.toString())
                    }
                )
            }

            ivReportUser.setOnClickListener {
                myPopupWindow.dismiss()
                val model = sharedViewModel.profile.value
                startActivity(
                    Intent(
                        this@OtherUserProfileActivity,
                        ReportPromptActivity::class.java
                    ).apply {
                        putExtra("id", model?.id.toString())
                        putExtra("username", model?.username.toString())
                    }
                )
            }


            tvNotDone.setOnClickListener {
                myPopupWindow.dismiss()
                val model = sharedViewModel.profile.value
                startActivity(
                    Intent(
                        this@OtherUserProfileActivity,
                        ReportUserActivity::class.java
                    ).apply {
                        putExtra("from", "prayer")
                        putExtra("id", model?.id.toString())
                        putExtra("username", model?.username.toString())
                    })
            }


            ivUser.setOnClickListener {
                myPopupWindow.dismiss()
                val model = sharedViewModel.profile.value
                startActivity(
                    Intent(
                        this@OtherUserProfileActivity,
                        ReportUserActivity::class.java
                    ).apply {
                        putExtra("from", "prayer")
                        putExtra("id", model?.id.toString())
                        putExtra("username", model?.username.toString())
                    })
            }

            ivBlockUser.setOnClickListener {
                myPopupWindow.dismiss()
                blockDialog()
            }

            tvBlockUser.setOnClickListener {
                myPopupWindow.dismiss()
                blockDialog()
            }

        }


        myPopupWindow.showAsDropDown(view1, 0, 6)
    }

    private fun blockDialog() {
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val confirmationBinding = SureDialogBinding.inflate(layoutInflater)
        customDialog.setContentView(confirmationBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val model = sharedViewModel.profile.value

        confirmationBinding.tvMsg.text =
            "Blocking will prevent the user from interacting with you, seeing your posts, and sending you messages."
        confirmationBinding.tvUsernameTaken.text = buildString {
            append("Are you sure you want to block ")
            append("@")
            append(model?.username ?: "")
            append("?")
        }

        confirmationBinding.tvYes.setOnClickListener {
            customDialog.dismiss()

            val map = HashMap<String, String>()
            map["block_by"] = getPreference("id", "")
            map["block_to"] = model?.id.toString()
            map["status"] = "1"
            viewModel.userBlock(map, this).observe(this) { value ->
                when (value.status) {
                    Status.SUCCESS -> {
                       LoaderDialog.dismiss()
                        when (value.data) {
                            is CommonResponse -> {
                                val res = value.data.body
                                showCustomSnackbar(this, binding.root, "User Blocked Successfully!")
                                lifecycleScope.launch {
                                    delay(700)
                                    finish()

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
        confirmationBinding.tvNo.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()
    }

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(binding.fragmentContainer.id, fragment)
            .addToBackStack(null).commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.shimmerLayout.stopShimmer()
    }
    private fun handleDeepLink(intent: Intent) {
        val data: Uri? = intent.data
        data?.let {
            // Example: https://app.azrius.co.uk/common_api/deepLinking?user_id=1
            val userId = it.getQueryParameter("user_id")
            if (!userId.isNullOrEmpty()) {
                Log.d("DeepLink", "User ID from link: $userId")
                id = userId
                // you can now use `id` as needed
            } else {
                Log.w("DeepLink", "No user_id found in the link")
            }
        }
    }

}