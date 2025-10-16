package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.activity.OtherUserProfileActivity
import com.live.azurah.activity.QuestDetailActivity
import com.live.azurah.activity.ViewPostActivity
import com.live.azurah.databinding.ItemNotificationBinding
import com.live.azurah.model.NotificationListingResponse
import com.live.azurah.model.PostLikesResposne
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.chatDate
import com.live.azurah.util.formatNotificationTime
import com.live.azurah.util.getTime
import com.live.azurah.util.gone
import com.live.azurah.util.loadImage
import com.live.azurah.util.showMaterialDialog
import com.live.azurah.util.visible

class NotificationAdapter(
    val ctx: Context,
    val list: ArrayList<NotificationListingResponse.Body.Data>
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    var acceptRejectListener: ((pos: Int, model: NotificationListingResponse.Body.Data,status:Int) -> Unit)? =null
    class ViewHolder(val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            val model = list[holder.absoluteAdapterPosition]
            if (model.entity_type== "ADMIN_WARNING"){
                name.text = "\uD83D\uDCCC Message from Azrius Admin"
            }else{
                name.text = model.message ?: ""
            }

            when (model.type) {
                1 -> {
//                    ivOverlay.setImageResource(R.drawable.like_icon)
                    tvAccept.visible()
                    tvReject.visible()
                }

                else -> {
//                    ivOverlay.setImageResource(R.drawable.like_icon)
//                    ivOverlay.gone()
                    tvAccept.gone()
                    tvReject.gone()
                }

            }

            if (model.is_read == "1") {
                clItem.backgroundTintList = ctx
                    .getColorStateList(R.color.white)
                ivDot.gone()

            } else {
                clItem.backgroundTintList = ctx
                    .getColorStateList(R.color.transparent_blue)
                ivDot.visible()
            }

            tvWeekName.text = formatNotificationTime(model.created_at ?: "")
            if (position > 0) {
                if (formatNotificationTime(
                        model.created_at ?: ""
                    ) == formatNotificationTime(list[holder.absoluteAdapterPosition - 1].created_at ?: "")
                ) {
                    tvWeekName.gone()
                } else {
                    tvWeekName.visible()
                }
            } else {
                tvWeekName.visible()
            }

            tvMessage.text = buildString {
                append(getTime(model.created_at ?: ""))
            }

            ivOverlay.loadImage(ApiConstants.IMAGE_BASE_URL+model.notification_sender?.image,placeholder = R.drawable.profile_icon)

            root.setOnClickListener {
                model.is_read = "1"
                clItem.backgroundTintList = ctx
                    .getColorStateList(R.color.white)
                ivDot.gone()

                if (!model.entity_type.isNullOrEmpty()) {
                    when (model.entity_type) {
                        "ADMIN_WARNING"->{
                            ctx.showMaterialDialog("Admin Notification",model.message ?: "")
                        }

                       "TESTIMONY_COMMENT" -> {
                            ctx.startActivity(Intent(ctx,QuestDetailActivity::class.java).apply {
                                putExtra("id",model.entity_id?.toString())
                                putExtra("from","testimony")
                            })
                        }
                        "TESTIMONY_LIKE" -> {
                            ctx.startActivity(Intent(ctx,QuestDetailActivity::class.java).apply {
                                putExtra("id",model.entity_id?.toString())
                                putExtra("from","testimony")
                            })
                        }

                        "PRAYER_COMMENT" -> {
                            ctx.startActivity(Intent(ctx,QuestDetailActivity::class.java).apply {
                                putExtra("id",model.entity_id?.toString())
                                putExtra("from","prayer")
                            })
                        }
                        "PRAYER_LIKE" -> {
                            ctx.startActivity(Intent(ctx,QuestDetailActivity::class.java).apply {
                                putExtra("id",model.entity_id?.toString())
                                putExtra("from","prayer")
                            })
                        }
                        "COMMUNITY_COMMENT" -> {
                            ctx.startActivity(Intent(ctx,QuestDetailActivity::class.java).apply {
                                putExtra("id",model.entity_id?.toString())
                            })
                        }
                        "COMMUNITY_LIKE" -> {
                            ctx.startActivity(Intent(ctx,QuestDetailActivity::class.java).apply {
                                putExtra("id",model.entity_id?.toString())
                            })
                        }

                        "POST_COMMENT" -> {
                            ctx.startActivity(Intent(ctx,ViewPostActivity::class.java).apply {
                                putExtra("postId",model.entity_id.toString())
                                putExtra("from","comment")
                            })
                        }
                        "POST_LIKE" -> {
                            ctx.startActivity(Intent(ctx,ViewPostActivity::class.java).apply {
                                putExtra("postId",model.entity_id.toString())
                                putExtra("from","like")
                            })
                        }

                        "FRIEND_ACCEPTED"->{
                            ctx.startActivity(Intent(ctx,OtherUserProfileActivity::class.java).apply {
                                putExtra("user_id",model.sender_id.toString())
                            })
                        }

                        "POST_COMMENT_TAG"->{
                            ctx.startActivity(Intent(ctx,ViewPostActivity::class.java).apply {
                                putExtra("postId",model.entity_id.toString())
                                putExtra("from","comment")
                            })
                        }

                        else -> {
                            ctx.startActivity(Intent(ctx,OtherUserProfileActivity::class.java).apply {
                                putExtra("user_id",model.sender_id.toString())
                            })
                        }

                    }
                }else{
                    when (model.type) {
                        1,2 -> {
                            ctx.startActivity(Intent(ctx,OtherUserProfileActivity::class.java).apply {
                                putExtra("user_id",model.notification_sender?.id.toString())
                            })
                        }
                        else -> {
                        }
                    }
                }

            }

            tvAccept.setOnClickListener {
                acceptRejectListener?.invoke(holder.absoluteAdapterPosition,model,1)
            }

            tvReject.setOnClickListener {
                acceptRejectListener?.invoke(holder.absoluteAdapterPosition,model,0)

            }


         /*   if (position == 0) {
                tvWeekName.visibility = View.VISIBLE
                tvWeekName.text = "Today"
                clItem.backgroundTintList = ctx
                    .getColorStateList(R.color.transparent_blue)
            } else if (position == 4) {
                tvWeekName.visibility = View.VISIBLE
                tvWeekName.text = "This Week"
                ivDot.visibility = View.GONE

            } else if (position == 6) {
                tvWeekName.visibility = View.VISIBLE
                tvWeekName.text = "Over a Month Ago"
                ivDot.visibility = View.GONE
                clItem.backgroundTintList = ctx
                    .getColorStateList(R.color.white)
            } else {
                tvWeekName.visibility = View.GONE
                ivDot.visibility = View.GONE
                clItem.backgroundTintList = ctx
                    .getColorStateList(R.color.white)

            }
            if (position == list.size - 1) {
                view1.visibility = View.GONE
            }

            root.setOnClickListener {

            }*/
        }
    }

}