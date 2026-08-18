package com.live.azurah.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import com.live.azurah.R
import com.live.azurah.databinding.ActivityStreakCelebrationBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StreakCelebrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStreakCelebrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStreakCelebrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.parseColor("#A8E0F9")
        window.navigationBarColor = Color.parseColor("#FFFFFF")
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = true
        controller.isAppearanceLightNavigationBars = true

        ViewCompat.setOnApplyWindowInsetsListener(binding.celebrationRoot) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.topLayout.updatePadding(top = bars.top + (8 * resources.displayMetrics.density).toInt())
            val params = binding.btnContinue.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = bars.bottom + (16 * resources.displayMetrics.density).toInt()
            binding.btnContinue.layoutParams = params
            insets
        }

        bindDynamicData()

        binding.btnContinue.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun bindDynamicData() {
        val streakCount = intent.getIntExtra(EXTRA_STREAK_COUNT, 1).coerceAtLeast(1)
        val headline = intent.getStringExtra(EXTRA_HEADLINE)
            ?: "Congrats on completing\ntoday's streak!"
        val subheadline = intent.getStringExtra(EXTRA_SUBHEADLINE)
            ?: "Well done for showing up today. Keep going — every day builds something that lasts."
        val streakCardSubtitle = intent.getStringExtra(EXTRA_STREAK_CARD_SUBTITLE)
            ?: "Your longest run this month"
        val lessonTitle = intent.getStringExtra(EXTRA_LESSON_TITLE) ?: "Today's Lesson"
        val lessonCompleted = intent.getBooleanExtra(EXTRA_LESSON_COMPLETED, true)
        val lessonSubtitle = intent.getStringExtra(EXTRA_LESSON_SUBTITLE)
            ?: if (lessonCompleted) {
                "Completed · Come back tomorrow"
            } else {
                "Pending · Finish today's lesson"
            }
        val dayLabel = if (streakCount == 1) "day" else "days"

        binding.tvFlameNumber.text = streakCount.toString()
        binding.tvStreakSubBadge.text = "🔥 DAY $streakCount STREAK"
        binding.tvHeadline.text = headline
        binding.tvSubheadline.text = subheadline
        binding.tvStreakCardSubtitle.text = streakCardSubtitle
        binding.tvStreakDaysCount.text = "$streakCount $dayLabel"
        binding.tvLessonTitle.text = lessonTitle
        binding.tvLessonSubtitle.text = lessonSubtitle

        if (lessonCompleted) {
            binding.lessonStatusIcon.visibility = View.VISIBLE
            binding.lessonStatusIcon.setBackgroundResource(R.drawable.streak_celebration_check_circle)
            binding.ivLessonCheck.setImageResource(R.drawable.ic_streak_check_white)
        } else {
            binding.lessonStatusIcon.visibility = View.INVISIBLE
        }
    }

    companion object {
        const val EXTRA_STREAK_COUNT = "streak_count"
        const val EXTRA_HEADLINE = "headline"
        const val EXTRA_SUBHEADLINE = "subheadline"
        const val EXTRA_STREAK_CARD_SUBTITLE = "streak_card_subtitle"
        const val EXTRA_LESSON_TITLE = "lesson_title"
        const val EXTRA_LESSON_SUBTITLE = "lesson_subtitle"
        const val EXTRA_LESSON_COMPLETED = "lesson_completed"

        fun createIntent(
            context: Context,
            streakCount: Int,
            lessonCompleted: Boolean = true,
            headline: String? = null,
            subheadline: String? = null,
            streakCardSubtitle: String? = null,
            lessonTitle: String? = null,
            lessonSubtitle: String? = null
        ): Intent {
            return Intent(context, StreakCelebrationActivity::class.java).apply {
                putExtra(EXTRA_STREAK_COUNT, streakCount)
                putExtra(EXTRA_LESSON_COMPLETED, lessonCompleted)
                headline?.let { putExtra(EXTRA_HEADLINE, it) }
                subheadline?.let { putExtra(EXTRA_SUBHEADLINE, it) }
                streakCardSubtitle?.let { putExtra(EXTRA_STREAK_CARD_SUBTITLE, it) }
                lessonTitle?.let { putExtra(EXTRA_LESSON_TITLE, it) }
                lessonSubtitle?.let { putExtra(EXTRA_LESSON_SUBTITLE, it) }
            }
        }
    }
}
