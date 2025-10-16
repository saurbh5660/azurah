package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.live.azurah.R
import com.live.azurah.activity.MyPostsActivity
import com.live.azurah.activity.OtherUserProfileActivity
import com.live.azurah.databinding.ItemEventsBinding
import com.live.azurah.databinding.ItemInterestBinding
import com.live.azurah.databinding.ItemMyPost1Binding
import com.live.azurah.databinding.ItemMyPostBinding
import com.live.azurah.databinding.ItemPostBinding
import com.live.azurah.databinding.ItemPostImageVideoBinding
import com.live.azurah.databinding.ItemPromptBinding
import com.live.azurah.databinding.ItemSearchPostBinding
import com.live.azurah.databinding.ItemSongBinding
import com.live.azurah.databinding.ItemSuggestionsBinding
import com.live.azurah.model.InterestModel
import com.live.azurah.model.PostResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.formatCount
import com.live.azurah.util.gone
import com.live.azurah.util.loadImage
import com.live.azurah.util.visible

class SearchPostAdapter(val ctx: Context, val list: ArrayList<PostResponse.Body.Data>):RecyclerView.Adapter<SearchPostAdapter.ViewHolder>() {
    var onClickListener: ((pos: Int,model: PostResponse.Body.Data) -> Unit)? = null

    class ViewHolder(val binding: ItemSearchPostBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSearchPostBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            with(holder.binding){
                val model = list[holder.absoluteAdapterPosition]

                tvName.text = buildString {
                    append(model.user?.username ?: "")
                }
                ivPosts.loadImage(ApiConstants.IMAGE_BASE_URL+model.user?.image, placeholder = R.drawable.profile_icon)
                tvLikes.text = buildString {
                    append(formatCount(model.like_count ?: 0))
                    append(" ")
                }
                if (model.post_images?.firstOrNull()?.type == 1) {
                    idPlay.gone()
                    rvImage.loadImage(ApiConstants.IMAGE_BASE_URL+model.post_images?.firstOrNull()?.image.toString())
                }else{
                    idPlay.visible()
                    val url = ApiConstants.IMAGE_BASE_URL + model.post_images?.firstOrNull()?.image
                    Glide.with(ctx)
                        .asBitmap()
                        .load(url)
                        .frame(1000000) // Extract a frame at 1 second
                        .into(rvImage)
                }
                root.setOnClickListener {
                    onClickListener?.invoke(holder.absoluteAdapterPosition,model)
                }

            }
        }
    }

}