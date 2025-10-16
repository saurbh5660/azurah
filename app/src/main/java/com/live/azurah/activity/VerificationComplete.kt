package com.live.azurah.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import com.live.azurah.R
import com.live.azurah.databinding.ActivityVerificationCompleteBinding

class VerificationComplete : AppCompatActivity() {
    private lateinit var binding:ActivityVerificationCompleteBinding
    private var from = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationCompleteBinding.inflate(layoutInflater)
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
        from = intent.getIntExtra("from",0)
        if (from == 1){
            binding.btnNext.text = "Done"
        }else{
            binding.btnNext.text = "Next"
        }
        initClickListener()
    }

    private fun initClickListener() {
        with(binding){
            btnNext.setOnClickListener {
                if (from==1){
                    finish()
                }else{
                    startActivity(Intent(this@VerificationComplete,SetUpProfileActivity::class.java))
                    finish()
                }
            }
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

        }
    }

}