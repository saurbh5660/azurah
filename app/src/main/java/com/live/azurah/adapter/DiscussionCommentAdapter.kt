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

data class DiscussionComment(
    val initials: String,
    val username: String,
    val timeAgo: String,
    val comment: String,
    var likes: Int,
    val replies: Int,
    val avatarColor: String,
    val isFollowing: Boolean = false,
    val isTop: Boolean = false,
    var isLiked: Boolean = false
)

class DiscussionCommentAdapter(
    private var items: MutableList<DiscussionComment> = mutableListOf()
) : RecyclerView.Adapter<DiscussionCommentAdapter.ViewHolder>() {

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
        with(holder.binding) {
            tvAvatar.text = item.initials
            tvAvatar.backgroundTintList = ColorStateList.valueOf(Color.parseColor(item.avatarColor))
            tvUsername.text = item.username
            tvTime.text = item.timeAgo
            tvComment.text = item.comment
            tvReply.text = if (item.replies > 0) "Reply (${item.replies})" else "Reply"
            tvFollowingBadge.visibility = if (item.isFollowing) View.VISIBLE else View.GONE
            tvTopBadge.visibility = if (item.isTop) View.VISIBLE else View.GONE
            bindLikeState(item)

            ivLike.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                val current = items[pos]
                if (current.isLiked) {
                    current.isLiked = false
                    current.likes = (current.likes - 1).coerceAtLeast(0)
                } else {
                    current.isLiked = true
                    current.likes += 1
                }
                notifyItemChanged(pos)
            }

            ivComment.setOnClickListener { /* reply UI later */ }
            tvReply.setOnClickListener { /* reply UI later */ }
        }
    }

    private fun ItemDiscussionCommentBinding.bindLikeState(item: DiscussionComment) {
        val context = root.context
        if (item.isLiked) {
            ivLike.setImageResource(R.drawable.selected_heart)
            ivLike.imageTintList = ContextCompat.getColorStateList(context, R.color.star_red_color)
            tvLikes.setTextColor(ContextCompat.getColor(context, R.color.star_red_color))
        } else {
            ivLike.setImageResource(R.drawable.unselected_heart)
            ivLike.imageTintList = ContextCompat.getColorStateList(context, R.color.black)
            tvLikes.setTextColor(Color.parseColor("#F472B6"))
        }
        tvLikes.text = item.likes.toString()
    }

    fun submitList(newItems: List<DiscussionComment>) {
        items = newItems.toMutableList()
        notifyDataSetChanged()
    }
}
