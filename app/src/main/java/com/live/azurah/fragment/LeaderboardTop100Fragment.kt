package com.live.azurah.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.live.azurah.adapter.LeaderboardTop100Adapter
import com.live.azurah.adapter.LeaderboardTop100Item
import com.live.azurah.databinding.FragmentLeaderboardTop100Binding
import com.live.azurah.model.LeaderboardGlobalResponse
import com.live.azurah.model.LeaderboardItem
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Status
import com.live.azurah.util.loadImage
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LeaderboardTop100Fragment : Fragment() {
    private var _binding: FragmentLeaderboardTop100Binding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<CommonViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLeaderboardTop100Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fetchGlobalLeaderboardData()
    }

    private fun fetchGlobalLeaderboardData() {
        viewModel.getLeaderboardGlobal(requireActivity()).observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    LoaderDialog.dismiss()
                    if (resource.data is LeaderboardGlobalResponse) {
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
                    showCustomSnackbar(requireActivity(), binding.root, resource.message ?: "Failed to load Top 100")
                }
            }
        }
    }

    private fun setupUI(body: LeaderboardGlobalResponse.Body) {
        val weekDate = body.weekStartDate ?: ""
        if (weekDate.isNotEmpty()) {
            binding.tvHeaderTitle.text = "Top 100 · Week of $weekDate"
        } else {
            binding.tvHeaderTitle.text = "Top 100"
        }

        val top100List = body.top100 ?: emptyList()

        // 1st place (Gold)
        val rank1 = top100List.firstOrNull { it.rank == 1 } ?: top100List.getOrNull(0)
        bindPodiumUser(rank1, 1)

        // 2nd place (Silver)
        val rank2 = top100List.firstOrNull { it.rank == 2 } ?: top100List.getOrNull(1)
        bindPodiumUser(rank2, 2)

        // 3rd place (Bronze)
        val rank3 = top100List.firstOrNull { it.rank == 3 } ?: top100List.getOrNull(2)
        bindPodiumUser(rank3, 3)

        // Positions 4 - 100
        val remainingList = top100List.filter { (it.rank ?: 0) > 3 }
        val colors = listOf("#009B72", "#0A66A0", "#E17942", "#7C6AF2", "#F59E0B", "#EC4899", "#14B8A6")

        val adapterItems = remainingList.mapIndexed { index, item ->
            val user = item.user
            val name = user?.username ?: if (!user?.firstName.isNullOrEmpty()) "${user?.firstName} ${user?.lastName ?: ""}".trim() else "User ${item.userId}"
            val usernameText = if (name.startsWith("@")) name else "@$name"
            val initials = (user?.firstName?.firstOrNull() ?: user?.username?.firstOrNull() ?: 'U').toString().uppercase()

            LeaderboardTop100Item(
                rank = item.rank ?: (index + 4),
                initials = initials,
                username = usernameText,
                points = (item.totalPoints ?: 0).toString(),
                avatarColor = colors[index % colors.size],
                imageUrl = user?.image ?: user?.profileImage
            )
        }

        binding.rvTop100.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTop100.adapter = LeaderboardTop100Adapter(adapterItems)
        binding.rvTop100.setHasFixedSize(false)
    }

    private fun bindPodiumUser(item: LeaderboardItem?, position: Int) {
        val user = item?.user
        val name = user?.username ?: if (!user?.firstName.isNullOrEmpty()) "${user?.firstName} ${user?.lastName ?: ""}".trim() else if (item != null) "User ${item.userId}" else "--"
        val usernameText = if (name == "--") "--" else if (name.startsWith("@")) name else "@$name"
        val initials = (user?.firstName?.firstOrNull() ?: user?.username?.firstOrNull() ?: 'U').toString().uppercase()
        val pointsText = (item?.totalPoints ?: 0).toString()
        val image = user?.image ?: user?.profileImage

        when (position) {
            1 -> {
                binding.tvGoldUsername.text = usernameText
                binding.tvGoldPoints.text = pointsText
                if (!image.isNullOrBlank()) {
                    binding.ivGoldAvatar.visibility = View.VISIBLE
                    binding.tvGoldAvatar.visibility = View.GONE
                    binding.ivGoldAvatar.loadImage(ApiConstants.IMAGE_BASE_URL + image)
                } else {
                    binding.ivGoldAvatar.visibility = View.GONE
                    binding.tvGoldAvatar.visibility = View.VISIBLE
                    binding.tvGoldAvatar.text = initials
                }
            }
            2 -> {
                binding.tvSilverUsername.text = usernameText
                binding.tvSilverPoints.text = pointsText
                if (!image.isNullOrBlank()) {
                    binding.ivSilverAvatar.visibility = View.VISIBLE
                    binding.tvSilverAvatar.visibility = View.GONE
                    binding.ivSilverAvatar.loadImage(ApiConstants.IMAGE_BASE_URL + image)
                } else {
                    binding.ivSilverAvatar.visibility = View.GONE
                    binding.tvSilverAvatar.visibility = View.VISIBLE
                    binding.tvSilverAvatar.text = initials
                }
            }
            3 -> {
                binding.tvBronzeUsername.text = usernameText
                binding.tvBronzePoints.text = pointsText
                if (!image.isNullOrBlank()) {
                    binding.ivBronzeAvatar.visibility = View.VISIBLE
                    binding.tvBronzeAvatar.visibility = View.GONE
                    binding.ivBronzeAvatar.loadImage(ApiConstants.IMAGE_BASE_URL + image)
                } else {
                    binding.ivBronzeAvatar.visibility = View.GONE
                    binding.tvBronzeAvatar.visibility = View.VISIBLE
                    binding.tvBronzeAvatar.text = initials
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
