package com.live.azurah.activity

import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import com.live.azurah.R
import com.live.azurah.databinding.ActivityBibleQuizBinding

class BibleQuizActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBibleQuizBinding
    private var questionIndex = 0
    private var score = 0
    private var correctCount = 0
    private var timer: CountDownTimer? = null
    private var timerView: TextView? = null
    private var systemTopInset = 0
    private var systemBottomInset = 0

    private val questions = listOf(
        QuizQuestion(
            question = "Why did Esther initially hesitate before approaching King Ahasuerus?",
            options = listOf(
                "She was afraid of public speaking",
                "Anyone who entered the king's court unsummoned risked death",
                "She did not believe Mordecai's warning",
                "She had not fasted and prayed as required"
            ),
            correctIndex = 1,
            context = "Under Persian law, approaching the king unsummoned was punishable by death — even for the queen. Esther's courage wasn't the absence of fear — it was choosing faith over it.",
            verse = "Esther 4:11"
        ),
        QuizQuestion(
            question = "What did Mordecai say would happen if Esther did not approach the king?",
            options = listOf(
                "He would go to the king himself",
                "The Jewish people would find deliverance another way, but Esther's family would perish",
                "He would fast and pray alone",
                "God would send another messenger"
            ),
            correctIndex = 1
        ),
        QuizQuestion(
            question = "How many days did Esther ask the Jewish people to fast?",
            options = listOf("1 day", "3 days", "7 days", "40 days"),
            correctIndex = 1
        ),
        QuizQuestion(
            question = "What did Esther ask Mordecai and the Jews to do before she went to the king?",
            options = listOf("Prepare gifts", "Leave the city", "Fast for her", "Write another letter"),
            correctIndex = 2
        ),
        QuizQuestion(
            question = "What did Esther say she would do if she perished?",
            options = listOf(
                "She said she would not go to the king",
                "She said 'if I perish, I perish' — and went anyway",
                "She asked God for a sign first",
                "She sent Mordecai in her place"
            ),
            correctIndex = 1
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBibleQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.dashboard_primary)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            systemTopInset = systemBars.top
            systemBottomInset = systemBars.bottom
            view.updatePadding(bottom = systemBottomInset)
            insets
        }
        showCountdown(3)
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }

    private fun showCountdown(value: Int) {
        timer?.cancel()
        binding.quizRoot.removeAllViews()
        binding.quizRoot.setBackgroundColor(getColor(R.color.dashboard_primary))

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), systemTopInset + dp(18), dp(16), dp(34))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        text("‹", 24, R.color.white, R.font.poppins_semibold).apply {
            background = ContextCompat.getDrawable(this@BibleQuizActivity, R.drawable.dashboard_icon_circle)
            gravity = Gravity.CENTER
            setOnClickListener { finish() }
            root.addView(this, LinearLayout.LayoutParams(dp(36), dp(36)))
        }

        root.addView(text("QUIZ • DAY 5", 8, R.color.dashboard_subtitle, R.font.poppins_semibold).apply {
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams.MATCH_PARENT, dp(60))

        root.addView(text("Esther — Such a Time as This", 20, R.color.white, R.font.poppins_semibold).apply {
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        root.addView(
            View(this),
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        )

        root.addView(LinearLayout(this).apply {
            gravity = Gravity.CENTER
            background = ContextCompat.getDrawable(this@BibleQuizActivity, R.drawable.quiz_countdown_circle)
            orientation = LinearLayout.VERTICAL
            addView(text("GET READY", 9, R.color.dashboard_subtitle, R.font.poppins_semibold).apply {
                gravity = Gravity.CENTER
                letterSpacing = 0.18f
            }, LinearLayout.LayoutParams.MATCH_PARENT, dp(40))
            addView(text(value.toString(), 62, R.color.white, R.font.cinzel_bold).apply {
                gravity = Gravity.CENTER
            }, LinearLayout.LayoutParams.MATCH_PARENT, dp(90))
        }, LinearLayout.LayoutParams(dp(220), dp(220)).apply { gravity = Gravity.CENTER_HORIZONTAL })

        root.addView(
            View(this),
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        )

        root.addView(createStatsRow())
        binding.quizRoot.addView(root)

        if (value > 1) {
            binding.quizRoot.postDelayed({ showCountdown(value - 1) }, 1000)
        } else {
            binding.quizRoot.postDelayed({ showQuestion() }, 1000)
        }
    }

    private fun showQuestion() {
        timer?.cancel()
        binding.quizRoot.removeAllViews()
        binding.quizRoot.setBackgroundColor(getColor(R.color.dashboard_background))
        val question = questions[questionIndex]

        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, systemBottomInset + dp(20))
        }
        scroll.addView(root)
        root.addView(createQuizHeader())
        root.addView(questionCard(question))

        question.options.forEachIndexed { index, option ->
            root.addView(optionCard(index, option) {
                showAnswerFeedback(index)
            })
        }
        binding.quizRoot.addView(scroll)
        startQuestionTimer()
    }

    private fun showAnswerFeedback(selectedIndex: Int) {
        timer?.cancel()
        val question = questions[questionIndex]
        val isCorrect = selectedIndex == question.correctIndex
        if (isCorrect) {
            score += 100
            correctCount++
        }

        binding.quizRoot.removeAllViews()
        val screen = FrameLayout(this)
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, systemBottomInset + dp(96))
        }
        scroll.addView(root)
        root.addView(createQuizHeader(timeText = if (isCorrect) "30 sec per Q" else "Time's up!"))
        question.options.forEachIndexed { index, option ->
            val optionView = optionCard(index, option, null)
            optionView.alpha = if (index == selectedIndex || index == question.correctIndex) 1f else 0.35f
            root.addView(optionView)
        }
        root.addView(feedbackCard(question, isCorrect))
        val nextButton = text(if (questionIndex == questions.lastIndex) "See Results  →" else "Next Question  →", 14, R.color.white, R.font.poppins_semibold).apply {
            gravity = Gravity.CENTER
            background = ContextCompat.getDrawable(this@BibleQuizActivity, R.drawable.dashboard_button_background)
            setOnClickListener {
                if (questionIndex == questions.lastIndex) showFinalResult() else {
                    questionIndex++
                    showQuestion()
                }
            }
        }
        screen.addView(scroll)
        screen.addView(
            nextButton,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dp(52),
                Gravity.BOTTOM
            ).apply {
                setMargins(dp(16), 0, dp(16), systemBottomInset + dp(14))
            }
        )
        binding.quizRoot.addView(screen)
    }

    private fun showFinalResult() {
        timer?.cancel()
        binding.quizRoot.removeAllViews()
        binding.quizRoot.setBackgroundColor(getColor(R.color.dashboard_background))
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, systemBottomInset + dp(22))
        }
        root.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(ContextCompat.getColor(this@BibleQuizActivity, R.color.dashboard_primary))
            addView(text("AZRIUS", 20, R.color.white, R.font.cinzel_bold).apply { gravity = Gravity.CENTER }, LinearLayout.LayoutParams.MATCH_PARENT, dp(54))
            addView(text("🏅", 46, R.color.white, R.font.poppins_semibold).apply { gravity = Gravity.CENTER }, LinearLayout.LayoutParams.MATCH_PARENT, dp(66))
            addView(text("Keep Learning!", 22, R.color.white, R.font.poppins_semibold).apply { gravity = Gravity.CENTER }, LinearLayout.LayoutParams.MATCH_PARENT, dp(34))
            addView(text("Esther · Day 5 · $correctCount of 5 correct", 13, R.color.white, R.font.poppins).apply { gravity = Gravity.CENTER }, LinearLayout.LayoutParams.MATCH_PARENT, dp(30))
            addView(createFinalStats(), LinearLayout.LayoutParams.MATCH_PARENT, dp(86))
        }, LinearLayout.LayoutParams.MATCH_PARENT, dp(280))

        root.addView(text("Today's Leaderboard Points", 16, R.color.dashboard_card_text, R.font.poppins_semibold), marginParams(top = 22))
        root.addView(summaryRow("📖", "Bible Quest Completed", "Devotional + Prayer done", "+500"))
        root.addView(summaryRow("🧠", "Quiz Score · $correctCount/5 correct", "100 pts per correct answer", "+$score"))
        root.addView(text("⭐  Total earned today                                      ${500 + score}", 18, R.color.white, R.font.poppins_semibold).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), 0, dp(16), 0)
            background = ContextCompat.getDrawable(this@BibleQuizActivity, R.drawable.bible_discuss_button_background)
        }, marginParams(height = 64, top = 12))
        root.addView(text("🏆  View Leaderboard", 14, R.color.white, R.font.poppins_semibold).apply {
            gravity = Gravity.CENTER
            background = ContextCompat.getDrawable(this@BibleQuizActivity, R.drawable.round_green_corner_background)
        }, marginParams(height = 52, top = 22))
        root.addView(text("← Back to Today's Journey", 14, R.color.dashboard_subtitle, R.font.poppins_semibold).apply {
            gravity = Gravity.CENTER
            background = ContextCompat.getDrawable(this@BibleQuizActivity, R.drawable.quiz_option_card_background)
            setOnClickListener { finish() }
        }, marginParams(height = 52, top = 12))
        binding.quizRoot.addView(root)
    }

    private fun createQuizHeader(timeText: String = "30 sec per Q"): View {
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), systemTopInset + dp(12), dp(16), dp(16))
            setBackgroundColor(ContextCompat.getColor(this@BibleQuizActivity, R.color.dashboard_primary))
        }
        val titleRow = FrameLayout(this)
        titleRow.addView(text("AZRIUS", 21, R.color.white, R.font.cinzel_bold).apply {
            gravity = Gravity.CENTER
        }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp(46)))
        titleRow.addView(text("×", 25, R.color.white, R.font.poppins).apply {
            gravity = Gravity.CENTER
            background = ContextCompat.getDrawable(this@BibleQuizActivity, R.drawable.dashboard_icon_circle)
            setOnClickListener { finish() }
        }, FrameLayout.LayoutParams(dp(36), dp(36), Gravity.END or Gravity.CENTER_VERTICAL))
        header.addView(titleRow)
        header.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(14))
            background = ContextCompat.getDrawable(this@BibleQuizActivity, R.drawable.quiz_blue_card_background)
            addView(text("🧠 QUIZ · DAY 5                                      ▣ Pro", 10, R.color.white, R.font.poppins_semibold))
            addView(LinearLayout(this@BibleQuizActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                weightSum = 2f
                addView(metricBox("TIME LEFT", timeText, timer = true), LinearLayout.LayoutParams(0, dp(64), 1f).apply { setMargins(0, dp(12), dp(8), 0) })
                addView(metricBox("QUIZ SCORE", score.toString(), timer = false), LinearLayout.LayoutParams(0, dp(64), 1f).apply { setMargins(dp(8), dp(12), 0, 0) })
            })
            addView(progressSegments())
        }, LinearLayout.LayoutParams.MATCH_PARENT, dp(150))
        return header
    }

    private fun questionCard(question: QuizQuestion): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(26), dp(24), dp(26), dp(24))
            background = ContextCompat.getDrawable(this@BibleQuizActivity, R.drawable.quiz_question_card_background)
            addView(text("QUESTION ${questionIndex + 1} OF 5 · 100 PTS IF CORRECT", 9, R.color.dashboard_subtitle, R.font.poppins_semibold))
            addView(text(question.question, 22, R.color.dashboard_card_text, R.font.cinzel_bold).apply {
                setPadding(0, dp(12), 0, 0)
            })
        }.also {
            it.layoutParams = marginParams(top = -10, marginHorizontal = 20)
        }
    }

    private fun optionCard(index: Int, value: String, click: (() -> Unit)?): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(18), 0, dp(18), 0)
            background = ContextCompat.getDrawable(this@BibleQuizActivity, R.drawable.quiz_option_card_background)
            addView(text(('A' + index).toString(), 11, R.color.dashboard_subtitle, R.font.poppins_semibold).apply {
                gravity = Gravity.CENTER
                background = ContextCompat.getDrawable(this@BibleQuizActivity, R.drawable.circle_background)
                backgroundTintList = ContextCompat.getColorStateList(this@BibleQuizActivity, R.color.size_color)
            }, LinearLayout.LayoutParams(dp(38), dp(38)))
            addView(text(value, 13, R.color.dashboard_card_text, R.font.poppins_semibold), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(dp(14), 0, 0, 0)
            })
            click?.let { setOnClickListener { it() } }
        }.also {
            it.layoutParams = marginParams(height = 66, top = 14, marginHorizontal = 16)
        }
    }

    private fun feedbackCard(question: QuizQuestion, isCorrect: Boolean): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = ContextCompat.getDrawable(this@BibleQuizActivity, if (isCorrect) R.drawable.bible_journey_card_background else R.drawable.quiz_answer_wrong_background)
            addView(text(if (isCorrect) "😊 Correct!" else "😔 Not quite!                                      +0 pts", 18, if (isCorrect) R.color.green_color else R.color.light_red1, R.font.poppins_semibold))
            addView(text("CORRECT ANSWER", 8, R.color.light_red1, R.font.poppins_semibold).apply { setPadding(0, dp(14), 0, 0) })
            addView(text("${('A' + question.correctIndex)} — ${question.options[question.correctIndex]}", 14, R.color.dashboard_card_text, R.font.poppins_semibold))
            addView(text("CONTEXT", 8, R.color.light_red1, R.font.poppins_semibold).apply { setPadding(0, dp(14), 0, 0) })
            addView(text("${question.context}\n\n— ${question.verse}", 13, R.color.dashboard_subtitle, R.font.poppins))
        }.also {
            it.layoutParams = marginParams(top = 14, marginHorizontal = 16)
        }
    }

    private fun startQuestionTimer() {
        timerView?.text = "30"
        timer = object : CountDownTimer(30_000, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                timerView?.text = ((millisUntilFinished / 1000) + 1).toString()
            }

            override fun onFinish() {
                showAnswerFeedback(-1)
            }
        }.start()
    }

    private fun createStatsRow(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 3f
            addView(countdownStat("5", "QUESTIONS"), LinearLayout.LayoutParams(0, dp(90), 1f).apply { setMargins(0, 0, dp(8), 0) })
            addView(countdownStat("30S", "PER QUESTION"), LinearLayout.LayoutParams(0, dp(90), 1f).apply { setMargins(dp(4), 0, dp(4), 0) })
            addView(countdownStat("100", "PTS/CORRECT"), LinearLayout.LayoutParams(0, dp(90), 1f).apply { setMargins(dp(8), 0, 0, 0) })
        }
    }

    private fun countdownStat(value: String, label: String): View {
        return LinearLayout(this).apply {
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL
            background = ContextCompat.getDrawable(this@BibleQuizActivity, R.drawable.quiz_blue_card_background)
            addView(text(value, 20, if (value == "30S" || value == "100") R.color.golden_yellow else R.color.white, R.font.poppins_semibold).apply { gravity = Gravity.CENTER })
            addView(text(label, 7, R.color.white, R.font.poppins_semibold).apply { gravity = Gravity.CENTER })
        }
    }

    private fun metricBox(label: String, value: String, timer: Boolean): View {
        return LinearLayout(this).apply {
            gravity = Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL
            background = ContextCompat.getDrawable(this@BibleQuizActivity, R.drawable.bible_header_card_background)
            val circle = text(if (timer) "30" else "⭐", 16, R.color.white, R.font.poppins_semibold).apply {
                gravity = Gravity.CENTER
                if (timer) timerView = this
            }
            addView(circle, LinearLayout.LayoutParams(dp(42), dp(42)))
            addView(text("$label\n$value", 10, R.color.white, R.font.poppins_semibold), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }
    }

    private fun progressSegments(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            repeat(5) { index ->
                addView(View(this@BibleQuizActivity).apply {
                    setBackgroundResource(if (index <= questionIndex) R.drawable.quiz_progress_done else R.drawable.quiz_progress_pending)
                }, LinearLayout.LayoutParams(0, dp(4), 1f).apply { setMargins(dp(3), dp(14), dp(3), 0) })
            }
        }
    }

    private fun createFinalStats(): View {
        return LinearLayout(this).apply {
            gravity = Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL
            weightSum = 3f
            addView(countdownStat(score.toString(), "QUIZ SCORE"), LinearLayout.LayoutParams(0, dp(70), 1f).apply { setMargins(dp(16), dp(8), dp(5), 0) })
            addView(countdownStat("$correctCount/5", "CORRECT"), LinearLayout.LayoutParams(0, dp(70), 1f).apply { setMargins(dp(5), dp(8), dp(5), 0) })
            addView(countdownStat("+0", "LB BONUS"), LinearLayout.LayoutParams(0, dp(70), 1f).apply { setMargins(dp(5), dp(8), dp(16), 0) })
        }
    }

    private fun summaryRow(icon: String, title: String, subtitle: String, points: String): View {
        return LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(16), 0, dp(16), 0)
            background = ContextCompat.getDrawable(this@BibleQuizActivity, R.drawable.quiz_option_card_background)
            addView(text(icon, 18, R.color.dashboard_card_text, R.font.poppins_semibold).apply {
                gravity = Gravity.CENTER
                background = ContextCompat.getDrawable(this@BibleQuizActivity, R.drawable.bible_icon_background)
            }, LinearLayout.LayoutParams(dp(44), dp(44)))
            addView(text("$title\n$subtitle", 12, R.color.dashboard_card_text, R.font.poppins_semibold), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(dp(12), 0, 0, 0)
            })
            addView(text(points, 16, R.color.blue, R.font.poppins_semibold))
        }.also {
            it.layoutParams = marginParams(height = 70, top = 12)
        }
    }

    private fun text(value: String, sp: Int, color: Int, font: Int): TextView {
        return TextView(this).apply {
            text = value
            textSize = sp.toFloat()
            setTextColor(ContextCompat.getColor(this@BibleQuizActivity, color))
            typeface = ResourcesCompat.getFont(this@BibleQuizActivity, font)
            includeFontPadding = false
        }
    }

    private fun marginParams(
        height: Int = LinearLayout.LayoutParams.WRAP_CONTENT,
        top: Int = 0,
        marginHorizontal: Int = 16
    ): LinearLayout.LayoutParams {
        val resolvedHeight = if (height == LinearLayout.LayoutParams.WRAP_CONTENT) {
            LinearLayout.LayoutParams.WRAP_CONTENT
        } else {
            dp(height)
        }
        return LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, resolvedHeight).apply {
            setMargins(dp(marginHorizontal), dp(top), dp(marginHorizontal), 0)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private data class QuizQuestion(
        val question: String,
        val options: List<String>,
        val correctIndex: Int,
        val context: String = "This answer follows Esther's story and the courage shown through faith and obedience.",
        val verse: String = "Esther"
    )
}
