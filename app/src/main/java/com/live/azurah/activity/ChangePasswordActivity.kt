package com.live.azurah.activity

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.live.azurah.R
import com.live.azurah.databinding.ActivityChangePasswordBinding
import com.live.azurah.databinding.DialogResetPasswordBinding
import com.live.azurah.databinding.FragmentMyPostTestimonyBinding
import com.live.azurah.model.CheckUsernameResponse
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.InterestModel
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChangePasswordActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivityChangePasswordBinding
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
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
            tlPassword.addOnEditTextAttachedListener {
                formatHint("Current Password*",tlPassword,etPassword.isFocused)
            }
            etPassword.setOnFocusChangeListener { _, hasFocus ->
                formatHint("Current Password*",tlPassword,hasFocus)
            }

            tlNewPassword.addOnEditTextAttachedListener {
                formatHint("New Password*",tlNewPassword,etNewPassword.isFocused)
            }
            etNewPassword.setOnFocusChangeListener { _, hasFocus ->
                formatHint("New Password*",tlNewPassword,hasFocus)
            }

            tvForgotPassword.setOnClickListener{
                startActivity(Intent(this@ChangePasswordActivity,ResetPasswordActivity::class.java).apply {
                    putExtra("from","1")
                })
            }

            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            btnSignUp.setOnClickListener {

              /*  if (etPassword.text.toString().length < 6) {
                    showCustomSnackbar(this@ChangePasswordActivity,it,"Password must be at least 6 characters.")
                    return@setOnClickListener
                }
*/
                if (etNewPassword.text.toString().length < 6) {
                    showCustomSnackbar(this@ChangePasswordActivity,it,"Password must be at least 6 characters.")
                    return@setOnClickListener
                }

                val map = HashMap<String,String>()
                map["old_password"] = binding.etPassword.text.toString().trim()
                map["new_password"] = binding.etNewPassword.text.toString().trim()
                viewModel.changePassword(map,this@ChangePasswordActivity).observe(this@ChangePasswordActivity,this@ChangePasswordActivity)

            }

            binding.etPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().trim().isNotEmpty() && binding.etNewPassword.text.toString().trim().isNotEmpty()){
                        binding.btnSignUp.backgroundTintList = getColorStateList(R.color.blue)
                        binding.btnSignUp.isEnabled = true
                    }else{
                        binding.btnSignUp.backgroundTintList = getColorStateList(R.color.button_grey)
                        binding.btnSignUp.isEnabled = false
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })
            binding.etNewPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().trim().isNotEmpty() && binding.etPassword.text.toString().trim().isNotEmpty()){
                        binding.btnSignUp.backgroundTintList = getColorStateList(R.color.blue)
                        binding.btnSignUp.isEnabled = true
                    }else{
                        binding.btnSignUp.backgroundTintList = getColorStateList(R.color.button_grey)
                        binding.btnSignUp.isEnabled = false
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })


        }
    }

    private fun formatHint(text: String, view: TextInputLayout, focus:Boolean) {
        view.hint = ""
        val asteriskIndex = text.indexOf("*")

        Log.d("dfdgd",focus.toString())
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

    private fun showResetSuccessDialog(){
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val resetBinding = DialogResetPasswordBinding.inflate(layoutInflater)
        customDialog.setContentView(resetBinding.root)
        customDialog.window?.setGravity(Gravity.BOTTOM)
        customDialog.setCancelable(false)
        customDialog.setCanceledOnTouchOutside(false)
        customDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        resetBinding.tvTitle.text = "Success!"
        resetBinding.btnSignIn.text = "Done"
        resetBinding.tvMessage.text = "Your password has been updated."
        resetBinding.btnSignIn.setOnClickListener {
            customDialog.dismiss()
            finish()
        }
        customDialog.show()

    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is CommonResponse -> {
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