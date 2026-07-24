package com.live.azurah.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.databinding.ItemLeaderboardTop100Binding

data class LeaderboardTop100Item(
    val rank: Int,
    val initials: String,
    val username: String,
    val points: String,
    val avatarColor: String
)

class LeaderboardTop100Adapter(
    private val items: List<LeaderboardTop100Item>
) : RecyclerView.Adapter<LeaderboardTop100Adapter.ViewHolder>() {

    class ViewHolder(val binding: ItemLeaderboardTop100Binding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemLeaderboardTop100Binding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvRank.text = item.rank.toString()
            tvAvatar.text = item.initials
            tvAvatar.backgroundTintList = ColorStateList.valueOf(Color.parseColor(item.avatarColor))
            tvUsername.text = item.username
            tvPoints.text = item.points
            divider.visibility = if (position == items.lastIndex) View.GONE else View.VISIBLE
        }
    }
}
