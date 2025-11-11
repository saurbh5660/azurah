package com.live.azurah.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.messaging.FirebaseMessaging
import com.live.azurah.R
import com.live.azurah.databinding.ActivitySignUpBinding
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.SignUpResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.isValidEmail
import com.live.azurah.util.savePreference
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SignUpActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivitySignUpBinding
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var deviceToken = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
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
        setSpannableText(binding.tvTerms)
        initClickListener()

    }

    private fun initClickListener() {
        with(binding){

            cbTerms.setOnClickListener{
                if (cbTerms.isChecked){
                    btnSignUp.isEnabled = true
                    btnSignUp.backgroundTintList = getColorStateList(R.color.blue)
                }else{
                    btnSignUp.isEnabled = false
                    btnSignUp.backgroundTintList = getColorStateList(R.color.button_grey)
                }
            }

            btnSignUp.setOnClickListener {

                if (etFirstName.text.toString().trim().isEmpty()) {
                    showCustomSnackbar(this@SignUpActivity,it,"First name field is required.")
                    return@setOnClickListener
                }

                if (etLastName.text.toString().trim().isEmpty()) {
                    showCustomSnackbar(this@SignUpActivity,it,"Last name field is required.")
                    return@setOnClickListener
                }

                if (etLEmailName.text.toString().trim().isEmpty()) {
                    showCustomSnackbar(this@SignUpActivity,it,"Email field is required.")
                    return@setOnClickListener
                }

                if (!isValidEmail(etLEmailName.text.toString().trim())) {
                    showCustomSnackbar(this@SignUpActivity,it,"Please enter valid email.")
                    return@setOnClickListener
                }

                if (etPassword.text.toString().trim().isEmpty()) {
                    showCustomSnackbar(this@SignUpActivity,it,"Password field is required.")
                    return@setOnClickListener
                }

                if (etPassword.text.toString().length < 6) {
                    showCustomSnackbar(this@SignUpActivity,it,"Password must be at least 6 characters.")
                    return@setOnClickListener
                }

                if (binding.etReferralCode.text.toString().trim().isNotEmpty()){
                    val map = HashMap<String,String>()
                    map["referral_code"] = etReferralCode.text.toString().trim()
                    viewModel.checkReferralCode(map,this@SignUpActivity).observe(this@SignUpActivity,this@SignUpActivity)
                }else{
                    val map = HashMap<String,String>()
                    map["first_name"] = etFirstName.text.toString().trim()
                    map["last_name"] = etLastName.text.toString().trim()
                    map["email"] = etLEmailName.text.toString().trim()
                    map["password"] = etPassword.text.toString().trim()
                    map["device_type"] = "1"
                    map["device_token"] = deviceToken

                    viewModel.signUp(map,this@SignUpActivity).observe(this@SignUpActivity,this@SignUpActivity)

                }

            }
            tvSignIn.setOnClickListener {
                startActivity(Intent(this@SignUpActivity,LoginActivity::class.java))
            }

            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

           /* val hintTypeface = ResourcesCompat.getFont(this@SignUpActivity,R.font.poppins_medium)
            val editTextTypeface = ResourcesCompat.getFont(this@SignUpActivity,R.font.poppins)

            binding.tlFirstName.typeface = hintTypeface
            binding.tlLastName.typeface = hintTypeface
            binding.tlEmailName.typeface = hintTypeface
            binding.tlPassword.typeface = hintTypeface
            binding.etFirstName.typeface = editTextTypeface
            binding.etLastName.typeface = editTextTypeface
            binding.etLEmailName.typeface = editTextTypeface
            binding.etPassword.typeface = editTextTypeface*/


            tlFirstName.addOnEditTextAttachedListener {
                formatHint("First Name*",tlFirstName,etFirstName.isFocused)
            }

            tlLastName.addOnEditTextAttachedListener {
                formatHint("Last Name*",tlLastName,etFirstName.isFocused)
            }
            tlEmailName.addOnEditTextAttachedListener {
                formatHint("Email Address*",tlEmailName,etLEmailName.isFocused)
            }
            tlPassword.addOnEditTextAttachedListener {
                formatHint("Password*",tlPassword,etPassword.isFocused)
            }

            etFirstName.setOnFocusChangeListener { _, hasFocus ->
                formatHint("First Name*",tlFirstName,hasFocus)
            }

            etLastName.setOnFocusChangeListener { _, hasFocus ->
                formatHint("Last Name*",tlLastName,hasFocus)
            }

            etLEmailName.setOnFocusChangeListener { _, hasFocus ->
                formatHint("Email Address*",tlEmailName,hasFocus)
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
                    is SignUpResponse -> {
                        val res = value.data.body
                        savePreference("firstName",res?.first_name ?: "")
                        savePreference("lastName",res?.last_name ?: "")
                        savePreference("token",res?.access_token ?: "")
                        savePreference("email",res?.email ?: "")

                        startActivity(Intent(this@SignUpActivity, VerificationActivity::class.java))
                    }

                    is CommonResponse -> {
                        val msg = value.data.message ?: ""
                        if (msg == "Refer code not found !"){
                            showCustomSnackbar(this,binding.root, msg)
                            return
                        }
                        val map = HashMap<String,String>()
                        map["first_name"] = binding.etFirstName.text.toString().trim()
                        map["last_name"] = binding.etLastName.text.toString().trim()
                        map["email"] = binding.etLEmailName.text.toString().trim()
                        map["password"] = binding.etPassword.text.toString().trim()
                        map["device_type"] = "1"
                        map["referral_code"] = binding.etReferralCode.text.toString().trim()
                        map["device_token"] = deviceToken

                        viewModel.signUp(map,this@SignUpActivity).observe(this@SignUpActivity,this@SignUpActivity)
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

    private fun setSpannableText(text:TextView){
        val termsAndConditionsText = "Terms & Conditions"
        val privacyPolicyText = "Privacy Policy"
        val privacyPolicyStar = "*"
        val fullText = "By signing up, you agree to our $termsAndConditionsText and $privacyPolicyText.$privacyPolicyStar"

        val spannableString = SpannableString(fullText)

        val termsClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@SignUpActivity,ContentActivity::class.java).apply {
                    putExtra("type",0)
                })
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@SignUpActivity, R.color.black)
               // ds.textSize = 40f
                val typeface = ResourcesCompat.getFont(this@SignUpActivity, R.font.poppins_semibold)
                if (typeface != null) {
                    ds.typeface = typeface
                }
                ds.isUnderlineText = false

            }
        }
        spannableString.setSpan(termsClickableSpan, fullText.indexOf(termsAndConditionsText), fullText.indexOf(termsAndConditionsText) + termsAndConditionsText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        val privacyClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@SignUpActivity,ContentActivity::class.java).apply {
                    putExtra("type",1)
                })
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@SignUpActivity, R.color.black)
               // ds.textSize = 40f
                val typeface = ResourcesCompat.getFont(this@SignUpActivity, R.font.poppins_semibold)
                if (typeface != null) {
                    ds.typeface = typeface
                }
                ds.isUnderlineText = false
            }
        }
        spannableString.setSpan(privacyClickableSpan, fullText.indexOf(privacyPolicyText), fullText.indexOf(privacyPolicyText) + privacyPolicyText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)


        val privacyStar = object : ClickableSpan() {
            override fun onClick(widget: View) {
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@SignUpActivity, R.color.star_red_color)
                // ds.textSize = 40f
                val typeface = ResourcesCompat.getFont(this@SignUpActivity, R.font.poppins)
                if (typeface != null) {
                    ds.typeface = typeface
                }
                ds.isUnderlineText = false
            }
        }
        spannableString.setSpan(privacyStar, fullText.indexOf(privacyPolicyStar), fullText.indexOf(privacyPolicyStar) + privacyPolicyStar.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        text.text = spannableString
        text.movementMethod = LinkMovementMethod.getInstance()

    }


    private fun formatHint(text: String,view:TextInputLayout,focus:Boolean) {
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