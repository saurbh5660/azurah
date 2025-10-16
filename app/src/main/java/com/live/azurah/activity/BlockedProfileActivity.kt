package com.live.azurah.activity

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.live.azurah.R
import com.live.azurah.databinding.ActivityBlockedProfileBinding
import com.live.azurah.databinding.SureDialogBinding
import com.live.azurah.fragment.FollowFollowingFragment
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
class BlockedProfileActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivityBlockedProfileBinding
    private var type = false
    private var isFollowed = true
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private lateinit var sharedViewModel: SharedViewModel
    private var id = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockedProfileBinding.inflate(layoutInflater)
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
        id = intent.getStringExtra("user_id") ?: ""
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        getProfile()
    }

    private fun getProfile() {
        viewModel.otherUserProfile(id, this).observe(this, this)
    }

    private fun initListener() {
        with(binding){

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


          /*  clFollowers.setOnClickListener {
                startActivity(
                    Intent(this@BlockedProfileActivity,
                        FollowFollowingActivity::class.java).apply {
                    putExtra("type","0")
                })
            }
            clFollowing.setOnClickListener {
                startActivity(
                    Intent(this@BlockedProfileActivity,
                        FollowFollowingActivity::class.java).apply {
                    putExtra("type","1")
                })
            }
            clChristianName.setOnClickListener {
                if (type){
                    type = false
                    tvType.visibility = View.GONE
                }else{
                    type = true
                    tvType.visibility = View.VISIBLE
                }
            }*/
            btnPost.setOnClickListener {
                sureDialog()
            }
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
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
                        with(binding) {
                            rvProfileBackground.loadImage(
                                ApiConstants.IMAGE_BASE_URL + res?.coverImage
                            )
                            ivProfile.loadImage(
                                ApiConstants.IMAGE_BASE_URL + res?.image,
                                R.drawable.profile_icon
                            )
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
                            tvFollowers.text = res?.followerCount
                            tvFollowing.text = res?.followingCount

                            if (res?.bio.isNullOrEmpty()) {
                                tvDescription.gone()
                            } else {
                                tvDescription.visible()
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
                showCustomSnackbar(this, binding.root, value.message.toString())

            }
        }
    }

    private fun sureDialog(){
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val confirmationBinding = SureDialogBinding.inflate(layoutInflater)
        customDialog.setContentView(confirmationBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val model = sharedViewModel.profile.value

        confirmationBinding.tvUsernameTaken.text = buildString {
            append("Are you sure you want to unblock @")
            append(model?.username)
        }
        confirmationBinding.tvMsg.text = buildString {
        append("They won't be notified that you've unblocked them.")
    }
        confirmationBinding.tvNo.text = "Cancel"

        confirmationBinding.tvYes.setOnClickListener {
            customDialog.dismiss()
            val map = HashMap<String, String>()
            map["block_by"] = getPreference("id", "")
            map["block_to"] = model?.id.toString()
            map["status"] = "0"
            viewModel.userBlock(map, this).observe(this) { value ->
                when (value.status) {
                    Status.SUCCESS -> {
                       LoaderDialog.dismiss()
                        when (value.data) {
                            is CommonResponse -> {
                                showCustomSnackbar(
                                    this,
                                    binding.root,
                                    "@" + model?.username + " has been unblocked."
                                )
                                lifecycleScope.launch {
                                    delay(800)
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

    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(binding.fragmentContainer.id, fragment)
            .addToBackStack(null).commit()
    }

}