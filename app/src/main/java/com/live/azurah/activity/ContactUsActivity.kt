package com.live.azurah.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.PopupWindow
import android.widget.RelativeLayout
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
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.live.azurah.R
import com.live.azurah.adapter.FeedbackPosAdapter
import com.live.azurah.databinding.ActivityContactUsBinding
import com.live.azurah.databinding.ItemFeedbackBinding
import com.live.azurah.databinding.MenuReportBinding
import com.live.azurah.model.FileUploadResponse
import com.live.azurah.model.ReportFeedback
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.containsBannedWord
import com.live.azurah.util.set30Characters
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactUsActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivityContactUsBinding
    private var positive = ArrayList<String>()
    private var negative = ArrayList<String>()
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var type = 0
    private var categoryDesc = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactUsBinding.inflate(layoutInflater)
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

        binding.backIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        formatFeedbackHint("Select a Category*", binding.tvCategory)

        binding.btnSendMessage.setOnClickListener {

            if (containsBannedWord(binding.etTitle.text.toString().trim())) {
                showCustomSnackbar(
                    this,
                    it,
                    "Your post contains banned or inappropriate words. Please remove them before posting."
                )
                return@setOnClickListener
            }
            if (containsBannedWord(binding.etDesc.text.toString().trim())) {
                showCustomSnackbar(
                    this,
                    it,
                    "Your post contains banned or inappropriate words. Please remove them before posting."
                )
                return@setOnClickListener
            }

            val map = HashMap<String, String>()
            map["title"] = binding.etTitle.text.toString().trim()
            map["description"] = binding.etDesc.text.toString().trim()
            map["category"] = categoryDesc
            /* if (type == 1) {
                 map["category"] = "Positive"
             }else{
                 map["category"] = "Negative"
             }*/
            viewModel.contactUs(map, this).observe(this, this)

        }
        val hintTypeface = ResourcesCompat.getFont(this, R.font.poppins_medium)
        val editTextTypeface = ResourcesCompat.getFont(this, R.font.poppins)

        binding.tlTitle.typeface = hintTypeface
        binding.etTitle.typeface = editTextTypeface

        binding.tlPositive.typeface = hintTypeface
        binding.etPositive.typeface = editTextTypeface

        binding.tlPostDes.typeface = hintTypeface
        binding.etDesc.typeface = editTextTypeface


        positive.addAll(
            listOf(
                "User-friendly interface",
                "Helpful content",
                "Engaging community interaction",
                "Quick and responsive customer support",
                "Quality of features and tools",
                "Regular updates and improvements",
                "Other (please specify)"
            )
        )

        negative.addAll(
            listOf(
                "Performance issues (e.g. slow loading times)",
                "Confusing navigation",
                "Lack of certain features",
                "Design inconsistencies",
                "Accessibility concerns",
                "Bugs or technical glitches",
                "Other (please specify)"
            )
        )

        intiListener()


    }

    private fun intiListener() {
        with(binding) {
            //  formatFeedbackHint("Feedback*",tvFeedBack)

            tlTitle.addOnEditTextAttachedListener {
                formatHint("Title*", tlTitle, etTitle.isFocused)
            }

            tlPositive.addOnEditTextAttachedListener {
                formatHint("Select an Option*", tlPositive, etPositive.isFocused)
            }

            llPositive.setOnClickListener {
                ivPositive.setImageResource(R.drawable.selected_radio_prayer)
                ivNegative.setImageResource(R.drawable.unselected_radio_prayer)
                type = 1
                etPositive.setText("")
            }

            llNegative.setOnClickListener {
                ivNegative.setImageResource(R.drawable.selected_radio_prayer)
                ivPositive.setImageResource(R.drawable.unselected_radio_prayer)
                type = 2
                etPositive.setText("")

            }


            etPositive.setOnClickListener {
                if (type == 0) {
                    showCustomSnackbar(this@ContactUsActivity, it, "Please select a category.")
                    return@setOnClickListener
                }
                setPopUpWindow(it, type)
            }


            etTitle.setOnFocusChangeListener { _, hasFocus ->
                formatHint("Title*", tlTitle, hasFocus)
            }

            tlPostDes.addOnEditTextAttachedListener {
                formatHint("Description*", tlPostDes, etDesc.isFocused)
            }

            etDesc.setOnFocusChangeListener { _, hasFocus ->
                formatHint("Description*", tlPostDes, hasFocus)
            }
            binding.etTitle.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().trim().isNotEmpty() && binding.etDesc.text.toString().trim()
                            .isNotEmpty()
                    ) {
                        binding.btnSendMessage.backgroundTintList = getColorStateList(R.color.blue)
                        binding.btnSendMessage.isEnabled = true
                    } else {
                        binding.btnSendMessage.backgroundTintList =
                            getColorStateList(R.color.button_grey)
                        binding.btnSendMessage.isEnabled = false
                    }

                    if (s.toString().trim().isNotEmpty()) {
                        if (s.toString().isNotEmpty()) {
                            tvCount.text = buildString {
                                append(set30Characters(s.toString().length))
                                append(" characters")
                            }
                        }
                    } else {
                        tvCount.text = buildString {
                            append("30 characters")
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })
            binding.etDesc.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().trim().isNotEmpty() && binding.etTitle.text.toString().trim()
                            .isNotEmpty()
                    ) {
                        binding.btnSendMessage.backgroundTintList = getColorStateList(R.color.blue)
                        binding.btnSendMessage.isEnabled = true
                    } else {
                        binding.btnSendMessage.backgroundTintList =
                            getColorStateList(R.color.button_grey)
                        binding.btnSendMessage.isEnabled = false
                    }

                    if (s.toString().trim().isNotEmpty()) {
                        if (s.toString().isNotEmpty()) {
                            tvCount.text = setCharacters(s.toString().length) + " characters"
                        }
                    } else {
                        tvCount.text = "300 characters"
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })
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


    private fun formatFeedbackHint(text: String, view: TextView) {
        val spannableString = SpannableString(text)
        val redSpan = ForegroundColorSpan(Color.RED)
        spannableString.setSpan(
            redSpan,
            text.indexOf('*'),
            text.indexOf('*') + 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val blueSpan = ForegroundColorSpan(ContextCompat.getColor(this, R.color.light_black))
        spannableString.setSpan(
            blueSpan,
            0,
            text.indexOf('*'),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        view.text = spannableString
    }

    private fun setCharacters(length: Int): String {
        return (300 - length).toString()
    }

    private fun setPopUpWindow(view1: View, type: Int) {
        val view = ItemFeedbackBinding.inflate(layoutInflater)
        val myPopupWindow = PopupWindow(
            view.root,
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        with(view) {
            if (type == 1) {
                val adapter = FeedbackPosAdapter(this@ContactUsActivity, positive)
                view.rvListing.adapter = adapter

                adapter.clickListener = { name ->
                    binding.etPositive.setText(name)
                    categoryDesc = name
                    myPopupWindow.dismiss()
                }

            } else {
                val adapter = FeedbackPosAdapter(this@ContactUsActivity, negative)
                view.rvListing.adapter = adapter

                adapter.clickListener = { name ->
                    binding.etPositive.setText(name)
                    categoryDesc = name
                    myPopupWindow.dismiss()
                }
            }
        }
        myPopupWindow.showAsDropDown(view1, 0, 0)
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
                when (value.data) {
                    is ReportFeedback -> {
                        startActivity(Intent(this, MessageSentActivity::class.java).apply {
                            putExtra("from", "2")
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


}