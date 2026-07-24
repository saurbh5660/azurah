package com.live.azurah.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import com.live.azurah.R
import com.live.azurah.databinding.ActivityBibleBadgesBinding
import com.live.azurah.fragment.BadgeDetailBottomSheet

class BibleBadgesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBibleBadgesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBibleBadgesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.badges_navy)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        ViewCompat.setOnApplyWindowInsetsListener(binding.badgesRoot) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.headerSection.updatePadding(top = bars.top + (8 * resources.displayMetrics.density).toInt())
            binding.badgesRoot.updatePadding(bottom = bars.bottom)
            insets
        }

        binding.tvBack.setOnClickListener { finish() }

        binding.cardSeed.setOnClickListener {
            showBadgeDetail(BadgeDetailBottomSheet.BadgeType.SEED)
        }
        binding.cardRooted.setOnClickListener {
            showBadgeDetail(BadgeDetailBottomSheet.BadgeType.ROOTED)
        }
        binding.cardGrowing.setOnClickListener {
            showBadgeDetail(BadgeDetailBottomSheet.BadgeType.GROWING)
        }
        binding.cardFlourishing.setOnClickListener {
            showBadgeDetail(BadgeDetailBottomSheet.BadgeType.FLOURISHING)
        }

        binding.tvHowOne.text = HtmlCompat.fromHtml(
            "Complete your <b>Devotional</b> and <b>Prayer</b> every day to build your streak.",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.tvHowTwo.text = HtmlCompat.fromHtml(
            "The <b>Seed badge</b> is earned on your very first day. Every other badge unlocks at its streak milestone.",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.tvHowThree.text = HtmlCompat.fromHtml(
            "Your badge appears on your profile. Your <b>streak number</b> stays private.",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }

    private fun showBadgeDetail(type: BadgeDetailBottomSheet.BadgeType) {
        BadgeDetailBottomSheet.newInstance(type)
            .show(supportFragmentManager, "BadgeDetailBottomSheet")
    }
}
