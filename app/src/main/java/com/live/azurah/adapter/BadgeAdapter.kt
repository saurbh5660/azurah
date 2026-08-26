package com.live.azurah.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.databinding.ItemBadgeCardBinding
import com.live.azurah.model.GrowthLevelsResponse

class BadgeAdapter(
    private val items: List<GrowthLevelsResponse.GrowthLevel>,
    private val onItemClick: (GrowthLevelsResponse.GrowthLevel) -> Unit
) : RecyclerView.Adapter<BadgeAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemBadgeCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemBadgeCardBinding.inflate(
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
            tvBadgeTitle.text = item.level ?: "Badge"
            tvBadgeSubtitle.text = if ((item.minStreak ?: 0) == 0) "Day 1" else "${item.minStreak}-day streak"

            val isUnlocked = item.unlocked == true
            val isCurrent = item.current == true

            if (isUnlocked) {
                val bgRes = when (item.level?.lowercase()) {
                    "seed" -> R.drawable.badges_card_seed
                    "rooted" -> R.drawable.badges_card_rooted
                    else -> R.drawable.badges_card_seed
                }
                val iconBgRes = when (item.level?.lowercase()) {
                    "seed" -> R.drawable.badges_icon_seed
                    "rooted" -> R.drawable.badges_icon_rooted
                    else -> R.drawable.badges_icon_seed
                }
                cardRoot.setBackgroundResource(bgRes)
                vBadgeIconBg.setBackgroundResource(iconBgRes)

                ivStatusBadge.setImageResource(R.drawable.badges_ic_check)
                ivStatusBadge.setBackgroundResource(if (item.level?.lowercase() == "seed") R.drawable.badges_check_yellow else R.drawable.badges_check_green)

                tvBadgeTitle.setTextColor(ContextCompat.getColor(context, R.color.dashboard_card_text))
                tvBadgeSubtitle.setTextColor(if (item.level?.lowercase() == "seed") ContextCompat.getColor(context, R.color.blue) else android.graphics.Color.parseColor("#2F9B55"))
            } else {
                cardRoot.setBackgroundResource(R.drawable.badges_card_locked)
                vBadgeIconBg.setBackgroundResource(R.drawable.badges_icon_locked)

                ivStatusBadge.setImageResource(R.drawable.badges_ic_lock)
                ivStatusBadge.setBackgroundResource(R.drawable.badges_lock_circle)

                tvBadgeTitle.setTextColor(ContextCompat.getColor(context, R.color.dashboard_subtitle))
                tvBadgeSubtitle.setTextColor(ContextCompat.getColor(context, R.color.dashboard_subtitle))
            }

            if (isCurrent) {
                tvCurrentBadgeTag.visibility = View.VISIBLE
            } else {
                tvCurrentBadgeTag.visibility = View.GONE
            }

            cardRoot.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
