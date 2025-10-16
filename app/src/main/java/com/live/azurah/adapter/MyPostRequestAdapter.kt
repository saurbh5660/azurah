package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
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
import com.live.azurah.util.setupSeeMoreText
import com.live.azurah.util.visible

class MyPostRequestAdapter(
    val ctx: Context,
    val prayerList: ArrayList<CommunityForumResponse.Body.Data>,
    val from : Int = 0
):RecyclerView.Adapter<MyPostRequestAdapter.ViewHolder>() {

    var deleteListener : ((pos:Int,model: CommunityForumResponse.Body.Data)->Unit)? = null
    var onLikeUnlike: ((pos: Int,model: CommunityForumResponse.Body.Data) -> Unit)? = null
    var likeListener: ((pos: Int,model: CommunityForumResponse.Body.Data) -> Unit)? = null
    var praiseListener: ((pos: Int, model: CommunityForumResponse.Body.Data) -> Unit)? = null
    var praiseClickListener: ((pos: Int, model: CommunityForumResponse.Body.Data) -> Unit)? = null

    var categoryListener: ((pos: Int, model: CommunityForumResponse.Body.Data,view:View,text:String) -> Unit)? = null

    class ViewHolder(val binding: ItemBiblePostBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemBiblePostBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return prayerList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            ivMore.visibility = View.GONE
            ivDel.visibility = View.VISIBLE
            dotPrayer.visibility = View.VISIBLE
            tvPrayers.visibility = View.VISIBLE
            ivDel.setImageResource(R.drawable.del_icon)
            ivPrayer.visibility = View.VISIBLE

            val item = prayerList[holder.absoluteAdapterPosition]
            tvName.text = buildString {
                append(item.user?.username ?: "")
            }

            tvTitle.text = buildString {
                append(item.title ?: "")
            }

            tvCat.text = buildString {
                if (item.prayer_category != null){
                    append(item.prayer_category.name ?: "")
                }else{
                    append(item.testimony_category?.name ?: "")
                }
            }

            if (item.prayer_category == null && item.testimony_category == null){
                clCat.visibility = View.INVISIBLE
            }else{
                clCat.visible()
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

            val prayerText = if ((item.praise_count ?: 0) > 1 || (item.praise_count ?: 0) == 0) "Prayers" else "Prayer"
            tvPrayers.text = buildString {
                append(formatCount(item.praise_count ?: 0))
                append(" ")
                append(prayerText)
            }

            tvTime.text = getRelativeTime(item.created_at ?: "")

            ivPosts.loadImage(ApiConstants.IMAGE_BASE_URL+item.user?.image, placeholder = R.drawable.profile_icon)
//            setupSeeMoreText(tvDescription,item.description ?: "",ctx,item.id.toString())
            setupSeeMoreText(tvDescription, item.description ?: "",item.id.toString())

            if (item.is_like == 1) {
                ivLike.setImageResource(R.drawable.selected_heart)
                ivLike.imageTintList = ContextCompat.getColorStateList(ctx, R.color.star_red_color)
            } else {
                ivLike.setImageResource(R.drawable.unselected_heart)
                ivLike.imageTintList = ContextCompat.getColorStateList(ctx,R.color.black)
            }

            if (item.is_praise == 1) {
                ivPrayer.imageTintList = ContextCompat.getColorStateList(ctx, R.color.golden_yellow)
            } else {
                ivPrayer.imageTintList = ContextCompat.getColorStateList(ctx,R.color.black)
            }


            ivPrayer.setOnClickListener {
                if (isInternetAvailable(ctx)) {
                    if (item.is_praise == 1) {
                        item.is_praise = 0
                        item.praise_count = (item.praise_count ?: 0).minus(1)
                        ivPrayer.imageTintList = ContextCompat.getColorStateList(ctx, R.color.black)

                    } else {
                        item.is_praise = 1
                        item.praise_count = (item.praise_count ?: 0).plus(1)
                        ivPrayer.imageTintList = ContextCompat.getColorStateList(ctx, R.color.golden_yellow)

                    }
                    val text = if ((item.praise_count ?: 0) > 1) "Prayers" else "Prayer"
                    tvPrayers.text = buildString {
                        append(formatCount(item.praise_count ?: 0))
                        append(" ")
                        append(text)
                    }

                    praiseListener?.invoke(holder.absoluteAdapterPosition, item)
                }
            }
            root.setOnClickListener {
                ctx.startActivity(Intent(ctx, QuestDetailActivity::class.java).apply {
                    if (from == 1){
                        putExtra("from","prayer")
                    }else{
                        putExtra("from","testimony")
                    }
                    putExtra("id",item.id.toString())
                })
            }

            ivDel.setOnClickListener {
                deleteListener?.invoke(position,item)
            }
            ivPosts.setOnClickListener{
                ctx.startActivity(Intent(ctx, QuestDetailActivity::class.java).apply {
                    if (from == 1){
                        putExtra("from","prayer")
                    }else{
                        putExtra("from","testimony")
                    }
                    putExtra("id",item.id.toString())
                })
              /*  if (getPreference("id", "") != item.user?.id.toString()) {
                    ctx.startActivity(Intent(ctx, OtherUserProfileActivity::class.java).apply {
                        putExtra("user_id",item.user?.id.toString())
                    })
                }*/

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

   /* fun setupSeeMoreText(textView: TextView, fullText: String, context: Context, id:String) {
        val words = fullText.split(" ")
        if (words.size > 30){
            val truncatedText = words.take(30).joinToString(" ") + "..."
            val seeMoreText = " See More"
            val spannableString = SpannableString(truncatedText + seeMoreText)
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
//                    textView.text = fullText
                    Log.d("fgdffgdf","dsgfdsgsdgdf")
                    ctx.startActivity(Intent(ctx, QuestDetailActivity::class.java).apply {
                        if (from == 1) {
                            putExtra("from", "prayer")

                        } else {
                            putExtra("from", "testimony")
                        }
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
//            textView.movementMethod = LinkMovementMethod.getInstance()
        }else{
            textView.text = fullText
//            textView.movementMethod = LinkMovementMethod.getInstance()
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
              /*  textView.text = fullText
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
            putExtra("from", if (from == 1) "prayer" else "testimony")
            putExtra("id", id)
        })
    }

}