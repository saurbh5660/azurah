package com.live.azurah.activity

import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import com.live.azurah.R
import com.live.azurah.databinding.ActivityShopSettingBinding

class ShopSettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShopSettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShopSettingBinding.inflate(layoutInflater)
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
    }

    private fun initListener() {
        with(binding){
            val activeColor = ContextCompat.getColor(this@ShopSettingActivity, R.color.blue)
            val inactiveColor = ContextCompat.getColor(this@ShopSettingActivity, R.color.button_grey)
            val trackColorStateList = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked)), intArrayOf(activeColor, inactiveColor))

            switchProduct.trackTintList = trackColorStateList
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

}