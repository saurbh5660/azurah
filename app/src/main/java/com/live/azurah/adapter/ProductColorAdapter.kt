package com.live.azurah.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.databinding.ItemColorBinding
import com.live.azurah.databinding.ItemProductImagesBinding
import com.live.azurah.databinding.ItemSizeBinding
import com.live.azurah.model.InterestModel

class ProductColorAdapter(val ctx: Context, val list: ArrayList<InterestModel>):RecyclerView.Adapter<ProductColorAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemColorBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemColorBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            tvSizeName.backgroundTintList = ColorStateList.valueOf(list[position].icon!!)
            if (list[position].isSelected){
                cvProductColor.strokeColor = ContextCompat.getColor(ctx,R.color.black)
                cvProductColor.setCardBackgroundColor(ContextCompat.getColor(ctx,android.R.color.transparent))
            }else{
                cvProductColor.strokeColor = ContextCompat.getColor(ctx,android.R.color.transparent)
                cvProductColor.setCardBackgroundColor(ColorStateList.valueOf(list[position].icon!!))
            }
            root.setOnClickListener {
                list.forEach {
                    it.isSelected = false
                }
                list[position].isSelected = true
                notifyDataSetChanged()
            }
        }
    }

}