package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.activity.OtherUserProfileActivity
import com.live.azurah.activity.QuestDetailActivity
import com.live.azurah.databinding.ItemBiblePostBinding
import com.live.azurah.model.CommunityForumResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.formatCount
import com.live.azurah.util.getPreference
import com.live.azurah.util.getRelativeTime
import com.live.azurah.util.gone
import com.live.azurah.util.isInternetAvailable
import com.live.azurah.util.loadImage
import com.live.azurah.util.removeExtraSpaces
import com.live.azurah.util.visible

class BiblePostAdapter(val ctx: Context,val listener: ClickListener,val type:Int = 0,val communityList: ArrayList<CommunityForumResponse.Body.Data>):RecyclerView.Adapter<BiblePostAdapter.ViewHolder>() {
    var onLikeUnlike: ((pos: Int,model: CommunityForumResponse.Body.Data) -> Unit)? = null
    var likeListener: ((pos: Int,model: CommunityForumResponse.Body.Data) -> Unit)? = null
    var menuListener: ((pos: Int, model: CommunityForumResponse.Body.Data,view:View) -> Unit)? = null
    var deleteListener : ((pos:Int,model: CommunityForumResponse.Body.Data)->Unit)? = null
    var categoryListener: ((pos: Int, model: CommunityForumResponse.Body.Data,view:View,text:String) -> Unit)? = null

    class ViewHolder(val binding: ItemBiblePostBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemBiblePostBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return communityList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            val item = communityList[holder.absoluteAdapterPosition]
            val marginInPx = ctx.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._8sdp)
            val layoutParams = tvComments.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.marginEnd = marginInPx
            tvComments.layoutParams = layoutParams

           /* if (position == 0){
                ivPosts.setImageResource(R.drawable.image123)
                tvName.text = "lightreturned"
                tvTitle.text = "How Do You Stay Consistent With Prayer?"
                tvLikes.text = "134 Likes"
                tvComments.text = "22 Comments"
                tvTime.text = "14 mins ago"
                setupSeeMoreText(tvDescription, "I’m trying to build a better prayer life, but I keep falling off after a few days. Life gets busy or I just forget. What’s helped you stay consistent? Any routines or reminders that actually work? Would love to hear what’s helped others.", ctx,item.id.toString())
            }
            else if (position == 1){
                ivPosts.setImageResource(R.drawable.image122)
                tvName.text = "marcushere"
                tvTitle.text = "Do You Think God Can Still Use You After Messing Up?"
                tvLikes.text = "162 Likes"
                tvComments.text = "13 Comments"
                tvTime.text = "17 mins ago"
                setupSeeMoreText(tvDescription, "I’ve been struggling with guilt from past mistakes, even though I’ve asked God for forgiveness. I know He’s merciful, but sometimes I wonder if I’ve disqualified myself from being used by Him. Has anyone else felt like this before?", ctx,item.id.toString())
            }*/

