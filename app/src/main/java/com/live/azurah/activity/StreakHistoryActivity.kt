package com.live.azurah.activity

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.Space
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
import com.live.azurah.databinding.ActivityStreakHistoryBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StreakHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStreakHistoryBinding
    private val displayedMonth: Calendar = Calendar.getInstance().apply {
        set(2026, Calendar.APRIL, 1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStreakHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = getColor(R.color.dashboard_primary)
        binding.root.setBackgroundColor(getColor(R.color.dashboard_primary))
        binding.contentLayout.setBackgroundColor(getColor(R.color.white))
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            view.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }

        binding.backIcon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.tvPrev.setOnClickListener {
            displayedMonth.add(Calendar.MONTH, -1)
            renderCalendar()
        }
        binding.tvNext.setOnClickListener {
            displayedMonth.add(Calendar.MONTH, 1)
            renderCalendar()
        }
        renderCalendar()
    }

    private fun renderCalendar() {
        binding.tvMonth.text = SimpleDateFormat("MMMM yyyy", Locale.US)
            .format(displayedMonth.time)
            .uppercase(Locale.US)
        binding.tvPrev.setTextColor(ContextCompat.getColor(this, R.color.blue))
        binding.tvNext.setTextColor(ContextCompat.getColor(this, R.color.blue))

        binding.calendarContainer.removeAllViews()
        binding.calendarContainer.addView(createHeaderRow())

        val calendar = displayedMonth.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOffset = calendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        var day = 1

        while (day <= maxDay) {
            val row = createCalendarRow()
            for (column in 0 until DAYS_IN_WEEK) {
                if ((binding.calendarContainer.childCount == 1 && column < firstDayOffset) || day > maxDay) {
                    row.addView(createBlankCell())
                } else {
                    row.addView(createDayCell(day, dayStatus(day)))
                    day++
                }
            }
            binding.calendarContainer.addView(row)
        }
    }

    private fun createHeaderRow(): LinearLayout {
        val row = createCalendarRow()
        listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach { label ->
            row.addView(TextView(this).apply {
                layoutParams = weightedParams(height = dp(20), margin = 0)
                gravity = Gravity.CENTER
                text = label
                setTextColor(ContextCompat.getColor(this@StreakHistoryActivity, R.color.dashboard_subtitle))
                textSize = 7f
                typeface = ResourcesCompat.getFont(this@StreakHistoryActivity, R.font.poppins_semibold)
                includeFontPadding = false
            })
        }
        return row
    }

    private fun createCalendarRow(): LinearLayout {
        return LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            weightSum = DAYS_IN_WEEK.toFloat()
            clipToPadding = false
        }
    }

    private fun createBlankCell(): View {
        return Space(this).apply {
            layoutParams = weightedParams()
        }
    }

    private fun createDayCell(day: Int, status: DayStatus): View {
        return LinearLayout(this).apply {
            layoutParams = weightedParams()
            gravity = Gravity.CENTER
            addView(TextView(this@StreakHistoryActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dp(30), dp(34))
                gravity = Gravity.CENTER
                text = day.toString()
                textSize = 10f
                typeface = ResourcesCompat.getFont(this@StreakHistoryActivity, R.font.poppins_semibold)
                includeFontPadding = false
                setTextColor(ContextCompat.getColor(this@StreakHistoryActivity, status.textColor))
                if (status.background != null) {
                    setBackgroundResource(status.background)
                }
            })
        }
    }

    private fun weightedParams(height: Int = dp(38), margin: Int = dp(1)): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(0, height, 1f).apply {
            setMargins(margin, margin, margin, margin)
        }
    }

    private fun dayStatus(day: Int): DayStatus {
        val isApril2026 = displayedMonth.get(Calendar.YEAR) == 2026 &&
            displayedMonth.get(Calendar.MONTH) == Calendar.APRIL
        if (!isApril2026) return DayStatus.Future

        return when (day) {
            12 -> DayStatus.Protected
            28 -> DayStatus.Today
            in setOf(1, 2, 3, 6, 7, 8, 9, 10, 13, 14, 15, 16, 17, 20, 21, 22, 23, 24, 27) -> DayStatus.Completed
            in setOf(4, 5, 11, 18, 19, 25, 26) -> DayStatus.Missed
            else -> DayStatus.Future
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private enum class DayStatus(
        val background: Int?,
        val textColor: Int
    ) {
        Completed(R.drawable.streak_day_completed, R.color.white),
        Missed(R.drawable.streak_day_missed, R.color.drink_color),
        Protected(R.drawable.streak_day_protected, R.color.white),
        Today(R.drawable.streak_day_today, R.color.red_color),
        Future(R.drawable.streak_day_future_dashed, R.color.day_unselected_color)
    }

    private companion object {
        const val DAYS_IN_WEEK = 7
    }
}
