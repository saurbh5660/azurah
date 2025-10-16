package com.live.azurah.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.live.azurah.R
import com.live.azurah.databinding.ActivitySplashBinding
import com.live.azurah.util.getPreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permission = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.blue)
        window.setBackgroundDrawable(
            ActivityCompat.getDrawable(
                this,
                R.drawable.gradient_bg_simple
            )
        )
        window.navigationBarColor = getColor(R.color.blue)
        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasPermissions1(permission)) {
                requestPermission1()
            } else {
                loginDirectly()
            }
        } else {
            loginDirectly()
        }
    }

    private fun hasPermissions1(permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission1() {
        requestNotificationPermissions.launch(permission)
    }

    private val requestNotificationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            loginDirectly()
        }

    private fun loginDirectly() {
        lifecycleScope.launch {
            delay(2000)
            if (getPreference("isLogin", false)) {
                startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                finishAffinity()
            } else {
                /* if (getPreference1("isAlreadyVisited",false)){
                     startActivity(Intent(this@SplashActivity, SignUpActivity::class.java))
                 }else {*/
                startActivity(Intent(this@SplashActivity, WalkthroughActivity::class.java))
//                }
                finish()
            }

        }
    }

}