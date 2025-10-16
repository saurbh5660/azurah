package com.live.azurah.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputLayout
import com.live.azurah.R
import com.live.azurah.databinding.ActivityResetPasswordBinding
import com.live.azurah.model.LoginResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.isValidEmail
import com.live.azurah.util.savePreference
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResetPasswordActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivityResetPasswordBinding
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var from = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
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

        from = intent.getStringExtra("from") ?: ""

        if (from == "1"){
            binding.tvRemember.visibility = View.GONE
            binding.tvSignIn.visibility = View.GONE
        }else{
            binding.tvRemember.visibility = View.VISIBLE
            binding.tvSignIn.visibility = View.VISIBLE
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

            etEmail.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().trim().isNotEmpty()) {
                        binding.btnContinue.backgroundTintList = ActivityCompat.getColorStateList(this@ResetPasswordActivity, R.color.blue)
                        binding.btnContinue.isEnabled = true
                    } else {
                        binding.btnContinue.backgroundTintList = ActivityCompat.getColorStateList(this@ResetPasswordActivity, R.color.button_grey)
                        binding.btnContinue.isEnabled = false
                    }

                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

            btnContinue.setOnClickListener{
                if (binding.etEmail.text.toString().trim().isEmpty()) {
                    showCustomSnackbar(this@ResetPasswordActivity,it,"Email field is required.")
                    return@setOnClickListener
                }

                if (!isValidEmail( binding.etEmail.text.toString().trim())) {
                    showCustomSnackbar(this@ResetPasswordActivity,it,"Please enter valid email.")
                    return@setOnClickListener
                }

                val map = HashMap<String,String>()
                map["email"] = binding.etEmail.text.toString().trim()
                viewModel.forgotPassword(map,this@ResetPasswordActivity).observe(this@ResetPasswordActivity,this@ResetPasswordActivity)
            }

            tvSignIn.setOnClickListener{
               onBackPressedDispatcher.onBackPressed()
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
                    is LoginResponse -> {
                        val res = value.data.body
                        savePreference("firstName",res?.first_name ?: "")
                        savePreference("lastName",res?.last_name ?: "")
                        savePreference("username",res?.username ?: "")
                        savePreference("token",res?.access_token ?: "")
                        savePreference("email",res?.email ?: "")
                        savePreference("dob",res?.dob ?: "")
                        savePreference("image",res?.image ?: "")
                        savePreference("id",res?.id.toString())

                        startActivity(Intent(this, VerificationActivity::class.java).apply {
                            putExtra("from",2)
                            putExtra("email",binding.etEmail.text.toString())
                        })
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

}