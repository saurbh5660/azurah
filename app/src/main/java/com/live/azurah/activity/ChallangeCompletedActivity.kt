package com.live.azurah.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import com.live.azurah.R
import com.live.azurah.databinding.ActivityChallangeCompletedBinding
import com.live.azurah.model.BibleQuestViewModel
import com.live.azurah.model.CommonResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.getCurrentDate
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@AndroidEntryPoint
class ChallangeCompletedActivity : AppCompatActivity(), Observer<Resource<Any>> {
    private lateinit var binding: ActivityChallangeCompletedBinding
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private val viewModel by viewModels<CommonViewModel>()
    private var id = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChallangeCompletedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.selected_color)
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
        id = intent.getStringExtra("id") ?: ""
        initListener()

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        with(binding){
            btnHome.setOnClickListener {
             /*   startActivity(Intent(this@ChallangeCompletedActivity, HomeActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                })*/
                startActivity(Intent(this@ChallangeCompletedActivity, HomeActivity::class.java))
                finishAffinity()
                /*.apply {
//                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                })*/
            }
            rvRating.setOnTouchListener { v, event ->
                if (event.action == android.view.MotionEvent.ACTION_UP) {
                    addRating()
                }
                false
            }
        }
    }

    private fun addRating(){
        val map = HashMap<String,String>()
        map["user_id"] = getPreference("id","")
        map["bible_quest_id"] = id
        map["rating"] = binding.rvRating.rating.toString()
        map["description"] = "Hello"
        viewModel.addRating(map,this).observe(this,this)
    }


    override fun onChanged(value: Resource<Any>) {
        when (value.status) {
            Status.SUCCESS -> {
               LoaderDialog.dismiss()
                when (value.data) {
                    is CommonResponse -> {
                        showCustomSnackbar(this,binding.root,"Thank you for your feedback.")
                        binding.rvRating.setIsIndicator(true)
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