            if (type == 1){
                ivMore.visibility = View.GONE
                ivDel.visibility = View.VISIBLE
                ivDel.setImageResource(R.drawable.del_icon)
            }
            else{
                ivDel.visibility = View.GONE
                ivMore.setImageResource(R.drawable.more_icon)

                if (getPreference("id","") == item.user?.id.toString()){
                    ivMore.gone()
                    val marginPx = ctx.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._9sdp)
                    val layoutParam = clCat.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParam.marginEnd = marginPx
                }else{
                    val marginPx = 0
                    val layoutParam = clCat.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParam.marginEnd = marginPx
                    ivMore.visible()
                }
            }
            ivPrayer.visibility = View.GONE

            tvName.text = buildString {
                append(item.user?.username ?: "")
            }
            tvTitle.text = buildString {
                append(item.title ?: "")
            }
            if (item.category == null) {
                tvCat.gone()
            }
            tvCat.text = buildString {
                append(item.category?.name ?: "")
            }

            tvCat.setOnClickListener {
                categoryListener?.invoke(holder.absoluteAdapterPosition,item,it,tvCat.text.toString())
            }

            val likeText = if ((item.like_count ?: 0) > 1 || (item.like_count ?: 0) == 0) "Likes" else "Like"
            tvLikes.text = buildString {
                append(formatCount(item.like_count ?: 0))
                append(" ")
                append(likeText)
            }

            val commentText = if ((item.comment_count ?: 0) > 1 || (item.comment_count ?: 0)  == 0) "Comments" else "Comment"
            tvComments.text = buildString {
                append(formatCount(item.comment_count ?: 0))
                append(" ")
                append(commentText)
            }
            tvTime.text = getRelativeTime(item.created_at ?: "")

            ivPosts.loadImage(ApiConstants.IMAGE_BASE_URL+item.user?.image, placeholder = R.drawable.profile_icon)

           /* if (position == 0){
                tvTime.text = "5 mins ago"
                setupSeeMoreText(tvDescription,"I've just moved to Manchester and I'm hoping to find a new church. I'd really love somewhere that's welcoming and teaches the Bible well...", ctx, item.id.toString(),0)
                ivPosts.loadImage(ApiConstants.IMAGE_BASE_URL+item.user?.image, placeholder = R.drawable.profile_icon)
                ivLike.setImageResource(R.drawable.selected_heart)
                ivLike.imageTintList = ContextCompat.getColorStateList(ctx, R.color.star_red_color)
            }else{
                tvTime.text = "12 mins ago"
                ivLike.setImageResource(R.drawable.unselected_heart)
                ivLike.imageTintList = ContextCompat.getColorStateList(ctx,R.color.black)
                setupSeeMoreText(tvDescription,"Hey everyone I'm new to Christianity and feeling a bit overwhelmed. I really want to grow in my faith but I'm not sure where to start. If you have any advice, resources, or words of encouragement, I'd be so grateful!", ctx, item.id.toString(),1)
                ivPosts.setImageResource(R.drawable.dummy_profile_image)

            }*/

            setupSeeMoreText(tvDescription,  removeExtraSpaces( item.description ?: ""),item.id.toString())

            if (item.is_like == 1) {
                ivLike.setImageResource(R.drawable.selected_heart)
                ivLike.imageTintList = ContextCompat.getColorStateList(ctx, R.color.star_red_color)
            } else {
                ivLike.setImageResource(R.drawable.unselected_heart)
                ivLike.imageTintList = ContextCompat.getColorStateList(ctx,R.color.black)
            }

            root.setOnClickListener {
                ctx.startActivity(Intent(ctx, QuestDetailActivity::class.java).apply {
                    putExtra("from", "community")
                    putExtra("id", item.id.toString())
                })
            }

            ivMore.setOnClickListener {
                menuListener?.invoke(holder.absoluteAdapterPosition,item,it)
            }

            ivDel.setOnClickListener {
                deleteListener?.invoke(holder.absoluteAdapterPosition,item)
            }
            ivPosts.setOnClickListener{
                if (getPreference("id", "") != item.user_id.toString()) {
                    ctx.startActivity(Intent(ctx, OtherUserProfileActivity::class.java).apply {
                        putExtra("user_id",item.user_id.toString())
                    })
                }
            }

            tvLikes.setOnClickListener {
                if ((item.like_count ?: 0) > 0){
                    likeListener?.invoke(holder.absoluteAdapterPosition,item)
                }
            }
            ivLike.setOnClickListener {
                if (isInternetAvailable(ctx)){
                    if (item.is_like == 1) {
                        item.is_like = 0
                        item.like_count= (item.like_count?: 0).minus(1)
                        ivLike.setImageResource(R.drawable.unselected_heart)
                        ivLike.imageTintList = ContextCompat.getColorStateList(ctx,R.color.black)
                    }else{
                        item.is_like = 1
                        item.like_count= (item.like_count?:0).plus(1)
                        ivLike.setImageResource(R.drawable.selected_heart)
                        ivLike.imageTintList = ContextCompat.getColorStateList(ctx, R.color.star_red_color)
                    }
                    val text = if ((item.like_count ?: 0) > 1) "Likes" else "Like"
                    tvLikes.text = buildString {
                        append(formatCount(item.like_count ?: 0))
                        append(" ")
                        append(text)
                    }

                    onLikeUnlike?.invoke(holder.absoluteAdapterPosition,item)
                }
            }
        }
    }

    /*private fun setupSeeMoreText(textView: TextView, fullText: String, context: Context, id:String) {
        val words = fullText.split(" ")
        if (words.size > 30){
            val truncatedText = words.take(30).joinToString(" ") + "..."
            val seeMoreText = " See More"
            val spannableString = SpannableString(truncatedText + seeMoreText)
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
//                    textView.text = fullText

                    ctx.startActivity(Intent(ctx, QuestDetailActivity::class.java).apply {
                        putExtra("from", "community")
                        putExtra("id", id)
                    })
                }
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                }
            }
            spannableString.setSpan(clickableSpan, truncatedText.length, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(context,R.color.blue)), truncatedText.length, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            textView.text = spannableString
        }else{
            textView.text = fullText
        }

    }*/

    private fun setupSeeMoreText(textView: TextView, fullText: String, id: String) {
        textView.post {
            val paint = textView.paint
            val width = textView.width - textView.paddingLeft - textView.paddingRight

            if (width > 0) {
                // Create a static layout to measure the full text
                val staticLayout = StaticLayout(
                    fullText,
                    paint,
                    width,
                    Layout.Alignment.ALIGN_NORMAL,
                    1.0f,
                    0.0f,
                    false
                )

                if (staticLayout.lineCount > 5) {
                    val seeMoreText = " See More"
                    val ellipsisWidth = paint.measureText("...")
                    val seeMoreWidth = paint.measureText(seeMoreText)
                    val totalAppendWidth = ellipsisWidth + seeMoreWidth

                    // Find the maximum text that can fit in 5 lines including "See More"
                    var low = 0
                    var high = fullText.length
                    var bestIndex = staticLayout.getLineEnd(4) // End of 5th line

                    while (low <= high) {
                        val mid = (low + high) / 2
                        val testText = fullText.substring(0, mid).trim() + "..."

                        val testLayout = StaticLayout(
                            testText,
                            paint,
                            width,
                            Layout.Alignment.ALIGN_NORMAL,
                            1.0f,
                            0.0f,
                            false
                        )

                        if (testLayout.lineCount <= 5) {
                            // This text fits in 5 lines, try to include more
                            bestIndex = mid
                            low = mid + 1
                        } else {
                            // Too long, reduce the text
                            high = mid - 1
                        }
                    }

                    // Now find where to actually truncate to make space for "See More"
                    var truncatedText = fullText.substring(0, bestIndex).trim()

                    // Create test layout with "See More" to check if it fits
                    val testTextWithSeeMore = truncatedText + "..." + seeMoreText
                    val finalLayout = StaticLayout(
                        testTextWithSeeMore,
                        paint,
                        width,
                        Layout.Alignment.ALIGN_NORMAL,
                        1.0f,
                        0.0f,
                        false
                    )

                    // If it still doesn't fit, backtrack more
                    if (finalLayout.lineCount > 5) {
                        var newBestIndex = bestIndex
                        while (newBestIndex > 0 && finalLayout.lineCount > 5) {
                            newBestIndex--
                            truncatedText = fullText.substring(0, newBestIndex).trim()
                            val newTestText = truncatedText + "..." + seeMoreText
                            val newLayout = StaticLayout(
                                newTestText,
                                paint,
                                width,
                                Layout.Alignment.ALIGN_NORMAL,
                                1.0f,
                                0.0f,
                                false
                            )
                            if (newLayout.lineCount <= 5) {
                                break
                            }
                        }
                    }

                    // Try to end at a word boundary
                    val lastSpaceIndex = truncatedText.lastIndexOf(' ')
                    if (lastSpaceIndex > 0 && lastSpaceIndex > truncatedText.length - 20) {
                        truncatedText = truncatedText.substring(0, lastSpaceIndex).trim()
                    }

                    val displayText = truncatedText + "..." + seeMoreText
                    setupSpannableText(textView, displayText, truncatedText + "...", fullText, id)

                } else {
                    textView.text = fullText
                    textView.setOnClickListener { openDetailActivity(id) }
                }
            }
        }
    }

 /*   private fun setupSpannableText(
        textView: TextView,
        displayText: String,
        truncatedText: String,
        fullText: String,
        id: String
    ) {
        val spannableString = SpannableString(displayText)
        var isExpanded = false

        val seeMoreClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                textView.text = fullText
                isExpanded = true
                textView.post {
                    textView.setOnClickListener {
                        if (isExpanded) {
                            openDetailActivity(id)
                        }
                    }
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
                ds.color = ContextCompat.getColor(textView.context, R.color.blue)
            }
        }

        val truncatedClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                if (!isExpanded) {
                    openDetailActivity(id)
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
                ds.color = ContextCompat.getColor(textView.context, R.color.black)
            }
        }

        spannableString.setSpan(
            truncatedClickable,
            0,
            truncatedText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            seeMoreClickable,
            truncatedText.length,
            displayText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
        textView.text = spannableString
        textView.setOnClickListener(null)
    }
    private fun openDetailActivity(id: String) {

    }*/

    private fun setupSpannableText(
        textView: TextView,
        displayText: String,
        truncatedText: String,
        fullText: String,
        id: String
    ) {
        val spannableString = SpannableString(displayText)
        var isExpanded = false

        val seeMoreClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openDetailActivity(id)

                /*textView.text = fullText
                isExpanded = true
                textView.post {
                    textView.setOnClickListener {
                        if (isExpanded) {
                            openDetailActivity(id)
                        }
                    }
                }*/
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
                ds.color = ContextCompat.getColor(textView.context, R.color.blue)
            }
        }

        val truncatedClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
//                if (!isExpanded) {
                openDetailActivity(id)
//                }
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
                ds.color = ContextCompat.getColor(textView.context, R.color.black)
            }
        }

        spannableString.setSpan(
            truncatedClickable,
            0,
            truncatedText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            seeMoreClickable,
            truncatedText.length,
            displayText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
        textView.text = spannableString
        textView.setOnClickListener(null)
    }
    private fun openDetailActivity(id: String) {
        ctx.startActivity(Intent(ctx, QuestDetailActivity::class.java).apply {
            putExtra("from","community")
            putExtra("id", id)
        })
    }


    interface ClickListener{
        fun onCLick(view: View)
    }

}