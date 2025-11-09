package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.Layout
import android.text.Spannable
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
import android.view.ViewTreeObserver
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.live.azurah.R
import com.live.azurah.activity.OtherUserProfileActivity
import com.live.azurah.databinding.ItemPostBinding
import com.live.azurah.model.PostResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.formatCount
import com.live.azurah.util.getPreference
import com.live.azurah.util.getRelativeTime
import com.live.azurah.util.gone
import com.live.azurah.util.isInternetAvailable
import com.live.azurah.util.loadImage
import com.live.azurah.util.removeExtraSpaces
import com.live.azurah.util.visible

class PostDetailAdapter(var context: Context, val recyclerView: RecyclerView,val type:Int = 0) :
    ListAdapter<PostResponse.Body.Data, PostDetailAdapter.HomeViewHolder>(DIFF_CALLBACK) {
    var onClick: ((pos: Int, userId: String, type: String, postId: String) -> Unit)? = null
    var onTagClick: ((tag:String) -> Unit)? = null
    var onSuggestionSeeAll: ((pos: Int) -> Unit)? = null
    var onLikeUnlike: ((pos: Int,model: PostResponse.Body.Data) -> Unit)? = null
    var onBookmark: ((pos: Int,model: PostResponse.Body.Data) -> Unit)? = null
    var likeListener: ((pos: Int,model: PostResponse.Body.Data) -> Unit)? = null
    var commentListener: ((pos: Int,model: PostResponse.Body.Data) -> Unit)? = null
    var shareListener: ((pos: Int,model: PostResponse.Body.Data) -> Unit)? = null
    var menuListener: ((pos: Int,model: PostResponse.Body.Data,view:View) -> Unit)? = null
    var followUnfollowListener: ((pos: Int,model: PostResponse.Body.Data.SuggestedUser,view:View) -> Unit)? = null
    var onPostImages: ((parentPos: Int,pos:Int,image: List<PostResponse.Body.Data.PostImage?>?) -> Unit)? = null
    var removeSuggestionListener: ((pos: Int,model:PostResponse.Body.Data.SuggestedUser) -> Unit)? = null

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PostResponse.Body.Data>() {
            override fun areItemsTheSame(oldItem: PostResponse.Body.Data, newItem: PostResponse.Body.Data): Boolean {
                return false
            }
            override fun areContentsTheSame(oldItem: PostResponse.Body.Data, newItem: PostResponse.Body.Data): Boolean {
                return false
            }
        }
    }

    inner class HomeViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder(ItemPostBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        with(holder.binding) {
            val item = getItem(position)

         /*   if (position == 0){
                tvName.text = "heyethan"
                tvTime.text = "5 mins ago"
                tvLikes.text = "121 Likes"
                tvComments.text = "57 Comments"
                tvDescription.text = "Grateful for this trip to London. Thank You God for making it happen! \uD83D\uDE4F"
                ivPosts.setImageResource(R.drawable.feed_picture)

            }else{
                tvName.text = buildString {
                    append(item.user?.username ?: "")
                }
                tvTime.text = "3 hours ago"
                tvLikes.text = "390 Likes"
                tvComments.text = "95 Comments"
                setupSeeMoreText(tvDescription, removeExtraSpaces(item?.description ?: ""))
                ivPosts.loadImage(ApiConstants.IMAGE_BASE_URL+item.user?.image, placeholder = R.drawable.profile_icon)
            }*/

            tvName.text = buildString {
                append(item.user?.username ?: "")
            }

            val likeText = if ((item.like_count ?: 0) > 1 || (item.like_count ?: 0) == 0) "Likes" else "Like"
            tvLikes.text = buildString {
                append(formatCount(item.like_count ?: 0))
                append(" ")
                append(likeText)
            }

            val commentText = if ((item.comment_count ?: 0) > 1 || (item.comment_count ?: 0) == 0) "Comments" else "Comment"
            tvComments.text = buildString {
                append(formatCount(item.comment_count ?: 0))
                append(" ")
                append(commentText)
            }
            tvTime.text = getRelativeTime(item.created_at ?: "")
            if (type == 0){
                if (getPreference("id","") == item.user?.id.toString()){
                    ivMore.gone()
                }else{
                    ivMore.visible()
                }

                if (!item.suggestedUsers.isNullOrEmpty()){
                    clFollow.visible()
                    view122.visible()
                }else{
                    clFollow.gone()
                    view122.gone()

                }
                val adapter = SuggestionAdapter(context,item.suggestedUsers ?: ArrayList())
                rvFollow.adapter = adapter

                adapter.followUnfollowListener = {pos, model, view ->
                    followUnfollowListener?.invoke(pos,model,view)
                }
                adapter.removeSuggestionListener= {pos,model->
                    removeSuggestionListener?.invoke(pos,model)
                    item.suggestedUsers?.removeAt(pos)
                    adapter.notifyItemRemoved(pos)
                    if (!item.suggestedUsers.isNullOrEmpty()){
                        clFollow.visible()
                        view122.visible()

                    }else{
                        clFollow.gone()
                        view122.gone()
                    }
                }

            }
            else if(type == 1){
                ivMore.visible()
                clFollow.gone()
                view122.gone()

            }else{
                if (getPreference("id","") == item.user?.id.toString()){
                    ivMore.gone()
                }else{
                    ivMore.visible()
                }
                clFollow.gone()
                view122.gone()

            }

            ivPosts.loadImage(ApiConstants.IMAGE_BASE_URL+item.user?.image, placeholder = R.drawable.profile_icon)
            setupSeeMoreText(tvDescription, removeExtraSpaces(item?.description ?: ""))

            tvSeeAll.setOnClickListener {
                onSuggestionSeeAll?.invoke(holder.absoluteAdapterPosition)
            }

            val snapHelper: SnapHelper = PagerSnapHelper()
            rvPosts.onFlingListener = null
            snapHelper.attachToRecyclerView(rvPosts)
            recyclerView.setTag(R.id.snap_helper_tag, snapHelper)

            val feedImageVideoAdapter = PostDetailImagesAdapter(context)
            rvPosts.adapter = feedImageVideoAdapter
/*//            if (position == 0 || position == 1){
            if (position == 0){
                val list = ArrayList<PostResponse.Body.Data.PostImage>()
                list.add(PostResponse.Body.Data.PostImage(type = 1, image = "https://i.ibb.co/bgRjkdNh/Image.jpg"))
                list.add(PostResponse.Body.Data.PostImage(type = 1, image = "https://i.ibb.co/Z1cNvvTJ/Whats-App-Image-2025-06-04-at-01-04-44.jpg"))
                item.post_images = list
            }*/
            feedImageVideoAdapter.submitList(item.post_images)
            feedImageVideoAdapter.onImages = {pos ->
                onPostImages?.invoke(holder.absoluteAdapterPosition,pos,item.post_images)
            }

            if (item.is_like == 1) {
                ivLike.setImageResource(R.drawable.selected_heart)
                ivLike.imageTintList = ContextCompat.getColorStateList(context, R.color.star_red_color)
            } else {
                ivLike.setImageResource(R.drawable.unselected_heart)
                ivLike.imageTintList = ContextCompat.getColorStateList(context,R.color.black)
            }

            if (item.is_bookmark == 0) {
                ivBookmark.setImageResource(R.drawable.bookmark_icon)
                ivBookmark.imageTintList = context.getColorStateList(R.color.black)
            } else {
                ivBookmark.setImageResource(R.drawable.selected_bookmark_icon)
                ivBookmark.imageTintList = context.getColorStateList(R.color.bookmark_color)
            }

            ivLike.setOnClickListener {
                if (isInternetAvailable(context)){
                    if (item.is_like == 1) {
                        item.is_like = 0
                        item.like_count= (item.like_count?: 0).minus(1)
                        ivLike.setImageResource(R.drawable.unselected_heart)
                        ivLike.imageTintList = ContextCompat.getColorStateList(context,R.color.black)
                    }else{
                        item.is_like = 1
                        item.like_count= (item.like_count?:0).plus(1)
                        ivLike.setImageResource(R.drawable.selected_heart)
                        ivLike.imageTintList = ContextCompat.getColorStateList(context, R.color.star_red_color)
                    }
                    val text = if ((item.like_count ?: 0) > 1 || (item.like_count ?: 0) == 0) "Likes" else "Like"
                    tvLikes.text = buildString {
                        append(formatCount(item.like_count ?: 0))
                        append(" ")
                        append(text)
                    }

                    onLikeUnlike?.invoke(holder.absoluteAdapterPosition,item)
                }
            }

            ivBookmark.setOnClickListener {
                if (isInternetAvailable(context)){
                    if (item.is_bookmark == 0) {
                        item.is_bookmark = 1
                        ivBookmark.setImageResource(R.drawable.selected_bookmark_icon)
                        ivBookmark.imageTintList = context.getColorStateList(R.color.bookmark_color)
                    }else{
                        item.is_bookmark = 0
                        ivBookmark.setImageResource(R.drawable.bookmark_icon)
                        ivBookmark.imageTintList = context.getColorStateList(R.color.black)
                    }
                    onBookmark?.invoke(holder.absoluteAdapterPosition,item)
                }
            }

            ivPosts.setOnClickListener {
                if (getPreference("id", "") != item.user_id.toString()) {
                    context.startActivity(Intent(context, OtherUserProfileActivity::class.java).apply {
                        putExtra("user_id",item.user_id.toString())
                    })
                }

            }

            ivShare.setOnClickListener {
                shareListener?.invoke(holder.absoluteAdapterPosition,item)
            }

            tvLikes.setOnClickListener {
                if ((item.like_count ?: 0) > 0){
                    likeListener?.invoke(holder.absoluteAdapterPosition,item)
                }
            }

            tvComments.setOnClickListener {
                commentListener?.invoke(holder.absoluteAdapterPosition,item)

            }
            ivComment.setOnClickListener {
                commentListener?.invoke(holder.absoluteAdapterPosition,item)
            }

            ivMore.setOnClickListener {
                menuListener?.invoke(holder.absoluteAdapterPosition,item,it)
            }
        }
    }

   /* private fun setupSeeMoreText(textView: TextView, fullText: String) {
        val words = fullText.split(" ")
        if (words.size > 30){
            val truncatedText = words.take(30).joinToString(" ") + "..."
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
            spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(context,R.color.blue)), truncatedText.length, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            textView.text = spannableString
            textView.movementMethod = LinkMovementMethod.getInstance()
        }else{
            textView.text = fullText
            textView.movementMethod = LinkMovementMethod.getInstance()
        }
    }*/

