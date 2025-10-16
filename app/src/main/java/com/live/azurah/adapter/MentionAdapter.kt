package com.live.azurah.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.databinding.ItemMentionBinding
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.loadImage

class MentionAdapter(
    public val users: List<Follower>,
    private val onUserSelected: (Follower) -> Unit
) : RecyclerView.Adapter<MentionAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMentionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: Follower) {
            binding.tvName.text = user.username
            binding.ivImage.loadImage(ApiConstants.IMAGE_BASE_URL+user.profileImageUrl, R.drawable.profile_icon)
            binding.root.setOnClickListener { onUserSelected(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemMentionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(users[position])

    override fun getItemCount() = users.size
}
