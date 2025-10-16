package com.live.azurah.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.databinding.ItemProductImagesBinding
import com.live.azurah.databinding.ItemSizeBinding
import com.live.azurah.model.InterestModel

class ProductSizeAdapter(val ctx: Context, val list: ArrayList<InterestModel>):RecyclerView.Adapter<ProductSizeAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSizeBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSizeBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            tvSizeName.text = list[position].name
            if (list[position].isSelected){
                cvProduct.strokeColor = ContextCompat.getColor(ctx,R.color.black)
            }else{
                cvProduct.strokeColor = ContextCompat.getColor(ctx,R.color.divider_grey)
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