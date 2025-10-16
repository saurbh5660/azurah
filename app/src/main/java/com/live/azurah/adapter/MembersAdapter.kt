package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.activity.OtherUserProfileActivity
import com.live.azurah.databinding.ItemFollowBinding
import com.live.azurah.model.FollowFollowingResponse
import com.live.azurah.model.ViewGroupResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.isInternetAvailable
import com.live.azurah.util.loadImage
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible

class MembersAdapter(
    val ctx: Context,
    val list: ArrayList<ViewGroupResponse.Body.GroupMembers>,
) : RecyclerView.Adapter<MembersAdapter.ViewHolder>() {

    var listener: ((pos: Int, view: View, model: ViewGroupResponse.Body.GroupMembers) -> Unit)? =
        null
    var followUnfollowListener: ((pos: Int, model: ViewGroupResponse.Body.GroupMembers, view: View) -> Unit)? =
        null

    class ViewHolder(val binding: ItemFollowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemFollowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            val model = list[holder.absoluteAdapterPosition]
            if (getPreference("id", "") == model.user?.id.toString()) {
                tvFollow.gone()
                ivMore.gone()
            } else {
                tvFollow.visible()
                ivMore.visible()
            }
            when (model.isFollowByMe) {
                0 -> {
                    tvFollow.background =
                        AppCompatResources.getDrawable(ctx, R.drawable.round_corner_background)
                    tvFollow.backgroundTintList = ctx.getColorStateList(R.color.following_color)
                    tvFollow.text = "Requested"
                }

                1 -> {
                    tvFollow.background =
                        AppCompatResources.getDrawable(ctx, R.drawable.round_corner_background)
                    tvFollow.backgroundTintList = ctx.getColorStateList(R.color.following_color)
                    tvFollow.text = "Following"
                }

                else -> {
                    tvFollow.background =
                        AppCompatResources.getDrawable(ctx, R.drawable.round_stroke_background)
                    tvFollow.backgroundTintList = ctx.getColorStateList(R.color.blue)
                    tvFollow.text = "Follow"
                }
            }

            ivImage.loadImage(
                ApiConstants.IMAGE_BASE_URL + model.user?.image,
                placeholder = R.drawable.profile_icon
            )
            tvName.text = buildString {
                append(model.user?.username ?: "")
            }

            ivMore.visibility = View.VISIBLE

            tvFollow.setOnClickListener {
                if (isInternetAvailable(ctx)) {
                    val followStatus = model.isFollowByMe ?: 4
                    if (model.user?.profile_type == 1) {
                        if (followStatus != 0) {
                            model.isFollowByMe = 0
                        } else {
                            model.isFollowByMe = 3
                        }
                    } else {
                        if (model.isFollowByMe == 1) {
                            model.isFollowByMe = 3
                        } else {
                            model.isFollowByMe = 1
                        }
                    }
                    followUnfollowListener?.invoke(holder.absoluteAdapterPosition, model, it)
                    notifyItemChanged(holder.absoluteAdapterPosition)
                } else {
                    showCustomSnackbar(ctx, holder.binding.tvFollow, "Internet not available")
                }
            }

            root.setOnClickListener {
                ctx.startActivity(Intent(ctx, OtherUserProfileActivity::class.java).apply {
                    putExtra("user_id", model.user?.id.toString())
                })


            }

            ivMore.setOnClickListener {
                listener?.invoke(position, it, model)
            }
        }
    }

}