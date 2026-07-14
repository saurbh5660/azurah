package com.live.azurah.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.live.azurah.activity.BibleDiscussionActivity
import com.live.azurah.activity.BibleLeaderboardActivity
import com.live.azurah.activity.BibleQuizActivity
import com.live.azurah.databinding.FragmentBibleBinding

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
        // Keep the earlier Bible screen layout — do not swap cards after quiz.
        binding.communityCard.root.visibility = View.VISIBLE
        binding.communityCompactCard.visibility = View.GONE
        binding.communityQuestionOne.root.visibility = View.GONE
        binding.communityQuestionTwo.root.visibility = View.GONE
        binding.quizCard.root.visibility = View.VISIBLE
        binding.quizCompleteCard.root.visibility = View.GONE

        binding.quizCard.tvAboutQuiz.setOnClickListener {
            AboutQuizBottomSheet().show(childFragmentManager, AboutQuizBottomSheet::class.java.simpleName)
        }
        binding.quizCard.tvStartQuiz.setOnClickListener {
            startActivity(Intent(requireActivity(), BibleQuizActivity::class.java))
        }
        binding.tvLeaderboard.setOnClickListener {
            startActivity(Intent(requireActivity(), BibleLeaderboardActivity::class.java))
        }
        binding.communityCard.questionOne.tvDiscuss.setOnClickListener {
            startActivity(BibleDiscussionActivity.createIntent(requireActivity(), questionIndex = 1))
        }
        binding.communityCard.questionTwo.tvDiscuss.setOnClickListener {
            startActivity(BibleDiscussionActivity.createIntent(requireActivity(), questionIndex = 2))
        }
    }
}
