package com.live.azurah.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextWatcher
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputLayout
import com.live.azurah.R
import com.live.azurah.databinding.ActivityDeleteAccountBinding
import com.live.azurah.model.CommonResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.clearPreferences
import com.live.azurah.util.getPreference
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject

@AndroidEntryPoint
class DeleteAccountActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivityDeleteAccountBinding
    private var privacy = false
    private var userExperience = false
    private var manyNotifications = false
    private var technicalIssue = false
    private var contentNotUseful = false
    private var other = false
    private var reason = ""
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
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
        with(binding) {
            setCustomFont(
                "Privacy Concerns: Concerns about how your data is being used.",
                "Privacy Concerns:",
                binding.tvBooked
            )
            setCustomFont(
                "User Experience Issues: Difficulty navigating or using the app.",
                "User Experience Issues:",
                binding.tvExpert
            )
            setCustomFont(
                "Too Many Notifications: Receiving too many notifications that you can’t manage.",
                "Too Many Notifications:",
                binding.tvVacation
            )
            setCustomFont(
                "Technical Issues: Experiencing bugs, crashes, or other technical problems.",
                "Technical Issues:",
                binding.tvTechnical
            )
            setCustomFont(
                "Content Not Useful or Relevant: The content is not interesting or relevant to you.",
                "Content Not Useful or Relevant:",
                binding.tvContent
            )
            setCustomFont("Other (please specify):", "Other (please specify):", binding.tvOther)
            formatHintHeading("Please select a reason for deleting your account:*", tvText)

            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            btnContinue.setOnClickListener {
                if (other) {
                    reason = binding.etPostDesc.text.toString().trim()
                }
                val map = HashMap<String, Any>()
                map["user_id"] = getPreference("id", "")
                /*  map["delete_reason"] = reason
                  map["password"] = binding.etPassword.text.toString().trim()*/
                /* viewModel.deleteAccount(getPreference("id", ""), map, this@DeleteAccountActivity)
                     .observe(this@DeleteAccountActivity, this@DeleteAccountActivity)
 */
                viewModel.sendDeleteAccount(map, this@DeleteAccountActivity)
                    .observe(this@DeleteAccountActivity, this@DeleteAccountActivity)
            }

            tlPassword.addOnEditTextAttachedListener {
                formatHint("Password*", tlPassword, etPassword.isFocused)
            }


            etPassword.setOnFocusChangeListener { _, hasFocus ->
                formatHint("Password*", tlPassword, hasFocus)
            }

            tlReason.addOnEditTextAttachedListener {
                formatHint("Reason*", tlReason, etPostDesc.isFocused)
            }

            etPostDesc.setOnFocusChangeListener { _, hasFocus ->
                formatHint("Reason*", tlReason, hasFocus)
            }

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
                            .isNotEmpty() && (privacy || userExperience || manyNotifications || technicalIssue || contentNotUseful || other)
                    ) {
                        binding.btnContinue.backgroundTintList = ActivityCompat.getColorStateList(
                            this@DeleteAccountActivity,
                            R.color.blue
                        )
                        binding.btnContinue.isEnabled = true
                    } else {
                        binding.btnContinue.backgroundTintList = ActivityCompat.getColorStateList(
                            this@DeleteAccountActivity,
                            R.color.button_grey
                        )
                        binding.btnContinue.isEnabled = false
                    }

                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

            etPostDesc.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    if (s.toString().trim().isNotEmpty()) {
                        if (s.toString().isNotEmpty()) {
                            tvCharacters.text = buildString {
                                append(setCharacters(s.toString().length))
                                append(" characters")
                            }
                        }
                    } else {
                        tvCharacters.text = buildString {
                            append("150 characters")
                        }
                    }
                    if (binding.etPassword.text.toString().trim()
                            .isNotEmpty() && (privacy || userExperience || manyNotifications || technicalIssue || contentNotUseful || other)
                    ) {
                        if (other) {
                            if (binding.etPostDesc.text.toString().trim().isNotEmpty()) {
                                binding.btnContinue.backgroundTintList =
                                    ActivityCompat.getColorStateList(
                                        this@DeleteAccountActivity,
                                        R.color.blue
                                    )
                                binding.btnContinue.isEnabled = true
                            } else {
                                binding.btnContinue.backgroundTintList =
                                    ActivityCompat.getColorStateList(
                                        this@DeleteAccountActivity,
                                        R.color.button_grey
                                    )
                                binding.btnContinue.isEnabled = false
                            }
                        } else {
                            binding.btnContinue.backgroundTintList =
                                ActivityCompat.getColorStateList(
                                    this@DeleteAccountActivity,
                                    R.color.blue
                                )
                            binding.btnContinue.isEnabled = true
                        }
                    } else {
                        binding.btnContinue.backgroundTintList = ActivityCompat.getColorStateList(
                            this@DeleteAccountActivity,
                            R.color.button_grey
                        )
                        binding.btnContinue.isEnabled = false
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

            llNoLonger.setOnClickListener {
                privacy = true
                userExperience = false
                manyNotifications = false
                technicalIssue = false
                contentNotUseful = false
                other = false
                reason = tvBooked.text.toString()
                ivNoLoger.setImageResource(R.drawable.selected_radio_prayer)
                ivNoAlter.setImageResource(R.drawable.unselected_radio_prayer)
                ivIssue.setImageResource(R.drawable.unselected_radio_prayer)
                ivContent.setImageResource(R.drawable.unselected_radio_prayer)
                ivTechnicalIssue.setImageResource(R.drawable.unselected_radio_prayer)
                ivOther.setImageResource(R.drawable.unselected_radio_prayer)
                clOtherReason.visibility = View.GONE
                if (binding.etPassword.text.toString().trim()
                        .isNotEmpty() && (privacy || userExperience || manyNotifications || technicalIssue || contentNotUseful || other)
                ) {
                    if (other) {
                        if (binding.etPostDesc.text.toString().trim().isNotEmpty()) {
                            binding.btnContinue.backgroundTintList =
                                ActivityCompat.getColorStateList(
                                    this@DeleteAccountActivity,
                                    R.color.blue
                                )
                            binding.btnContinue.isEnabled = true
                        } else {
                            binding.btnContinue.backgroundTintList =
                                ActivityCompat.getColorStateList(
                                    this@DeleteAccountActivity,
                                    R.color.button_grey
                                )
                            binding.btnContinue.isEnabled = false
                        }
                    } else {
                        binding.btnContinue.backgroundTintList = ActivityCompat.getColorStateList(
                            this@DeleteAccountActivity,
                            R.color.blue
                        )
                        binding.btnContinue.isEnabled = true
                    }
                } else {
                    binding.btnContinue.backgroundTintList = ActivityCompat.getColorStateList(
                        this@DeleteAccountActivity,
                        R.color.button_grey
                    )
                    binding.btnContinue.isEnabled = false
                }
            }

            llAlternative.setOnClickListener {
                privacy = false
                userExperience = true
                manyNotifications = false
                technicalIssue = false
                contentNotUseful = false
                other = false
                reason = tvExpert.text.toString()
                ivNoLoger.setImageResource(R.drawable.unselected_radio_prayer)
                ivNoAlter.setImageResource(R.drawable.selected_radio_prayer)
                ivIssue.setImageResource(R.drawable.unselected_radio_prayer)
                ivContent.setImageResource(R.drawable.unselected_radio_prayer)
                ivTechnicalIssue.setImageResource(R.drawable.unselected_radio_prayer)
                ivOther.setImageResource(R.drawable.unselected_radio_prayer)
                clOtherReason.visibility = View.GONE
                if (binding.etPassword.text.toString().trim()
                        .isNotEmpty() && (privacy || userExperience || manyNotifications || technicalIssue || contentNotUseful || other)
                ) {
                    if (other) {
                        if (binding.etPostDesc.text.toString().trim().isNotEmpty()) {
                            binding.btnContinue.backgroundTintList =
                                ActivityCompat.getColorStateList(
                                    this@DeleteAccountActivity,
                                    R.color.blue
                                )
                            binding.btnContinue.isEnabled = true
                        } else {
                            binding.btnContinue.backgroundTintList =
                                ActivityCompat.getColorStateList(
                                    this@DeleteAccountActivity,
                                    R.color.button_grey
                                )
                            binding.btnContinue.isEnabled = false
                        }
                    } else {
                        binding.btnContinue.backgroundTintList = ActivityCompat.getColorStateList(
                            this@DeleteAccountActivity,
                            R.color.blue
                        )
                        binding.btnContinue.isEnabled = true
                    }
                } else {
                    binding.btnContinue.backgroundTintList = ActivityCompat.getColorStateList(
                        this@DeleteAccountActivity,
                        R.color.button_grey
                    )
                    binding.btnContinue.isEnabled = false
                }

            }

            llIssues.setOnClickListener {
                privacy = false
                userExperience = false
                manyNotifications = true
                technicalIssue = false
                contentNotUseful = false
                other = false
                reason = tvVacation.text.toString()
                ivNoLoger.setImageResource(R.drawable.unselected_radio_prayer)
                ivNoAlter.setImageResource(R.drawable.unselected_radio_prayer)
                ivIssue.setImageResource(R.drawable.selected_radio_prayer)
                ivTechnicalIssue.setImageResource(R.drawable.unselected_radio_prayer)
                ivContent.setImageResource(R.drawable.unselected_radio_prayer)
                ivOther.setImageResource(R.drawable.unselected_radio_prayer)
                clOtherReason.visibility = View.GONE

                if (binding.etPassword.text.toString().trim()
                        .isNotEmpty() && (privacy || userExperience || manyNotifications || technicalIssue || contentNotUseful || other)
                ) {
                    if (other) {
                        if (binding.etPostDesc.text.toString().trim().isNotEmpty()) {
                            binding.btnContinue.backgroundTintList =
                                ActivityCompat.getColorStateList(
                                    this@DeleteAccountActivity,
                                    R.color.blue
                                )
                            binding.btnContinue.isEnabled = true
                        } else {
                            binding.btnContinue.backgroundTintList =
                                ActivityCompat.getColorStateList(
                                    this@DeleteAccountActivity,
                                    R.color.button_grey
                                )
                            binding.btnContinue.isEnabled = false
                        }
                    } else {
                        binding.btnContinue.backgroundTintList = ActivityCompat.getColorStateList(
                            this@DeleteAccountActivity,
                            R.color.blue
                        )
                        binding.btnContinue.isEnabled = true
                    }
                } else {
                    binding.btnContinue.backgroundTintList = ActivityCompat.getColorStateList(
                        this@DeleteAccountActivity,
                        R.color.button_grey
                    )
                    binding.btnContinue.isEnabled = false
                }

            }

            llTechnicalIssues.setOnClickListener {
                privacy = false
                userExperience = false
                manyNotifications = false
                technicalIssue = true
                contentNotUseful = false
                other = false
                reason = tvTechnical.text.toString()

                ivNoLoger.setImageResource(R.drawable.unselected_radio_prayer)
                ivNoAlter.setImageResource(R.drawable.unselected_radio_prayer)
                ivIssue.setImageResource(R.drawable.unselected_radio_prayer)
                ivTechnicalIssue.setImageResource(R.drawable.selected_radio_prayer)
                ivContent.setImageResource(R.drawable.unselected_radio_prayer)
                ivOther.setImageResource(R.drawable.unselected_radio_prayer)
                clOtherReason.visibility = View.GONE

                if (binding.etPassword.text.toString().trim()
                        .isNotEmpty() && (privacy || userExperience || manyNotifications || technicalIssue || contentNotUseful || other)
                ) {
                    if (other) {
                        if (binding.etPostDesc.text.toString().trim().isNotEmpty()) {
                            binding.btnContinue.backgroundTintList =
                                ActivityCompat.getColorStateList(
                                    this@DeleteAccountActivity,
                                    R.color.blue
                                )
                            binding.btnContinue.isEnabled = true
                        } else {
                            binding.btnContinue.backgroundTintList =
                                ActivityCompat.getColorStateList(
                                    this@DeleteAccountActivity,
                                    R.color.button_grey
                                )
                            binding.btnContinue.isEnabled = false
                        }
                    } else {
                        binding.btnContinue.backgroundTintList = ActivityCompat.getColorStateList(
                            this@DeleteAccountActivity,
                            R.color.blue
                        )
                        binding.btnContinue.isEnabled = true
                    }
                } else {
                    binding.btnContinue.backgroundTintList = ActivityCompat.getColorStateList(
                        this@DeleteAccountActivity,
                        R.color.button_grey
                    )
                    binding.btnContinue.isEnabled = false
                }

            }

            llContent.setOnClickListener {
                privacy = false
                userExperience = false
                manyNotifications = false
                technicalIssue = false
                contentNotUseful = true
                other = false
                reason = tvContent.text.toString()

                ivNoLoger.setImageResource(R.drawable.unselected_radio_prayer)
                ivNoAlter.setImageResource(R.drawable.unselected_radio_prayer)
                ivIssue.setImageResource(R.drawable.unselected_radio_prayer)
                ivContent.setImageResource(R.drawable.selected_radio_prayer)
                ivTechnicalIssue.setImageResource(R.drawable.unselected_radio_prayer)
                ivOther.setImageResource(R.drawable.unselected_radio_prayer)
                clOtherReason.visibility = View.GONE

                if (binding.etPassword.text.toString().trim()
                        .isNotEmpty() && (privacy || userExperience || manyNotifications || technicalIssue || contentNotUseful || other)
                ) {
                    if (other) {
                        if (binding.etPostDesc.text.toString().trim().isNotEmpty()) {
                            binding.btnContinue.backgroundTintList =
                                ActivityCompat.getColorStateList(
                                    this@DeleteAccountActivity,
                                    R.color.blue
                                )
                            binding.btnContinue.isEnabled = true
                        } else {
                            binding.btnContinue.backgroundTintList =
                                ActivityCompat.getColorStateList(
                                    this@DeleteAccountActivity,
                                    R.color.button_grey
                                )
                            binding.btnContinue.isEnabled = false
                        }
                    } else {
                        binding.btnContinue.backgroundTintList = ActivityCompat.getColorStateList(
                            this@DeleteAccountActivity,
                            R.color.blue
                        )
                        binding.btnContinue.isEnabled = true
                    }
                } else {
                    binding.btnContinue.backgroundTintList = ActivityCompat.getColorStateList(
                        this@DeleteAccountActivity,
                        R.color.button_grey
                    )
                    binding.btnContinue.isEnabled = false
                }

            }

            llOther.setOnClickListener {
                privacy = false
                userExperience = false
                manyNotifications = true
                technicalIssue = false
                contentNotUseful = false
                other = true
                ivNoLoger.setImageResource(R.drawable.unselected_radio_prayer)
                ivNoAlter.setImageResource(R.drawable.unselected_radio_prayer)
                ivIssue.setImageResource(R.drawable.unselected_radio_prayer)
                ivTechnicalIssue.setImageResource(R.drawable.unselected_radio_prayer)
                ivContent.setImageResource(R.drawable.unselected_radio_prayer)
                ivOther.setImageResource(R.drawable.selected_radio_prayer)
                clOtherReason.visibility = View.VISIBLE

                if (binding.etPassword.text.toString().trim()
                        .isNotEmpty() && (privacy || userExperience || manyNotifications || technicalIssue || contentNotUseful || other)
                ) {
                    if (other) {
                        if (binding.etPostDesc.text.toString().trim().isNotEmpty()) {
                            binding.btnContinue.backgroundTintList =
                                ActivityCompat.getColorStateList(
                                    this@DeleteAccountActivity,
                                    R.color.blue
                                )
                            binding.btnContinue.isEnabled = true
                        } else {
                            binding.btnContinue.backgroundTintList =
                                ActivityCompat.getColorStateList(
                                    this@DeleteAccountActivity,
                                    R.color.button_grey
                                )
                            binding.btnContinue.isEnabled = false
                        }
                    } else {
                        binding.btnContinue.backgroundTintList = ActivityCompat.getColorStateList(
                            this@DeleteAccountActivity,
                            R.color.blue
                        )
                        binding.btnContinue.isEnabled = true
                    }
                } else {
                    binding.btnContinue.backgroundTintList = ActivityCompat.getColorStateList(
                        this@DeleteAccountActivity,
                        R.color.button_grey
                    )
                    binding.btnContinue.isEnabled = false
                }

            }
        }
    }

    private fun formatHint(text: String, view: TextInputLayout, focus: Boolean) {
        view.hint = ""
        val asteriskIndex = text.indexOf("*")

        Log.d("dfdgd", focus.toString())
        val spannableString = SpannableString(text)
        spannableString.setSpan(
            ForegroundColorSpan(
                if (focus) ContextCompat.getColor(
                    this,
                    R.color.blue
                ) else ContextCompat.getColor(this, R.color.light_black)
            ),
            0,
            asteriskIndex,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.star_red_color)),
            asteriskIndex,
            asteriskIndex + 1,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        view.hint = spannableString
    }

    private fun formatHintHeading(text: String, view: TextView) {
        view.hint = ""
        val asteriskIndex = text.indexOf("*")

        val spannableString = SpannableString(text)
        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.black)),
            0,
            asteriskIndex,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.star_red_color)),
            asteriskIndex,
            asteriskIndex + 1,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        view.text = spannableString
    }

    private fun setCharacters(length: Int): String {
        return (150 - length).toString()
    }

    private fun setCustomFont(fullText: String, targetText: String, view: TextView) {

        val spannableString = SpannableString(fullText)
        val termsClickableSpan = object : ClickableSpan() {

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@DeleteAccountActivity, R.color.black)
                val typeface =
                    ResourcesCompat.getFont(this@DeleteAccountActivity, R.font.poppins_semibold)
                if (typeface != null) {
                    ds.typeface = typeface
                }
                ds.isUnderlineText = false
            }

            override fun onClick(widget: View) {

            }
        }
        spannableString.setSpan(
            termsClickableSpan,
            0,
            targetText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        view.text = spannableString
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is CommonResponse -> {
                        startActivity(
                            Intent(
                                this@DeleteAccountActivity,
                                VerificationActivity::class.java
                            ).apply {
                                putExtra("from", 3)
                                putExtra("email", getPreference("email", ""))
                                putExtra("reason", reason)
                                putExtra("password", binding.etPassword.text.toString().trim())
                            }
                        )


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