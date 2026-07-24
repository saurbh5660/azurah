package com.live.azurah.fragment

import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.live.azurah.R
import com.live.azurah.activity.BibleQuizActivity
import com.live.azurah.databinding.FragmentQuizQuestionBinding
import com.live.azurah.databinding.ItemQuizOptionBinding
import com.live.azurah.model.BibleQuizData

class QuizQuestionFragment : Fragment() {
    private var _binding: FragmentQuizQuestionBinding? = null
    private val binding get() = _binding!!
    private var timer: CountDownTimer? = null
    private var answered = false
    private val letterLabels = listOf("A", "B", "C", "D")

    private lateinit var optionBindings: List<ItemQuizOptionBinding>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentQuizQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        optionBindings = listOf(
            binding.optionA,
            binding.optionB,
            binding.optionC,
            binding.optionD
        )

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = bars.top)
            binding.tvNext.updatePadding(bottom = 0)
            (binding.tvNext.layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin =
                bars.bottom + (12 * resources.displayMetrics.density).toInt()
            insets
        }

        binding.tvClose.setOnClickListener { activity?.finish() }
        binding.tvNext.setOnClickListener {
            val host = activity as? BibleQuizActivity ?: return@setOnClickListener
            if (host.questionIndex >= BibleQuizData.questions.lastIndex) {
                host.showResultFragment()
            } else {
                host.questionIndex++
                host.showQuestionFragment()
            }
        }
        bindQuestion()
    }

    private fun bindQuestion() {
        val host = activity as? BibleQuizActivity ?: return
        val question = BibleQuizData.questions[host.questionIndex]
        answered = false
        binding.feedbackCard.visibility = View.GONE
        binding.tvNext.visibility = View.GONE
        binding.tvTimeLabel.text = "30 sec per Q"
        binding.tvScore.text = host.score.toString()
        binding.tvQuestionMeta.text = "QUESTION ${host.questionIndex + 1} OF 5 • 100 PTS IF CORRECT"
        binding.tvQuestion.text = question.question
        updateProgress(host)

        optionBindings.forEachIndexed { index, optionBinding ->
            optionBinding.root.alpha = 1f
            optionBinding.root.isClickable = true
            optionBinding.tvOptionLetter.text = letterLabels[index]
            optionBinding.tvOptionText.text = question.options.getOrElse(index) { "" }
            optionBinding.root.setOnClickListener {
                if (!answered) showFeedback(index)
            }
        }
        startTimer()
    }

    private fun updateProgress(host: BibleQuizActivity, forceWrong: Boolean = false) {
        val segments = listOf(binding.seg0, binding.seg1, binding.seg2, binding.seg3, binding.seg4)
        segments.forEachIndexed { index, view ->
            view.setBackgroundResource(
                when {
                    index < host.questionIndex -> R.drawable.quiz_progress_correct
                    index == host.questionIndex && forceWrong -> R.drawable.quiz_progress_wrong
                    index == host.questionIndex && answered && !forceWrong -> R.drawable.quiz_progress_correct
                    index == host.questionIndex -> R.drawable.quiz_progress_current
                    else -> R.drawable.quiz_progress_pending
                }
            )
        }
    }

    private fun startTimer() {
        timer?.cancel()
        binding.timerCircle.setRemainingSeconds(30)
        timer = object : CountDownTimer(30_000, 250) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = ((millisUntilFinished + 999) / 1000).toInt().coerceAtLeast(0)
                binding.timerCircle.setRemainingSeconds(seconds)
            }

            override fun onFinish() {
                if (!answered) {
                    binding.timerCircle.showTimeUp()
                    binding.tvTimeLabel.text = "Time's up!"
                    showFeedback(-1)
                }
            }
        }.start()
    }

    private fun showFeedback(selectedIndex: Int) {
        val host = activity as? BibleQuizActivity ?: return
        val question = BibleQuizData.questions[host.questionIndex]
        answered = true
        timer?.cancel()

        val isCorrect = selectedIndex == question.correctIndex
        if (isCorrect) {
            host.score += 100
            host.correctCount++
            binding.tvTimeLabel.text = "30 sec per Q"
        } else if (selectedIndex < 0) {
            binding.timerCircle.showTimeUp()
            binding.tvTimeLabel.text = "Time's up!"
        }

        binding.tvScore.text = host.score.toString()
        updateProgress(host, forceWrong = !isCorrect)

        optionBindings.forEachIndexed { index, optionBinding ->
            optionBinding.root.isClickable = false
            optionBinding.root.alpha =
                if (index == selectedIndex || index == question.correctIndex) 1f else 0.35f
        }

        binding.feedbackCard.visibility = View.VISIBLE
        binding.feedbackCard.setBackgroundResource(
            if (isCorrect) R.drawable.quiz_feedback_correct_background
            else R.drawable.quiz_feedback_wrong_background
        )
        binding.tvFeedbackTitle.text = if (isCorrect) "😊 Correct!" else "😔 Not quite!"
        binding.tvFeedbackTitle.setTextColor(
            Color.parseColor(if (isCorrect) "#16A34A" else "#EF4444")
        )
        binding.tvPtsBadge.text = if (isCorrect) "+100 pts" else "+0 pts"
        binding.tvPtsBadge.setTextColor(
            Color.parseColor(if (isCorrect) "#16A34A" else "#EF4444")
        )
        binding.tvPtsBadge.setBackgroundResource(
            if (isCorrect) R.drawable.leaderboard_icon_green else R.drawable.quiz_pts_badge_wrong
        )

        val correctLetter = letterLabels[question.correctIndex]
        binding.tvCorrectAnswer.text = "$correctLetter — ${question.options[question.correctIndex]}"
        binding.tvContext.text = question.context
        binding.tvVerse.text = "— ${question.verse}"

        binding.tvNext.visibility = View.VISIBLE
        binding.tvNext.text =
            if (host.questionIndex >= BibleQuizData.questions.lastIndex) "See Results →" else "Next Question →"
        binding.tvNext.setBackgroundResource(
            if (isCorrect) R.drawable.quiz_next_button_correct else R.drawable.quiz_next_button_wrong
        )

        binding.scrollContent.post {
            binding.scrollContent.fullScroll(View.FOCUS_DOWN)
        }
    }

    override fun onDestroyView() {
        timer?.cancel()
        _binding = null
        super.onDestroyView()
    }
}
