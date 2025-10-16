package com.live.azurah.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.databinding.ItemCategoryBinding
import com.live.azurah.databinding.ItemShopCategoryBinding
import com.live.azurah.model.CategoryModel
import com.live.azurah.model.ProductResponse
import com.live.azurah.model.ShopCategoryResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.loadImage

class ShopCategoryAdapter(val ctx: Context, val catList: ArrayList<ShopCategoryResponse.Body.Data>):RecyclerView.Adapter<ShopCategoryAdapter.ViewHolder>() {
    var categoryClickListener : ((pos:Int, model: ShopCategoryResponse.Body.Data)->Unit)? = null

    class ViewHolder(val binding: ItemShopCategoryBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemShopCategoryBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return catList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            tvCat.text = catList[position].name
            ivCatImage.loadImage(ApiConstants.IMAGE_BASE_URL+catList[position].image)

            root.setOnClickListener {
                categoryClickListener?.invoke(holder.absoluteAdapterPosition,catList[holder.absoluteAdapterPosition])
            }
        }
    }
}