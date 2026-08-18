package com.live.azurah.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import com.live.azurah.R
import com.live.azurah.databinding.ActivityBiblePrayerBinding
import com.live.azurah.model.CommonResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Status
import com.live.azurah.util.getCurrentDate
import com.live.azurah.util.sanitizeHtml
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BiblePrayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBiblePrayerBinding
    private val viewModel by viewModels<CommonViewModel>()

    private var questId: String = ""
    private var challengeId: String = ""
    private var prayerId: String = ""
    private var dayNo: String = "1"
    private var isCompleted: Int = 0
    private var isDevotionalCompleted: Int = 0
    private var streakCount: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBiblePrayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = getColor(R.color.dashboard_primary)
        window.navigationBarColor = Color.parseColor("#F4F7FA")
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = true

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            val topPadding = (14 * resources.displayMetrics.density).toInt()
            binding.headerLayout.updatePadding(top = bars.top + topPadding)

            val bottomMargin = (12 * resources.displayMetrics.density).toInt()
            val params = binding.btnPrayerComplete.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = bars.bottom + bottomMargin
            binding.btnPrayerComplete.layoutParams = params
            insets
        }

        binding.backIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        extractIntentData()
        bindViews()
    }

    private fun extractIntentData() {
        questId = intent.getStringExtra("bible_quest_id") ?: ""
        challengeId = intent.getStringExtra("bible_quest_challenge_id") ?: ""
        prayerId = intent.getStringExtra("bible_quest_challenge_prayer_id") ?: ""
        dayNo = intent.getStringExtra("day_no") ?: "1"
        isCompleted = intent.getIntExtra("is_completed", 0)
        isDevotionalCompleted = intent.getIntExtra("is_devotional_completed", 0)
        streakCount = intent.getIntExtra("streak_count", 1)
    }

    private fun bindViews() {
        val title = intent.getStringExtra("title") ?: "Prayer"
        val description = intent.getStringExtra("description") ?: ""

        binding.tvPrayerSubHeader.text = "Day $dayNo · Prayer"
        binding.tvHeaderTitle.text = title

        val cleanDesc = sanitizeHtml(description).toString().trim()
        binding.tvPrayerDescription.text = if (cleanDesc.isNotEmpty()) cleanDesc else description

        if (isCompleted == 1) {
            binding.btnPrayerComplete.text = "✓ Completed"
        } else {
            binding.btnPrayerComplete.text = "✓ Prayer Complete"
            binding.btnPrayerComplete.setOnClickListener {
                submitPrayerCompletion()
            }
        }
    }

    private fun submitPrayerCompletion() {
        val map = HashMap<String, String>()
        map["bible_quest_id"] = questId
        map["bible_quest_challenge_id"] = challengeId
        map["date"] = getCurrentDate()
        map["bible_quest_challenge_prayer_id"] = prayerId
        map["day_no"] = dayNo

        viewModel.completePrayer(map, this).observe(this) { response ->
            when (response.status) {
                Status.LOADING -> {
                    LoaderDialog.show(this)
                }
                Status.SUCCESS -> {
                    LoaderDialog.dismiss()
                    if (response.data is CommonResponse && (response.data as CommonResponse).success == true) {
                        Toast.makeText(this, "Prayer Completed!", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)

                        // If both devotional and prayer are now complete, launch 3rd Celebration Screen!
                        if (isDevotionalCompleted == 1) {
                            startActivity(Intent(this, StreakCelebrationActivity::class.java).apply {
                                putExtra("streak_count", streakCount)
                            })
                        }
                        finish()
                    } else {
                        val msg = (response.data as? CommonResponse)?.message ?: "Failed to mark prayer complete"
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    }
                }
                Status.ERROR -> {
                    LoaderDialog.dismiss()
                    Toast.makeText(this, response.message ?: "An error occurred", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }
}
