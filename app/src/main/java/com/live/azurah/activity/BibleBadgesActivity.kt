package com.live.azurah.activity

import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.GridLayoutManager
import com.live.azurah.R
import com.live.azurah.adapter.BadgeAdapter
import com.live.azurah.databinding.ActivityBibleBadgesBinding
import com.live.azurah.fragment.BadgeDetailBottomSheet
import com.live.azurah.model.GrowthLevelsResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Status
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BibleBadgesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBibleBadgesBinding
    private val viewModel by viewModels<CommonViewModel>()

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

        fetchGrowthLevels()
    }

    private fun fetchGrowthLevels() {
        viewModel.getGrowthLevels(this).observe(this) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    LoaderDialog.dismiss()
                    if (resource.data is GrowthLevelsResponse) {
                        val body = resource.data.body
                        if (body != null) {
                            setupUI(body)
                        }
                    }
                }
                Status.LOADING -> {
                    LoaderDialog.show(this)
                }
                Status.ERROR -> {
                    LoaderDialog.dismiss()
                    showCustomSnackbar(this, binding.root, resource.message ?: "Failed to load growth levels")
                }
            }
        }
    }

    private fun setupUI(body: GrowthLevelsResponse.Body) {
        val levels = body.levels ?: emptyList()
        val currentStreak = body.currentStreak ?: 0
        val daysToNextLevel = body.daysToNextLevel ?: 0

        val currentLevelIndex = levels.indexOfFirst { it.current == true }.let { if (it == -1) 0 else it }
        val currentLevel = levels.getOrNull(currentLevelIndex) ?: GrowthLevelsResponse.GrowthLevel(level = "Beginner", minStreak = 0, unlocked = true, current = true)
        val nextLevel = levels.getOrNull(currentLevelIndex + 1)

        binding.tvCurrentBadgeTitle.text = currentLevel.level

        if (nextLevel != null) {
            val nextTitle = nextLevel.level ?: "Next Level"
            binding.tvCurrentBadgeSubtitle.text = if (daysToNextLevel > 0) "$daysToNextLevel more days to unlock $nextTitle" else "Unlock $nextTitle soon!"
            binding.tvProgressStart.text = "${currentLevel.level?.uppercase()} — ${currentLevel.minStreak ?: 0} DAYS"
            binding.tvProgressEnd.text = "${nextLevel.level?.uppercase()} — ${nextLevel.minStreak ?: 0} DAYS"

            val minS = currentLevel.minStreak ?: 0
            val maxS = nextLevel.minStreak ?: (minS + 1)
            val diff = (maxS - minS).coerceAtLeast(1)
            val currentDiff = (currentStreak - minS).coerceAtLeast(0)
            val progressPercent = ((currentDiff.toFloat() / diff) * 100).toInt().coerceIn(5, 100)

            (binding.vProgressActive.layoutParams as? LinearLayout.LayoutParams)?.let { lp ->
                lp.weight = progressPercent.toFloat()
                binding.vProgressActive.layoutParams = lp
            }
        } else {
            binding.tvCurrentBadgeSubtitle.text = "All growth levels unlocked! 🎉"
            binding.tvProgressStart.text = "${currentLevel.level?.uppercase()} — ${currentLevel.minStreak ?: 0} DAYS"
            binding.tvProgressEnd.text = "MAX LEVEL"
            (binding.vProgressActive.layoutParams as? LinearLayout.LayoutParams)?.let { lp ->
                lp.weight = 100f
                binding.vProgressActive.layoutParams = lp
            }
        }

        binding.rvBadges.layoutManager = GridLayoutManager(this, 2)
        binding.rvBadges.adapter = BadgeAdapter(levels) { selectedLevel ->
            val badgeType = runCatching {
                BadgeDetailBottomSheet.BadgeType.valueOf(selectedLevel.level?.uppercase() ?: "BEGINNER")
            }.getOrDefault(BadgeDetailBottomSheet.BadgeType.BEGINNER)

            BadgeDetailBottomSheet.newInstance(badgeType)
                .show(supportFragmentManager, "BadgeDetailBottomSheet")
        }
    }
}
