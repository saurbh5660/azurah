package com.live.azurah.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.live.azurah.R
import com.live.azurah.databinding.BottomSheetBadgeDetailBinding

class BadgeDetailBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetBadgeDetailBinding? = null
    private val binding get() = _binding!!

    enum class BadgeType {
        SEED,
        ROOTED,
        GROWING,
        FLOURISHING
    }

    override fun getTheme(): Int = R.style.CustomBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetBadgeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            setOnShowListener { dialog ->
                val bottomSheet = (dialog as BottomSheetDialog)
                    .findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                bottomSheet?.setBackgroundResource(android.R.color.transparent)
                bottomSheet?.let {
                    val behavior = BottomSheetBehavior.from(it)
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    behavior.skipCollapsed = true
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindBadge(resolveBadgeType())
        binding.tvClose.setOnClickListener { dismiss() }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { root, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            root.updatePadding(bottom = systemBars.bottom)
            insets
        }
    }

    private fun resolveBadgeType(): BadgeType {
        val name = arguments?.getString(ARG_BADGE) ?: BadgeType.SEED.name
        return runCatching { BadgeType.valueOf(name) }.getOrDefault(BadgeType.SEED)
    }

    private fun bindBadge(type: BadgeType) {
        val content = contentFor(type)
        binding.ivBadgeIcon.setImageResource(content.iconRes)
        binding.tvBadgeTitle.text = content.title
        binding.tvBadgeSubtitle.text = content.subtitle
        binding.tvCongrats.text = content.congrats
        binding.tvMeans.text = content.means
        binding.tvInsight.text = content.insight
        binding.scriptureSection.isVisible = content.scriptureQuote != null
        if (content.scriptureQuote != null) {
            binding.tvScriptureQuote.text = content.scriptureQuote
            binding.tvScriptureRef.isVisible = !content.scriptureRef.isNullOrBlank()
            binding.tvScriptureRef.text = content.scriptureRef.orEmpty()
        }
    }

    private fun contentFor(type: BadgeType): BadgeContent = when (type) {
        BadgeType.SEED -> BadgeContent(
            iconRes = R.drawable.seed_icon,
            title = "Seed",
            subtitle = "DAY 1",
            congrats = "Congratulations! You've unlocked the Seed badge. Your faith journey on AZRIUS has begun.",
            means = "Every time you spend time in God's Word or prayer, a seed is planted that can grow into something greater.",
            insight = "Jesus taught that God's Word is like a seed planted in our hearts. Even the smallest beginning has the potential to bear great fruit.",
            scriptureQuote = "“The seed is the word of God.”",
            scriptureRef = "LUKE 8:11 (NIV)"
        )
        BadgeType.ROOTED -> BadgeContent(
            iconRes = R.drawable.rooted_icon,
            title = "Rooted",
            subtitle = "30-DAY STREAK",
            congrats = "Congratulations! You've unlocked the Rooted badge. Your consistency is helping your faith grow deeper.",
            means = "Just as roots anchor a plant firmly in the ground, spending regular time with God strengthens your foundation and establishes your faith.",
            insight = "The Bible teaches that believers should be rooted and built up in Christ, becoming more firmly established with each passing day.",
            scriptureQuote = "“Rooted and built up in him, strengthened in the faith as you were taught.”",
            scriptureRef = "COLOSSIANS 2:7 (NIV)"
        )
        BadgeType.GROWING -> BadgeContent(
            iconRes = R.drawable.growing_icon,
            title = "Growing",
            subtitle = "60-DAY STREAK",
            congrats = "Congratulations! You've unlocked the Growing badge. Your commitment to learning from Scripture is helping your faith develop day by day.",
            means = "Growth takes time, but every step forward matters. Your relationship with God is actively developing through your daily habits.",
            insight = "Scripture calls believers to grow in their knowledge of Jesus. Faith that is exercised daily becomes stronger and more fruitful.",
            scriptureQuote = null,
            scriptureRef = null
        )
        BadgeType.FLOURISHING -> BadgeContent(
            iconRes = R.drawable.flourishing_icon,
            title = "Flourishing",
            subtitle = "90-DAY STREAK",
            congrats = "Congratulations! You've unlocked the Flourishing badge. Your faith is thriving through your continued dedication to spiritual growth.",
            means = "Like a tree planted by water, your life is being strengthened and sustained. Your faith has become a strong part of your daily life.",
            insight = "The Bible describes the righteous as trees that flourish when rooted in God. A life nourished daily by His Word bears fruit in every season.",
            scriptureQuote = "“They are like trees planted by streams of water, which yield their fruit in season and whose leaf does not wither.”",
            scriptureRef = "PSALM 1:3 (NIV)"
        )
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private data class BadgeContent(
        @DrawableRes val iconRes: Int,
        val title: String,
        val subtitle: String,
        val congrats: String,
        val means: String,
        val insight: String,
        val scriptureQuote: String?,
        val scriptureRef: String?
    )

    companion object {
        private const val ARG_BADGE = "badge_type"

        fun newInstance(type: BadgeType): BadgeDetailBottomSheet {
            return BadgeDetailBottomSheet().apply {
                arguments = Bundle().apply { putString(ARG_BADGE, type.name) }
            }
        }
    }
}
