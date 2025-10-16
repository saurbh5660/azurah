package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
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
    var removeSuggestionListener: ((pos: Int) -> Unit)? = null

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
            ivProfile.loadImage(
                ApiConstants.IMAGE_BASE_URL + model.image,
                placeholder = R.drawable.profile_icon
            )
            if (model.image.isNullOrEmpty()) {
                ivProfile.borderColor = ContextCompat.getColor(ctx, R.color.blue)
                ivProfile.borderWidth = 2
            } else {
                ivProfile.borderColor = ContextCompat.getColor(ctx, android.R.color.transparent)
                ivProfile.borderWidth = 0
            }

            if (model.display_name_preference == 1){
                tvName.text = buildString {
                    append(model.first_name)
                }
            }else{
                tvName.text = buildString {
                    append(model.first_name)
                    append(" ")
                    append(model.last_name)
                }
            }

            tvUserName.text = buildString {
                append("@")
                append(model.username ?: "")
            }

            when (model.isFollowByMe) {
                0 -> {
                    tvFollow.text = "Requested"
                    tvFollow.backgroundTintList = ContextCompat.getColorStateList(ctx,R.color.profile_stroke_color)
//                    tvFollow.setTextColor(ContextCompat.getColorStateList(ctx,R.color.black))
                }

                1 -> {
                    tvFollow.text = "Following"
                    tvFollow.backgroundTintList = ContextCompat.getColorStateList(ctx,R.color.shop_cat_color)
//                    tvFollow.setTextColor(ContextCompat.getColorStateList(ctx,R.color.blue))
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

                    /*if (model.profile_type == 1) {
                        tvFollow.text = "Request"
                        tvFollow.backgroundTintList = ContextCompat.getColorStateList(ctx,R.color.divider_grey)
//                        tvFollow.setTextColor(ContextCompat.getColorStateList(ctx,R.color.blue))
                    } else {
                        tvFollow.text = "Follow"
//                        tvFollow.setTextColor(ContextCompat.getColorStateList(ctx,R.color.blue))
                    }*/
                    tvFollow.backgroundTintList = ContextCompat.getColorStateList(ctx,R.color.shop_cat_color)
//                        tvFollow.setTextColor(ContextCompat.getColorStateList(ctx,R.color.black))


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
                suggestedUsers.removeAt(holder.absoluteAdapterPosition)
                notifyItemRemoved(holder.absoluteAdapterPosition)
                removeSuggestionListener?.invoke(holder.absoluteAdapterPosition)
            }

            root.setOnClickListener {
                ctx.startActivity(Intent(ctx, OtherUserProfileActivity::class.java).apply {
                    putExtra("user_id", model.id.toString())
                })
            }
        }
    }

}