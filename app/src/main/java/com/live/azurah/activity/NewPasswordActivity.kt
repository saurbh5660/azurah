package com.live.azurah.activity

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
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
import com.live.azurah.databinding.ActivityNewPasswordBinding
import com.live.azurah.databinding.DialogResetPasswordBinding
import com.live.azurah.model.LoginResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.isValidEmail
import com.live.azurah.util.savePreference
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class NewPasswordActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivityNewPasswordBinding
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var resetToken =""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewPasswordBinding.inflate(layoutInflater)
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
        resetToken = intent.getStringExtra("reset_token") ?: ""
        initListener()
    }

    private fun initListener() {
        with(binding){
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            btnVerification.setOnClickListener{
                if (binding.etPassword.text.toString().trim().isEmpty()) {
                    showCustomSnackbar(this@NewPasswordActivity,it,"Password must be at least 6 characters.")
                    return@setOnClickListener
                }
                val map = HashMap<String,String>()
                map["new_password"] = binding.etPassword.text.toString().trim()
                map["confirm_password"] = binding.etPassword.text.toString().trim()
                map["reset_token"] = resetToken
                viewModel.newPassword(map,this@NewPasswordActivity).observe(this@NewPasswordActivity,this@NewPasswordActivity)
            }

            tlPassword.addOnEditTextAttachedListener {
                formatHint("New Password*",tlPassword,etPassword.isFocused)
            }

            etPassword.setOnFocusChangeListener { _, hasFocus ->
                formatHint("New Password*",tlPassword,hasFocus)
            }
        }
    }

  private fun showResetSuccessDialog(){
     val customDialog = Dialog(this)
      customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
      val resetBinding = DialogResetPasswordBinding.inflate(layoutInflater)
      customDialog.setContentView(resetBinding.root)
      customDialog.setCancelable(false)
      customDialog.window?.setGravity(Gravity.BOTTOM)
      customDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
      customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

      resetBinding.btnSignIn.setOnClickListener {
          customDialog.dismiss()
          startActivity(Intent(this,LoginActivity::class.java))
          finishAffinity()
      }
      customDialog.show()

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
                    is LoginResponse -> {
                        showResetSuccessDialog()
                    }
                }
            }
            Status.LOADING -> {
                LoaderDialog.show(this)
            }
            Status.ERROR -> {
               LoaderDialog.dismiss()
                showCustomSnackbar(this,binding.root, value.message.toString())
            }
        }
    }

}