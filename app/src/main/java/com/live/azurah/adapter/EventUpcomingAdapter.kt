package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.activity.EventDetailActivity
import com.live.azurah.databinding.ItemCurrentEventBinding
import com.live.azurah.model.EventListResponse
import com.live.azurah.model.ProductResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.convertDateRange
import com.live.azurah.util.formatStartEndRange
import com.live.azurah.util.formatStartEndTimeRange
import com.live.azurah.util.loadImage
import okhttp3.internal.notifyAll

class EventUpcomingAdapter(
    val ctx: Context, val recommendedList: ArrayList<EventListResponse.Body.RecommendedEvents.Data>):RecyclerView.Adapter<EventUpcomingAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCurrentEventBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemCurrentEventBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }
    override fun getItemCount(): Int {
        return recommendedList.size
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = recommendedList[position]
        with(holder.binding){
            ivImage.loadImage(ApiConstants.IMAGE_BASE_URL+model.image)
            tvGroupName.text = model.title
//            tvDate.text = convertDateRange(model.startDate+","+model.endDate)
            tvDate.text = formatStartEndRange(model.startDate+","+model.startTime+" 44 "+model.endDate+","+model.endTime)
            tvClock.text = formatStartEndTimeRange(model.startDate+","+model.startTime+" 44 "+model.endDate+","+model.endTime)
            Log.d("dvdsggdfg",model.startDate+","+model.startTime+"-"+model.endDate+","+model.endTime)

            tvPrice.text = "£"+model.price
            tvLoc.text = model.location ?: ""
            root.setOnClickListener {
                ctx.startActivity(Intent(ctx, EventDetailActivity::class.java).apply {
                    putExtra("id",model.id.toString())
                })
            }
        }
    }

}