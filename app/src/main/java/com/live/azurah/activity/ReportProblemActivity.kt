package com.live.azurah.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
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
import com.google.gson.Gson
import com.live.azurah.R
import com.live.azurah.adapter.AddPostImageAdapter
import com.live.azurah.databinding.ActivityReportProblemBinding
import com.live.azurah.model.FileUploadResponse
import com.live.azurah.model.FullImageModel
import com.live.azurah.model.ImageVideoModel
import com.live.azurah.model.ReportFeedback
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.ImagePickerActivity
import com.live.azurah.util.ShowImagesDialogFragment
import com.live.azurah.util.countWords
import com.live.azurah.util.prepareFilePart
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.trimToWordLimit
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@AndroidEntryPoint
class ReportProblemActivity : ImagePickerActivity(), AddPostImageAdapter.ClickListener,
    Observer<Resource<Any>> {
    private lateinit var binding: ActivityReportProblemBinding
    private var list = ArrayList<ImageVideoModel>()
    private lateinit var adapter: AddPostImageAdapter
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var showDialog = false


    override fun selectedImage(imagePath: String?, code: Int) {
        if (!imagePath.isNullOrEmpty()) {
            list.add(ImageVideoModel(image = imagePath, type = "1"))
        }
        if (list.isEmpty()) {
            binding.rvScreenShots.visibility = View.GONE
        } else {
            binding.rvScreenShots.visibility = View.VISIBLE
        }

        if (list.size == 3) {
            binding.tvUploadPhotos.setTextColor(
                ActivityCompat.getColorStateList(
                    this,
                    R.color.button_grey
                )
            )
            binding.tvUploadPhotos.isEnabled = false
        } else {
            binding.tvUploadPhotos.setTextColor(
                ActivityCompat.getColorStateList(
                    this,
                    R.color.black
                )
            )
            binding.tvUploadPhotos.isEnabled = true
        }
        adapter.notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportProblemBinding.inflate(layoutInflater)
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
        setAdapter()
        initListener()
    }

    private fun setAdapter() {
        adapter = AddPostImageAdapter(this, list, this, 0)
        binding.rvScreenShots.adapter = adapter

        adapter.zoomImageListener = { pos ->
            val imageList = list.map { FullImageModel(image = it.image, type = 0) } as ArrayList
            val fullImageDialog = ShowImagesDialogFragment.newInstance(imageList, pos)
            fullImageDialog.show(supportFragmentManager, "FullImageDialog")
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initListener() {
        with(binding) {
            tvUploadPhotos.setOnClickListener {
                if (list.size < 3) {
                    askStorageManagerPermission(this@ReportProblemActivity, 99, false, 2)
                }
            }
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            binding.btnSubmit.setOnClickListener {

                val map = HashMap<String, RequestBody>()
                map["type"] = "image".toRequestBody("text/plain".toMediaTypeOrNull())
                map["folder"] = "users".toRequestBody("text/plain".toMediaTypeOrNull())
                val imageList = ArrayList<MultipartBody.Part>()

                list.forEach {
                    imageList.add(prepareFilePart("image", File(it.image.toString())))
                }

                if (imageList.isNotEmpty()){
                    viewModel.fileUpload(map, imageList, this@ReportProblemActivity)
                        .observe(this@ReportProblemActivity, this@ReportProblemActivity)
                }else{
                    showDialog = true
                    reportFeedback("")
                }

            }
            tlTitle.addOnEditTextAttachedListener {
                formatHint("Title*", tlTitle, etTitle.isFocused)
            }

            etTitle.setOnFocusChangeListener { _, hasFocus ->
                formatHint("Title*", tlTitle, hasFocus)
            }

            tlPostDes.addOnEditTextAttachedListener {
                formatHint("Description*", tlPostDes, etPostDesc.isFocused)
            }

            etPostDesc.setOnFocusChangeListener { _, hasFocus ->
                formatHint("Description*", tlPostDes, hasFocus)
            }

            val hintTypeface =
                ResourcesCompat.getFont(this@ReportProblemActivity, R.font.poppins_medium)
            val editTextTypeface =
                ResourcesCompat.getFont(this@ReportProblemActivity, R.font.poppins)

            binding.tlTitle.typeface = hintTypeface
            binding.etTitle.typeface = editTextTypeface

            binding.tlPostDes.typeface = hintTypeface
            binding.etPostDesc.typeface = editTextTypeface

            binding.etTitle.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().trim().isNotEmpty() && binding.etPostDesc.text.toString()
                            .trim().isNotEmpty()
                    ) {
                        binding.btnSubmit.backgroundTintList = getColorStateList(R.color.blue)
                        binding.btnSubmit.isEnabled = true
                    } else {
                        binding.btnSubmit.backgroundTintList =
                            getColorStateList(R.color.button_grey)
                        binding.btnSubmit.isEnabled = false
                    }

                    if (s.toString().trim().isNotEmpty()) {
                        if (s.toString().isNotEmpty()) {
                            tvTitleCount.text = buildString {
                                append(setCharacters1(s.toString().length))
                                append(" characters")
                            }
                        }
                    } else {
                        tvTitleCount.text = buildString {
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
                    if (s.toString().trim().isNotEmpty() && binding.etTitle.text.toString().trim()
                            .isNotEmpty()
                    ) {
                        binding.btnSubmit.backgroundTintList = getColorStateList(R.color.blue)
                        binding.btnSubmit.isEnabled = true
                    } else {
                        binding.btnSubmit.backgroundTintList =
                            getColorStateList(R.color.button_grey)
                        binding.btnSubmit.isEnabled = false
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

    private fun setCharacters(length: Int): String {
        return (300 - length).toString()
    }

    private fun setCharacters1(length: Int): String {
        return (30 - length).toString()
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

    override fun onClick(position: Int) {
        list.removeAt(position)
        adapter.notifyDataSetChanged()
        if (list.size == 3) {
            binding.tvUploadPhotos.setTextColor(
                ActivityCompat.getColorStateList(
                    this,
                    R.color.button_grey
                )
            )
            binding.tvUploadPhotos.isEnabled = false
        } else {
            binding.tvUploadPhotos.setTextColor(
                ActivityCompat.getColorStateList(
                    this,
                    R.color.black
                )
            )
            binding.tvUploadPhotos.isEnabled = true
        }
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
                when (value.data) {
                    is FileUploadResponse -> {
                        val jsonString = Gson().toJson(value.data.body)
                        showDialog = false
                        reportFeedback(jsonString)
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

    private fun reportFeedback(jsonString: String) {
        val map = HashMap<String, String>()
        if (jsonString.isNotEmpty()) {
            map["media_file"] = jsonString
        }
        map["title"] = binding.etTitle.text.toString().trim()
        map["description"] = binding.etPostDesc.text.toString().trim()
        viewModel.reportFeedBack(map, this).observe(this) { it ->
            when (it.status) {
                Status.SUCCESS -> {
                   LoaderDialog.dismiss()
                    when (it.data) {
                        is ReportFeedback -> {
                            startActivity(Intent(this@ReportProblemActivity, MessageSentActivity::class.java))
                            finish()
                        }
                    }
                }

                Status.LOADING -> {
                    if (showDialog) {
                        LoaderDialog.show(this)
                    }
                }

                Status.ERROR -> {
                   LoaderDialog.dismiss()
                    showCustomSnackbar(
                        this,
                        binding.root,
                        it.message.toString()
                    )

                }
            }

        }
    }

}