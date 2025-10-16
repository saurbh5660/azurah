package com.live.azurah.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.live.azurah.databinding.ItemAllShopCategoryBinding
import com.live.azurah.model.ShopCategoryResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.loadImage

class AllShopCategoryAdapter(
    val ctx: Context,
    val categoryList: ArrayList<ShopCategoryResponse.Body.Data>
):RecyclerView.Adapter<AllShopCategoryAdapter.ViewHolder>() {
    var clickListener : ((model:ShopCategoryResponse.Body.Data, pos:Int)-> Unit)? = null

    class ViewHolder(val binding: ItemAllShopCategoryBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemAllShopCategoryBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            tvCatName.text = categoryList[holder.absoluteAdapterPosition].name

            Glide.with(ctx)
                .asBitmap()  // Ensures bitmap quality
                .load(ApiConstants.IMAGE_BASE_URL+categoryList[holder.absoluteAdapterPosition].image)
                .override(500, 500)
//                .transform(CenterCrop(), RoundedCorners(8))
                .format(DecodeFormat.PREFER_ARGB_8888)
                .into(ivImage)

//            ivImage.loadImage(ApiConstants.IMAGE_BASE_URL+categoryList[holder.absoluteAdapterPosition].image)
            root.setOnClickListener {
                clickListener?.invoke(categoryList[holder.absoluteAdapterPosition],holder.absoluteAdapterPosition)
            }
        }
    }

}