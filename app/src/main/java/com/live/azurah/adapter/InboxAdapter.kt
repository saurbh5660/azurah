package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.activity.ChatActivity
import com.live.azurah.databinding.ItemInboxBinding
import com.live.azurah.model.InboxResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.formatChatDate
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.loadImage
import com.live.azurah.util.visible

class InboxAdapter(
    val ctx: Context,
    val list: ArrayList<InboxResponse.Body.Data>,
    val type: Int
) : RecyclerView.Adapter<InboxAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemInboxBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemInboxBinding.inflate(
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
        val model = list[position]
        with(holder.binding) {

            if (model.group != null) {
                name.text = buildString {
                    append(model.group.name ?: "User Deleted")
                }
                if (getPreference(
                        "id",
                        ""
                    ) == model.message?.firstOrNull()?.messageSender?.id.toString()
                ) {
                    tvMessage.text = buildString {
                        append("You: ")
                        if (model.message?.firstOrNull()?.msgType == 0) {
                            append(model.message.firstOrNull()?.message ?: "")
                        } else {
                            append("Image Sent")
                        }
                    }
                } else {
                    tvMessage.text = buildString {
                        val firstName = model.message?.firstOrNull()?.messageSender?.firstName ?: ""
                        val lastName = model.message?.firstOrNull()?.messageSender?.firstName ?: ""
                        if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
                            append(model.message?.firstOrNull()?.messageSender?.firstName ?: "")
                            append(" ")
                            append(model.message?.firstOrNull()?.messageSender?.lastName ?: "")
                            append(": ")
                            if (model.message?.firstOrNull()?.msgType == 0) {
                                append(model.message.firstOrNull()?.message ?: "")
                            } else {
                                append("Image Sent")
                            }
                        }
                    }
                }

                tvDate.text = model.updatedAt?.let { formatChatDate(it) }
                ivOverlay.loadImage(
                    ApiConstants.IMAGE_BASE_URL + model.group.image,
                    placeholder = R.drawable.profile_icon
                )

            } else {
                if (getPreference(
                        "id",
                        ""
                    ) == model.message?.firstOrNull()?.messageSender?.id.toString()
                ) {
                    name.text = buildString {
                        append(
                            model.message?.firstOrNull()?.messageReceiver?.username
                                ?: "User Deleted"
                        )
                    }

                    tvMessage.text = buildString {
                        if (model.message?.firstOrNull()?.msgType == 0) {
                            append(model.message.firstOrNull()?.message ?: "")
                        } else {
                            append("Image Sent")
                        }
                    }
                    tvDate.text = model.updatedAt?.let { formatChatDate(it) }
                    ivOverlay.loadImage(
                        ApiConstants.IMAGE_BASE_URL + model.message?.firstOrNull()?.messageReceiver?.image,
                        placeholder = R.drawable.profile_icon
                    )
                } else {
                    name.text = buildString {
                        append(
                            model.message?.firstOrNull()?.messageSender?.username ?: "User Deleted"
                        )
                    }
                    tvMessage.text = buildString {
                        if (model.message?.firstOrNull()?.msgType == 0) {
                            append(model.message.firstOrNull()?.message ?: "")
                        } else {
                            append("Image Sent")
                        }
                    }
                    tvDate.text = model.updatedAt?.let { formatChatDate(it) }
                    ivOverlay.loadImage(
                        ApiConstants.IMAGE_BASE_URL + model.message?.firstOrNull()?.messageSender?.image,
                        placeholder = R.drawable.profile_icon
                    )
                }
            }
            ivCount.text = model.unreadCount.toString()
            if ((model.unreadCount ?: 0) > 0) {
                ivCount.visible()
            } else {
                ivCount.gone()
            }

        }
        holder.itemView.setOnClickListener {
            var name = ""
            var image = ""
            var uid = ""
            var username = ""
            if (model.group != null) {
                uid = "0"
                name = model.group.name ?: ""
                image = model.group.image ?: ""
            } else {
                if (getPreference(
                        "id",
                        ""
                    ) == model.message?.firstOrNull()?.messageSender?.id.toString()
                ) {
                    uid = model.message?.firstOrNull()?.messageReceiver?.id.toString()
                    name = buildString {
                        append(model.message?.firstOrNull()?.messageReceiver?.firstName ?: "")
                        append(" ")
                        append(model.message?.firstOrNull()?.messageReceiver?.lastName ?: "")
                    }
                    image = model.message?.firstOrNull()?.messageReceiver?.image ?: ""
                    username = model.message?.firstOrNull()?.messageReceiver?.username ?: ""
                } else {
                    uid = model.message?.firstOrNull()?.messageSender?.id.toString()
                    name = buildString {
                        append(model.message?.firstOrNull()?.messageSender?.firstName ?: "")
                        append(" ")
                        append(model.message?.firstOrNull()?.messageSender?.lastName ?: "")
                    }
                    image = model.message?.firstOrNull()?.messageSender?.image ?: ""
                    username = model.message?.firstOrNull()?.messageSender?.username ?: ""

                }
            }

            if (uid.isNotEmpty()) {
                ctx.startActivity(Intent(ctx, ChatActivity::class.java).apply {
                    putExtra("uid2", uid)
                    putExtra("name", name)
                    putExtra("image", image)
                    putExtra("username", username)
                    putExtra("constant_id", model.message?.firstOrNull()?.constantId.toString())
                    if (model.groupId != 0 && model.groupId != null) {
                        putExtra("groupId", model.groupId.toString())
                    }
                })
            }
        }
    }

}