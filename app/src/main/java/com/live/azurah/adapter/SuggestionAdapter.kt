package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.activity.OtherUserProfileActivity
import com.live.azurah.databinding.ItemSuggestionsBinding
import com.live.azurah.model.PostResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.AvatarUtils
import com.live.azurah.util.getPreference
import com.live.azurah.util.isInternetAvailable
import com.live.azurah.util.loadImage
import com.live.azurah.util.showCustomSnackbar

class SuggestionAdapter(
    val ctx: Context,
    val suggestedUsers: ArrayList<PostResponse.Body.Data.SuggestedUser>
) : RecyclerView.Adapter<SuggestionAdapter.ViewHolder>() {
    var followUnfollowListener: ((pos: Int, model: PostResponse.Body.Data.SuggestedUser, view: View) -> Unit)? =
        null
    var removeSuggestionListener: ((pos: Int,model:PostResponse.Body.Data.SuggestedUser) -> Unit)? = null

    class ViewHolder(val binding: ItemSuggestionsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSuggestionsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return suggestedUsers.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = suggestedUsers[holder.absoluteAdapterPosition]
        with(holder.binding) {
            val name = if (model.display_name_preference == 1) {
                model.first_name ?: ""
            } else {
                "${model.first_name ?: ""} ${model.last_name ?: ""}".trim()
            }
            val displayName = if (name.isNotBlank()) name else (model.username ?: "")
            tvName.text = displayName

            AvatarUtils.setupAvatar(ivProfile, tvInitials, model.image, displayName)

            tvUserName.text = buildString {
                append("@")
                append(model.username ?: "")
            }

            when (model.isFollowByMe) {
                0 -> {
                    tvFollow.text = "Requested"
                    tvFollow.backgroundTintList = ContextCompat.getColorStateList(ctx, R.color.profile_stroke_color)
                }

                1 -> {
                    tvFollow.text = "Following"
                    tvFollow.backgroundTintList = ContextCompat.getColorStateList(ctx, R.color.profile_stroke_color)
                }

                else -> {
                    if (model.profile_type == 1) {
                        if (model.isFollowByOther == 1) {
                            tvFollow.text = "Follow back"
                        } else {
                            tvFollow.text = "Request"
                        }
                    } else {
                        if (model.isFollowByOther == 1) {
                            tvFollow.text = "Follow back"
                        } else {
                            tvFollow.text = "Follow"
                        }
                    }
                    tvFollow.backgroundTintList = null
                }
            }
            tvFollow.setOnClickListener {
                if (isInternetAvailable(ctx)){
                    val followStatus = model.isFollowByMe ?: 4
                    if (model.profile_type == 1) {
                        if (followStatus != 0){
                            model.isFollowByMe = 0
                        }else{
                            model.isFollowByMe = 3
                        }
                    } else {
                        if (model.isFollowByMe == 1){
                            model.isFollowByMe = 3
                        }else{
                            model.isFollowByMe = 1
                        }
                    }
                    followUnfollowListener?.invoke(holder.absoluteAdapterPosition, model, it)
                    notifyItemChanged(holder.absoluteAdapterPosition)
                }else{
                    showCustomSnackbar(ctx,holder.binding.tvFollow,"Internet not available")
                }
            }

            ivCross.setOnClickListener {
                if (isInternetAvailable(ctx)) {
//                    suggestedUsers.removeAt(holder.absoluteAdapterPosition)
                    removeSuggestionListener?.invoke(holder.absoluteAdapterPosition,model)
//                    notifyItemRemoved(holder.absoluteAdapterPosition)

                } else {
                    showCustomSnackbar(ctx, holder.binding.tvFollow, "Internet not available")
                }

            }

            root.setOnClickListener {
                ctx.startActivity(Intent(ctx, OtherUserProfileActivity::class.java).apply {
                    putExtra("user_id", model.id.toString())
                })
            }
        }
    }

}