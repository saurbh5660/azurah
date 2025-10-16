package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.activity.OtherUserProfileActivity
import com.live.azurah.databinding.ItemPostBinding
import com.live.azurah.model.PostResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.loadImage

class PostAdapter(val ctx: Context,val postList: ArrayList<PostResponse.Body.Data>,val listener: ClickListener):RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    var shareClick : ((pos:Int)->Unit)? = null

    class ViewHolder(val binding: ItemPostBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemPostBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            val model = postList[holder.absoluteAdapterPosition]

            if (!postList[position].suggestedUsers.isNullOrEmpty()){
//                clPost.visibility = View.GONE
                clFollow.visibility = View.VISIBLE

                val adapter = SuggestionAdapter(ctx, ArrayList())
                rvFollow.adapter = adapter
                tvSeeAll.setOnClickListener {
                    listener.onSuggestionClick()
                }

            }else{
                clPost.visibility = View.VISIBLE
                clFollow.visibility = View.GONE
                val helper = LinearSnapHelper()
                rvPosts.onFlingListener =null
                helper.attachToRecyclerView(rvPosts)
                val adapter = ImagesVideoAdapter(ctx)
                rvPosts.adapter = adapter
            }

            tvName.text = buildString {
                append(model.user?.first_name ?: "")
                append(" ")
                append(model.user?.last_name ?: "")
            }
            ivPosts.loadImage(ApiConstants.IMAGE_BASE_URL+model.user?.image)
            setupSeeMoreText(tvDescription,model.description ?: "")

            tvLikes.setOnClickListener {
                listener.onLikesClick()
            }
            ivMore.setOnClickListener {
                listener.onClick(it)
            }

            ivShare.setOnClickListener {
                shareClick?.invoke(position)
            }

            ivPosts.setOnClickListener {
                ctx.startActivity(Intent(ctx,OtherUserProfileActivity::class.java))
            }

            tvComments.setOnClickListener {
                listener.onCommentClick()
            }
            ivComment.setOnClickListener {
                listener.onCommentClick()
            }

          /*  ivLike.setOnClickListener {
                if (postList[position].heartSelected){
                    postList[position].heartSelected = false
                    ivLike.setImageResource(R.drawable.unselected_heart)
                    ivLike.imageTintList = ContextCompat.getColorStateList(ctx,R.color.black)

                }else{
                    postList[position].heartSelected = true
                    ivLike.setImageResource(R.drawable.selected_heart)
                    ivLike.imageTintList = ContextCompat.getColorStateList(ctx, R.color.star_red_color)
                }
            }

            ivBookmark.setOnClickListener {
                if (postList[position].bookmarkSelected){
                    postList[position].bookmarkSelected = false
                    ivBookmark.setImageResource(R.drawable.bookmark_icon)
                    ivBookmark.imageTintList = ctx.getColorStateList(R.color.black)
                }else{
                    postList[position].bookmarkSelected = true
                    ivBookmark.setImageResource(R.drawable.selected_bookmark_icon)
                    ivBookmark.imageTintList = ctx.getColorStateList(R.color.bookmark_color)
                }
            }*/
        }
    }

    private fun setupSeeMoreText(textView: TextView, fullText: String) {
        val words = fullText.split(" ")
        val truncatedText = if (words.size > 30) {
            words.take(30).joinToString(" ") + "..."
        } else {
            fullText
        }
        val seeMoreText = " See More"

        val spannableString = SpannableString(truncatedText + seeMoreText)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                textView.text = fullText
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        spannableString.setSpan(clickableSpan, truncatedText.length, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(ctx,R.color.blue)), truncatedText.length, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
    }


    interface ClickListener{
        fun onClick(view: View)
        fun onCommentClick(){}
        fun onSuggestionClick(){}
        fun onLikesClick(){}
    }

}