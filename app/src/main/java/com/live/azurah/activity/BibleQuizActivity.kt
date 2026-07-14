package com.live.azurah.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.live.azurah.R
import com.live.azurah.databinding.ActivityBibleQuizBinding
import com.live.azurah.fragment.QuizCountdownFragment
import com.live.azurah.fragment.QuizQuestionFragment
import com.live.azurah.fragment.QuizResultFragment

class BibleQuizActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBibleQuizBinding

    var questionIndex = 0
    var score = 0
    var correctCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBibleQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.dashboard_primary)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        if (savedInstanceState == null) {
            showFragment(QuizCountdownFragment(), false)
        }
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
        const val QUIZ_COMPLETED_KEY = "bible_quiz_day_5_completed"
    }
}
