package com.live.azurah.activity

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.lifecycle.Observer
import com.live.azurah.R
import com.live.azurah.databinding.ActivitySettingBinding
import com.live.azurah.databinding.ConfirmationDialogBinding
import com.live.azurah.databinding.LogoutDialogBinding
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.ProfileResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.clearPreferences
import com.live.azurah.util.openBrowser
import com.live.azurah.util.openUrlInBrowser
import com.live.azurah.util.savePreference
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivitySettingBinding
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var showLoader = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
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
    }

    private fun initListener() {
        with(binding){
            clEdit.setOnClickListener {
                startActivity(Intent(this@SettingActivity,AccountSettingActivity::class.java))
            }

            clChangePassword.setOnClickListener {
                startActivity(Intent(this@SettingActivity,ChangePasswordActivity::class.java))
            }

            clChange.setOnClickListener {
                startActivity(Intent(this@SettingActivity,SubscriptionActivity::class.java))
            }

            clSongs.setOnClickListener {
                openUrlInBrowser(this@SettingActivity,"https://azrius.com/pages/song-submission")
            }

            clDonate.setOnClickListener {
                openUrlInBrowser(this@SettingActivity,"https://donate.stripe.com/dR6cQO252alBdVe146")
            }

            clNotification.setOnClickListener {
                startActivity(Intent(this@SettingActivity,PushNotificationActivity::class.java))
            }

            clBlocked.setOnClickListener {
                startActivity(Intent(this@SettingActivity,BlockedUserList::class.java))
            }

            clPrivacy.setOnClickListener {
                startActivity(Intent(this@SettingActivity,PrivacySettingActivity::class.java))
            }

            clLogout.setOnClickListener {
                logoutDialog()
            }

            clTwoStep.setOnClickListener {
                inviteFriend()
            }

            clHelp.setOnClickListener {
                startActivity(Intent(this@SettingActivity,HelpCenterActivity::class.java))
            }

            clLogin.setOnClickListener {
                startActivity(Intent(this@SettingActivity,RecentLoginActivity::class.java))
            }

            clShopSetting.setOnClickListener {
                startActivity(Intent(this@SettingActivity,ShopSettingActivity::class.java))
            }

            clQuestSetting.setOnClickListener {
                startActivity(Intent(this@SettingActivity,QuestSettingActivity::class.java))
            }


            clDeleteAcoount.setOnClickListener {
                startActivity(Intent(this@SettingActivity,DeleteAccountActivity::class.java))
            }

            clLegalInfo.setOnClickListener {
                startActivity(Intent(this@SettingActivity,LegalInfoActivity::class.java))
            }

            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }


            ivCross.setOnClickListener {
                etSearch.setText("")
            }
            etSearch.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().isNotBlank()){
                        ivCross.visibility = View.VISIBLE
                    }else{
                        ivCross.visibility = View.GONE
                    }
                }
                override fun afterTextChanged(s: Editable?) {
                }

            })
        }
    }

    private fun logoutDialog(){
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val confirmationBinding = LogoutDialogBinding.inflate(layoutInflater)
        customDialog.setContentView(confirmationBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)


        confirmationBinding.tvNo.setOnClickListener {
            customDialog.dismiss()
        }

        confirmationBinding.tvYes.setOnClickListener {
            customDialog.dismiss()
            showLoader = true
            viewModel.logOut(this).observe(this,this)
        }
        customDialog.show()
        
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is CommonResponse -> {
                       clearPreferences()
                        startActivity(Intent(this,LoginActivity::class.java))
                        finishAffinity()
                    }
                    is ProfileResponse -> {
                        val res = value.data.body
                        with(binding) {
                            savePreference("shopPush",res?.isShopPush ?: "")
                            savePreference("isNewBibleQuestPush",res?.isNewBibleQuestPush ?: "")
                            savePreference("isPostPush",res?.isPostPush ?: "")
                            savePreference("isTestimonyPush",res?.isTestimonyPush ?: "")
                            savePreference("isPrayerPush",res?.isPrayerPush ?: "")
                            savePreference("isForumPush",res?.isForumPush ?: "")
                            savePreference("isNotification",res?.isNotification ?: "")
                            savePreference("is_post_comment_mention_push",res?.is_post_comment_mention_push ?: "")

                        }
                    }
                }
            }
            Status.LOADING -> {
                if (showLoader){
                    LoaderDialog.show(this)
                }
            }
            Status.ERROR -> {
               LoaderDialog.dismiss()
                showCustomSnackbar(this,binding.root, value.message.toString())
            }
        }
    }

    private fun inviteFriend() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
//            putExtra(Intent.EXTRA_TEXT, "Hey! Check out this amazing app: https://play.google.com/store/apps/details?id=${packageName}")
            putExtra(Intent.EXTRA_TEXT, "Hey! Check out this amazing app: https://app.azrius.co.uk/common_api/downloadApp")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Invite a friend via"))
    }

    override fun onResume() {
        super.onResume()
        getProfile()
    }

    private fun getProfile(){
        showLoader = false
        viewModel.getProfile(this).observe(this,this)
    }
}