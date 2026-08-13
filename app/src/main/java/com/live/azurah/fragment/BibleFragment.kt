package com.live.azurah.fragment

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.live.azurah.R
import com.live.azurah.activity.AboutQuestActivity
import com.live.azurah.activity.BibleDevotionalActivity
import com.live.azurah.activity.BibleDiscussionActivity
import com.live.azurah.activity.BibleLeaderboardActivity
import com.live.azurah.activity.BiblePrayerActivity
import com.live.azurah.activity.BibleQuizActivity
import com.live.azurah.databinding.FragmentBibleBinding
import com.live.azurah.model.ActiveChallengeResponse
import com.live.azurah.retrofit.Status
import com.live.azurah.util.gone
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel

class BibleFragment : Fragment() {
    private lateinit var binding: FragmentBibleBinding
    private lateinit var commonViewModel: CommonViewModel

    private var questTitle: String = ""
    private var questId: Int = 0
    private var challengeId: Int = 0
    private var dayNo: Int = 1
    private var discussionOne: ActiveChallengeResponse.Body.Challenge.Discussion? = null
    private var discussionTwo: ActiveChallengeResponse.Body.Challenge.Discussion? = null
    private var currentQuest: ActiveChallengeResponse.Body.Quest? = null
    private var currentChallenge: ActiveChallengeResponse.Body.Challenge? = null

