package com.live.azurah.activity

import android.app.Activity
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.live.azurah.R
import com.live.azurah.databinding.ActivityAboutQuestBinding
import com.live.azurah.databinding.ItemQuickFactBinding
import com.live.azurah.util.gone
import com.live.azurah.util.sanitizeHtml
import com.live.azurah.util.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutQuestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutQuestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutQuestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = getColor(R.color.dashboard_primary)
        window.navigationBarColor = android.graphics.Color.parseColor("#F4F7FA")
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = true

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val topPadding = (14 * resources.displayMetrics.density).toInt()
            binding.headerLayout.updatePadding(top = bars.top + topPadding)

            val bottomMargin = (12 * resources.displayMetrics.density).toInt()
            val params = binding.btnBeginQuest.layoutParams as android.view.ViewGroup.MarginLayoutParams
            params.bottomMargin = bars.bottom + bottomMargin
            binding.btnBeginQuest.layoutParams = params
            insets
        }

        binding.backIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnBeginQuest.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }

        displayQuestData()
    }

    private fun displayQuestData() {
        val title = intent.getStringExtra("quest_title") ?: "Quest Details"
        val description = intent.getStringExtra("quest_description") ?: ""
        val bibleVerse = intent.getStringExtra("bible_verse") ?: ""
        val bibleVersion = intent.getStringExtra("bible_version") ?: ""
        val quickFactsJson = intent.getStringExtra("quick_facts") ?: ""
        val totalDays = intent.getIntExtra("total_days", 1)

        binding.tvHeaderQuestTitle.text = title
        binding.tvCardQuestTitle.text = title
        binding.tvDaysBadge.text = buildString {
            append(totalDays)
            append("\nDays")
        }

        val cleanDesc = sanitizeHtml(description).toString().trim()
        binding.tvQuestDesc.text = if (cleanDesc.isNotEmpty()) cleanDesc else description

        val cleanVerse = sanitizeHtml(bibleVerse).toString().trim()
        binding.tvKeyVerseText.text = if (cleanVerse.isNotEmpty()) "\"$cleanVerse\"" else "\"$bibleVerse\""
        binding.tvKeyVerseVersion.text = bibleVersion

        populateQuickFacts(quickFactsJson)
    }

    private fun populateQuickFacts(json: String) {
        binding.llQuickFactsContainer.removeAllViews()
        if (json.isBlank()) {
            binding.cardQuickFacts.gone()
            return
        }

        try {
            val type = object : TypeToken<List<QuickFactItem>>() {}.type
            val list: List<QuickFactItem>? = Gson().fromJson(json, type)
            if (!list.isNullOrEmpty()) {
                binding.cardQuickFacts.visible()
                list.forEachIndexed { index, item ->
                    val factBinding = ItemQuickFactBinding.inflate(
                        LayoutInflater.from(this),
                        binding.llQuickFactsContainer,
                        false
                    )
                    factBinding.tvFactTitle.text = item.name ?: ""
                    factBinding.tvFactDesc.text = item.description ?: ""

                    if (index == list.size - 1) {
                        factBinding.viewDivider.gone()
                    } else {
                        factBinding.viewDivider.visible()
                    }

                    binding.llQuickFactsContainer.addView(factBinding.root)
                }
            } else {
                binding.cardQuickFacts.gone()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.cardQuickFacts.gone()
        }
    }

    private data class QuickFactItem(
        val name: String? = null,
        val description: String? = null,
        val icon: String? = null
    )
}
