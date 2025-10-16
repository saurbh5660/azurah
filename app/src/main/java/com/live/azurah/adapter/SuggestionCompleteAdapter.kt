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
    var removeSuggestionListener: ((pos: Int) -> Unit)? = null
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
                ivProfile.loadImage(
                    ApiConstants.IMAGE_BASE_URL + model.user?.image,
                    placeholder = R.drawable.profile_icon
                )
                ivCross.gone()
                if (model.user?.image.isNullOrEmpty()) {
                    ivProfile.borderColor = ContextCompat.getColor(ctx, R.color.blue)
                    ivProfile.borderWidth = 2
                } else {
                    ivProfile.borderColor = ContextCompat.getColor(ctx, android.R.color.transparent)
                    ivProfile.borderWidth = 0
                }
                if (model.user?.display_name_preference == "1") {
                    tvName.text = buildString {
                        append(model.user.first_name ?: "")
                    }
                } else {
                    tvName.text = buildString {
                        append(model.user?.first_name ?: "")
                        append(" ")
                        append(model.user?.last_name ?: "")
                    }
                }

                if (model.user?.username.toString().contains("@")) {
                    tvName.text = buildString {
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
                ivProfile.loadImage(
                    ApiConstants.IMAGE_BASE_URL + model.image,
                    placeholder = R.drawable.profile_icon
                )
                ivCross.visible()
                if (model.image.isNullOrEmpty()) {
                    ivProfile.borderColor = ContextCompat.getColor(ctx, R.color.blue)
                    ivProfile.borderWidth = 2
                } else {
                    ivProfile.borderColor = ContextCompat.getColor(ctx, android.R.color.transparent)
                    ivProfile.borderWidth = 0
                }
                if (model.user?.display_name_preference == "1") {
                    tvName.text = buildString {
                        append(model.first_name ?: "")
                    }
                } else {
                    tvName.text = buildString {
                        append(model.first_name ?: "")
                        append(" ")
                        append(model.last_name ?: "")
                    }
                }


                tvUserName.text = buildString {
                    append("@")
                    append(model.username ?: "")
                }

                root.setOnClickListener {
                    if (getPreference("id", "") != model.user?.id.toString()) {
                        ctx.startActivity(Intent(ctx, OtherUserProfileActivity::class.java).apply {
                            putExtra("user_id", model.id.toString())
                        })
                    }
                }

                when (model.isFollowByMe) {
                    0 -> {
                        tvFollow.text = "Requested"
                        tvFollow.backgroundTintList =
                            ContextCompat.getColorStateList(ctx, R.color.shop_cat_color)
//                        tvFollow.setTextColor(ContextCompat.getColorStateList(ctx,R.color.black))
                    }

                    1 -> {
                        tvFollow.text = "Following"
                        tvFollow.backgroundTintList =
                            ContextCompat.getColorStateList(ctx, R.color.shop_cat_color)
//                        tvFollow.setTextColor(ContextCompat.getColorStateList(ctx,R.color.blue))
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
                        tvFollow.backgroundTintList =
                            ContextCompat.getColorStateList(ctx, R.color.shop_cat_color)
//                            tvFollow.setTextColor(ContextCompat.getColorStateList(ctx,R.color.black))

                        /*  if (model.profile_type == 1) {
                              tvFollow.text = "Request"
  //                            tvFollow.setTextColor(ContextCompat.getColorStateList(ctx,R.color.blue))
                          } else {
                              tvFollow.text = "Follow"
                              tvFollow.backgroundTintList = ContextCompat.getColorStateList(ctx,R.color.divider_grey)
  //                            tvFollow.setTextColor(ContextCompat.getColorStateList(ctx,R.color.blue))
                          }*/
                    }
                }
            }


            ivCross.setOnClickListener {
                list.removeAt(holder.absoluteAdapterPosition)
                notifyItemRemoved(holder.absoluteAdapterPosition)
                removeSuggestionListener?.invoke(holder.absoluteAdapterPosition)
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