    private val aboutQuestLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                fetchActiveChallenge()
            }
        }

    private val journeyLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                fetchActiveChallenge()
            }
        }

    private val quizLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            fetchActiveChallenge()
        }

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
            AboutQuizBottomSheet.newInstance(questId, challengeId, dayNo).show(
                childFragmentManager,
                AboutQuizBottomSheet::class.java.simpleName
            )
        }
        binding.quizCard.tvStartQuiz.setOnClickListener {
            val challenge = currentChallenge
            val isBothCompleted =
                ((challenge?.isDevotionalCompleted ?: 0) == 1 && (challenge?.isPrayerCompleted
                    ?: 0) == 1)
            if (!isBothCompleted) {
                Toast.makeText(
                    requireContext(),
                    "Please complete Devotional and Prayer first!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                launchQuiz()
            }
        }
        binding.tvLeaderboard.setOnClickListener {
            startActivity(Intent(requireActivity(), BibleLeaderboardActivity::class.java))
        }
        binding.communityCard.questionOne.tvDiscuss.setOnClickListener {
            startActivity(
                BibleDiscussionActivity.createIntent(
                    requireActivity(),
                    1,
                    challengeId,
                    questTitle,
                    dayNo,
                    discussionOne?.id ?: 0,
                    discussionOne?.description ?: ""
                )
            )
        }
        binding.communityCard.questionTwo.tvDiscuss.setOnClickListener {
            startActivity(
                BibleDiscussionActivity.createIntent(
                    requireActivity(),
                    2,
                    challengeId,
                    questTitle,
                    dayNo,
                    discussionTwo?.id ?: 0,
                    discussionTwo?.description ?: ""
                )
            )
        }

        binding.llAboutQuestContainer.setOnClickListener {
            openAboutQuestScreen()
        }
        binding.tvAboutQuest.setOnClickListener {
            openAboutQuestScreen()
        }

        binding.devotionalCard.root.setOnClickListener {
            openDevotionalScreen()
        }

        binding.prayerCard.root.setOnClickListener {
            openPrayerScreen()
        }

        fetchActiveChallenge()
    }

    fun launchQuiz() {
        if (challengeId == 0) return
        quizLauncher.launch(
            BibleQuizActivity.createIntent(requireActivity(), questId, challengeId, dayNo)
        )
    }

    private fun updateQuizCardsVisibility(isQuizCompleted: Boolean) {
        if (isQuizCompleted) {
            binding.quizCard.root.visibility = View.GONE
            binding.quizCompleteCard.root.visibility = View.VISIBLE
        } else {
            binding.quizCard.root.visibility = View.VISIBLE
            binding.quizCompleteCard.root.visibility = View.GONE
        }
    }

    private fun openDevotionalScreen() {
        val quest = currentQuest
        val challenge = currentChallenge
        val devotional = challenge?.devotionals?.firstOrNull()

        val intent = Intent(requireContext(), BibleDevotionalActivity::class.java).apply {
            putExtra("bible_quest_id", quest?.id?.toString() ?: "")
            putExtra("bible_quest_challenge_id", challenge?.id?.toString() ?: "")
            putExtra("bible_quest_challenge_devotional_id", devotional?.id?.toString() ?: "")
            putExtra("day_no", dayNo.toString())
            putExtra("title", devotional?.title ?: "Devotional")
            putExtra("description", devotional?.description ?: "")
            putExtra("read_time", challenge?.readTime ?: 5)
            putExtra("verse_ref", quest?.bibleVersion ?: "")
            putExtra("is_completed", challenge?.isDevotionalCompleted ?: 0)
        }
        journeyLauncher.launch(intent)
    }

    private fun openPrayerScreen() {
        val challenge = currentChallenge
        if ((challenge?.isDevotionalCompleted ?: 0) == 0) {
            Toast.makeText(
                requireContext(),
                "Please complete today's Devotional first!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val quest = currentQuest
        val prayer = challenge?.prayers?.firstOrNull()

        val intent = Intent(requireContext(), BiblePrayerActivity::class.java).apply {
            putExtra("bible_quest_id", quest?.id?.toString() ?: "")
            putExtra("bible_quest_challenge_id", challenge?.id?.toString() ?: "")
            putExtra("bible_quest_challenge_prayer_id", prayer?.id?.toString() ?: "")
            putExtra("day_no", dayNo.toString())
            putExtra("title", prayer?.title ?: "Prayer")
            putExtra("description", prayer?.description ?: "")
            putExtra("is_completed", challenge?.isPrayerCompleted ?: 0)
            putExtra("is_devotional_completed", challenge?.isDevotionalCompleted ?: 0)
            putExtra("streak_count", quest?.totalCompletedDayCount ?: 1)
        }
        journeyLauncher.launch(intent)
    }

    private fun openAboutQuestScreen() {
        val quest = currentQuest
        val intent = Intent(requireContext(), AboutQuestActivity::class.java).apply {
            putExtra("quest_title", quest?.title ?: questTitle)
            putExtra("quest_description", quest?.description ?: "")
            putExtra("bible_verse", quest?.bibleVerse ?: "")
            putExtra("bible_version", quest?.bibleVersion ?: "")
            putExtra("quick_facts", quest?.quickFacts ?: "")
            putExtra("total_days", quest?.totalChallengeDayCount ?: 1)
        }
        aboutQuestLauncher.launch(intent)
    }

    private fun fetchActiveChallenge() {
        commonViewModel.getActiveChallenge(requireActivity())
            .observe(viewLifecycleOwner) { response ->
                when (response.status) {
                    Status.LOADING -> {
                        binding.shimmerLayout.visible()
                        binding.shimmerLayout.startShimmer()
                        binding.contentLayout.gone()
                    }

                    Status.SUCCESS -> {
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.gone()
                        binding.contentLayout.visible()

                        response.data?.let { data ->
                            if (data is ActiveChallengeResponse && data.success == true) {
                                val quest = data.body?.quest
                                val challenge = data.body?.challenge
                                currentQuest = quest
                                currentChallenge = challenge

                                if (quest != null && challenge != null) {
                                    questTitle = quest.title ?: ""
                                    questId = quest.id ?: challenge.bibleQuestId ?: 0
                                    challengeId = challenge.id ?: 0
                                    dayNo = challenge.dayNo ?: 1
                                    val readTime = challenge.readTime ?: 5
                                    val totalDays = quest.totalChallengeDayCount ?: 1
                                    val completedDays = quest.totalCompletedDayCount ?: 0

                                    binding.tvQuestTitle.text = questTitle
                                    binding.tvDayCount.text = buildString {
                                        append("Day ")
                                        append(completedDays)
                                        append(" of ")
                                        append(totalDays)
                                    }

                                    // Progress Percent & Bar using total_completed_day_count & total_challenge_day_count
                                    val dayFraction =
                                        (if (challenge.isDevotionalCompleted == 1) 0.5f else 0f) + (if (challenge.isPrayerCompleted == 1) 0.5f else 0f)
                                    val effectiveCompleted =
                                        if (completedDays >= dayNo) completedDays.toFloat() else (completedDays.toFloat() + dayFraction)
                                    val percent =
                                        if (totalDays > 0) ((effectiveCompleted / totalDays.toFloat()) * 100).toInt()
                                            .coerceIn(0, 100) else 0
                                    binding.tvCompletePercent.text = "$percent% complete"
                                    binding.progressQuest.progress = percent

                                    // Devotional Card UI & Checkmark / Unchecked Circle
                                    if (challenge.isDevotionalCompleted == 1) {
                                        binding.devotionalCard.tvCheck.setBackgroundResource(R.drawable.circle_background)
                                        binding.devotionalCard.tvCheck.backgroundTintList =
                                            ColorStateList.valueOf(0xFF22C55E.toInt())
                                        binding.devotionalCard.tvCheck.text = "✓"
                                        binding.devotionalCard.tvCheck.setTextColor(android.graphics.Color.WHITE)
                                        binding.devotionalCard.tvSubtitle.text =
                                            "Completed · $readTime min read"
                                        binding.devotionalCard.tvActionBanner.text =
                                            "✓ Revisit Devotional"
                                    } else {
                                        binding.devotionalCard.tvCheck.setBackgroundResource(R.drawable.circle_blue_stroke)
                                        binding.devotionalCard.tvCheck.backgroundTintList = null
                                        binding.devotionalCard.tvCheck.text = ""
                                        binding.devotionalCard.tvSubtitle.text =
                                            "$readTime min read"
                                        binding.devotionalCard.tvActionBanner.text =
                                            "✓ Start Devotional"
                                    }

                                    // Prayer Card UI (Locked / Unchecked Circle / Completed)
                                    if (challenge.isDevotionalCompleted == 0) {
                                        binding.prayerCard.tvCheck.setBackgroundResource(R.drawable.circle_lock_bg)
                                        binding.prayerCard.tvCheck.backgroundTintList = null
                                        binding.prayerCard.tvCheck.text = "🔒"
                                        binding.prayerCard.tvCheck.setTextColor(
                                            android.graphics.Color.parseColor(
                                                "#0284C7"
                                            )
                                        )
                                        binding.prayerCard.tvSubtitle.text = "Guided prayer"
                                        binding.prayerCard.tvActionBanner.text = "✓ Start Prayer"
                                    } else if (challenge.isPrayerCompleted == 1) {
                                        binding.prayerCard.tvCheck.setBackgroundResource(R.drawable.circle_background)
                                        binding.prayerCard.tvCheck.backgroundTintList =
                                            ColorStateList.valueOf(0xFF22C55E.toInt())
                                        binding.prayerCard.tvCheck.text = "✓"
                                        binding.prayerCard.tvCheck.setTextColor(android.graphics.Color.WHITE)
                                        binding.prayerCard.tvSubtitle.text =
                                            "Completed · Guided prayer"
                                        binding.prayerCard.tvActionBanner.text = "✓ Revisit Prayer"
                                    } else {
                                        binding.prayerCard.tvCheck.setBackgroundResource(R.drawable.circle_blue_stroke)
                                        binding.prayerCard.tvCheck.backgroundTintList = null
                                        binding.prayerCard.tvCheck.text = ""
                                        binding.prayerCard.tvSubtitle.text = "Guided prayer"
                                        binding.prayerCard.tvActionBanner.text = "✓ Start Prayer"
                                    }

                                    val qCount = challenge.questionCount ?: 5
                                    binding.quizCard.tvQuizTitle.text = "Quiz"
                                    binding.quizCard.tvQuizDesc.text =
                                        "$qCount questions on today's reading"

                                    // Community Discussions & Comment Counts
                                    val discussions = challenge.discussions
                                    if (!discussions.isNullOrEmpty()) {
                                        discussionOne = discussions.getOrNull(0)
                                        discussionTwo = discussions.getOrNull(1)

                                        val c1 = discussionOne?.commentCount ?: 0
                                        val c2 = discussionTwo?.commentCount ?: 0
                                        val totalResponses = c1 + c2

                                        binding.tvCommunityCompactSubtitle.text =
                                            "${discussions.size} questions · $totalResponses responses"

                                        if (discussionOne != null) {
                                            binding.communityCard.questionOne.tvQuestion.text =
                                                discussionOne?.title ?: ""
                                            binding.communityCard.questionOne.tvDesc.text =
                                                discussionOne?.description ?: ""
                                            binding.communityCard.questionOne.tvResponses.text =
                                                "$c1 responses"
                                        }
                                        if (discussionTwo != null) {
                                            binding.communityCard.questionTwo.tvQuestion.text =
                                                discussionTwo?.title ?: ""
                                            binding.communityCard.questionTwo.tvDesc.text =
                                                discussionTwo?.description ?: ""
                                            binding.communityCard.questionTwo.tvResponses.text =
                                                "$c2 responses"
                                        }
                                    }

                                    updateQuizCardsVisibility(challenge.isQuizCompleted == 1)

                                    binding.quizCard.tvStartQuiz.visibility = View.VISIBLE
                                    val isBothCompleted =
                                        (challenge.isDevotionalCompleted == 1 && challenge.isPrayerCompleted == 1)
                                    if (isBothCompleted) {
                                        binding.quizCard.tvStartQuizText.text = "Start Quiz →"
                                        binding.streakCompleteCard.visibility = View.VISIBLE
                                        val streakCount = quest.totalCompletedDayCount ?: 1
                                        binding.tvStreakSafeSubtitle.text =
                                            "Devotional & Prayer done — your $streakCount-day streak is safe."
                                    } else {
                                        binding.quizCard.tvStartQuizText.text = "Start Quiz 🔒"
                                        binding.streakCompleteCard.visibility = View.GONE
                                    }
                                }
                            }
                        }
                    }

                    Status.ERROR -> {
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.gone()
                        binding.contentLayout.visible()
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                            .show()
                    }

                    else -> {}
                }
            }
    }
}

