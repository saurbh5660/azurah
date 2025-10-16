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
import com.live.azurah.databinding.ActivityPrivacySettingBinding
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
class PrivacySettingActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivityPrivacySettingBinding
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var showLoader = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacySettingBinding.inflate(layoutInflater)
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
        with(binding) {

            val activeColor = ContextCompat.getColor(this@PrivacySettingActivity, R.color.blue)
            val inactiveColor =
                ContextCompat.getColor(this@PrivacySettingActivity, R.color.button_grey)
            val trackColorStateList = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
                ), intArrayOf(activeColor, inactiveColor)
            )

            switchNews.trackTintList = trackColorStateList
            switcPublichNews.trackTintList = trackColorStateList

            switchNews.setOnClickListener {
                if (switchNews.isChecked) {
                    updateNotification("1")
                    switchNews.isChecked = true
                    switcPublichNews.isChecked = false
                } else {
                    updateNotification("2")
                    switchNews.isChecked = false
                    switcPublichNews.isChecked = true
                }
            }


            switcPublichNews.setOnClickListener {
                if (switcPublichNews.isChecked) {
                    updateNotification("2")
                    switchNews.isChecked = false
                    switcPublichNews.isChecked = true
                } else {
                    updateNotification("1")
                    switchNews.isChecked = true
                    switcPublichNews.isChecked = false
                }
            }

           /* switchNews.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    switcPublichNews.isChecked = false
                }

            }
            switcPublichNews.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    switchNews.isChecked = false
                }

            }*/

            if (getPreference("isProfileType", "") == "1") {
                switchNews.isChecked = true
                switcPublichNews.isChecked = false
            } else if (getPreference("isProfileType", "") == "2") {
                switchNews.isChecked = false
                switcPublichNews.isChecked = true

            }


            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun getProfile() {
        showLoader = false
        viewModel.getProfile(this).observe(this, this)
    }

    private fun updateNotification(value: String) {
        val map = HashMap<String, String>()
        map["profile_type"] = value
        showLoader = false
        viewModel.updateNotification(map, this).observe(this, this)
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is CommonResponse -> {
                        if (binding.switchNews.isChecked){
                            savePreference("isProfileType", "1")
                        }else{
                            savePreference("isProfileType", "2")

                        }
                    }

                    is ProfileResponse -> {
                        val res = value.data.body
                        with(binding) {
                            if (res?.profileType == 2) {
                                switcPublichNews.isChecked = true
                                switchNews.isChecked = false
                                savePreference("isProfileType", "2")

                            } else if (res?.profileType == 1) {
                                switchNews.isChecked = true
                                switcPublichNews.isChecked = false
                                savePreference("isProfileType", "1")
                            }

                        }
                    }

                }
            }

            Status.LOADING -> {
                if (showLoader) {
                    LoaderDialog.show(this)
                }
            }

            Status.ERROR -> {
               LoaderDialog.dismiss()
                showCustomSnackbar(this, binding.root, value.message.toString())

            }
        }
    }
}