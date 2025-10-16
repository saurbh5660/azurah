package com.live.azurah.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.live.azurah.R
import com.live.azurah.databinding.ItemInterestBinding
import com.live.azurah.model.InterestModel
import com.live.azurah.model.InterestResponse
import com.live.azurah.retrofit.ApiConstants

class InterestAdapter(val ctx: Context,val list: ArrayList<InterestResponse.Body>,val listener: ClickListener,val type: Int = 0):RecyclerView.Adapter<InterestAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemInterestBinding): RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemInterestBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            if (list[position].isSelected){
                clInterest.backgroundTintList = ctx.getColorStateList(R.color.cursor_color)
            }else{
                clInterest.backgroundTintList = ctx.getColorStateList(R.color.divider_grey)
            }

            Glide.with(ctx).load(ApiConstants.IMAGE_BASE_URL+list[position].image).into(ivIcon)
            tvName.text = list[position].name

            root.setOnClickListener {
                if (type != 1){
                    if (list[position].isSelected){
                        list[position].isSelected = !list[position].isSelected
                        listener.onClick()
                    }else{
                        if (list.filter { it.isSelected }.size < 6){
                            list[position].isSelected = !list[position].isSelected
                            notifyDataSetChanged()
                            listener.onClick()
                        }
                    }
                    notifyDataSetChanged()
                }
            }
        }
    }

    interface ClickListener{
        fun onClick()
    }

}