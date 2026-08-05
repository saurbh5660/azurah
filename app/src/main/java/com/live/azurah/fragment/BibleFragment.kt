package com.live.azurah.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.live.azurah.activity.BibleDiscussionActivity
import com.live.azurah.activity.BibleLeaderboardActivity
import com.live.azurah.activity.BibleQuizActivity
import com.live.azurah.databinding.FragmentBibleBinding
import com.live.azurah.model.ActiveChallengeResponse
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.viewmodel.CommonViewModel

class BibleFragment : Fragment() {
    private lateinit var binding: FragmentBibleBinding
    private lateinit var commonViewModel: CommonViewModel
    
    private var questTitle: String = ""
    private var challengeId: Int = 0
    private var dayNo: Int = 1
    private var discussionOne: ActiveChallengeResponse.Body.Challenge.Discussion? = null
    private var discussionTwo: ActiveChallengeResponse.Body.Challenge.Discussion? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBibleBinding.inflate(inflater, container, false)
        commonViewModel = ViewModelProvider(requireActivity())[CommonViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
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
            if (challengeId != 0) {
                startActivity(Intent(requireActivity(), BibleQuizActivity::class.java).apply {
                    putExtra("challenge_id", challengeId)
                })
            }
        }
        binding.tvLeaderboard.setOnClickListener {
            startActivity(Intent(requireActivity(), BibleLeaderboardActivity::class.java))
        }
        binding.communityCard.questionOne.tvDiscuss.setOnClickListener {
            startActivity(BibleDiscussionActivity.createIntent(requireActivity(), 1, challengeId, questTitle, dayNo, discussionOne?.id ?: 0, discussionOne?.description ?: ""))
        }
        binding.communityCard.questionTwo.tvDiscuss.setOnClickListener {
            startActivity(BibleDiscussionActivity.createIntent(requireActivity(), 2, challengeId, questTitle, dayNo, discussionTwo?.id ?: 0, discussionTwo?.description ?: ""))
        }

        fetchActiveChallenge()
    }

    private fun fetchActiveChallenge() {
        commonViewModel.getActiveChallenge(requireActivity()).observe(viewLifecycleOwner) { response ->
            when (response.status) {
                Status.LOADING -> {}
                Status.SUCCESS -> {
                    response.data?.let { data ->
                        if (data is ActiveChallengeResponse && data.success == true) {
                            val quest = data.body?.quest
                            val challenge = data.body?.challenge
                            
                            if (quest != null && challenge != null) {
                                questTitle = quest.title ?: ""
                                challengeId = challenge.id ?: 0
                                dayNo = challenge.dayNo ?: 1

                                binding.quizCard.tvQuizDesc.text = Html.fromHtml(quest.description ?: "", Html.FROM_HTML_MODE_LEGACY).toString().trim()
                                binding.quizCard.tvQuizTitle.text = questTitle

                                if (challenge.discussions != null && challenge.discussions.size >= 2) {
                                    discussionOne = challenge.discussions[0]
                                    discussionTwo = challenge.discussions[1]
                                    
                                    binding.communityCard.questionOne.tvQuestion.text = discussionOne?.title ?: ""
                                    binding.communityCard.questionOne.tvDesc.text = discussionOne?.description ?: ""
                                    
                                    binding.communityCard.questionTwo.tvQuestion.text = discussionTwo?.title ?: ""
                                    binding.communityCard.questionTwo.tvDesc.text = discussionTwo?.description ?: ""
                                }

                                if (challenge.isDevotionalCompleted == 1 && challenge.isPrayerCompleted == 1) {
                                    binding.quizCard.tvStartQuiz.visibility = View.VISIBLE
                                } else {
                                    binding.quizCard.tvStartQuiz.visibility = View.GONE
                                }
                            }
                        }
                    }
                }
                Status.ERROR -> {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }
}
