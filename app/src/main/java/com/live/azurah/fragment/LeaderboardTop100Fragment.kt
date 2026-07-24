package com.live.azurah.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.live.azurah.adapter.LeaderboardTop100Adapter
import com.live.azurah.adapter.LeaderboardTop100Item
import com.live.azurah.databinding.FragmentLeaderboardTop100Binding

class LeaderboardTop100Fragment : Fragment() {
    private var _binding: FragmentLeaderboardTop100Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLeaderboardTop100Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvTop100.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTop100.adapter = LeaderboardTop100Adapter(samplePositions4To10())
        binding.rvTop100.setHasFixedSize(false)
    }

    private fun samplePositions4To10(): List<LeaderboardTop100Item> = listOf(
        LeaderboardTop100Item(4, "GW", "@grace_w", "35,100", "#009B72"),
        LeaderboardTop100Item(5, "J", "@jenny_faith", "32,840", "#0A66A0"),
        LeaderboardTop100Item(6, "DM", "@daniel_m", "31,450", "#E17942"),
        LeaderboardTop100Item(7, "SK", "@sara_k", "29,880", "#7C6AF2"),
        LeaderboardTop100Item(8, "NL", "@noah_l", "28,420", "#F59E0B"),
        LeaderboardTop100Item(9, "AM", "@ava_m", "27,150", "#EC4899"),
        LeaderboardTop100Item(10, "BL", "@ben_l", "25,900", "#14B8A6")
    )

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
