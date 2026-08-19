package com.live.azurah.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.live.azurah.activity.BibleLeaderboardActivity
import com.live.azurah.adapter.LeaderboardFollowingAdapter
import com.live.azurah.adapter.LeaderboardFollowingItem
import com.live.azurah.databinding.FragmentLeaderboardFollowingBinding
import com.live.azurah.model.LeaderboardItem
import com.live.azurah.model.LeaderboardResponse
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Status
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LeaderboardFollowingFragment : Fragment() {
    private var _binding: FragmentLeaderboardFollowingBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<CommonViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLeaderboardFollowingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCommunityTop100.setOnClickListener {
            (activity as? BibleLeaderboardActivity)?.selectTab(BibleLeaderboardActivity.Tab.TOP_100)
        }

        fetchLeaderboardData()
    }

    private fun fetchLeaderboardData() {
        viewModel.getLeaderboard(requireActivity()).observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    LoaderDialog.dismiss()
                    if (resource.data is LeaderboardResponse) {
                        val body = resource.data.body
                        if (body != null) {
                            setupUI(body)
                        }
                    }
                }
                Status.LOADING -> {
                    LoaderDialog.show(requireActivity())
                }
                Status.ERROR -> {
                    LoaderDialog.dismiss()
                    showCustomSnackbar(requireActivity(), binding.root, resource.message ?: "Failed to load leaderboard")
                }
            }
        }
    }

    private fun setupUI(body: LeaderboardResponse.Body) {
        val myPoints = body.myPoints ?: 0
        val myRank = body.myRank ?: 0
        val growthLevel = body.growthLevel ?: "Beginner"

        binding.tvMyPoints.text = myPoints.toString()
        binding.tvGrowthLevel.text = "🌱 $growthLevel"
        binding.tvMyRankSubtitle.text = if (myRank > 0) "Your Rank: #$myRank" else "Participate to get ranked!"

        val allItems = mutableListOf<LeaderboardItem>()
        body.top3?.let { allItems.addAll(it) }
        body.rankings?.let { allItems.addAll(it) }

        val colors = listOf("#F6C332", "#AEB7C2", "#FF7A1A", "#7DB8E8", "#0066A0", "#9CC8FF", "#FB7D24", "#FFC233", "#8BD4C4", "#B66565")
        val adapterItems = allItems.mapIndexed { index, item ->
            val user = item.user
            val name = user?.username ?: if (!user?.firstName.isNullOrEmpty()) "${user?.firstName} ${user?.lastName ?: ""}".trim() else "User ${item.userId}"
            val usernameText = if (name.startsWith("@")) name else "@$name"
            val initials = (user?.firstName?.firstOrNull() ?: user?.username?.firstOrNull() ?: 'U').toString().uppercase()
            val isMe = item.rank == myRank

            LeaderboardFollowingItem(
                rank = item.rank ?: (index + 1),
                initials = initials,
                username = usernameText,
                points = (item.totalPoints ?: 0).toString(),
                avatarColor = colors[index % colors.size],
                subtitle = item.growthLevel,
                isCurrentUser = isMe,
                imageUrl = user?.image ?: user?.profileImage
            )
        }

        binding.tvFollowingCount.text = "Leaderboard (${adapterItems.size})"
        binding.rvFollowing.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFollowing.adapter = LeaderboardFollowingAdapter(adapterItems)
        binding.rvFollowing.setHasFixedSize(false)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
