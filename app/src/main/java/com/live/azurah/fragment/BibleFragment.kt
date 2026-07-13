package com.live.azurah.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.live.azurah.activity.BibleLeaderboardActivity
import com.live.azurah.activity.BibleQuizActivity
import com.live.azurah.databinding.FragmentBibleBinding
import com.live.azurah.util.getPreference

class BibleFragment : Fragment() {
    private lateinit var binding: FragmentBibleBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBibleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.quizCard.tvAboutQuiz.setOnClickListener {
            AboutQuizBottomSheet().show(childFragmentManager, AboutQuizBottomSheet::class.java.simpleName)
        }
        binding.quizCard.tvStartQuiz.setOnClickListener {
            startActivity(Intent(requireActivity(), BibleQuizActivity::class.java))
        }
        binding.tvLeaderboard.setOnClickListener {
            startActivity(Intent(requireActivity(), BibleLeaderboardActivity::class.java))
        }
        updateQuizState()
    }

    override fun onResume() {
        super.onResume()
        updateQuizState()
    }

    private fun updateQuizState() {
        val isQuizCompleted = getPreference(BibleQuizActivity.QUIZ_COMPLETED_KEY, false)
        binding.communityCard.root.visibility = if (isQuizCompleted) View.GONE else View.VISIBLE
        binding.communityCompactCard.visibility = if (isQuizCompleted) View.VISIBLE else View.GONE
        binding.communityQuestionOne.root.visibility = if (isQuizCompleted) View.VISIBLE else View.GONE
        binding.communityQuestionTwo.root.visibility = if (isQuizCompleted) View.VISIBLE else View.GONE
        binding.quizCard.root.visibility = if (isQuizCompleted) View.GONE else View.VISIBLE
        binding.quizCompleteCard.root.visibility = if (isQuizCompleted) View.VISIBLE else View.GONE
    }
}
