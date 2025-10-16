package com.live.azurah.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
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
import com.live.azurah.databinding.ActivityReportPostBinding
import com.live.azurah.model.CommonResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.countWords
import com.live.azurah.util.getPreference
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.trimToWordLimit
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReportUserActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivityReportPostBinding
    private var from = ""
    var isSelected = false
    var id = ""
    var description = ""
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportPostBinding.inflate(layoutInflater)
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
        id = intent.getStringExtra("id") ?: ""

        initListener()
      //  setCustomFont("Please select the reason for reporting this user and provide any additional details to help us understand your concern.",binding.tvMessage)
    }
    private fun initListener() {
        with(binding){
            etPassword.setText(buildString {
                append("@")
                append(intent.getStringExtra("username"))
            })
            if (from == "postComment"){
                tvCreateUsername.text = "Report Comment Form"
            }else{
                tvCreateUsername.text = "Report User Form"
            }
            btnSave.setOnClickListener {
                val map = HashMap<String,String>()
                map["reported_by"] = getPreference("id","")
                map["description"] = description
                map["additional_description"] = etPostDesc.text.toString().trim()
                if (from == "postComment"){
                    map["post_id"] = intent.getStringExtra("postId").toString()
                    map["post_comment_id"] = intent.getStringExtra("id").toString()
                    map["reported_to"] = intent.getStringExtra("reportedTo").toString()
                    viewModel.postCommentReport(map,this@ReportUserActivity).observe(this@ReportUserActivity,this@ReportUserActivity)
                }else{
                    map["reported_to"] = id
                    viewModel.userReport(map,this@ReportUserActivity).observe(this@ReportUserActivity,this@ReportUserActivity)
                }
            }
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            btnCancel.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            val hintTypeface = ResourcesCompat.getFont(this@ReportUserActivity,R.font.poppins_medium)
            val editTextTypeface = ResourcesCompat.getFont(this@ReportUserActivity,R.font.poppins)

            binding.tlPassword.typeface = hintTypeface
            binding.etPassword.typeface = editTextTypeface
            binding.etPostDesc.typeface = editTextTypeface

            llNoLonger.setOnClickListener {
                isSelected = true
                ivNoLoger.setImageResource(R.drawable.selected_radio_prayer)
                ivNoAlter.setImageResource(R.drawable.unselected_radio_prayer)
                ivIssue.setImageResource(R.drawable.unselected_radio_prayer)
                ivTechnicalIssue.setImageResource(R.drawable.unselected_radio_prayer)
                ivContent.setImageResource(R.drawable.unselected_radio_prayer)
                ivViolence.setImageResource(R.drawable.unselected_radio_prayer)
                ivOther.setImageResource(R.drawable.unselected_radio_prayer)
                description = tvBooked.text.toString()

                enableButton()
            }

            llAlternative.setOnClickListener {
                isSelected = true
                ivNoLoger.setImageResource(R.drawable.unselected_radio_prayer)
                ivNoAlter.setImageResource(R.drawable.selected_radio_prayer)
                ivIssue.setImageResource(R.drawable.unselected_radio_prayer)
                ivTechnicalIssue.setImageResource(R.drawable.unselected_radio_prayer)
                ivContent.setImageResource(R.drawable.unselected_radio_prayer)
                ivViolence.setImageResource(R.drawable.unselected_radio_prayer)
                ivOther.setImageResource(R.drawable.unselected_radio_prayer)
                description = tvExpert.text.toString()

                enableButton()
            }

            llIssues.setOnClickListener {
                isSelected = true
                ivNoLoger.setImageResource(R.drawable.unselected_radio_prayer)
                ivNoAlter.setImageResource(R.drawable.unselected_radio_prayer)
                ivIssue.setImageResource(R.drawable.selected_radio_prayer)
                ivTechnicalIssue.setImageResource(R.drawable.unselected_radio_prayer)
                ivContent.setImageResource(R.drawable.unselected_radio_prayer)
                ivViolence.setImageResource(R.drawable.unselected_radio_prayer)
                ivOther.setImageResource(R.drawable.unselected_radio_prayer)
                description = tvVacation.text.toString()
                enableButton()
            }

            llTechnicalIssues.setOnClickListener {
                isSelected = true
                ivNoLoger.setImageResource(R.drawable.unselected_radio_prayer)
                ivNoAlter.setImageResource(R.drawable.unselected_radio_prayer)
                ivIssue.setImageResource(R.drawable.unselected_radio_prayer)
                ivTechnicalIssue.setImageResource(R.drawable.selected_radio_prayer)
                ivContent.setImageResource(R.drawable.unselected_radio_prayer)
                ivViolence.setImageResource(R.drawable.unselected_radio_prayer)
                ivOther.setImageResource(R.drawable.unselected_radio_prayer)
                description = tvTechnical.text.toString()
                enableButton()
            }

            llContent.setOnClickListener {
                isSelected = true
                ivNoLoger.setImageResource(R.drawable.unselected_radio_prayer)
                ivNoAlter.setImageResource(R.drawable.unselected_radio_prayer)
                ivIssue.setImageResource(R.drawable.unselected_radio_prayer)
                ivTechnicalIssue.setImageResource(R.drawable.unselected_radio_prayer)
                ivContent.setImageResource(R.drawable.selected_radio_prayer)
                ivViolence.setImageResource(R.drawable.unselected_radio_prayer)
                ivOther.setImageResource(R.drawable.unselected_radio_prayer)
                description = tvContent.text.toString()
                enableButton()
            }

            llViolence.setOnClickListener {
                isSelected = true
                ivNoLoger.setImageResource(R.drawable.unselected_radio_prayer)
                ivNoAlter.setImageResource(R.drawable.unselected_radio_prayer)
                ivIssue.setImageResource(R.drawable.unselected_radio_prayer)
                ivTechnicalIssue.setImageResource(R.drawable.unselected_radio_prayer)
                ivContent.setImageResource(R.drawable.unselected_radio_prayer)
                ivViolence.setImageResource(R.drawable.selected_radio_prayer)
                ivOther.setImageResource(R.drawable.unselected_radio_prayer)
                description = tvViolence.text.toString()

                enableButton()
            }

            llOther.setOnClickListener {
                isSelected = true
                ivNoLoger.setImageResource(R.drawable.unselected_radio_prayer)
                ivNoAlter.setImageResource(R.drawable.unselected_radio_prayer)
                ivIssue.setImageResource(R.drawable.unselected_radio_prayer)
                ivTechnicalIssue.setImageResource(R.drawable.unselected_radio_prayer)
                ivContent.setImageResource(R.drawable.unselected_radio_prayer)
                ivViolence.setImageResource(R.drawable.unselected_radio_prayer)
                ivOther.setImageResource(R.drawable.selected_radio_prayer)
                description = tvOther.text.toString()
                enableButton()

            }

            tlPassword.addOnEditTextAttachedListener {
                formatHint("Username*",tlPassword,etPassword.isFocused)
            }

            etPassword.setOnFocusChangeListener { _, hasFocus ->
                formatHint("Username*",tlPassword,hasFocus)
            }

            etPostDesc.addTextChangedListener(object :TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().isNotEmpty() && isSelected){
                        btnSave.backgroundTintList = ActivityCompat.getColorStateList(this@ReportUserActivity,R.color.yes_color)
                        btnSave.isEnabled = true
                    }else{
                        btnSave.backgroundTintList = ActivityCompat.getColorStateList(this@ReportUserActivity,R.color.button_grey)
                        btnSave.isEnabled = false
                    }

                    if (s.toString().trim().isNotEmpty()){
                        val wordCount = countWords(s.toString())
                        tvWords.text = (150 - wordCount).toString()+ " words"
                        if (wordCount > 150){
                            val trimmedText = trimToWordLimit(s.toString(), 150)
                            binding.etPostDesc.setText(trimmedText)
                            binding.etPostDesc.setSelection(trimmedText.length)
                        }
                    }else{
                        tvWords.text = "150 words"
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })
        }
    }

   /* private fun setCustomFont(fullText:String,targetText:String,view: TextView){

        val spannableString = SpannableString(fullText)
        val termsClickableSpan = object : ClickableSpan() {

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@ReportUserActivity, R.color.black)
                val typeface = ResourcesCompat.getFont(this@ReportUserActivity, R.font.poppins_semibold)
                if (typeface != null) {
                    ds.typeface = typeface
                }
                ds.isUnderlineText = false
            }

            override fun onClick(widget: View) {

            }
        }
        spannableString.setSpan(termsClickableSpan, 0, targetText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        view.text = spannableString
    }*/

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

    private fun enableButton(){
        with(binding){
            if (etPostDesc.text.toString().isNotEmpty() && isSelected){
                btnSave.backgroundTintList = ActivityCompat.getColorStateList(this@ReportUserActivity,R.color.yes_color)
                btnSave.isEnabled = true
            }else{
                btnSave.backgroundTintList = ActivityCompat.getColorStateList(this@ReportUserActivity,R.color.button_grey)
                btnSave.isEnabled = false
            }
        }

    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is CommonResponse -> {
                        val res = value.data.body
                        startActivity(Intent(this,MessageSentActivity::class.java).apply {
                            putExtra("from","1")
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
                showCustomSnackbar(this,binding.root, value.message.toString())
            }
        }
    }

}