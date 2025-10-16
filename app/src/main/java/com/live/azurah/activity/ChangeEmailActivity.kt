package com.live.azurah.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputLayout
import com.live.azurah.R
import com.live.azurah.databinding.ActivityChangeEmailBinding
import com.live.azurah.model.CommonResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.getPreference
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangeEmailActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivityChangeEmailBinding
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeEmailBinding.inflate(layoutInflater)
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

            tlEmail.addOnEditTextAttachedListener {
                formatHint("Email Address*",tlEmail,etEmail.isFocused)
            }

            etEmail.setOnFocusChangeListener { _, hasFocus ->
                formatHint("Email Address*",tlEmail,hasFocus)
            }

            btnContinue.setOnClickListener{
                val map = HashMap<String, String>()
                map["email"] = binding.etEmail.text.toString().trim()
                viewModel.changeEmailRequest(map, this@ChangeEmailActivity)
                    .observe(this@ChangeEmailActivity, this@ChangeEmailActivity)

            }
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }


    private fun formatHint(text: String, view: TextInputLayout, focus:Boolean) {
        view.hint = ""
        val asteriskIndex = text.indexOf("*")

        val spannableString = SpannableString(text)
        spannableString.setSpan(
            ForegroundColorSpan(if (focus) ContextCompat.getColor(this,R.color.blue) else ContextCompat.getColor(this,R.color.light_black)),
            0,
            asteriskIndex,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this,R.color.star_red_color)),
            asteriskIndex,
            asteriskIndex + 1,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        view.hint = spannableString
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is CommonResponse -> {
                        startActivity(
                            Intent(
                                this,
                                VerificationActivity::class.java
                            ).apply {
                                putExtra("from", 1)
                                putExtra("email", binding.etEmail.text.toString())
                            }
                        )
                        finish()
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