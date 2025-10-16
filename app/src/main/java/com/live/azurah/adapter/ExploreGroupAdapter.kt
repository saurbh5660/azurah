package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.MetricAffectingSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.activity.GuidelineParticipationActivity

import com.live.azurah.databinding.ItemExploreGroupBinding
import com.live.azurah.model.ExploreGroupResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.convertDate
import com.live.azurah.util.loadImage

class ExploreGroupAdapter(
    val ctx: Context,
    val exploreGroupList: ArrayList<ExploreGroupResponse.Body.Data>
):RecyclerView.Adapter<ExploreGroupAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemExploreGroupBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemExploreGroupBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return exploreGroupList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            val model = exploreGroupList[holder.absoluteAdapterPosition]
//            setCustomFont("Created On: Thu 26 May 2024","Created On:",tvLoc)
            setCustomFont("Created On: ${convertDate(model.created_at ?: "","dd/MM/yyyy")}","Created On:",tvLoc)
            setCustomFont1("Total Members: ${model.group_member_count}/250","Total Members:",tvDate,model.group_member_count ?: 0," ${model.group_member_count}/250")
            ivImage.loadImage(ApiConstants.IMAGE_BASE_URL+model.image)
            tvGroupName.text = model.name ?: ""
            root.setOnClickListener {
                ctx.startActivity(Intent(ctx, GuidelineParticipationActivity::class.java).apply {
                    putExtra("id",model.id.toString())
                })
            }

        }
    }

    private fun setCustomFont(fullText:String,targetText:String,view: TextView){

        val spannableString = SpannableString(fullText)
        val termsClickableSpan = object : ClickableSpan() {

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(ctx, R.color.black)
                val typeface = ResourcesCompat.getFont(ctx, R.font.poppins_semibold)
                if (typeface != null) {
                    ds.typeface = typeface
                }
                ds.isUnderlineText = false
            }

            override fun onClick(widget: View) {

            }
        }
        spannableString.setSpan(termsClickableSpan, 0, targetText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        view.text = spannableString
    }

    private fun setCustomFont1(fullText: String, targetText: String, view: TextView,count : Int,targetCount:String) {
        val spannableString = SpannableString(fullText)
        val context = view.context


        val blackSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                ds.color = ContextCompat.getColor(context, R.color.black)
                val typeface = ResourcesCompat.getFont(ctx, R.font.poppins_semibold)
                if (typeface != null) {
                    ds.typeface = typeface
                }
                ds.isUnderlineText = false
            }

            override fun onClick(widget: View) {

            }

        }
        spannableString.setSpan(blackSpan, 0, targetText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        val redSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                if (count == 250){
                    ds.color = ContextCompat.getColor(context, R.color.star_red_color)
                }else {
                    ds.color = ContextCompat.getColor(context, R.color.black)
                }
            }

            override fun onClick(widget: View) {
            }
        }

        val startIndex = fullText.indexOf(targetCount)
        if (startIndex != -1) {
            spannableString.setSpan(
                redSpan,
                startIndex,
                startIndex + targetCount.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        view.text = spannableString
    }




}