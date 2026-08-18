package com.live.azurah.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.databinding.ItemDiscussionCommentBinding
import com.live.azurah.model.DiscussionListResponse
import com.live.azurah.util.getPreference
import com.live.azurah.util.getRelativeTime
import com.live.azurah.util.loadImage

class DiscussionAdapter(
    private var items: MutableList<DiscussionListResponse.DiscussionData> = mutableListOf(),
    private val onCommentClick: (DiscussionListResponse.DiscussionData, Int) -> Unit = { _, _ -> },
    private val onLikeClick: (DiscussionListResponse.DiscussionData, Int) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<DiscussionAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemDiscussionCommentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemDiscussionCommentBinding.inflate(
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
            tvUsername.text = item.user?.username ?: "User"
            tvComment.text = item.description ?: ""
            tvTime.text = getRelativeTime(item.created_at ?: "")
            
            if (!item.user?.profile_image.isNullOrEmpty()) {
                ivAvatar.visibility = View.VISIBLE
                tvAvatar.visibility = View.GONE
                ivAvatar.loadImage(item.user?.profile_image)
            } else {
                ivAvatar.visibility = View.GONE
                tvAvatar.visibility = View.VISIBLE
                tvAvatar.text = item.user?.username?.take(2)?.uppercase() ?: "AZ"
                tvAvatar.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#7DB8E8"))
            }

            ivDelete.visibility = View.GONE
            tvFollowingBadge.visibility = View.GONE
            tvTopBadge.visibility = View.GONE

            tvReply.text = "Reply (${item.comment_count ?: 0})"

            bindLikeState(item)

            ivLike.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                val current = items[pos]
                
                if (current.is_like == 1) {
                    current.is_like = 0
                    current.like_count = (current.like_count ?: 1) - 1
                } else {
                    current.is_like = 1
                    current.like_count = (current.like_count ?: 0) + 1
                }
                notifyItemChanged(pos)
                
                onLikeClick(current, pos)
            }
            
            tvReply.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onCommentClick(items[pos], pos)
                }
            }
        }
    }

    private fun ItemDiscussionCommentBinding.bindLikeState(item: DiscussionListResponse.DiscussionData) {
        val context = root.context
        if (item.is_like == 1) {
            ivLike.setImageResource(R.drawable.selected_heart)
            ivLike.imageTintList = ContextCompat.getColorStateList(context, R.color.star_red_color)
            tvLikes.setTextColor(ContextCompat.getColor(context, R.color.star_red_color))
        } else {
            ivLike.setImageResource(R.drawable.unselected_heart)
            ivLike.imageTintList = ContextCompat.getColorStateList(context, R.color.black)
            tvLikes.setTextColor(Color.parseColor("#F472B6"))
        }
        tvLikes.text = (item.like_count ?: 0).toString()
    }

    fun submitList(newItems: List<DiscussionListResponse.DiscussionData>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }
}
