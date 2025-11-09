package com.live.azurah.activity

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import com.live.azurah.R
import com.live.azurah.databinding.ActivityVerificationBinding
import com.live.azurah.databinding.DialogResetPasswordBinding
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.LoginResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.clearPreferences
import com.live.azurah.util.getPreference
import com.live.azurah.util.savePreference
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import `in`.aabhasjindal.otptextview.OTPListener
import org.json.JSONObject


@AndroidEntryPoint
class VerificationActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivityVerificationBinding
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var from = 0
    private var email = ""
    private var reason = ""
    private var password = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
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
        initClickListener()

        from = intent.getIntExtra("from", 0)
        email = intent.getStringExtra("email") ?: ""
        reason = intent.getStringExtra("reason") ?: ""
        password = intent.getStringExtra("password") ?: ""
        sendOtpTimer()
        /* if (from == 2){
             showCustomSnackbar(this@VerificationActivity,binding.root, "Your otp is 123456")
         }else{
             showCustomSnackbar(this@VerificationActivity,binding.root, "Your otp is 123456")
         }*/

    }

    private fun initClickListener() {
        with(binding) {
            otpView.requestFocusOTP()
            otpView.otpListener = object : OTPListener {
                override fun onInteractionListener() {
                    if (otpView.otp.toString().length == 6) {
                        binding.btnVerification.backgroundTintList =
                            ActivityCompat.getColorStateList(
                                this@VerificationActivity,
                                R.color.blue
                            )
                        binding.btnVerification.isEnabled = true
                    } else {
                        binding.btnVerification.backgroundTintList =
                            ActivityCompat.getColorStateList(
                                this@VerificationActivity,
                                R.color.button_grey
                            )
                        binding.btnVerification.isEnabled = false
                    }
                }

                override fun onOTPComplete(otp: String) {
                    if (otp.length == 6) {
                        binding.btnVerification.backgroundTintList =
                            ActivityCompat.getColorStateList(
                                this@VerificationActivity,
                                R.color.blue
                            )
                        binding.btnVerification.isEnabled = true
                    } else {
                        binding.btnVerification.backgroundTintList =
                            ActivityCompat.getColorStateList(
                                this@VerificationActivity,
                                R.color.button_grey
                            )
                        binding.btnVerification.isEnabled = false
                    }
                }
            }

            btnVerification.setOnClickListener {
                if (otpView.otp.toString().length != 6) {
                    showCustomSnackbar(this@VerificationActivity, it, "Please enter valid otp.")
                    return@setOnClickListener
                }
                val map = HashMap<String, String>()
                map["otp"] = otpView.otp.toString()
                if (from == 2) {
                    map["email"] = email
                    viewModel.verifyPassword(map, this@VerificationActivity)
                        .observe(this@VerificationActivity, this@VerificationActivity)
                } else if (from == 3) {
                    val map1  = HashMap<String,Any>()
                    map1["otp"] = otpView.otp.toString()
                    map1["delete_reason"] = reason
                    map1["password"] = password
                    viewModel.deleteAccount(getPreference("id", ""), map1, this@VerificationActivity)
                        .observe(this@VerificationActivity, this@VerificationActivity)

                } else {
                    viewModel.verifyOtp(map, this@VerificationActivity)
                        .observe(this@VerificationActivity, this@VerificationActivity)
                }
            }

            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            btnResend.setOnClickListener {
                if (from == 2) {
                    val map = HashMap<String, String>()
                    map["email"] = email
                    viewModel.forgotPassword(map, this@VerificationActivity)
                        .observe(this@VerificationActivity) { value ->
                            when (value.status) {
                                Status.SUCCESS -> {
                                   LoaderDialog.dismiss()
                                    when (value.data) {
                                        is LoginResponse -> {
                                            showCustomSnackbar(
                                                this@VerificationActivity,
                                                binding.root,
                                                value.data.message.toString()
                                            )
                                            binding.btnResend.isEnabled = false
                                            binding.btnResend.backgroundTintList =
                                                ContextCompat.getColorStateList(
                                                    this@VerificationActivity,
                                                    R.color.button_grey
                                                )
                                            sendOtpTimer()
                                        }
                                    }
                                }

                                Status.LOADING -> {
                                    LoaderDialog.show(this@VerificationActivity)
                                }

                                Status.ERROR -> {
                                   LoaderDialog.dismiss()
                                    showCustomSnackbar(
                                        this@VerificationActivity,
                                        binding.root,
                                        value.message.toString()
                                    )
                                }
                            }
                        }

                } else {
                    viewModel.resendOtp(HashMap(), this@VerificationActivity)
                        .observe(this@VerificationActivity) { value ->
                            when (value.status) {
                                Status.SUCCESS -> {
                                   LoaderDialog.dismiss()
                                    when (value.data) {
                                        is CommonResponse -> {
                                            showCustomSnackbar(
                                                this@VerificationActivity,
                                                binding.root,
                                                value.data.message.toString()
                                            )
                                            binding.btnResend.isEnabled = false
                                            binding.btnResend.backgroundTintList =
                                                ContextCompat.getColorStateList(
                                                    this@VerificationActivity,
                                                    R.color.button_grey
                                                )
                                            sendOtpTimer()
                                        }
                                    }
                                }

                                Status.LOADING -> {
                                    LoaderDialog.show(this@VerificationActivity)
                                }

                                Status.ERROR -> {
                                   LoaderDialog.dismiss()
                                    showCustomSnackbar(
                                        this@VerificationActivity,
                                        binding.root,
                                        value.message.toString()
                                    )
                                }
                            }
                        }
                }
            }
        }
    }

    private fun sendOtpTimer() {
        val durationInMillis: Long = 180000

        val timer = object : CountDownTimer(durationInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                binding.tvResendTime.text = secondsLeft.toString()

                if (secondsLeft > 1) {
                    binding.tvSeconds.text = "seconds."
                } else {
                    binding.tvSeconds.text = "second."
                }
            }

            override fun onFinish() {
                binding.btnResend.isEnabled = true
                binding.btnResend.backgroundTintList =
                    ContextCompat.getColorStateList(this@VerificationActivity, R.color.blue)
            }
        }

        timer.start()
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is CommonResponse -> {
                        if (from == 1) {
                            val map = HashMap<String,String>()
                            map["email"] = email
                            viewModel.editProfile(map,this).observe(this,this)
                            savePreference("email",email)
                            showResetSuccessDialog()
                        } else if (from == 2) {
                            startActivity(
                                Intent(
                                    this@VerificationActivity,
                                    NewPasswordActivity::class.java
                                )
                            )
                            finish()
                        } else if (from == 3) {
                            clearPreferences()
                            startActivity(
                                Intent(
                                    this@VerificationActivity,
                                    ConfirmDeleteAccountActivity::class.java
                                )
                            )
                            finishAffinity()
                        } else {
                            startActivity(
                                Intent(
                                    this@VerificationActivity,
                                    VerificationComplete::class.java
                                )
                            )
                            finish()
                        }
                    }

                    is LoginResponse -> {
                        Log.d("dskjgdjkgg", "sdkugufd")
                        val resetToken = value.data.body?.reset_token ?: ""
                        startActivity(
                            Intent(
                                this@VerificationActivity,
                                NewPasswordActivity::class.java
                            ).apply {
                                putExtra("reset_token", resetToken)
                            })
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

    private fun showResetSuccessDialog() {
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val resetBinding = DialogResetPasswordBinding.inflate(layoutInflater)
        customDialog.setContentView(resetBinding.root)
        customDialog.setCancelable(false)
        customDialog.setCanceledOnTouchOutside(false)
        customDialog.window?.setGravity(Gravity.BOTTOM)
        customDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        resetBinding.tvTitle.text = "Success!"
        resetBinding.btnSignIn.text = "Done"
        if (from == 1) {
            resetBinding.tvMessage.text = "Your email address has been updated."
        } else {
            resetBinding.tvMessage.text = "Your password has been updated."
        }
        resetBinding.btnSignIn.setOnClickListener {
            customDialog.dismiss()
            finish()
        }
        customDialog.show()

    }
}