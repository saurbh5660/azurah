package com.live.azurah.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColorStateList
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.live.azurah.R
import com.live.azurah.activity.ContentActivity
import com.live.azurah.activity.SetUpProfileActivity
import com.live.azurah.databinding.FragmentBirthdayBinding
import com.live.azurah.databinding.InstructionDialogBinding
import com.live.azurah.databinding.SureDialogBinding
import com.live.azurah.model.FileUploadResponse
import com.live.azurah.model.SignUpResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.isAgeValid
import com.live.azurah.util.longToTime
import com.live.azurah.util.savePreference
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class BirthdayFragment : Fragment(), Observer<Resource<Any>> {
    private lateinit var binding: FragmentBirthdayBinding
//    private val loaderDialog by lazy { LoaderDialog(requireContext()) }
    private val viewModel by viewModels<CommonViewModel>()
    private var dob = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBirthdayBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListener()
    }

    private fun initListener() {
        with(binding){
            setSpannableText(tvTerms)
            btnContinue.setOnClickListener {
                val map = HashMap<String,String>()
                map["dob"] = dob
                map["form_step"] = "4"

                viewModel.editProfile(map,requireActivity()).observe(viewLifecycleOwner,this@BirthdayFragment)
            }
            backIcon.setOnClickListener {
                requireActivity().finish()
            }
            etDob.setOnClickListener {
                showDatePicker()
            }

            cbTerms.setOnClickListener{
                if (binding.cbTerms.isChecked && binding.etDob.text.toString().trim().isNotEmpty()){
                    binding.btnContinue.isEnabled = true
                    binding.btnContinue.backgroundTintList = getColorStateList(requireContext(),R.color.blue)
                }else{
                    binding.btnContinue.isEnabled = false
                    binding.btnContinue.backgroundTintList = getColorStateList(requireContext(),R.color.button_grey)
                }
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
                        savePreference("dob",res?.dob ?: "")
//                        replaceFragment(NewsletterFragment())
                        replaceFragment(LocationFragment())
                    }
                }
            }
            Status.LOADING -> {
                LoaderDialog.show(requireActivity())
            }

            Status.ERROR -> {
               LoaderDialog.dismiss()
                showCustomSnackbar(requireActivity(),binding.root, value.message.toString())

            }
        }
    }

    private fun replaceFragment(fragment: Fragment){
        with(requireActivity() as SetUpProfileActivity){
            requireActivity().supportFragmentManager.beginTransaction().replace(binding.frameContainer.id, fragment).commit()
        }
    }

    private fun setSpannableText(view: TextView){
        val text = "By ticking this box, you confirm that you are at least 13 years old.*"

        val asteriskIndex = text.indexOf("*")
        val spannableString = SpannableString(text)
        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.black)),
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
        view.text = spannableString
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),R.style.MyAppTheme,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                val date = longToTime(selectedCalendar.time.time,"dd MMMM yyyy")
                if (isAgeValid(date.toString())){
                    binding.etDob.setText(date)
                    dob = longToTime(selectedCalendar.time.time,"yyyy-MM-dd") ?: ""

                }else{
                    sureDeleteDialog()
                }
                if (binding.cbTerms.isChecked && binding.etDob.text.toString().trim().isNotEmpty()){
                    binding.btnContinue.isEnabled = true
                    binding.btnContinue.backgroundTintList = getColorStateList(requireContext(),R.color.blue)
                }else{
                    binding.btnContinue.isEnabled = false
                    binding.btnContinue.backgroundTintList = getColorStateList(requireContext(),R.color.button_grey)
                }
            },
            year, month, day
        )
/*
        val minCalender = Calendar.getInstance()
        minCalender.add(Calendar.YEAR, -13)
        datePickerDialog.datePicker.maxDate = minCalender.time.time*/
        datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis

        datePickerDialog.show()
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).textSize = 14f
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).textSize = 14f
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(
            ContextCompat.getColor(requireContext(), R.color.cancel_red_color))
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(
            ContextCompat.getColor(requireContext(), R.color.blue))
    }

    private fun sureDeleteDialog(){
        val customDialog = Dialog(requireContext())
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val confirmationBinding = InstructionDialogBinding.inflate(layoutInflater)
        customDialog.setContentView(confirmationBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        confirmationBinding.tvYes.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()
    }
}