package com.live.azurah.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.live.azurah.R
import com.live.azurah.databinding.ActivityBibleLeaderboardBinding
import com.live.azurah.fragment.LeaderboardFollowingFragment
import com.live.azurah.fragment.LeaderboardHowItWorksFragment
import com.live.azurah.fragment.LeaderboardTop100Fragment

class BibleLeaderboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBibleLeaderboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBibleLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.dashboard_primary)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.headerContainer.updatePadding(top = bars.top + (10 * resources.displayMetrics.density).toInt())
            binding.leaderboardContainer.updatePadding(bottom = bars.bottom)
            insets
        }

        binding.tvStar.setOnClickListener {
            startActivity(Intent(this, BibleBadgesActivity::class.java))
        }

        binding.ivBack.setOnClickListener {
           onBackPressedDispatcher.onBackPressed()
        }
        binding.tvFollowing.setOnClickListener { selectTab(Tab.FOLLOWING) }
        binding.tvTop100.setOnClickListener { selectTab(Tab.TOP_100) }
        binding.tvHowItWorks.setOnClickListener { selectTab(Tab.HOW_IT_WORKS) }

        if (savedInstanceState == null) {
            selectTab(Tab.FOLLOWING)
        }
    }

    fun selectTab(tab: Tab) {
        binding.tvFollowing.background =
            if (tab == Tab.FOLLOWING) ContextCompat.getDrawable(this, R.drawable.leaderboard_tab_selected) else null
        binding.tvTop100.background =
            if (tab == Tab.TOP_100) ContextCompat.getDrawable(this, R.drawable.leaderboard_tab_selected) else null
        binding.tvHowItWorks.background =
            if (tab == Tab.HOW_IT_WORKS) ContextCompat.getDrawable(this, R.drawable.leaderboard_tab_selected) else null

        val bold = ResourcesCompat.getFont(this, R.font.inter_bold)
        val regular = ResourcesCompat.getFont(this, R.font.inter)
        binding.tvFollowing.typeface = if (tab == Tab.FOLLOWING) bold else regular
        binding.tvTop100.typeface = if (tab == Tab.TOP_100) bold else regular
        binding.tvHowItWorks.typeface = if (tab == Tab.HOW_IT_WORKS) bold else regular

        val fragment: Fragment = when (tab) {
            Tab.FOLLOWING -> LeaderboardFollowingFragment()
            Tab.TOP_100 -> LeaderboardTop100Fragment()
            Tab.HOW_IT_WORKS -> LeaderboardHowItWorksFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.leaderboardContainer, fragment)
            .commit()
    }

    enum class Tab {
        FOLLOWING,
        TOP_100,
        HOW_IT_WORKS
    }
}
