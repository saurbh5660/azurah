package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.activity.EventDetailActivity
import com.live.azurah.databinding.ItemEventsBinding
import com.live.azurah.model.EventListResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.convertDateRange
import com.live.azurah.util.formatStartEndRange
import com.live.azurah.util.formatStartEndTimeRange
import com.live.azurah.util.loadImage

class EventAdapter(
    val ctx: Context,
    val upcomingList: ArrayList<EventListResponse.Body.UpcomingEvents.Data>
):RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemEventsBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemEventsBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return upcomingList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = upcomingList[position]

        with(holder.binding){
            ivImage.loadImage(ApiConstants.IMAGE_BASE_URL+model.image)
            tvGroupName.text = model.title
            btnViewGroup.text = buildString {
                append("£")
                append(model.price)
            }

            tvLoc.text = model.location ?: ""
//            tvDate.text = convertDateRange(model.startDate+","+model.endDate)
            tvDate.text = formatStartEndRange(model.startDate+","+model.startTime+" 44 "+model.endDate+","+model.endTime)
            tvClock.text = formatStartEndTimeRange(model.startDate+","+model.startTime+" 44 "+model.endDate+","+model.endTime)
            Log.d("dvdsggdfg",model.startDate+","+model.startTime+"-"+model.endDate+","+model.endTime)

            root.setOnClickListener {
                ctx.startActivity(Intent(ctx, EventDetailActivity::class.java).apply {
                    putExtra("id",model.id.toString())
                })
            }
        }
    }

}