package com.live.azurah.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.live.azurah.activity.BibleLeaderboardActivity
import com.live.azurah.adapter.LeaderboardFollowingAdapter
import com.live.azurah.adapter.LeaderboardFollowingItem
import com.live.azurah.databinding.FragmentLeaderboardFollowingBinding

class LeaderboardFollowingFragment : Fragment() {
    private var _binding: FragmentLeaderboardFollowingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLeaderboardFollowingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val items = sampleFollowingItems()
        binding.tvFollowingCount.text = "Following (${items.size})"
        binding.rvFollowing.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFollowing.adapter = LeaderboardFollowingAdapter(items)
        binding.rvFollowing.setHasFixedSize(false)

        binding.btnCommunityTop100.setOnClickListener {
            (activity as? BibleLeaderboardActivity)?.selectTab(BibleLeaderboardActivity.Tab.TOP_100)
        }
    }

    private fun sampleFollowingItems(): List<LeaderboardFollowingItem> = listOf(
        LeaderboardFollowingItem(1, "FM", "@floyd_m", "12,350", "#F6C332"),
        LeaderboardFollowingItem(2, "SJ", "@sarika_j", "9,200", "#AEB7C2"),
        LeaderboardFollowingItem(3, "LO", "@lara_o", "8,750", "#FF7A1A"),
        LeaderboardFollowingItem(4, "TK", "@tanya_k", "8,100", "#7DB8E8"),
        LeaderboardFollowingItem(
            rank = 5,
            initials = "J",
            username = "@jenny_faith",
            points = "6,840",
            avatarColor = "#0066A0",
            subtitle = "Top 30%",
            isCurrentUser = true
        ),
        LeaderboardFollowingItem(6, "DB", "@daniel_b", "5,900", "#9CC8FF"),
        LeaderboardFollowingItem(7, "MJ", "@marcus_j", "4,200", "#FB7D24"),
        LeaderboardFollowingItem(8, "JD", "@john_doe", "3,500", "#FFC233"),
        LeaderboardFollowingItem(9, "AS", "@amy_smith", "2,800", "#8BD4C4"),
        LeaderboardFollowingItem(10, "RW", "@ryan_w", "2,100", "#B66565")
    )

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