/*    private fun setupSeeMoreText(textView: TextView, fullText: String) {
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
                    setupSpannableText(textView, displayText, truncatedText + "...", fullText)

                } else {
                    textView.text = fullText
                }
            }
        }
    }

    private fun setupSpannableText(
        textView: TextView,
        displayText: String,
        truncatedText: String,
        fullText: String
    ) {
        val spannableString = SpannableString(displayText)
        var isExpanded = false

        val seeMoreClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                textView.text = fullText
                isExpanded = true

            }

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
                ds.color = ContextCompat.getColor(textView.context, R.color.blue)
            }
        }

        val truncatedClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {

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
    }*/

    private fun setupSeeMoreText(textView: TextView, fullText: String) {
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
                    setupSpannableText(textView, displayText, truncatedText + "...", fullText)

                } else {
                    // If text fits in 5 lines, just apply hashtag coloring and click
                    val spannableText = createHashtagSpannableText(fullText)
                    textView.text = spannableText
                    setupHashtagClick(textView, fullText)
                }
            }
        }
    }

    private fun setupSpannableText(
        textView: TextView,
        displayText: String,
        truncatedText: String,
        fullText: String
    ) {
        val spannableString = SpannableString(displayText)
        var isExpanded = false

        val seeMoreClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // When "See More" is clicked, show full text with hashtag formatting
                val fullSpannableText = createHashtagSpannableText(fullText)
                textView.text = fullSpannableText
                setupHashtagClick(textView, fullText)
                isExpanded = true
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
                ds.color = ContextCompat.getColor(textView.context, R.color.blue)
            }
        }

        val truncatedClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Do nothing for truncated text click
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

        // Apply hashtag coloring to the truncated text
        applyHashtagColoring(spannableString, truncatedText)

        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
        textView.text = spannableString
        textView.setOnClickListener(null)
    }

    private fun createHashtagSpannableText(fullText: String): SpannableString {
        val spannableString = SpannableString(fullText)

        val newlineIndex = fullText.indexOf('\n')
        if (newlineIndex != -1) {
            // Color hashtags only after newline
            val hashtagsLine = fullText.substring(newlineIndex + 1)
            val hashtagPattern = "#\\w+".toRegex()

            hashtagPattern.findAll(hashtagsLine).forEach { matchResult ->
                val startIndex = newlineIndex + 1 + matchResult.range.first
                val endIndex = newlineIndex + 1 + matchResult.range.last + 1

                spannableString.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // Make hashtag clickable
                val hashtagClickable = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        onHashtagClick(matchResult.value)
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        ds.isUnderlineText = false
                        ds.color = ContextCompat.getColor(context, R.color.blue)
                    }
                }

                spannableString.setSpan(
                    hashtagClickable,
                    startIndex,
                    endIndex,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        } else {
            // If no newline, color all hashtags (optional - remove if you don't want this)
            val hashtagPattern = "#\\w+".toRegex()
            hashtagPattern.findAll(fullText).forEach { matchResult ->
                spannableString.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)),
                    matchResult.range.first,
                    matchResult.range.last + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // Make hashtag clickable
                val hashtagClickable = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        onHashtagClick(matchResult.value)
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        ds.isUnderlineText = false
                        ds.color = ContextCompat.getColor(context, R.color.blue)
                    }
                }

                spannableString.setSpan(
                    hashtagClickable,
                    matchResult.range.first,
                    matchResult.range.last + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        return spannableString
    }

    private fun applyHashtagColoring(spannableString: SpannableString, text: String) {
        val newlineIndex = text.indexOf('\n')
        if (newlineIndex != -1) {
            val hashtagsLine = text.substring(newlineIndex + 1)
            val hashtagPattern = "#\\w+".toRegex()

            hashtagPattern.findAll(hashtagsLine).forEach { matchResult ->
                val startIndex = newlineIndex + 1 + matchResult.range.first
                val endIndex = newlineIndex + 1 + matchResult.range.last + 1

                spannableString.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    private fun setupHashtagClick(textView: TextView, fullText: String) {
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
    }

    private fun onHashtagClick(hashtag: String) {
        onTagClick?.invoke(hashtag)
    }

}
