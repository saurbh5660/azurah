package com.live.azurah.activity


import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
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
import com.live.azurah.adapter.CategoryAdapter
import com.live.azurah.databinding.ActivityAddPrayerRequestTestimoneyBinding
import com.live.azurah.model.CategoryModel
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.CommunityCategoryResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.containsBannedWord
import com.live.azurah.util.set30Characters
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddPrayerRequestTestimonies : AppCompatActivity(),Observer<Resource<Any>> {
    private lateinit var binding: ActivityAddPrayerRequestTestimoneyBinding
    private val catList = ArrayList<CategoryModel>()
    //   private val loaderDialog by lazy { LoaderDialog(this) }
    private val viewModel by viewModels<CommonViewModel>()
    private var categoryAdapter: CategoryAdapter? = null
    private var selectedCategory = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPrayerRequestTestimoneyBinding.inflate(layoutInflater)
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
        setCatAdapter()
        getPrayerCategoryList()
    }

    private fun getPrayerCategoryList() {
        val map = HashMap<String,String>()
        map["page"] = "1"
        map["limit"] = "50"
        viewModel.getPrayerCategoryList(map,this).observe(this,this)
    }

    private fun getTestimonyCategoryList() {
        val map = HashMap<String,String>()
        map["page"] = "1"
        map["limit"] = "50"
        viewModel.getTestimonyCategoryList(map,this).observe(this,this)
    }

    private fun initListener() {
        with(binding){
            formatFeedbackHint("Select a Category*",tvCategory)

            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            btnPost.setOnClickListener {
                if (containsBannedWord(binding.etTitle.text.toString().trim())) {
                    showCustomSnackbar(
                        this@AddPrayerRequestTestimonies,
                        it,
                        "Your post contains banned or inappropriate words. Please remove them before posting."
                    )
                    return@setOnClickListener
                }
                if (containsBannedWord(binding.etPostDesc.text.toString().trim())) {
                    showCustomSnackbar(
                        this@AddPrayerRequestTestimonies,
                        it,
                        "Your post contains banned or inappropriate words. Please remove them before posting."
                    )
                    return@setOnClickListener
                }
                addRequest()
//                startActivity(Intent(this@AddPrayerRequestTestimonies,UploadActivity::class.java))
            }
            ivPrayerRequest.setOnClickListener {
                ivPrayerRequest.setImageResource(R.drawable.selected_radio_prayer)
                ivTestimoney.setImageResource(R.drawable.unselected_radio_prayer)
                selectedCategory = 1
                getPrayerCategoryList()
            }

            ivTestimoney.setOnClickListener {
                ivTestimoney.setImageResource(R.drawable.selected_radio_prayer)
                ivPrayerRequest.setImageResource(R.drawable.unselected_radio_prayer)
                selectedCategory = 2
                getTestimonyCategoryList()
            }

            tlTitle.addOnEditTextAttachedListener {
                formatHint("Title*",tlTitle,etTitle.isFocused)
            }

            etTitle.setOnFocusChangeListener { _, hasFocus ->
                formatHint("Title*",tlTitle,hasFocus)
            }

            tlPostDes.addOnEditTextAttachedListener {
                formatHint("Write your Post*",tlPostDes,etPostDesc.isFocused)
            }

            etPostDesc.setOnFocusChangeListener { _, hasFocus ->
                formatHint("Write your Post*",tlPostDes,hasFocus)
            }

            val hintTypeface = ResourcesCompat.getFont(this@AddPrayerRequestTestimonies,R.font.poppins_medium)
            val editTextTypeface = ResourcesCompat.getFont(this@AddPrayerRequestTestimonies,R.font.poppins)

            tlPostDes.typeface = hintTypeface
            etPostDesc.typeface = editTextTypeface

            tlTitle.typeface = hintTypeface
            etTitle.typeface = editTextTypeface

            etTitle.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().trim().isNotEmpty() && binding.etPostDesc.text.toString().trim().isNotEmpty()){
                        binding.btnPost.backgroundTintList = getColorStateList(R.color.blue)
                        binding.btnPost.isEnabled = true
                    }else{
                        binding.btnPost.backgroundTintList = getColorStateList(R.color.button_grey)
                        binding.btnPost.isEnabled = false
                    }

                    if (s.toString().trim().isNotEmpty()) {
                        tvTitleWords.text = buildString {
                            append(set30Characters(s.toString().length))
                            append(" characters")
                        }
                    } else {
                        tvTitleWords.text = buildString {
                            append("30 characters")
                        }
                    }

                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

            binding.etPostDesc.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().trim().isNotEmpty() && binding.etTitle.text.toString().trim().isNotEmpty()){
                        binding.btnPost.backgroundTintList = getColorStateList(R.color.blue)
                        binding.btnPost.isEnabled = true
                    }else{
                        binding.btnPost.backgroundTintList = getColorStateList(R.color.button_grey)
                        binding.btnPost.isEnabled = false
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

    private fun addRequest(){
        if (selectedCategory == 0) {
            showCustomSnackbar(this, binding.root, "Please select category!")
            return
        }
        val selectedId = catList.firstOrNull { it.isSelected }?.id ?: ""

        val map = HashMap<String,String>()
        map["title"] = binding.etTitle.text.toString().trim()
        map["description"] = binding.etPostDesc.text.toString().trim()
        map["category_type"] = selectedCategory.toString()

        if (selectedId.isNotEmpty()){
            if (selectedCategory == 1) {
                map["prayer_category_id"] = selectedId
                viewModel.addPrayer(map,this).observe(this,this)
            }else {
                map["testimony_category_id"] =selectedId
                viewModel.addTestimony(map,this).observe(this,this)
            }
        }else{
            if (selectedCategory == 1) {
                viewModel.addPrayer(map,this).observe(this,this)
            }else {
                viewModel.addTestimony(map,this).observe(this,this)
            }
        }
    }

    private fun setCatAdapter() {
        categoryAdapter = CategoryAdapter(this,catList,2)
        binding.rvCategory.adapter = categoryAdapter
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


    private fun formatFeedbackHint(text: String, view: TextView){
        val spannableString = SpannableString(text)
        val redSpan = ForegroundColorSpan(Color.RED)
        spannableString.setSpan(
            redSpan,
            text.indexOf('*'),
            text.indexOf('*') + 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val blueSpan = ForegroundColorSpan(ContextCompat.getColor(this,R.color.light_black))
        spannableString.setSpan(
            blueSpan,
            0,
            text.indexOf('*'),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        view.text = spannableString
    }

    private fun countWords(text: String): Int {
        val words = text.trim().split("\\s+".toRegex())
        return words.size
    }

    private fun trimToWordLimit(text: String, limit: Int): String {
        val words = text.trim().split("\\s+".toRegex())
        return words.take(limit).joinToString(" ")
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is CommonResponse -> {
                        showCustomSnackbar(this, binding.root, "Post added Successfully.")
//                        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent("addPrayRequest"))
//                        finish()
                      /*  startActivity(Intent(this, HomeActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        })*/
                        finish()
                    }
                    is CommunityCategoryResponse -> {
                        catList.clear()
                        value.data.body?.data?.let {
                            val list =  it.map { CategoryModel(name = it?.name.toString(), id = it?.id.toString()) }
                            catList.addAll(list)
                        }
                        categoryAdapter?.notifyDataSetChanged()
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