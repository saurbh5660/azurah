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
import com.live.azurah.databinding.ItemReplyCommentBinding
import com.live.azurah.model.CommentResponse
import com.live.azurah.model.PostResponse
import com.live.azurah.model.UserTag
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.formatCount
import com.live.azurah.util.getPreference
import com.live.azurah.util.getTime
import com.live.azurah.util.getTime1
import com.live.azurah.util.gone
import com.live.azurah.util.isCommentEditable
import com.live.azurah.util.isInternetAvailable
import com.live.azurah.util.loadImage
import com.live.azurah.util.removeMentionIfMatches
import com.live.azurah.util.styleTextDes
import com.live.azurah.util.visible
import java.util.ArrayList

class ReplyAdapter(val ctx: Context,val replies: ArrayList<CommentResponse.Replies>):RecyclerView.Adapter<ReplyAdapter.ViewHolder>() {
    var menuListener : ((pos:Int,view:View,item: CommentResponse.Replies?,notificationId:String)->Unit)? =null
    var replyListener : ((pos:Int,view:View,model: CommentResponse.Replies)->Unit)? =null
    var onLikeUnlike: ((pos: Int,commentId: String,status:String) -> Unit)? = null
    var onCommentEdit: ((pos: Int,commentId: String,desc:String,notificationId: String) -> Unit)? = null

    class ViewHolder(val binding: ItemReplyCommentBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemReplyCommentBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return replies.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            val model = replies[holder.absoluteAdapterPosition]

            if (model.user?.id.toString() == getPreference("id","")){
                ivEdit.visible()
            }else{
                ivEdit.gone()
            }
            tvComments.visible()
            ivForwrd.visible()

            ivPosts.loadImage(ApiConstants.IMAGE_BASE_URL+model.user?.image,R.drawable.profile_icon)
            tvName.text = buildString {
                append(model.user?.username ?: "")
               /* append(" ")
                append(model.user?.last_name ?: "")*/
            }
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

            /*styleTextDes(comment ,tvDescription){
              ctx.startActivity(Intent(ctx, OtherUserProfileActivity::class.java).apply {
                  putExtra("user_id",model.tagged_user_data?.id.toString())
              })
          }*/

            tvName.setOnClickListener {
                if (getPreference("id", "") != model.user?.id.toString()) {
                    ctx.startActivity(Intent(ctx, OtherUserProfileActivity::class.java).apply {
                        putExtra("user_id",model.user?.id.toString())
                    })
                }
            }

            ivPosts.setOnClickListener {
                if (getPreference("id", "") != model.user?.id.toString()) {
                    ctx.startActivity(Intent(ctx, OtherUserProfileActivity::class.java).apply {
                        putExtra("user_id",model.user?.id.toString())
                    })
                }
            }

            if (isCommentEditable(model.created_at.toString())) {
                ivEdit.visible()
            }else{
                ivEdit.gone()
            }

            tvCommentsText.text = buildString {
                append(formatCount(model.like_count ?: 0))
            }
            if (model.is_like == 1){
                ivForwrd.setImageResource(R.drawable.selected_heart)
                ivForwrd.imageTintList = ContextCompat.getColorStateList(ctx,R.color.star_red_color)
            }else{
                ivForwrd.setImageResource(R.drawable.unselected_heart)
                ivForwrd.imageTintList = ContextCompat.getColorStateList(ctx,R.color.light_black)
            }

            tvCommentsText.text = formatCount(model.like_count ?: 0)
            tvDate.text = getTime1(model.created_at ?: "")

            val gestureDetector = GestureDetector(ctx, object : GestureDetector.SimpleOnGestureListener() {
                override fun onLongPress(e: MotionEvent) {
                    super.onLongPress(e)
                    Log.d("fkjjkddg", "Long click detected")
                    clComment.backgroundTintList = ContextCompat.getColorStateList(ctx, R.color.transparent_blue)
                    menuListener?.invoke(holder.absoluteAdapterPosition, ivMore,model,(model.notification_id ?: 0).toString())
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
                        clComment.backgroundTintList = ContextCompat.getColorStateList(ctx, R.color.white)
                        true
                    }
                    else -> false
                }
            }


            ivForwrd.setOnClickListener {
                if (isInternetAvailable(ctx)){
                    if (model.is_like == 1) {
                        model.is_like = 0
                        model.like_count= (model.like_count?: 0).minus(1)
                        ivForwrd.setImageResource(R.drawable.unselected_heart)
                        ivForwrd.imageTintList = ContextCompat.getColorStateList(ctx,R.color.black)
                    }else{
                        model.is_like = 1
                        model.like_count= (model.like_count?:0).plus(1)
                        ivForwrd.setImageResource(R.drawable.selected_heart)
                        ivForwrd.imageTintList = ContextCompat.getColorStateList(ctx, R.color.star_red_color)
                    }
                    tvCommentsText.text = buildString {
                        append(formatCount(model.like_count ?: 0))
                    }

                    onLikeUnlike?.invoke(holder.absoluteAdapterPosition,model.id.toString(),model.is_like.toString())
                }
            }

            tvComments.setOnClickListener {
                replyListener?.invoke(holder.absoluteAdapterPosition,it,model)
            }

            ivEdit.setOnClickListener {
                onCommentEdit?.invoke(holder.absoluteAdapterPosition,model.id.toString(),model.description ?: "",(model.notification_id ?: 0).toString())
            }

        }
    }

    interface ClickListener{
        fun onClick()
    }

}