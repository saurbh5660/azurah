package com.live.azurah.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.live.azurah.R
import com.live.azurah.controller.MyApplication
import com.live.azurah.databinding.ActivityRequestGroupChatBinding
import com.live.azurah.model.ChatResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.socket.SocketManager
import com.live.azurah.util.getPreference
import com.live.azurah.util.set30Characters
import com.live.azurah.util.showCustomSnackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class RequestGroupChat : AppCompatActivity(),SocketManager.Observer {
    private lateinit var binding: ActivityRequestGroupChatBinding
    private lateinit var socketManager: SocketManager
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private var categoryId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestGroupChatBinding.inflate(layoutInflater)
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
        categoryId = intent.getStringExtra("id") ?: ""
        initListener()
        socketManager = MyApplication.instance!!.getSocketManager()!!
        if (!socketManager.isConnected() || socketManager.getmSocket() == null) {
            socketManager.init()
        }
    }

    private fun initListener() {
        with(binding){

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

            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            btnPost.setOnClickListener {
                requestGroup()
            }

            tlTitle.addOnEditTextAttachedListener {
                formatHint("Group Chat Name*",tlTitle,etTitle.isFocused)
            }

            etTitle.setOnFocusChangeListener { _, hasFocus ->
                formatHint("Group Chat Name*",tlTitle,hasFocus)
            }

            tlPostDes.addOnEditTextAttachedListener {
                formatHint("Description of Group*",tlPostDes,etPostDesc.isFocused)
            }

            val hintTypeface = ResourcesCompat.getFont(this@RequestGroupChat,R.font.poppins_medium)
            val editTextTypeface = ResourcesCompat.getFont(this@RequestGroupChat,R.font.poppins)

            tlPostDes.typeface = hintTypeface
            etPostDesc.typeface = editTextTypeface

            tlTitle.typeface = hintTypeface
            etTitle.typeface = editTextTypeface


            etPostDesc.setOnFocusChangeListener { _, hasFocus ->
                formatHint("Description of Group*",tlPostDes,hasFocus)
            }
        }
    }

    private fun requestGroup() {
        val jsonObject = JSONObject()
        jsonObject.put("group_category_id", categoryId)
        jsonObject.put("added_by", getPreference("id",""))
        jsonObject.put("name", binding.etTitle.text.toString())
        jsonObject.put("description", binding.etPostDesc.text.toString())
        socketManager.requestGroupChat(jsonObject)
        Log.d("ddfdsdfd",jsonObject.toString())
        LoaderDialog.show(this)
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

    private fun countWords(text: String): Int {
        val words = text.trim().split("\\s+".toRegex())
        return words.size
    }

    private fun trimToWordLimit(text: String, limit: Int): String {
        val words = text.trim().split("\\s+".toRegex())
        return words.take(limit).joinToString(" ")
    }

    override fun onError(event: String, vararg args: Any) {
    }

    override fun onResponse(event: String, vararg args: Any) {
        when (event) {
            SocketManager.CREATE_GROUP_LISTENER -> {
                lifecycleScope.launch(Dispatchers.Main) {
                   LoaderDialog.dismiss()
                    try {
                        val jsonObject = args[0] as JSONObject
                        Log.d("fdsgdsgd", jsonObject.toString())
                        startActivity(Intent(this@RequestGroupChat,MessageSentActivity::class.java).apply {
                            putExtra("from","3")
                        })
                        finish()
                    } catch (e: Exception) {
                        Log.d("fdsgdsgd", args[0].toString())
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        socketManager.unRegister(this)
        socketManager.onRegister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        socketManager.unRegister(this)
    }

}