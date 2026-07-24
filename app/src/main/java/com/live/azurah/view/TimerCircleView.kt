package com.live.azurah.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.live.azurah.R

/**
 * Circular timer with green remaining arc and red elapsed arc.
 */
class TimerCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    val numberView: TextView = TextView(context).apply {
        gravity = Gravity.CENTER
        textSize = 12f
        setTextColor(Color.WHITE)
        typeface = ResourcesCompat.getFont(context, R.font.inter_bold)
        includeFontPadding = false
        text = "30"
    }

    private val elapsedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#EF4444")
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = dp(4f)
    }

    private val remainingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#22C55E")
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = dp(4f)
    }

    private val bounds = RectF()
    private var remainingRatio = 1f
    private var timeUp = false
    private val totalSeconds = 30

    init {
        setWillNotDraw(false)
        setBackgroundColor(Color.TRANSPARENT)
        addView(numberView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    fun setRemainingSeconds(seconds: Int) {
        timeUp = false
        val clamped = seconds.coerceIn(0, totalSeconds)
        remainingRatio = clamped / totalSeconds.toFloat()
        numberView.text = clamped.toString()
        numberView.setTextColor(Color.WHITE)
        numberView.textSize = 12f
        invalidate()
    }

    fun showTimeUp() {
        timeUp = true
        remainingRatio = 0f
        numberView.text = "•"
        numberView.setTextColor(Color.parseColor("#EF4444"))
        numberView.textSize = 18f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val inset = dp(5f)
        bounds.set(inset, inset, width - inset, height - inset)

        // Full elapsed (red) track
        canvas.drawOval(bounds, elapsedPaint)

        if (!timeUp && remainingRatio > 0f) {
            // Remaining time in green from top, clockwise
            canvas.drawArc(bounds, -90f, 360f * remainingRatio, false, remainingPaint)
        } else if (timeUp) {
            canvas.drawOval(bounds, elapsedPaint)
        }
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
