package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.activity.EventDetailActivity
import com.live.azurah.databinding.ItemPopularEventsBinding
import com.live.azurah.model.EventListResponse
import com.live.azurah.model.ProductResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.formatDateRange
import com.live.azurah.util.loadImage

class EventPopularAdapter(
    val ctx: Context,
    val popularList: ArrayList<EventListResponse.Body.PopularEvents.Data>
):RecyclerView.Adapter<EventPopularAdapter.ViewHolder>() {
    var bookmarkListener : ((pos:Int, model: EventListResponse.Body.PopularEvents.Data)->Unit)? = null

    class ViewHolder(val binding: ItemPopularEventsBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemPopularEventsBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return popularList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = popularList[position]
        with(holder.binding){
            ivComm.loadImage(ApiConstants.IMAGE_BASE_URL+model.image)
            tvCommunityForum.text = model.title
            tvPrice.text = "£"+model.price
            tvDate.text = formatDateRange(model.startDate ?: "",model.endDate ?: "")
            if (model.isBookmark == 1){
                ivSelected.setImageResource(R.drawable.selected_bookmark_icon)
            }else{
                ivSelected.setImageResource(R.drawable.bookmark_icon)
            }

            root.setOnClickListener {
                ctx.startActivity(Intent(ctx, EventDetailActivity::class.java).apply {
                    putExtra("id",model.id.toString())
                })
            }

            ivSelected.setOnClickListener {
                bookmarkListener?.invoke(holder.absoluteAdapterPosition,model)
            }

        }
    }

}