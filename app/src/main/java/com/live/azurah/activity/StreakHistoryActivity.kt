package com.live.azurah.activity

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.activity.viewModels
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
import com.live.azurah.model.StreakCalendarResponse
import com.live.azurah.retrofit.Status
import com.live.azurah.util.gone
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class StreakHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStreakHistoryBinding
    private val viewModel by viewModels<CommonViewModel>()
    private val displayedMonth: Calendar = Calendar.getInstance()

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
            fetchCalendarData()
        }
        binding.tvNext.setOnClickListener {
            displayedMonth.add(Calendar.MONTH, 1)
            fetchCalendarData()
        }

        fetchCalendarData()
    }

    private fun fetchCalendarData() {
        val monthQuery = SimpleDateFormat("yyyy-MM", Locale.US).format(displayedMonth.time)
        binding.tvMonth.text = SimpleDateFormat("MMMM yyyy", Locale.US)
            .format(displayedMonth.time)
            .uppercase(Locale.US)
        binding.tvPrev.setTextColor(ContextCompat.getColor(this, R.color.blue))
        binding.tvNext.setTextColor(ContextCompat.getColor(this, R.color.blue))

        binding.shimmerCalendarLayout.visible()
        binding.shimmerCalendarLayout.startShimmer()
        binding.calendarContainer.gone()

        val map = HashMap<String, String>()
        map["month"] = monthQuery

        viewModel.getStreakCalendar(map, this).observe(this) { value ->
            when (value.status) {
                Status.SUCCESS -> {
                    binding.shimmerCalendarLayout.stopShimmer()
                    binding.shimmerCalendarLayout.gone()
                    binding.calendarContainer.visible()
                    when (value.data) {
                        is StreakCalendarResponse -> {
                            val data = value.data.body
                            if (data != null) {
                                binding.tvCurrentStreak.text = (data.currentStreak ?: 0).toString()
                                binding.tvPersonalBest.text = (data.bestStreak ?: 0).toString()
                                renderCalendarGrid(data)
                            }
                        }
                    }
                }
                Status.LOADING -> {
                    binding.shimmerCalendarLayout.visible()
                    binding.shimmerCalendarLayout.startShimmer()
                    binding.calendarContainer.gone()
                }
                Status.ERROR -> {
                    binding.shimmerCalendarLayout.stopShimmer()
                    binding.shimmerCalendarLayout.gone()
                    binding.calendarContainer.visible()
                    showCustomSnackbar(this, binding.root, value.message.toString())
                }
            }
        }
    }

    private fun renderCalendarGrid(body: StreakCalendarResponse.Body) {
        binding.calendarContainer.removeAllViews()
        binding.calendarContainer.addView(createHeaderRow())

        val dayStatusMap = HashMap<Int, DayStatus>()
        body.days?.forEach { dayItem ->
            val dateStr = dayItem.date
            if (!dateStr.isNullOrEmpty()) {
                val parts = dateStr.split("-")
                if (parts.size == 3) {
                    val dayNum = parts[2].toIntOrNull()
                    if (dayNum != null) {
                        val status = when (dayItem.status?.lowercase()) {
                            "completed" -> DayStatus.Completed
                            "missed" -> DayStatus.Missed
                            "protected" -> DayStatus.Protected
                            "today" -> DayStatus.Today
                            "upcoming" -> DayStatus.Upcoming
                            else -> DayStatus.Normal
                        }
                        dayStatusMap[dayNum] = status
                    }
                }
            }
        }

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
                    val status = dayStatusMap[day] ?: DayStatus.Normal
                    row.addView(createDayCell(day, status))
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
                layoutParams = weightedParams(height = dp(24), margin = 0)
                gravity = Gravity.CENTER
                text = label
                setTextColor(ContextCompat.getColor(this@StreakHistoryActivity, R.color.dashboard_subtitle))
                textSize = 10f
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
            layoutParams = weightedParams(height = dp(40))
            gravity = Gravity.CENTER
            addView(TextView(this@StreakHistoryActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dp(34), dp(36))
                gravity = Gravity.CENTER
                text = day.toString()
                textSize = 13f
                typeface = ResourcesCompat.getFont(this@StreakHistoryActivity, R.font.poppins_semibold)
                includeFontPadding = false
                setTextColor(ContextCompat.getColor(this@StreakHistoryActivity, status.textColor))
                if (status.background != null) {
                    setBackgroundResource(status.background)
                } else {
                    background = null
                }
            })
        }
    }

    private fun weightedParams(height: Int = dp(40), margin: Int = dp(1)): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(0, height, 1f).apply {
            setMargins(margin, margin, margin, margin)
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
        Missed(R.drawable.streak_day_missed, R.color.dashboard_subtitle),
        Protected(R.drawable.streak_day_protected, R.color.white),
        Today(R.drawable.streak_day_today, R.color.red_color),
        Upcoming(null, R.color.black),
        Normal(null, R.color.black)
    }

    private companion object {
        const val DAYS_IN_WEEK = 7
    }
}
