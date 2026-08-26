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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.activity.QuestDetailActivity
import com.live.azurah.databinding.ItemPrayerRequestBinding
import com.live.azurah.model.CommunityForumResponse
import com.live.azurah.util.AvatarUtils
import com.live.azurah.util.formatCount
import com.live.azurah.util.getRelativeTime
import com.live.azurah.util.isInternetAvailable

class MyPostRequestAdapter(
    val ctx: Context,
    val prayerList: ArrayList<CommunityForumResponse.Body.Data>,
    val from: Int = 0
) : RecyclerView.Adapter<MyPostRequestAdapter.ViewHolder>() {

    var deleteListener: ((pos: Int, model: CommunityForumResponse.Body.Data) -> Unit)? = null
    var onLikeUnlike: ((pos: Int, model: CommunityForumResponse.Body.Data) -> Unit)? = null
    var likeListener: ((pos: Int, model: CommunityForumResponse.Body.Data) -> Unit)? = null
    var praiseListener: ((pos: Int, model: CommunityForumResponse.Body.Data) -> Unit)? = null
    var praiseClickListener: ((pos: Int, model: CommunityForumResponse.Body.Data) -> Unit)? = null
    var categoryListener: ((pos: Int, model: CommunityForumResponse.Body.Data, view: View, text: String) -> Unit)? = null

    class ViewHolder(val binding: ItemPrayerRequestBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemPrayerRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = prayerList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            val item = prayerList[holder.absoluteAdapterPosition]

            // Show delete trash-can icon, hide more icon
            ivMore.visibility = View.GONE
            ivDel.visibility = View.VISIBLE

            tvName.text = item.user?.username ?: ""
            tvTitle.text = item.title ?: ""

            val relativeTime = getRelativeTime(item.created_at ?: "")
            val categoryName = item.prayer_category?.name ?: item.testimony_category?.name ?: item.category?.name
            tvTime.text = if (!categoryName.isNullOrBlank()) {
                "$relativeTime · $categoryName"
            } else {
                relativeTime
            }

            tvLikes.text = formatCount(item.like_count ?: 0)

            val commentText = if ((item.comment_count ?: 0) == 1) "Comment" else "Comments"
            tvComments.text = buildString {
                append(formatCount(item.comment_count ?: 0))
                append(" ")
                append(commentText)
            }

            val prayerText = if ((item.praise_count ?: 0) == 1) "Prayer" else "Prayers"
            tvPrayers.text = buildString {
                append(formatCount(item.praise_count ?: 0))
                append(" ")
                append(prayerText)
            }

            // In Prayer Requests (from == 1), show clPrayer; in Testimonies (from != 1), hide clPrayer
            if (from == 1) {
                clPrayer.visibility = View.VISIBLE
            } else {
                clPrayer.visibility = View.GONE
            }

            AvatarUtils.setupAvatar(ivPosts, tvInitials, item.user?.image, item.user?.username)

            setupSeeMoreText(tvDescription, item.description ?: "", item.id.toString())

            if (item.is_like == 1) {
                ivLike.setImageResource(R.drawable.selected_heart)
                ivLike.imageTintList = ContextCompat.getColorStateList(ctx, R.color.star_red_color)
            } else {
                ivLike.setImageResource(R.drawable.unselected_heart)
                ivLike.imageTintList = ContextCompat.getColorStateList(ctx, R.color.black)
            }

            if (item.is_praise == 1) {
                ivPrayer.imageTintList = ContextCompat.getColorStateList(ctx, R.color.golden_yellow)
            } else {
                ivPrayer.imageTintList = ContextCompat.getColorStateList(ctx, R.color.black)
            }

            ivDel.setOnClickListener {
                deleteListener?.invoke(holder.absoluteAdapterPosition, item)
            }

            val togglePraise: (View) -> Unit = {
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
                    val text = if ((item.praise_count ?: 0) == 1) "Prayer" else "Prayers"
                    tvPrayers.text = buildString {
                        append(formatCount(item.praise_count ?: 0))
                        append(" ")
                        append(text)
                    }
                    praiseListener?.invoke(holder.absoluteAdapterPosition, item)
                }
            }
            ivPrayer.setOnClickListener(togglePraise)
            clPrayer.setOnClickListener(togglePraise)

            tvLikes.setOnClickListener {
                if ((item.like_count ?: 0) > 0) {
                    likeListener?.invoke(holder.absoluteAdapterPosition, item)
                }
            }
            tvPrayers.setOnClickListener {
                if ((item.praise_count ?: 0) > 0) {
                    praiseClickListener?.invoke(holder.absoluteAdapterPosition, item)
                }
            }

            val toggleLike: (View) -> Unit = {
                if (isInternetAvailable(ctx)) {
                    if (item.is_like == 1) {
                        item.is_like = 0
                        item.like_count = (item.like_count ?: 0).minus(1)
                        ivLike.setImageResource(R.drawable.unselected_heart)
                        ivLike.imageTintList = ContextCompat.getColorStateList(ctx, R.color.black)
                    } else {
                        item.is_like = 1
                        item.like_count = (item.like_count ?: 0).plus(1)
                        ivLike.setImageResource(R.drawable.selected_heart)
                        ivLike.imageTintList = ContextCompat.getColorStateList(ctx, R.color.star_red_color)
                    }
                    tvLikes.text = formatCount(item.like_count ?: 0)
                    onLikeUnlike?.invoke(holder.absoluteAdapterPosition, item)
                }
            }
            ivLike.setOnClickListener(toggleLike)
            clLike.setOnClickListener(toggleLike)

            val openDetails: (View) -> Unit = {
                openDetailActivity(item.id.toString())
            }
            ivPosts.setOnClickListener(openDetails)
            clComment.setOnClickListener(openDetails)
            root.setOnClickListener(openDetails)
        }
    }

    private fun setupSeeMoreText(textView: TextView, fullText: String, id: String) {
        if (fullText.isEmpty()) {
            textView.text = ""
            return
        }

        textView.post {
            val maxLines = 4
            val width = textView.width - textView.paddingLeft - textView.paddingRight

            if (width <= 0) {
                textView.text = fullText
                textView.setOnClickListener { openDetailActivity(id) }
                return@post
            }

            val layout = StaticLayout.Builder.obtain(fullText, 0, fullText.length, textView.paint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(textView.lineSpacingExtra, textView.lineSpacingMultiplier)
                .setIncludePad(textView.includeFontPadding)
                .build()

            if (layout.lineCount > maxLines) {
                val lineEndIndex = layout.getLineEnd(maxLines - 1)
                val seeMoreText = " ...See more"

                val availableWidthForText = width - textView.paint.measureText(seeMoreText)

                var truncatedText = fullText.substring(0, lineEndIndex)
                while (textView.paint.measureText(truncatedText) > availableWidthForText && truncatedText.isNotEmpty()) {
                    truncatedText = truncatedText.dropLast(1)
                }

                val lastSpaceIndex = truncatedText.lastIndexOf(' ')
                if (lastSpaceIndex > 0 && lastSpaceIndex > truncatedText.length - 20) {
                    truncatedText = truncatedText.substring(0, lastSpaceIndex).trim()
                }

                val displayText = "$truncatedText...$seeMoreText"
                setupSpannableText(textView, displayText, "$truncatedText...", id)
            } else {
                textView.text = fullText
                textView.setOnClickListener { openDetailActivity(id) }
            }
        }
    }

    private fun setupSpannableText(
        textView: TextView,
        displayText: String,
        truncatedText: String,
        id: String
    ) {
        val spannableString = SpannableString(displayText)

        val seeMoreClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openDetailActivity(id)
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
                ds.color = ContextCompat.getColor(textView.context, R.color.blue)
            }
        }

        val truncatedClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openDetailActivity(id)
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