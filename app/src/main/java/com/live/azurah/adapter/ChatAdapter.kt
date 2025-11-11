package com.live.azurah.adapter

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.pgreze.reactions.ReactionPopup
import com.github.pgreze.reactions.ReactionsConfigBuilder
import com.live.azurah.R
import com.live.azurah.activity.OtherUserProfileActivity
import com.live.azurah.databinding.ItemChatBinding
import com.live.azurah.model.ChatResponse
import com.live.azurah.model.CommentResponse
import com.live.azurah.model.InboxResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.chatDate
import com.live.azurah.util.convertTimestampToTime
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.loadImage
import com.live.azurah.util.visible
import androidx.core.net.toUri
import com.live.azurah.model.FullImageModel
import com.live.azurah.model.PostResponse
import com.live.azurah.util.ShowImagesDialogFragment
import com.live.azurah.util.makeLinksClickable
import com.live.azurah.util.showCustomToast
import com.live.azurah.util.showMaterialDialog
import com.live.azurah.util.showSaveDialog

class ChatAdapter(
    val ctx: Context,
    val list: ArrayList<ChatResponse.Body.Data>
) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    private val strings = arrayOf("Like", "Love", "Prayer Hands", "Cross")
    var menuListener: ((msgId: String, pos: Int, view: View, userId: String, userName: String, type: Int) -> Unit)? =
        null
    var onPostImages: ((pos:Int,image: ChatResponse.Body.Data?) -> Unit)? = null

    class ViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChatBinding.inflate(
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
    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {

            val popup = ReactionPopup(
                ctx,
                ReactionsConfigBuilder(ctx)
                    .withReactions(
                        intArrayOf(
                            R.drawable.like1_icon,
                            R.drawable.love,
                            R.drawable.pray_icon_1,
                            R.drawable.cross_icon1
                        )
                    )

                    .withReactionTexts { position: Int -> strings[position] }
                    .withTextColor(Color.WHITE)
                    .withTextSize(12f)
                    .withPopupColor(ContextCompat.getColor(ctx, R.color.popup_color))
                    .withTextHorizontalPadding(16)
                    .withTextVerticalPadding(0)

                    .build(),
                { _: Int? ->

                    true
                })

            popup.reactionSelectedListener = { position ->
                true
            }

            /*holder.binding.receiverMsg.setOnLongClickListener {
                lastMotionEvent?.let { event ->
                    popup.onTouch(it, event)
                }
                true
            }

            holder.binding.receiverMsg.setOnTouchListener { v, event ->
                lastMotionEvent = event
                false
            }


            holder.binding.senderMsg.setOnTouchListener { v, event ->
                lastMotionEvent = event
                false
            }

            holder.binding.senderMsg.setOnLongClickListener {
                lastMotionEvent?.let { event ->
                    popup.onTouch(it, event)
                }
                true
            }*/

            val model = list[position]

            if (model.groupId != 0 && model.groupId == null) {
                clSeen.visibility = View.VISIBLE
                tvSeenBy.visibility = View.GONE
            } else {
                clSeen.visibility = View.GONE
                if ((model.message_read_status?.size ?: 0) > 1) {
                    tvSeenBy.visibility = View.VISIBLE
                    tvSeenBy.text = buildString {
                        append("Seen by: ")
                        append(model.message_read_status?.size ?: 0)
                    }
                } else {
                    tvSeenBy.visibility = View.GONE
                }

            }

            tvDate.text = chatDate(model.createdAt ?: "")
            if (position > 0) {
                if (chatDate(
                        model.createdAt ?: ""
                    ) == chatDate(list[holder.absoluteAdapterPosition - 1].createdAt ?: "")
                ) {
                    cvTime.gone()
                } else {
                    cvTime.visible()
                }
            } else {
                cvTime.visible()
            }

            clReceiver.setOnLongClickListener {
                if (model.group != null) {
                    menuListener?.invoke(
                        model.id.toString(),
                        holder.absoluteAdapterPosition,
                        receiverMsg,
                        model.messageSender?.id.toString(),
                        model.messageSender?.username.toString(),
                        2
                    )
                } else {
                    menuListener?.invoke(
                        model.id.toString(),
                        holder.absoluteAdapterPosition,
                        receiverMsg,
                        model.messageReceiver?.id.toString(),
                        model.messageReceiver?.username.toString(),
                        2
                    )
                }
                true
            }

            clSSender.setOnLongClickListener {
                menuListener?.invoke(
                    model.id.toString(),
                    holder.absoluteAdapterPosition,
                    senderMsg,
                    model.messageSender?.id.toString(),
                    model.messageSender?.username.toString(),
                    1
                )
                true
            }

            with(holder.binding) {
                if (model.group != null) {
                    if (model.messageSender?.id.toString() == getPreference("id", "")) {
                        clSSender.visible()
                        clReceiver.gone()
                        makeLinksClickable(senderMsg, model.message)
                        tvSenderTime.text = model.created?.let { convertTimestampToTime(it) }
                        senderProfile.loadImage(
                            ApiConstants.IMAGE_BASE_URL + model.messageSender?.image,
                            R.drawable.profile_icon
                        )
                    } else {
                        clSSender.gone()
                        clReceiver.visible()
                        makeLinksClickable(receiverMsg, model.message)
                        tvTime.text = model.created?.let { convertTimestampToTime(it) }
                        ivProfile.loadImage(
                            ApiConstants.IMAGE_BASE_URL + model.messageSender?.image,
                            R.drawable.profile_icon
                        )

                        ivProfile.setOnClickListener {
                            ctx.startActivity(
                                Intent(
                                    ctx,
                                    OtherUserProfileActivity::class.java
                                ).apply {
                                    putExtra("user_id", model.messageSender?.id.toString())
                                })
                        }
                    }
                } else {

                    if (model.isRead == "1") {
                        ivSeen.setImageResource(R.drawable.tick_icon)
                    } else {
                        ivSeen.setImageResource(R.drawable.tick_icon_grey)
                    }

                    if (model.messageSender?.id.toString() == getPreference("id", "")) {
                        clSSender.visible()
                        clReceiver.gone()
                        if (model.msgType == 0) {
                            senderMsg.visible()
                            ivSenderImage.gone()
                            ivSenderDownload.gone()
                            makeLinksClickable(senderMsg, model.message)

                        } else {
                            senderMsg.gone()
                            ivSenderImage.visible()
                            ivSenderDownload.visible()

                            ivSenderImage.loadImage(
                                ApiConstants.IMAGE_BASE_URL + model.message,
                                R.drawable.image_placeholder
                            )
                            Log.d("fgdsgadsgdsg","fdsgfdsg")
                            ivSenderDownload.setOnClickListener {
                                ctx.showSaveDialog("Save Image","Are you sure you want to save this image in your phone?"){
                                    downloadImage(ctx,ApiConstants.IMAGE_BASE_URL + model.message,"image_${System.currentTimeMillis()}.jpg")
                                }
                            }

                            ivSenderImage.setOnClickListener {
                                onPostImages?.invoke(holder.absoluteAdapterPosition,model)

                            }

                        }
                        tvSenderTime.text = model.created?.let { convertTimestampToTime(it) }
                        senderProfile.loadImage(
                            ApiConstants.IMAGE_BASE_URL + model.messageSender?.image,
                            R.drawable.profile_icon
                        )
                    } else {
                        clSSender.gone()
                        clReceiver.visible()
                        makeLinksClickable(receiverMsg, model.message)

                        if (model.msgType == 0) {
                            receiverMsg.visible()
                            ivReceiverImage.gone()
                            ivReceiverDownload.gone()
                            makeLinksClickable(receiverMsg, model.message)

                        } else {
                            receiverMsg.gone()
                            ivReceiverImage.visible()
                            ivReceiverDownload.visible()
                            ivReceiverImage.loadImage(
                                ApiConstants.IMAGE_BASE_URL + model.message,
                                R.drawable.image_placeholder
                            )
                            ivReceiverDownload.setOnClickListener {
                                ctx.showSaveDialog("Save Image","Are you sure you want to save this image in your phone?"){
                                    downloadImage(ctx,ApiConstants.IMAGE_BASE_URL + model.message,"image_${System.currentTimeMillis()}.jpg")
                                }

                            }

                        }
                        tvTime.text = model.created?.let { convertTimestampToTime(it) }
                        ivProfile.loadImage(
                            ApiConstants.IMAGE_BASE_URL + model.messageSender?.image,
                            R.drawable.profile_icon
                        )

                        ivProfile.setOnClickListener {
                            ctx.startActivity(
                                Intent(
                                    ctx,
                                    OtherUserProfileActivity::class.java
                                ).apply {
                                    putExtra("user_id", model.messageSender?.id.toString())
                                })
                        }

                    }
                }

            }

        }
    }

    fun downloadImage(context: Context, url: String, fileName: String) {
        try {
            Log.d("asgvsnas",url)
            val request = DownloadManager.Request(url.toUri())
                .setTitle(fileName)
                .setDescription("Downloading image...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setMimeType("image/jpeg")

            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            showCustomToast(ctx,"Download started...")
        } catch (e: Exception) {
            e.printStackTrace()
            showCustomToast(ctx,"Download failed: ${e.message}")
        }
    }



}