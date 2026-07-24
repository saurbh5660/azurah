package com.live.azurah.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.live.azurah.R
import com.live.azurah.activity.BibleLeaderboardActivity
import com.live.azurah.activity.BibleQuizActivity
import com.live.azurah.databinding.FragmentQuizResultBinding
import com.live.azurah.util.savePreference

class QuizResultFragment : Fragment() {
    private var _binding: FragmentQuizResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentQuizResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity()
        activity.window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.dashboard_primary)
        WindowInsetsControllerCompat(activity.window, activity.window.decorView)
            .isAppearanceLightStatusBars = false

        ViewCompat.setOnApplyWindowInsetsListener(binding.resultRoot) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.headerSection.updatePadding(top = bars.top + (8 * resources.displayMetrics.density).toInt())
            binding.resultRoot.updatePadding(bottom = bars.bottom)
            insets
        }

        val host = activity as? BibleQuizActivity ?: return
        savePreference(BibleQuizActivity.QUIZ_COMPLETED_KEY, true)

        val score = host.score
        val correct = host.correctCount
        val title = when {
            correct >= 5 -> "Perfect!"
            correct >= 3 -> "Well Done!"
            else -> "Keep Learning!"
        }

        binding.tvResultTitle.text = title
        binding.tvResultSubtitle.text = "Esther · Day 5 · $correct of 5 correct"
        binding.tvResultScore.text = score.toString()
        binding.tvResultCorrect.text = "$correct/5"
        binding.tvLbBonus.text = "+0"
        binding.tvQuizRowTitle.text = "Quiz Score · $correct/5 correct"
        binding.tvQuizPoints.text = "+$score"
        binding.tvTotalPoints.text = (500 + score).toString()

        binding.tvViewLeaderboard.setOnClickListener {
            startActivity(Intent(requireContext(), BibleLeaderboardActivity::class.java))
            activity.finish()
        }
        binding.tvBackJourney.setOnClickListener { activity.finish() }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
