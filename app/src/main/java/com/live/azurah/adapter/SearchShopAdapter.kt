package com.live.azurah.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.databinding.ItemSearchProductBinding
import com.live.azurah.model.ProductResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.loadImage

class SearchShopAdapter(val ctx: Context, val list: ArrayList<ProductResponse.Body.Data>):RecyclerView.Adapter<SearchShopAdapter.ViewHolder>() {
    var heartListener : ((pos:Int, model:ProductResponse.Body.Data)->Unit)? = null
    var productClickListener : ((pos:Int, model:ProductResponse.Body.Data)->Unit)? = null
    class ViewHolder(val binding: ItemSearchProductBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSearchProductBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        with(holder.binding){

            root.setOnClickListener {
                productClickListener?.invoke(holder.absoluteAdapterPosition,model)

            }
            val imageUrl = when {
                !model.cover_image.isNullOrEmpty() -> model.cover_image
                !model.product_images.isNullOrEmpty() -> model.product_images?.firstOrNull()?.image
                else -> null
            }
            ivProduct.loadImage(imageUrl)
            tvProductName.text = model.name
            tvPrice.text = "£"+model.price
            ivHeart.visibility = View.VISIBLE

            if(model.is_wishlist == 1){
                ivHeart.setImageResource(R.drawable.selected_heart)
                ivHeart.imageTintList = ContextCompat.getColorStateList(ctx, R.color.star_red_color)

            }else{
                ivHeart.setImageResource(R.drawable.unselected_heart)
                ivHeart.imageTintList = ContextCompat.getColorStateList(ctx, R.color.black)
            }

            ivHeart.setOnClickListener {
                heartListener?.invoke(holder.absoluteAdapterPosition,model)
            }
        }
    }


    interface ClickListener{
        fun onClick()
    }

}