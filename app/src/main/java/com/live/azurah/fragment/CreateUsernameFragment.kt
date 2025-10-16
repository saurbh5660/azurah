package com.live.azurah.fragment

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.live.azurah.R
import com.live.azurah.activity.SetUpProfileActivity
import com.live.azurah.adapter.SuggestionUsernameAdapter
import com.live.azurah.databinding.FragmentCreateUsernameBinding
import com.live.azurah.databinding.UsernameTakenDialogBinding
import com.live.azurah.model.CheckUsernameResponse
import com.live.azurah.model.InterestModel
import com.live.azurah.model.SignUpResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.containsBannedWord
import com.live.azurah.util.savePreference
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.validateUsername
import com.live.azurah.viewmodel.CommonViewModel
import com.live.azurah.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

@AndroidEntryPoint
class CreateUsernameFragment : Fragment(), Observer<Resource<Any>> {
    private lateinit var binding: FragmentCreateUsernameBinding
    private var count = 0
    private var list = ArrayList<InterestModel>()
    private  var customDialog :Dialog? = null
    private var selectedUserName = ""
    private var showDialog = false
    private lateinit var sharedViewModel: SharedViewModel
//    private val loaderDialog by lazy { LoaderDialog(requireContext()) }
    private val viewModel by viewModels<CommonViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateUsernameBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        initListener()
    }

    private fun initListener() {
        with(binding) {
            tlUserName.isEndIconVisible = false
            btnCreateUser.setOnClickListener {
                if (containsBannedWord( binding.etUserName.text.toString().trim())) {
                    showCustomSnackbar(
                        requireContext(),
                        it,
                        "Your username contains banned or inappropriate words. Please remove them before posting."
                    )
                    return@setOnClickListener
                }
                addUsernameApi()
            }

            backIcon.setOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }

            tlUserName.addOnEditTextAttachedListener {
                formatHint("Username*",tlUserName,etUserName.isFocused)
            }

            etUserName.setOnFocusChangeListener { _, hasFocus ->
                formatHint("Username*",tlUserName,hasFocus)
            }

            etUserName.addTextChangedListener(object : TextWatcher {
                var delay: Long = 2000
                var timer = Timer()

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // cancel any pending API call
                    timer.cancel()
                    timer.purge()
                }

                override fun afterTextChanged(s: Editable?) {
                    val username = s?.toString()?.trim().orEmpty()

                    binding.btnCreateUser.isEnabled = false
                    binding.btnCreateUser.backgroundTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.button_grey)

                    val errorMsg = validateUsername(username)

                    if (username.isEmpty()) {
                        tlUserName.error = null
                        tlUserName.isEndIconVisible = false
                        binding.searchProgress.visibility = View.GONE
                        return
                    }

                    if (errorMsg != null) {
                        // show error instantly
                        tlUserName.error = errorMsg
                        tlUserName.isEndIconVisible = false


                        binding.searchProgress.visibility = View.GONE
                        return
                    } else {
                        tlUserName.error = null
                   /*     tlUserName.isEndIconVisible = false
                        binding.searchProgress.visibility = View.VISIBLE*/
//                        tlUserName.error = null
                    }

                    timer = Timer()
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            lifecycleScope.launch(Dispatchers.Main) {
                                if (selectedUserName != username) {
                                    tlUserName.isEndIconVisible = false
                                    binding.searchProgress.visibility = View.VISIBLE
                                    if (customDialog?.isShowing != true) {
                                        checkUserName(username)
                                    }
                                } else {
                                    binding.searchProgress.visibility = View.GONE
                                }
                            }
                        }
                    }, delay)
                }
            })


            /* etUserName.addTextChangedListener(object: TextWatcher {
                 var delay : Long = 2000
                 var timer = Timer()
                 override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                 }

                 override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                     timer.cancel()
                     timer.purge()
                 }
                 override fun afterTextChanged(s: Editable?) {
                     binding.btnCreateUser.isEnabled = false
                     binding.btnCreateUser.backgroundTintList = ContextCompat.getColorStateList(requireContext(),R.color.button_grey)

                     timer = Timer()
                     timer.schedule(object : TimerTask() {
                         override fun run() {
                             lifecycleScope.launch(Dispatchers.Main){
                                 if (s.toString().isNotEmpty()){
                                     if (selectedUserName != s.toString()){
                                         tlUserName.isEndIconVisible = false
                                         binding.searchProgress.visibility = View.VISIBLE
                                         if (customDialog?.isShowing != true) {
                                            checkUserName(etUserName.text.toString().trim())
                                         }
                                     }else{
                                         binding.searchProgress.visibility = View.GONE
                                     }

                                 }else{
                                     tlUserName.isEndIconVisible = false
                                     binding.searchProgress.visibility = View.GONE
                                 }
                             }
                         }
                     }, delay)
                 }
             })*/

        }
    }

    private fun checkUserName(name: String){
        showDialog = false
        val map = HashMap<String,String>()
        map["username"] = name
        viewModel.checkUserName(map,requireActivity()).observe(viewLifecycleOwner,this@CreateUsernameFragment)
    }

    private fun addUsernameApi(){
        val map = HashMap<String,String>()
        map["username"] = binding.etUserName.text.toString().trim()
        map["form_step"] = "2"
        viewModel.editProfile(map,requireActivity()).observe(viewLifecycleOwner) { value ->
            when (value.status) {
                Status.SUCCESS -> {
                   LoaderDialog.dismiss()
                    when (value.data) {
                        is SignUpResponse -> {
                            val res = value.data.body
                            savePreference("username",res?.username ?: "")
                            replaceFragment(UploadProfilePhotoFragment())
                        }
                    }
                }

                Status.LOADING -> {
                    LoaderDialog.show(requireActivity())
                }

                Status.ERROR -> {
                   LoaderDialog.dismiss()
                    showCustomSnackbar(requireActivity(), binding.root, value.message.toString())
                }
            }
        }
    }

    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is CheckUsernameResponse -> {
                        val res = value.data.body
                        if (!res.isNullOrEmpty()){
                            list.clear()
                           val userList =  res.map { InterestModel(name = it) }.take(3)
                            list.addAll(userList)
                            userTakenDialog()
                            binding.tlUserName.isEndIconVisible = false
                            binding.searchProgress.visibility = View.GONE
                            binding.btnCreateUser.isEnabled = false
                            binding.btnCreateUser.backgroundTintList = ContextCompat.getColorStateList(requireContext(),R.color.button_grey)
                        }else{
                            binding.searchProgress.visibility = View.GONE
                            binding.tlUserName.isEndIconVisible = true
                            binding.btnCreateUser.isEnabled = true
                            binding.btnCreateUser.backgroundTintList = ContextCompat.getColorStateList(requireContext(),R.color.blue)
                        }
                    }

                }
            }

            Status.LOADING -> {
                if (showDialog){
                    LoaderDialog.show(requireActivity())
                }
            }

            Status.ERROR -> {
               LoaderDialog.dismiss()
                binding.tlUserName.isEndIconVisible = false
                binding.searchProgress.visibility = View.GONE
                showCustomSnackbar(requireActivity(),binding.root, value.message.toString())

            }
        }
    }


    private fun replaceFragment(fragment: Fragment){
        with(requireActivity() as SetUpProfileActivity){
            requireActivity().supportFragmentManager.beginTransaction().replace(binding.frameContainer.id, fragment).commit()
        }
    }

    private fun formatHint(text: String, view: TextInputLayout, focus:Boolean) {
        view.hint = ""
        val asteriskIndex = text.indexOf("*")
        val spannableString = SpannableString(text)
        spannableString.setSpan(
            ForegroundColorSpan(if (focus) ContextCompat.getColor(requireContext(),R.color.blue) else ContextCompat.getColor(requireContext(),R.color.light_black)),
            0,
            asteriskIndex,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.star_red_color)),
            asteriskIndex,
            asteriskIndex + 1,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        view.hint = spannableString
    }

    private fun userTakenDialog() {
        count = 1
         customDialog = Dialog(requireContext(),R.style.CustomDialogStyle)
        customDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val userBinding = UsernameTakenDialogBinding.inflate(layoutInflater)
        customDialog?.setContentView(userBinding.root)
        customDialog?.window?.setGravity(Gravity.CENTER)
        customDialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        customDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        userBinding.ivCancel.setOnClickListener {
            customDialog?.dismiss()
        }

        val adapter = SuggestionUsernameAdapter(requireContext(),list)
        userBinding.rvSuggestions.adapter = adapter

        adapter.nameListener = {
            selectedUserName = it
            binding.etUserName.setText(it)
            binding.etUserName.setSelection(it.length)
            binding.tlUserName.isEndIconVisible = true
            binding.searchProgress.visibility = View.GONE
            binding.btnCreateUser.isEnabled = true
            binding.btnCreateUser.backgroundTintList = ContextCompat.getColorStateList(requireContext(),R.color.blue)
            customDialog?.dismiss()
        }
        customDialog?.show()
    }

}