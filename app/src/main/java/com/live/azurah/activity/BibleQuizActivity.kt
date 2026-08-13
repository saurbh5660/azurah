package com.live.azurah.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.live.azurah.R
import com.live.azurah.databinding.ActivityBibleQuizBinding
import com.live.azurah.fragment.QuizCountdownFragment
import com.live.azurah.fragment.QuizQuestionFragment
import com.live.azurah.fragment.QuizResultFragment
import com.live.azurah.model.QuizAnswerRequest
import com.live.azurah.model.QuizQuestion
import com.live.azurah.model.QuizQuestionResponse
import com.live.azurah.retrofit.Status
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BibleQuizActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBibleQuizBinding
    val viewModel: CommonViewModel by viewModels()

    var questionIndex = 0
    var score = 0
    var correctCount = 0

    var questId: Int = -1
    var challengeId: Int = -1
    var dayNo: Int = -1

    var questions: List<QuizQuestion> = emptyList()
    val answers: MutableList<QuizAnswerRequest> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBibleQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.dashboard_primary)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        questId = intent.getIntExtra(EXTRA_QUEST_ID, -1)
        challengeId = intent.getIntExtra(EXTRA_CHALLENGE_ID, -1)
        dayNo = intent.getIntExtra(EXTRA_DAY_NO, -1)

        if (savedInstanceState == null) {
            fetchQuestions()
        }

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showQuitDialog()
            }
        })
    }

    private fun fetchQuestions() {
        if (challengeId == -1) {
            Toast.makeText(this, "Invalid Challenge", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        viewModel.getQuizQuestionList(challengeId, this).observe(this, Observer { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    val res = resource.data as? QuizQuestionResponse
                    if (res?.success == true && !res.body.isNullOrEmpty()) {
                        questions = res.body
                        if (questId == -1) {
                            questId = questions.firstOrNull()?.bibleQuestId ?: -1
                        }
                        showFragment(QuizCountdownFragment(), false)
                    } else {
                        Toast.makeText(this, "No questions found.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                Status.ERROR -> {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this, resource.message ?: "Failed to load questions", Toast.LENGTH_SHORT).show()
                    finish()
                }
                Status.LOADING -> {
                    binding.progressBar.visibility = android.view.View.VISIBLE
                }
            }
        })
    }

    fun showQuitDialog() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Quit Quiz")
            .setMessage("Are you sure you want to quit the quiz?")
            .setPositiveButton("Yes") { _, _ ->
                quitQuiz()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun quitQuiz() {
        val request = com.live.azurah.model.QuizQuitRequest(
            bibleQuestId = questId,
            bibleQuestChallengeId = challengeId,
            dayNo = dayNo
        )
        viewModel.quitQuiz(request, this).observe(this, Observer { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    finish()
                }
                Status.ERROR -> {
                    Toast.makeText(this, resource.message ?: "Failed to quit", Toast.LENGTH_SHORT).show()
                    finish()
                }
                Status.LOADING -> {
                    // Could show loading
                }
            }
        })
    }

    fun showQuestionFragment() {
        showFragment(QuizQuestionFragment(), false)
    }

    fun showResultFragment() {
        showFragment(QuizResultFragment(), false)
    }

    private fun showFragment(fragment: Fragment, addToBackStack: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.quizContainer, fragment)
        if (addToBackStack) transaction.addToBackStack(null)
        transaction.commit()
    }

    companion object {
        const val EXTRA_QUEST_ID = "questId"
        const val EXTRA_CHALLENGE_ID = "challenge_id"
        const val EXTRA_DAY_NO = "dayNo"
        const val QUIZ_COMPLETED_KEY = "bible_quiz_day_5_completed"

        fun createIntent(context: Context, questId: Int, challengeId: Int, dayNo: Int): Intent {
            return Intent(context, BibleQuizActivity::class.java).apply {
                putExtra(EXTRA_QUEST_ID, questId)
                putExtra(EXTRA_CHALLENGE_ID, challengeId)
                putExtra(EXTRA_DAY_NO, dayNo)
            }
        }
    }
}
