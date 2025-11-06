package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.live.azurah.R
import com.live.azurah.activity.ChallangeDetailActivity
import com.live.azurah.databinding.ItemChallangeBinding
import com.live.azurah.model.BibleQuestListResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.loadImage

class ChallangeItemAdapter(
    val ctx: Context,
    val bibleQuestList: ArrayList<BibleQuestListResponse.Body.Data>,
    val type:Int = 0
) : RecyclerView.Adapter<ChallangeItemAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemChallangeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChallangeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return bibleQuestList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            val model = bibleQuestList[holder.absoluteAdapterPosition]
            tvTitle.text = model.title ?: ""
//            tvDesc.text = model.shortDescription ?: ""
            setTextWithLastLineSpacer(tvDesc,model.shortDescription ?: "",R.drawable.transparent_space,60)
            if ((model.isChallengeCompleted ?: 0) > 0){
                ivTick.setImageResource(R.drawable.tick_icon)
            }else{
                ivTick.setImageResource(R.drawable.unselected_tick_grey)
            }
            ivChallange.loadImage(ApiConstants.IMAGE_BASE_URL+model.bibleQuestImages?.firstOrNull()?.image)
           /* val snapHelper: SnapHelper = PagerSnapHelper()
            ivChallange.onFlingListener = null
            snapHelper.attachToRecyclerView(ivChallange)
            val imagesList =
                model.bibleQuestImages?.map { it.image ?: "" } as ArrayList
            val imagesAdapter =
                ChallengeImageAdapter(ctx, imagesList)
            ivChallange.adapter = imagesAdapter*/



            root.setOnClickListener {
                if (type != 1){
                    ctx.startActivity(Intent(ctx, ChallangeDetailActivity::class.java).apply {
                        putExtra("from", "1")
                        putExtra("id", model.id.toString())
                        putExtra("isPremium", model.isPremium.toString())
                    })
                }
            }
        }
    }

    fun setTextWithLastLineSpacer(
        textView: TextView,
        text: String,
        @DrawableRes spacerDrawableRes: Int,
        spacerWidthDp: Int
    ) {
        val context = textView.context
        val sb = SpannableStringBuilder(text)

        val transparentDrawable = ContextCompat.getDrawable(context, spacerDrawableRes)
        val widthPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            spacerWidthDp.toFloat(),
            context.resources.displayMetrics
        ).toInt()

        transparentDrawable?.setBounds(0, 0, widthPx, 1)
        transparentDrawable?.let {
            val imageSpan = ImageSpan(it, ImageSpan.ALIGN_BOTTOM)
            sb.append(" ") // Needed for the span to attach
            sb.setSpan(imageSpan, sb.length - 1, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        textView.text = sb
        textView.maxLines = 9

    }


}