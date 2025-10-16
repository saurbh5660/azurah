package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.activity.OtherUserProfileActivity
import com.live.azurah.databinding.ItemCommentBinding
import com.live.azurah.model.CommentResponse
import com.live.azurah.model.UserTag
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.formatCount
import com.live.azurah.util.getPreference
import com.live.azurah.util.getTime1
import com.live.azurah.util.gone
import com.live.azurah.util.isCommentEditable
import com.live.azurah.util.isInternetAvailable
import com.live.azurah.util.loadImage
import com.live.azurah.util.removeMentionIfMatches
import com.live.azurah.util.styleTextDes
import com.live.azurah.util.visible

class CommentAdapter(val ctx: Context, private val commentList: ArrayList<CommentResponse>) :
    RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    var menuListener: ((pos: Int, view: View,replyPos: Int, model: CommentResponse, item: CommentResponse.Replies?,notificationId:String) -> Unit)? = null
    var replyListener: ((pos: Int, view: View, model: CommentResponse, item: CommentResponse.Replies?, replyPos: Int?) -> Unit)? = null
    var onLikeUnlike: ((pos: Int, commentId: String, status: String, postId: String) -> Unit)? = null
    var onCommentEdit: ((pos: Int, replyPos: Int, commentId: String, desc: String,notificationId:String) -> Unit)? = null

    class ViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCommentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            val model = commentList[holder.absoluteAdapterPosition]

            if (model.user?.id.toString() == getPreference("id", "")) {
                ivEdit.visible()
            } else {
                ivEdit.gone()
            }
            tvReplies.visible()
            tvComments.visible()
            ivForwrd.visible()

            ivPosts.loadImage(
                ApiConstants.IMAGE_BASE_URL + model.user?.image,
                R.drawable.profile_icon
            )
            tvName.text = buildString {
                append(model.user?.username ?: "")
               /* append(" ")
                append(model.user?.last_name ?: "")*/
            }

            /*tvDescription.text =
                removeMentionIfMatches(model.description ?: "", model.user?.username ?: "")*/

            val comment= removeMentionIfMatches(model.description ?: "",model.user?.username ?: "")

            styleTextDes(
                comment,
                tvDescription,
                model.post_comment_tags.map { UserTag(id = it.user?.id.toString(),it.user?.username ?: "") }
            ) { username, userId ->
                ctx.startActivity(Intent(ctx, OtherUserProfileActivity::class.java).apply {
                    putExtra("user_id", userId)
                })
            }

            tvCommentsText.text = buildString {
                append(formatCount(model.like_count ?: 0))
            }
            if (model.is_like == 1) {
                ivForwrd.setImageResource(R.drawable.selected_heart)
                ivForwrd.imageTintList =
                    ContextCompat.getColorStateList(ctx, R.color.star_red_color)
            } else {
                ivForwrd.setImageResource(R.drawable.unselected_heart)
                ivForwrd.imageTintList = ContextCompat.getColorStateList(ctx, R.color.light_black)
            }

            tvCommentsText.text = formatCount(model.like_count ?: 0)
            tvDate.text = getTime1(model.created_at ?: "")

            tvReplies.setOnClickListener {
                rvReply.visibility = View.VISIBLE
                tvReplies.visibility = View.GONE
            }

            Log.d("dsvsdvsd", model.created_at.toString())

            if (isCommentEditable(model.created_at.toString())) {
                ivEdit.visible()
            }else{
                ivEdit.gone()
            }

            tvName.setOnClickListener {
                if (getPreference("id", "") != model.user?.id.toString()) {
                    ctx.startActivity(Intent(ctx, OtherUserProfileActivity::class.java).apply {
                        putExtra("user_id", model.user?.id.toString())
                    })
                }
            }

            ivPosts.setOnClickListener {
                if (getPreference("id", "") != model.user?.id.toString()) {
                    ctx.startActivity(Intent(ctx, OtherUserProfileActivity::class.java).apply {
                        putExtra("user_id", model.user?.id.toString())
                    })
                }
            }

            val adapter = ReplyAdapter(ctx, model.replies)
            rvReply.adapter = adapter

            if (model.replies.isEmpty()) {
                tvReplies.gone()
            } else {
                tvReplies.visible()
                if (model.replies.size > 1) {
                    tvReplies.text = buildString {
                        append("-- View ")
                        append(model.replies.size)
                        append(" replies --")
                    }

                } else {
                    tvReplies.text = buildString {
                        append("-- View ")
                        append(model.replies.size)
                        append(" reply --")
                    }
                }
            }

            adapter.menuListener = { pos, view, item,notificationId ->
                menuListener?.invoke(holder.absoluteAdapterPosition, view,pos, model, item,notificationId)
            }
            adapter.replyListener = { pos, view, item ->
                replyListener?.invoke(holder.absoluteAdapterPosition, view, model, item, pos)
            }

            adapter.onLikeUnlike = { pos, id, status ->
                onLikeUnlike?.invoke(
                    holder.absoluteAdapterPosition,
                    id,
                    status,
                    model.post_id.toString()
                )
            }

            adapter.onCommentEdit = { pos, commentId, desc,notificationId ->
                onCommentEdit?.invoke(holder.absoluteAdapterPosition, pos, commentId, desc,notificationId)
            }

            val gestureDetector =
                GestureDetector(ctx, object : GestureDetector.SimpleOnGestureListener() {
                    override fun onLongPress(e: MotionEvent) {
                        super.onLongPress(e)
                        Log.d("fkjjkddg", "Long click detected")
                        clComment.backgroundTintList =
                            ContextCompat.getColorStateList(ctx, R.color.transparent_blue)
                        menuListener?.invoke(holder.absoluteAdapterPosition, ivMore,-1, model, null,(model.notification_id ?: 0).toString())
                    }
                })

            root.setOnTouchListener { view, event ->
                gestureDetector.onTouchEvent(event)

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Change background to indicate pressed state
                        true
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        // Restore the background if the touch is released or canceled
                        clComment.backgroundTintList =
                            ContextCompat.getColorStateList(ctx, R.color.white)
                        true
                    }

                    else -> false
                }
            }

            ivEdit.setOnClickListener {
                onCommentEdit?.invoke(
                    holder.absoluteAdapterPosition,
                    -1,
                    model.id.toString(),
                    model.description ?: "",
                    (model.notification_id ?: 0).toString()
                )
            }

            ivForwrd.setOnClickListener {
                if (isInternetAvailable(ctx)) {
                    if (model.is_like == 1) {
                        model.is_like = 0
                        model.like_count = (model.like_count ?: 0).minus(1)
                        ivForwrd.setImageResource(R.drawable.unselected_heart)
                        ivForwrd.imageTintList = ContextCompat.getColorStateList(ctx, R.color.black)
                    } else {
                        model.is_like = 1
                        model.like_count = (model.like_count ?: 0).plus(1)
                        ivForwrd.setImageResource(R.drawable.selected_heart)
                        ivForwrd.imageTintList =
                            ContextCompat.getColorStateList(ctx, R.color.star_red_color)
                    }
                    tvCommentsText.text = buildString {
                        append(formatCount(model.like_count ?: 0))
                    }

                    onLikeUnlike?.invoke(
                        holder.absoluteAdapterPosition,
                        model.id.toString(),
                        model.is_like.toString(),
                        model.post_id.toString()
                    )
                }
            }

            ivPosts.setOnClickListener {
                if (getPreference("id", "") != model.user_id.toString()) {
                    ctx.startActivity(Intent(ctx, OtherUserProfileActivity::class.java).apply {
                        putExtra("user_id", model.user_id.toString())
                    })
                }
            }

            tvName.setOnClickListener {
                if (getPreference("id", "") != model.user_id.toString()) {
                    ctx.startActivity(Intent(ctx, OtherUserProfileActivity::class.java).apply {
                        putExtra("user_id", model.user_id.toString())
                    })
                }
            }


            tvComments.setOnClickListener {
                replyListener?.invoke(holder.absoluteAdapterPosition, it, model, null, null)
            }

        }
    }


}