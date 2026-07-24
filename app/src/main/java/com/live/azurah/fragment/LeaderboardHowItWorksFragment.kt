package com.live.azurah.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.live.azurah.databinding.FragmentLeaderboardHowItWorksBinding

class LeaderboardHowItWorksFragment : Fragment() {
    private var _binding: FragmentLeaderboardHowItWorksBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLeaderboardHowItWorksBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = _binding ?: return
        binding.tvRuleOne.text = HtmlCompat.fromHtml(
            "<b>Following</b> ranks you among people you follow or who follow you. Top 10 always pinned, then you see the 5 people directly above and below your position.",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.tvRuleTwo.text = HtmlCompat.fromHtml(
            "<b>Top 100</b> shows the highest scorers across all of AZRIUS. If you are in the Top 100, you will see your position highlighted.",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.tvRuleThree.text = HtmlCompat.fromHtml(
            "Scores <b>reset on the 1st of every month.</b> Your badge is permanent and never resets.",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.tvRuleFour.text = HtmlCompat.fromHtml(
            "The quiz is <b>Premium only.</b> Complete your Devotional + Prayer first each day to unlock it.",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
