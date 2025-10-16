package com.live.azurah.activity

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import com.google.android.material.textfield.TextInputLayout
import com.live.azurah.R
import com.live.azurah.databinding.ActivityAccountSettingBinding
import com.live.azurah.util.getPreference
import com.live.azurah.util.longToTime
import java.util.Calendar

class AccountSettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountSettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountSettingBinding.inflate(layoutInflater)
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

        setData()
        initListener()
    }

    override fun onResume() {
        super.onResume()
        setData()
    }

    private fun setData() {
        with(binding){
            etUserName.setText(getPreference("username",""))
            etLEmailName.setText(getPreference("email",""))
            etDob.setText(getPreference("dob",""))
        }
    }

    private fun initListener() {
        with(binding){
            tlEmailName.addOnEditTextAttachedListener {
                formatHint("Email Address*",tlEmailName,etLEmailName.isFocused)
            }

            etLEmailName.setOnFocusChangeListener { _, hasFocus ->
                formatHint("Email Address*",tlEmailName,hasFocus)
            }

            tlDateOfBirth.addOnEditTextAttachedListener {
                formatHint("Birthday*",tlDateOfBirth,etDob.isFocused)
            }

            etDob.setOnFocusChangeListener { _, hasFocus ->
                formatHint("Birthday*",tlDateOfBirth,hasFocus)
            }

            tlUserName.addOnEditTextAttachedListener {
                formatHint("Username*",tlUserName,etUserName.isFocused)
            }

            etUserName.setOnFocusChangeListener { _, hasFocus ->
                formatHint("Username*",tlUserName,hasFocus)
            }

           /* etDob.setOnClickListener {
                showDatePicker()
            }*/


            tvForgotPassword.setOnClickListener {
                startActivity(Intent(this@AccountSettingActivity,ChangeEmailActivity::class.java))
            }

            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            etLEmailName.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if (s.toString().isNotEmpty()){
                        binding.btnSignUp.backgroundTintList = getColorStateList(R.color.blue)
                        binding.btnSignUp.isEnabled = true
                    }else{
                        binding.btnSignUp.backgroundTintList = getColorStateList(R.color.button_grey)
                        binding.btnSignUp.isEnabled = false
                    }
                }

            })

        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this,R.style.MyAppTheme,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                val date = longToTime(selectedCalendar.time.time,"dd MMMM yyyy")
                binding.etDob.setText(date)

            },
            year, month, day
        )
        datePickerDialog.show()
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(
            ContextCompat.getColor(this, R.color.cancel_red_color))
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(
            ContextCompat.getColor(this, R.color.blue))
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
}