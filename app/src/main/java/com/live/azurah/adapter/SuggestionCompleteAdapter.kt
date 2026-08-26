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
import com.live.azurah.databinding.ItemCompleteSuggestionBinding
import com.live.azurah.model.PostLikesResposne
import com.live.azurah.model.PostResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.AvatarUtils
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.isInternetAvailable
import com.live.azurah.util.loadImage
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible

class SuggestionCompleteAdapter(
    val ctx: Context,
    val from: Int,
    val list: ArrayList<PostLikesResposne.Body.Data>
) : RecyclerView.Adapter<SuggestionCompleteAdapter.ViewHolder>() {
    var removeSuggestionListener: ((pos: Int,model: PostLikesResposne.Body.Data,) -> Unit)? = null
    var followUnfollowListener: ((pos: Int, model: PostLikesResposne.Body.Data, view: View) -> Unit)? =
        null

    class ViewHolder(val binding: ItemCompleteSuggestionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCompleteSuggestionBinding.inflate(
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
        val model = list[holder.absoluteAdapterPosition]
        with(holder.binding) {
            if (from == 1) {
                holder.binding.tvFollow.visibility = View.GONE
                ivCross.gone()

                val name = if (model.user?.display_name_preference == "1") {
                    model.user.first_name ?: ""
                } else {
                    "${model.user?.first_name ?: ""} ${model.user?.last_name ?: ""}".trim()
                }
                val displayName = if (name.isNotBlank()) name else (model.user?.username ?: "")
                tvName.text = displayName

                AvatarUtils.setupAvatar(ivProfile, tvInitials, model.user?.image, displayName)

                if (model.user?.username.toString().contains("@")) {
                    tvUserName.text = buildString {
                        append(model.user?.username ?: "")
                    }
                } else {
                    tvUserName.text = buildString {
                        append("@")
                        append(model.user?.username ?: "")
                    }
                }

                root.setOnClickListener {
                    if (getPreference("id", "") != model.user?.id.toString()) {
                        ctx.startActivity(Intent(ctx, OtherUserProfileActivity::class.java).apply {
                            putExtra("user_id", model.user?.id.toString())
                        })
                    }
                }
            } else {
                holder.binding.tvFollow.visibility = View.VISIBLE
                ivCross.visible()

                val name = if (model.display_name_preference == "1") {
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

                root.setOnClickListener {
                    if (getPreference("id", "") != model.id.toString()) {
                        ctx.startActivity(Intent(ctx, OtherUserProfileActivity::class.java).apply {
                            putExtra("user_id", model.id.toString())
                        })
                    }
                }

                when (model.isFollowByMe) {
                    0 -> {
                        tvFollow.text = "Requested"
                        tvFollow.backgroundTintList =
                            ContextCompat.getColorStateList(ctx, R.color.profile_stroke_color)
                    }

                    1 -> {
                        tvFollow.text = "Following"
                        tvFollow.backgroundTintList =
                            ContextCompat.getColorStateList(ctx, R.color.profile_stroke_color)
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
            }


            ivCross.setOnClickListener {
                if (isInternetAvailable(ctx)) {
                    removeSuggestionListener?.invoke(holder.absoluteAdapterPosition,model)
                } else {
                    showCustomSnackbar(ctx, holder.binding.tvFollow, "Internet not available")
                }

            }

            tvFollow.setOnClickListener {
                if (isInternetAvailable(ctx)) {
                    val followStatus = model.isFollowByMe ?: 4
                    if (model.profile_type == 1) {
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


        }
    }

}