package com.live.azurah.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.live.azurah.R
import com.live.azurah.activity.BibleLeaderboardActivity
import com.live.azurah.activity.BibleQuizActivity
import com.live.azurah.databinding.FragmentQuizResultBinding
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.QuizSummaryResponse
import com.live.azurah.retrofit.Status
import com.live.azurah.util.getPreference
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

        binding.tvViewLeaderboard.setOnClickListener {
            startActivity(Intent(requireContext(), BibleLeaderboardActivity::class.java))
            activity.finish()
        }
        binding.tvBackJourney.setOnClickListener { activity.finish() }

        fetchSummary(host)
    }

    private fun fetchSummary(host: BibleQuizActivity) {
        val userId = getPreference("id", "")
        host.viewModel.getQuizSummary(host.challengeId, userId, host).observe(viewLifecycleOwner, Observer { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    val response = resource.data as? QuizSummaryResponse
                    if (response?.success == true && response.body != null) {
                        updateUI(response.body)
                    }
                }
                Status.ERROR -> {
                    Toast.makeText(context, resource.message ?: "Failed to fetch summary", Toast.LENGTH_SHORT).show()
                }
                Status.LOADING -> {
                    // Show Loading
                }
            }
        })
    }

    private fun updateUI(summary: com.live.azurah.model.QuizSummaryBody) {
        binding.tvResultTitle.text = summary.title ?: "Keep Learning!"
        val correct = summary.correctCount ?: 0
        val total = summary.totalQuestions ?: 5
        val questTitle = summary.questTitle ?: ""
        val dayNo = summary.dayNo ?: 0
        
        binding.tvResultSubtitle.text = "$questTitle · Day $dayNo · $correct of $total correct"
        binding.tvResultScore.text = (summary.quizScore ?: 0).toString()
        binding.tvResultCorrect.text = "$correct/$total"
        binding.tvLbBonus.text = "+${summary.lbBonus ?: 0}"
        
        binding.tvQuizRowTitle.text = "Quiz Score · $correct/$total correct"
        binding.tvQuizPoints.text = "+${summary.quizScorePoints ?: 0}"
        binding.tvTotalPoints.text = (summary.totalEarnedToday ?: 0).toString()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
