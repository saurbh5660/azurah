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
import com.live.azurah.databinding.ActivityMessageSentBinding

class MessageSentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMessageSentBinding
    private var from = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageSentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.blue)
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

        from = intent.getStringExtra("from") ?: ""

        if (from == "1"){
            binding.tvChallangeCompleted.text = "Thank you for bringing this to our attention."
            binding.tvChallengeDesc.text = "We will review the content and take the necessary action to ensure our community standards are upheld."
        }else if (from == "2"){
            binding.tvChallangeCompleted.text = "Thank you for your feedback!"
            binding.tvChallengeDesc.text = "We have received it and will review it to improve our service."
        } else if (from == "3"){
            binding.tvChallangeCompleted.text = "Request submitted!"
            binding.tvChallengeDesc.text = "Our team will review your group chat request. If approved, you’ll see it appear in the app soon!"
        }
        binding.btnHome.setOnClickListener {
            startActivity(Intent(this,HomeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })
        }

    }
}