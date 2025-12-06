package com.live.azurah.activity

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.live.azurah.R
import com.live.azurah.adapter.CountryPickerAdapter
import com.live.azurah.adapter.InterestAdapter
import com.live.azurah.adapter.PopUpPromptAdapter
import com.live.azurah.adapter.PromptAdapter
import com.live.azurah.databinding.ActivityEditProfileBinding
import com.live.azurah.databinding.CountryPickerDialogBinding
import com.live.azurah.databinding.PopupPromptBinding
import com.live.azurah.fragment.InterestFragment
import com.live.azurah.model.CountryModel
import com.live.azurah.model.FileUploadResponse
import com.live.azurah.model.InterestModel
import com.live.azurah.model.InterestResponse
import com.live.azurah.model.ProfileResponse
import com.live.azurah.model.PromptModel
import com.live.azurah.model.QuestionResponse
import com.live.azurah.model.SignUpResponse
import com.live.azurah.model.countryInfoList
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.ImagePickerActivity
import com.live.azurah.util.gone
import com.live.azurah.util.loadImage
import com.live.azurah.util.prepareFilePart
import com.live.azurah.util.savePreference
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import com.live.azurah.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.glide.transformations.ColorFilterTransformation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File

@AndroidEntryPoint
class EditProfileActivity : ImagePickerActivity(), InterestAdapter.ClickListener,
    PromptAdapter.ClickListener,
    Observer<Resource<Any>> {
    private lateinit var binding: ActivityEditProfileBinding
    private var list = ArrayList<InterestResponse.Body>()
    private lateinit var adapter: InterestAdapter
    private var type = false
    private var namePreference = 2
    private var christianJourney = ""
    private var selectedFlagUrl = ""
    private var myPopupWindow: PopupWindow? = null
    private var countryList = ArrayList<CountryModel>()
    private var countryAdapter: CountryPickerAdapter? = null
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private lateinit var sharedViewModel: SharedViewModel
    private val questionList = ArrayList<QuestionResponse.Body>()
    private var countryCode = ""

    private val promptList = ArrayList<PromptModel>()
    private lateinit var promptAdapter: PromptAdapter
    private var image = ""
    private var showLoader = false
    private var coverImage = ""
    private var uploadImageData = ""
    private var uploadCoverImageData = ""
    private var country = ""
    override fun selectedImage(imagePath: String?, code: Int) {
        imagePath?.let {
            when (code) {
                1 -> {
                    image = imagePath
                    Glide.with(this).load(imagePath).into(binding.ivProfile)
                    uploadImage()
                }

                2 -> {
                    coverImage = imagePath
                    Glide.with(this)
                        .load(imagePath)
                        .apply(bitmapTransform(ColorFilterTransformation(0)))
                        .into(binding.rvProfileBackground)
                    uploadCoverImage()
                }
                3->{
                    image = imagePath
                    uploadImageData = ""
                    Glide.with(this).load(imagePath).placeholder(R.drawable.profile_icon).into(binding.ivProfile)
                }
                4->{
                    coverImage = imagePath
                    uploadCoverImageData = ""
                    Glide.with(this)
                        .load(imagePath)
                        .apply(bitmapTransform(ColorFilterTransformation(0)))
                        .into(binding.rvProfileBackground)
                }

                else -> {

                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
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
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        countryList.clear()
        countryList.addAll(countryInfoList)
        setInterestAdapter()
        initListener()
        getProfile()
        getQuestion()



        sharedViewModel.interest.observe(this, Observer {
            list.clear()
            list.addAll(it)
            Log.d("Dsfdsgdsggdfgfd",it.size.toString())
            adapter.notifyDataSetChanged()
        })
    }

    private fun setInterestAdapter() {
        adapter = InterestAdapter(this, list, this)
        binding.rvInterest.adapter = adapter

        promptAdapter = PromptAdapter(this, promptList, 0, this, 1)
        binding.rvPrompts.adapter = promptAdapter
    }

    private fun getProfile(){
        showLoader = true
        viewModel.getProfile(this).observe(this,this)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initListener() {
        with(binding) {
            tvAddPrompt.setOnClickListener {
                promptList.add(PromptModel())
                promptAdapter.notifyDataSetChanged()
                binding.tvAddPrompt.text = "Add Another Prompt"
                binding.tvChoosePrompt.visibility = View.VISIBLE

                if (promptList.size > 2) {
                    tvAddPrompt.visibility = View.GONE
                } else {
                    tvAddPrompt.visibility = View.VISIBLE

                }
            }
            clLocation.setOnClickListener {
                showCountryDialog()
            }

            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            btnFinish.setOnClickListener {
                val selectedInterest = list.filter { it.isSelected }.map { it.id.toString() }

                val question = promptList.any { it.question_id.isNullOrEmpty()}
                val answer = promptList.any { it.description.isNullOrEmpty()}

                if (binding.etFirstName.text.toString().isEmpty()){
                    showCustomSnackbar(this@EditProfileActivity, binding.root, "Please select first name.")
                    return@setOnClickListener
                }

                if (binding.etLastName.text.toString().isEmpty()){
                    showCustomSnackbar(this@EditProfileActivity, binding.root, "Please select last name.")
                    return@setOnClickListener
                }

              /*  if (country.isEmpty()){
                    showCustomSnackbar(this@EditProfileActivity, binding.root, "Please select country")
                    return@setOnClickListener
                }*/

                if (christianJourney.isEmpty()){
                    showCustomSnackbar(this@EditProfileActivity, binding.root, "Please select christian journey.")
                    return@setOnClickListener
                }

                if (selectedInterest.isEmpty()){
                    showCustomSnackbar(this@EditProfileActivity, binding.root, "Please select interest.")
                    return@setOnClickListener
                }

                if (question){
                    showCustomSnackbar(this@EditProfileActivity,binding.root, "Please select question.")
                    return@setOnClickListener
                }
                if (answer){
                    showCustomSnackbar(this@EditProfileActivity,binding.root, "Please enter description.")
                    return@setOnClickListener
                }


                val data = Gson().toJson(promptList)
                Log.d("dataaaaaaaaaaa",data)
                Log.d("dataaaaaaaaaaa",selectedInterest.joinToString(prefix = "[", separator = ",", postfix = "]"))

                val map = HashMap<String,String>()
//                if (uploadImageData.isNotEmpty()){
                    map["profile_image"] = uploadImageData
//                }
//                if (uploadCoverImageData.isNotEmpty()){
                Log.d("hvnsdvhdsvhdf",uploadImageData)
                Log.d("hvnsdvhdsvhdf",uploadCoverImageData)
                    map["cover_image"] = uploadCoverImageData
//                }
                map["first_name"] = binding.etFirstName.text.toString().trim()
                map["last_name"] = binding.etLastName.text.toString().trim()
                map["bio"] = etPostDesc.text.toString().trim()
                map["christian_journey"] = christianJourney
                map["country"] = country
                map["country_code"] = countryCode
                map["display_name_preference"] = namePreference.toString()
                map["interest_ids"] = selectedInterest.joinToString(prefix = "[", separator = ",", postfix = "]")
                map["answer_ids"] = data
                map["is_profile_completed"] = "1"

                val json = JSONObject(map as Map<*, *>).toString()
                Log.d("fhdbfhdfd",json)


                showLoader = true
                viewModel.editProfile(map,this@EditProfileActivity).observe(this@EditProfileActivity,this@EditProfileActivity)

            }
            clChristianName.setOnClickListener {
                if (type) {
                    type = false
                    tvType.visibility = View.GONE
                } else {
                    type = true
                    tvType.visibility = View.VISIBLE
                }
            }

            tlFirstName.addOnEditTextAttachedListener {
                formatHint("First Name*", tlFirstName, etFirstName.isFocused)
            }

            tlLastName.addOnEditTextAttachedListener {
                formatHint("Last Name*", tlLastName, etFirstName.isFocused)
            }

            etFirstName.setOnFocusChangeListener { _, hasFocus ->
                formatHint("First Name*", tlFirstName, hasFocus)
            }

            etLastName.setOnFocusChangeListener { _, hasFocus ->
                formatHint("Last Name*", tlLastName, hasFocus)
            }

            tvViewAll.setOnClickListener {
                val fragment = InterestFragment()
                val bundle = Bundle()
                bundle.putString("from", "edit")
                fragment.arguments = bundle
                replaceFragment(fragment)
            }

            /*  ivSetting.setOnClickListener {
                  startActivity(Intent(this@EditProfileActivity,SettingActivity::class.java))
              }*/

            rbInterested.setOnClickListener {
                christianJourney = "Interested/New Christian"

                rbInterested.setImageResource(R.drawable.selected_radio_icon)
                rbNext.setImageResource(R.drawable.unselected_radio_icon)
                rbDeeper.setImageResource(R.drawable.unselected_radio_icon)

                clInterested.backgroundTintList =
                    ActivityCompat.getColorStateList(this@EditProfileActivity, R.color.blue)
                clNextStep.backgroundTintList =
                    ActivityCompat.getColorStateList(this@EditProfileActivity, R.color.divider_grey)
                clDelvingDeeper.backgroundTintList =
                    ActivityCompat.getColorStateList(this@EditProfileActivity, R.color.divider_grey)
            }

            rbNext.setOnClickListener {
                christianJourney = "Taking the Next Steps"
                rbInterested.setImageResource(R.drawable.unselected_radio_icon)
                rbNext.setImageResource(R.drawable.selected_radio_icon)
                rbDeeper.setImageResource(R.drawable.unselected_radio_icon)

                clInterested.backgroundTintList =
                    ActivityCompat.getColorStateList(this@EditProfileActivity, R.color.divider_grey)
                clNextStep.backgroundTintList =
                    ActivityCompat.getColorStateList(this@EditProfileActivity, R.color.blue)
                clDelvingDeeper.backgroundTintList =
                    ActivityCompat.getColorStateList(this@EditProfileActivity, R.color.divider_grey)
            }

            rbDeeper.setOnClickListener {
                christianJourney = "Delving Deeper"

                rbInterested.setImageResource(R.drawable.unselected_radio_icon)
                rbNext.setImageResource(R.drawable.unselected_radio_icon)
                rbDeeper.setImageResource(R.drawable.selected_radio_icon)

                clInterested.backgroundTintList =
                    ActivityCompat.getColorStateList(this@EditProfileActivity, R.color.divider_grey)
                clNextStep.backgroundTintList =
                    ActivityCompat.getColorStateList(this@EditProfileActivity, R.color.divider_grey)
                clDelvingDeeper.backgroundTintList =
                    ActivityCompat.getColorStateList(this@EditProfileActivity, R.color.blue)
            }

            tvChange.setOnClickListener {
                askStorageManagerPermission(this@EditProfileActivity, 1, false, 1,true,uploadImageData)
            }

            tvCover.setOnClickListener {
                askStorageManagerPermission(this@EditProfileActivity, 2, false, 3,true,uploadCoverImageData)
            }

            clInterested.setOnClickListener {
                rbInterested.performClick()
            }

            ivDrop.setOnClickListener {
                if (country.isNotEmpty()){
                    country = ""
                    countryCode = ""
                    ivFlag.gone()
                    tvCountry.text = "-- Select Country --"
                    ivDrop.setImageResource(R.drawable.drop_down_icon)
                }
            }

            clNextStep.setOnClickListener {
                rbNext.performClick()
            }

            clDelvingDeeper.setOnClickListener {
                rbDeeper.performClick()
            }

            clShowFirstName.setOnClickListener {
                namePreference = 1
                ivFirstName.setImageResource(R.drawable.selected_radio)
                ivLastName.setImageResource(R.drawable.unselected_radio)
            }
            clShowLastName.setOnClickListener {
                namePreference = 2
                ivFirstName.setImageResource(R.drawable.unselected_radio)
                ivLastName.setImageResource(R.drawable.selected_radio)
            }

            etPostDesc.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().isNotEmpty()) {
                        tvChar.text = remainingChar(s.toString().length) + " Characters"
                    } else {
                        tvChar.text = "80 Characters"
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })

        }
    }

    override fun onClick() {

    }

    private fun getQuestion(){
        showLoader = false
        viewModel.getQuestion(this).observe(this,this)
    }

    private fun uploadImage(){
        val map = HashMap<String, RequestBody>()
        map["type"] = "image".toRequestBody("text/plain".toMediaTypeOrNull())
        map["folder"] = "users".toRequestBody("text/plain".toMediaTypeOrNull())

        val list = ArrayList<MultipartBody.Part>()
        list.add(prepareFilePart("image", File(image)))
        viewModel.fileUpload(map,list,this).observe(this) { value ->
            when (value.status) {
                Status.SUCCESS -> {
                   LoaderDialog.dismiss()
                    when (value.data) {
                        is FileUploadResponse -> {
                            uploadImageData = Gson().toJson(value.data.body)
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

    private fun uploadFlagImage(){
        val map = HashMap<String, RequestBody>()
        map["type"] = "image".toRequestBody("text/plain".toMediaTypeOrNull())
        map["folder"] = "users".toRequestBody("text/plain".toMediaTypeOrNull())

        val list = ArrayList<MultipartBody.Part>()
        list.add(prepareFilePart("image", File(image)))
        viewModel.fileUpload(map,list,this).observe(this) { value ->
            when (value.status) {
                Status.SUCCESS -> {
                    LoaderDialog.dismiss()
                    when (value.data) {
                        is FileUploadResponse -> {
                            selectedFlagUrl = Gson().toJson(value.data.body)
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

    private fun uploadCoverImage(){
        val map = HashMap<String, RequestBody>()
        map["type"] = "image".toRequestBody("text/plain".toMediaTypeOrNull())
        map["folder"] = "users".toRequestBody("text/plain".toMediaTypeOrNull())

        val list = ArrayList<MultipartBody.Part>()
        list.add(prepareFilePart("image", File(coverImage)))
        viewModel.fileUpload(map,list,this).observe(this,Observer<Resource<Any>>{value->
            when (value.status) {
                Status.SUCCESS -> {
                   LoaderDialog.dismiss()
                    when (value.data) {
                        is FileUploadResponse -> {
                            uploadCoverImageData = Gson().toJson(value.data.body)
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
        })

    }


    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is ProfileResponse -> {
                        val res = value.data.body
                        with(binding) {

                            val jsonArray = JSONArray()
                            if (!res?.image.isNullOrEmpty()){
                                val jsonObject = JSONObject().apply {
                                    put("image", res?.image ?: "")
                                    put("thumbnail",  res?.imageThumb ?: "")
                                    put("fileName", res?.image?.substringAfterLast("/"))
                                    put("folder", "users")
                                    put("file_type", "image")
                                }
                                jsonArray.put(jsonObject)
                                uploadImageData = jsonArray.toString()

                            }

                            when(res?.christianJourney){
                                "Interested/New Christian"->{
                                    ivType.setImageResource(R.drawable.unselected_christian_icon)
                                }
                                "Taking the Next Steps"->{
                                    ivType.setImageResource(R.drawable.selected_christian_icon)
                                }
                                "Delving Deeper"->{
                                    ivType.setImageResource(R.drawable.deeper_icon)
                                }
                            }

                            val jsonCoverArray = JSONArray()
                            if (!res?.coverImage.isNullOrEmpty()){
                                val jsonObject = JSONObject().apply {
                                    put("image", res?.coverImage ?: "")
                                    put("thumbnail",  res?.coverImageThumb ?: "")
                                    put("fileName",  res?.coverImage?.substringAfterLast("/"))
                                    put("folder", "users")
                                    put("file_type", "image")
                                }
                                jsonCoverArray.put(jsonObject)
                                uploadCoverImageData = jsonCoverArray.toString()
                            }


                            rvProfileBackground.loadImage(
                                ApiConstants.IMAGE_BASE_URL + res?.coverImage
                            )
                            ivProfile.loadImage(
                                ApiConstants.IMAGE_BASE_URL + res?.image,
                                R.drawable.profile_icon
                            )
                            tvType.text = res?.christianJourney
                            etFirstName.setText(res?.firstName)
                            etLastName.setText(res?.lastName)
                            val pos = countryInfoList.indexOfFirst { it.name == res?.country }
                            if (pos != -1) {
                                binding.tvCountry.text = countryInfoList[pos].name
                                countryCode = countryInfoList[pos].countryCode
                                country = countryInfoList[pos].name
                                binding.ivFlag.setImageResource(countryInfoList[pos].flag!!)
                                binding.ivFlag.visibility = View.VISIBLE
                                binding.viewLocation.visibility = View.VISIBLE
                                binding.ivDrop.setImageResource(R.drawable.cross_grey_icon)
                            }

                            etPostDesc.setText(res?.bio)
                            when (res?.displayNamePreference) {
                                1 -> {
                                    namePreference = 1
                                    ivFirstName.setImageResource(R.drawable.selected_radio)
                                    ivLastName.setImageResource(R.drawable.unselected_radio)
                                }

                                2 -> {
                                    namePreference = 2
                                    ivFirstName.setImageResource(R.drawable.unselected_radio)
                                    ivLastName.setImageResource(R.drawable.selected_radio)
                                }
                            }

                            when (res?.christianJourney) {
                                "Interested/New Christian" -> {
                                    rbInterested.performClick()
                                }

                                "Taking the Next Steps" -> {
                                    rbNext.performClick()

                                }

                                "Delving Deeper" -> {
                                    rbDeeper.performClick()
                                }

                                else -> {}
                            }

                            val interestList = res?.userInterests?.map { InterestResponse.Body(
                                image = it?.interest?.image,
                                name = it?.interest?.name,
                                id = it?.interest?.id,
                                isSelected = true
                            ) }
                            if (interestList != null) {
                                list.addAll(interestList)
                            }
                            val prompt = res?.userAnswers?.map { PromptModel(
                                description = it?.description,
                                name = it?.question?.title?: "",
                                question_id = it?.questionId.toString()
                            ) }
                            if (prompt != null) {
                                promptList.addAll(prompt)
                            }
                            setInterestAdapter()

                            sharedViewModel.setInterestData(interestList as ArrayList)
                        }
                    }

                    is QuestionResponse -> {
                        questionList.clear()
                        value.data.body?.let { questionList.addAll(it) }
                    }
                    is SignUpResponse -> {
                        lifecycleScope.launch {
                            showCustomSnackbar(this@EditProfileActivity, binding.root, "Your profile has been updated!")
                            delay(700)
                            onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }
            }

            Status.LOADING -> {
                if (showLoader){
                    LoaderDialog.show(this)
                }
            }

            Status.ERROR -> {
               LoaderDialog.dismiss()
                showCustomSnackbar(this, binding.root, value.message.toString())
            }
        }
    }


    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(binding.clContainer.id, fragment)
            .addToBackStack(null).commit()
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

    override fun onCLick(type: Int, view: View, pos: Int) {
        if (type == 0) {
            if (promptList.isEmpty()) {
                binding.tvAddPrompt.text = "Add Prompt"
                binding.tvChoosePrompt.visibility = View.GONE

            } else {
                binding.tvChoosePrompt.visibility = View.VISIBLE
                binding.tvAddPrompt.text = "Add Another Prompt"
                if (promptList.size > 2) {
                    binding.tvAddPrompt.visibility = View.GONE
                } else {
                    binding.tvAddPrompt.visibility = View.VISIBLE
                }
            }

        } else {
            setPopUpWindow(view, pos)
        }

    }

    private fun remainingChar(length: Int): String {
        return (80 - length).toString()
    }

    private fun setPopUpWindow(view1: View, qusPos: Int) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popUpPromptBinding = PopupPromptBinding.inflate(inflater)

        myPopupWindow = PopupWindow(
            popUpPromptBinding.root,
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        val adapter =
            PopUpPromptAdapter(this, questionList, object : PopUpPromptAdapter.ClickListener {
                override fun onPopClick(pos: Int,qusPos:Int) {
                    promptList[qusPos].name = questionList[pos].title
                    promptList[qusPos].question_id = questionList[pos].id.toString()
                    promptAdapter.notifyItemChanged(qusPos)
                    myPopupWindow?.dismiss()
                }


            }, qusPos)
        popUpPromptBinding.rvPrompts.adapter = adapter
        myPopupWindow?.showAsDropDown(view1, 0, 0)

    }

    private fun showCountryDialog() {
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val countryBinding = CountryPickerDialogBinding.inflate(layoutInflater)
        customDialog.setContentView(countryBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        customDialog.window?.setBackgroundDrawableResource(android.R.color.white)

        countryBinding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) {
                    countryBinding.ivCross.visibility = View.VISIBLE
                    val newList = countryList.filter {
                        it.name.contains(
                            s.toString(),
                            ignoreCase = true
                        )
                    } as ArrayList
                    countryAdapter = CountryPickerAdapter(this@EditProfileActivity, newList)
                    countryBinding.rvCountry.adapter = countryAdapter
                    if (newList.isNotEmpty()) {
                        countryBinding.tvNoResult.visibility = View.GONE
                        countryBinding.rvCountry.visibility = View.VISIBLE
                    } else {
                        countryBinding.tvNoResult.visibility = View.VISIBLE
                        countryBinding.rvCountry.visibility = View.GONE
                    }
                } else {
                    countryBinding.ivCross.visibility = View.GONE
                    countryBinding.tvNoResult.visibility = View.GONE
                    countryBinding.rvCountry.visibility = View.VISIBLE
                    countryAdapter = CountryPickerAdapter(this@EditProfileActivity, countryList)
                    countryBinding.rvCountry.adapter = countryAdapter
                }
                countryAdapter?.listener = { model, pos ->
                    binding.tvCountry.text = model.name
                    countryCode = model.countryCode
                    country = model.name
                    binding.ivFlag.visibility = View.VISIBLE
                    binding.viewLocation.visibility = View.VISIBLE
                    binding.ivFlag.setImageResource(model.flag!!)
                    binding.ivDrop.setImageResource(R.drawable.cross_grey_icon)
                    customDialog.dismiss()
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        countryBinding.ivCross.setOnClickListener {
            countryBinding.etSearch.setText("")
        }

        countryAdapter = CountryPickerAdapter(this, countryList)
        countryBinding.rvCountry.adapter = countryAdapter


        countryAdapter?.listener = { model, pos ->
            binding.tvCountry.text = model.name
            countryCode = model.countryCode
            country = model.name
            binding.ivFlag.visibility = View.VISIBLE
            binding.viewLocation.visibility = View.VISIBLE
            binding.ivFlag.setImageResource(model.flag!!)
            binding.ivDrop.setImageResource(R.drawable.cross_grey_icon)
            uploadFlagImage()
            customDialog.dismiss()
        }
        countryBinding.backIcon.setOnClickListener {
            customDialog.dismiss()
        }

        customDialog.show()

    }


}