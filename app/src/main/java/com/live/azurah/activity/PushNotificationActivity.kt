package com.live.azurah.activity

import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import com.live.azurah.R
import com.live.azurah.databinding.ActivityPushNotificationBinding
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.ProfileResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.getPreference
import com.live.azurah.util.savePreference
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PushNotificationActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivityPushNotificationBinding
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var showLoader = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPushNotificationBinding.inflate(layoutInflater)
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
        getProfile()
        initListener()
    }

    private fun initListener() {
        with(binding){
            val activeColor = ContextCompat.getColor(this@PushNotificationActivity, R.color.blue)
            val inactiveColor = ContextCompat.getColor(this@PushNotificationActivity, R.color.button_grey)
            val trackColorStateList = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked)), intArrayOf(activeColor, inactiveColor))

            switchNews.trackTintList = trackColorStateList
            switchPush.trackTintList = trackColorStateList
            switchQuest.trackTintList = trackColorStateList
            switchShopPush.trackTintList = trackColorStateList
            switchTestimonyPush.trackTintList = trackColorStateList
            switchPrayerPush.trackTintList = trackColorStateList
            switchForumPush.trackTintList = trackColorStateList
            switchMention.trackTintList = trackColorStateList

            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            switchNews.setOnCheckedChangeListener { buttonView, isChecked ->
                updateNotification("is_notification", if (isChecked) "1" else "0")
            }
            switchPrayerPush.setOnCheckedChangeListener { buttonView, isChecked ->
                updateNotification("is_post_push", if (isChecked) "1" else "0")
            }
            switchTestimonyPush.setOnCheckedChangeListener { buttonView, isChecked ->
                updateNotification("is_forum_push", if (isChecked) "1" else "0")
            }
            switchQuest.setOnCheckedChangeListener { buttonView, isChecked ->
                updateNotification("is_prayer_push", if (isChecked) "1" else "0")
            }
            switchShopPush.setOnCheckedChangeListener { buttonView, isChecked ->
                updateNotification("is_testimony_push", if (isChecked) "1" else "0")
            }
            switchForumPush.setOnCheckedChangeListener { buttonView, isChecked ->
                updateNotification("is_new_bible_quest_push", if (isChecked) "1" else "0")
            }
            switchPush.setOnCheckedChangeListener { buttonView, isChecked ->
                updateNotification("is_shop_push", if (isChecked) "1" else "0")
            }
            switchMention.setOnCheckedChangeListener { buttonView, isChecked ->
                updateNotification("is_post_comment_mention_push", if (isChecked) "1" else "0")
            }

            switchNews.isChecked = getPreference("isNotification","") == "1"
            switchPrayerPush.isChecked = getPreference("isPostPush","") == "1"
            switchTestimonyPush.isChecked = getPreference("isForumPush","") == "1"
            switchQuest.isChecked = getPreference("isPrayerPush","") == "1"
            switchShopPush.isChecked = getPreference("isTestimonyPush","") == "1"
            switchForumPush.isChecked = getPreference("isNewBibleQuestPush","") == "1"
            switchPush.isChecked = getPreference("shopPush","") == "1"
            switchMention.isChecked = getPreference("is_post_comment_mention_push","") == "1"

        }
    }

    private fun getProfile(){
        showLoader = true
        viewModel.getProfile(this).observe(this,this)
    }

    private fun updateNotification(name:String,value:String){
        val map = HashMap<String,String>()
        map[name] = value
        showLoader = false
        viewModel.updateNotification(map,this).observe(this,this)
    }


    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is CommonResponse -> {

                    }

                    is ProfileResponse -> {
                        val res = value.data.body
                        with(binding) {
                            switchPush.isChecked = res?.isShopPush == "1"
                            switchForumPush.isChecked = res?.isNewBibleQuestPush == "1"
                            switchPrayerPush.isChecked = res?.isPostPush == "1"
                            switchShopPush.isChecked = res?.isTestimonyPush == "1"
                            switchQuest.isChecked = res?.isPrayerPush == "1"
                            switchTestimonyPush.isChecked = res?.isForumPush == "1"
                            switchNews.isChecked = res?.isNotification == "1"
                            switchMention.isChecked = res?.is_post_comment_mention_push == "1"


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

}