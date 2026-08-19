package com.live.azurah.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.databinding.ItemLeaderboardFollowingBinding

import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.loadImage

data class LeaderboardFollowingItem(
    val rank: Int,
    val initials: String,
    val username: String,
    val points: String,
    val avatarColor: String,
    val subtitle: String? = null,
    val isCurrentUser: Boolean = false,
    val imageUrl: String? = null
)

class LeaderboardFollowingAdapter(
    private val items: List<LeaderboardFollowingItem>
) : RecyclerView.Adapter<LeaderboardFollowingAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemLeaderboardFollowingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemLeaderboardFollowingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.binding.root.context
        with(holder.binding) {
            tvRank.text = item.rank.toString()
            if (!item.imageUrl.isNullOrBlank()) {
                ivAvatar.visibility = View.VISIBLE
                tvAvatar.visibility = View.GONE
                ivAvatar.loadImage(ApiConstants.IMAGE_BASE_URL + item.imageUrl)
            } else {
                ivAvatar.visibility = View.GONE
                tvAvatar.visibility = View.VISIBLE
                tvAvatar.text = item.initials
                tvAvatar.backgroundTintList = ColorStateList.valueOf(Color.parseColor(item.avatarColor))
            }
            tvUsername.text = item.username
            tvPoints.text = item.points

            if (item.subtitle.isNullOrBlank()) {
                tvSubtitle.visibility = View.GONE
            } else {
                tvSubtitle.visibility = View.VISIBLE
                tvSubtitle.text = item.subtitle
            }

            val rankColor = when {
                item.isCurrentUser -> ContextCompat.getColor(context, R.color.blue)
                item.rank == 1 -> Color.parseColor("#E5A800")
                item.rank == 2 -> Color.parseColor("#9CA3AF")
                item.rank == 3 -> Color.parseColor("#FF7A1A")
                else -> ContextCompat.getColor(context, R.color.dashboard_subtitle)
            }
            val nameColor = if (item.isCurrentUser) {
                ContextCompat.getColor(context, R.color.blue)
            } else {
                ContextCompat.getColor(context, R.color.dashboard_card_text)
            }

            tvRank.setTextColor(rankColor)
            tvUsername.setTextColor(nameColor)
            tvPoints.setTextColor(nameColor)

            if (item.isCurrentUser) {
                rowRoot.setBackgroundResource(R.drawable.leaderboard_highlight_row)
                val density = context.resources.displayMetrics.density
                val horizontal = (6 * density).toInt()
                val vertical = (4 * density).toInt()
                (rowRoot.layoutParams as? ViewGroup.MarginLayoutParams)?.let { lp ->
                    lp.setMargins(horizontal, vertical, horizontal, vertical)
                    rowRoot.layoutParams = lp
                }
            } else {
                rowRoot.background = null
                (rowRoot.layoutParams as? ViewGroup.MarginLayoutParams)?.let { lp ->
                    lp.setMargins(0, 0, 0, 0)
                    rowRoot.layoutParams = lp
                }
            }
        }
    }
}
