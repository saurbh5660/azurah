package com.live.azurah.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import com.live.azurah.R
import com.live.azurah.databinding.ActivityHelpCenterBinding

class HelpCenterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHelpCenterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpCenterBinding.inflate(layoutInflater)
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

        binding.backIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.clEdit.setOnClickListener {
            startActivity(Intent(this,ReportProblemActivity::class.java))
        }

        binding.clChange.setOnClickListener {
            startActivity(Intent(this,ContactUsActivity::class.java))
        }

        binding.clGuidelines.setOnClickListener {
            startActivity(Intent(this,ContentActivity::class.java).apply {
                putExtra("type",2)
            })
        }

    /*    binding.ivCross.setOnClickListener {
            binding.etSearch.setText("")
        }
        binding.etSearch.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotBlank()){
                    binding.ivCross.visibility = View.VISIBLE
                }else{
                    binding.ivCross.visibility = View.GONE
                }
            }
            override fun afterTextChanged(s: Editable?) {
            }

        })*/

    }
}