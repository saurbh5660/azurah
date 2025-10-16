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
import android.view.ViewGroup
import android.view.Window
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.messaging.FirebaseMessaging
import com.live.azurah.R
import com.live.azurah.controller.MyApplication
import com.live.azurah.databinding.ActivityLoginBinding
import com.live.azurah.databinding.DialogInvalidLoginBinding
import com.live.azurah.databinding.DialogResetPasswordBinding
import com.live.azurah.model.LoginResponse
import com.live.azurah.model.SignUpResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.socket.SocketManager
import com.live.azurah.util.isValidEmail
import com.live.azurah.util.savePreference
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivityLoginBinding
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private lateinit var socketManager: SocketManager
    private var deviceToken = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
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
        socketManager = MyApplication.instance!!.getSocketManager()!!
        socketManager.disconnect()
        initListener()
    }

    private fun initListener() {
        with(binding){

            etEmailAddrress.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().trim()
                            .isNotEmpty() && etPassword.text.toString().trim().isNotBlank())
                     {
                        binding.btnSignIn.backgroundTintList = ActivityCompat.getColorStateList(
                            this@LoginActivity,
                            R.color.blue
                        )
                        binding.btnSignIn.isEnabled = true
                    } else {
                        binding.btnSignIn.backgroundTintList = ActivityCompat.getColorStateList(
                            this@LoginActivity,
                            R.color.button_grey
                        )
                        binding.btnSignIn.isEnabled = false
                    }

                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

            etPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().trim()
                            .isNotEmpty() && etEmailAddrress.text.toString().trim().isNotBlank())
                    {
                        binding.btnSignIn.backgroundTintList = ActivityCompat.getColorStateList(
                            this@LoginActivity,
                            R.color.blue
                        )
                        binding.btnSignIn.isEnabled = true
                    } else {
                        binding.btnSignIn.backgroundTintList = ActivityCompat.getColorStateList(
                            this@LoginActivity,
                            R.color.button_grey
                        )
                        binding.btnSignIn.isEnabled = false
                    }

                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

            btnSignIn.setOnClickListener{
                if (binding.etEmailAddrress.text.toString().trim().isEmpty()) {
                    showCustomSnackbar(this@LoginActivity,it,"Please check your email and password and try again.")
                    return@setOnClickListener
                }

                if (!isValidEmail( binding.etEmailAddrress.text.toString().trim())) {
                    showCustomSnackbar(this@LoginActivity,it,"Please check your email and password and try again.")
                    return@setOnClickListener
                }

                if (etPassword.text.toString().trim().isEmpty()) {
                    showCustomSnackbar(this@LoginActivity,it,"Please check your email and password and try again.")
                    return@setOnClickListener
                }

                val map = HashMap<String,String>()
                map["email"] = binding.etEmailAddrress.text.toString().trim()
                map["password"] = binding.etPassword.text.toString().trim()
                map["device_token"] = deviceToken
                map["device_type"] = "1"
                Log.d("dkjbsg",binding.etEmailAddrress.text.toString().trim())
                Log.d("dkjbsg",binding.etPassword.text.toString().trim())
                viewModel.login(map,this@LoginActivity).observe(this@LoginActivity,this@LoginActivity)
            }

            tvSignUp.setOnClickListener{
                startActivity(Intent(this@LoginActivity,SignUpActivity::class.java))
            }
            tvForgotPassword.setOnClickListener{
                startActivity(Intent(this@LoginActivity,ResetPasswordActivity::class.java))
            }

           /* val hintTypeface = ResourcesCompat.getFont(this@LoginActivity,R.font.poppins_medium)
            val editTextTypeface = ResourcesCompat.getFont(this@LoginActivity,R.font.poppins)

            binding.tlPassword.typeface = hintTypeface
            binding.tlEmail.typeface = hintTypeface
            binding.etPassword.typeface = editTextTypeface
            binding.etEmailAddrress.typeface = editTextTypeface*/


            tlEmail.addOnEditTextAttachedListener {
                formatHint("Email Address*",tlEmail,etEmailAddrress.isFocused)
            }

            etEmailAddrress.setOnFocusChangeListener { _, hasFocus ->
                formatHint("Email Address*",tlEmail,hasFocus)
            }

            tlPassword.addOnEditTextAttachedListener {
                formatHint("Password*",tlPassword,etPassword.isFocused)
            }

            etPassword.setOnFocusChangeListener { _, hasFocus ->
                formatHint("Password*",tlPassword,hasFocus)
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
                        savePreference("displayNamePreference",res?.displayNamePreference ?: 1)

                        socketManager.init()
                        if (res?.is_otp_verified == 0){
                            startActivity(Intent(this, VerificationActivity::class.java))
                            return
                        }

                        if (res?.is_profile_completed != "1"){
                            startActivity(Intent(this, SetUpProfileActivity::class.java).apply {
                                putExtra("page",res?.form_step ?: "0")
                            })
                            return
                        }
                        savePreference("isLogin",true)
                        startActivity(Intent(this, HomeActivity::class.java))
                        finishAffinity()
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

    private fun invalidLoginDialog(){
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val resetBinding = DialogInvalidLoginBinding.inflate(layoutInflater)
        customDialog.setContentView(resetBinding.root)
        customDialog.window?.setGravity(Gravity.BOTTOM)
        customDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        resetBinding.btnSignIn.setOnClickListener {
            customDialog.dismiss()
            startActivity(Intent(this@LoginActivity,HomeActivity::class.java))

        }
        customDialog.show()

    }

    override fun onResume() {
        super.onResume()
        getDeviceToken()
    }

    private fun getDeviceToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("Devicetoken", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            deviceToken = task.result ?: ""
            Log.d("Devicetoken", "FCM Token : $deviceToken")
        })
    }
}