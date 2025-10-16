package com.live.azurah.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.databinding.ItemSearchProductBinding
import com.live.azurah.model.InterestModel
import com.live.azurah.model.ProductResponse
import com.live.azurah.model.WishlistResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.loadImage

class FavouriteAdapter(val ctx: Context,val list: ArrayList<WishlistResponse.Body.Data>):RecyclerView.Adapter<FavouriteAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSearchProductBinding): RecyclerView.ViewHolder(binding.root)
    var heartListener : ((pos:Int, model: WishlistResponse.Body.Data)->Unit)? = null
    var productClickListener : ((pos:Int, model:WishlistResponse.Body.Data)->Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSearchProductBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[holder.absoluteAdapterPosition]

        with(holder.binding){
            ivHeart.visibility = View.VISIBLE
            ivProduct.loadImage(
                (ApiConstants.IMAGE_BASE_URL + model.product?.productImages?.firstOrNull()?.image)
            )
            tvProductName.text = model.product?.name ?: ""
            tvPrice.text = buildString {
                append("£")
                append((model.product?.price ?: ""))
            }
            root.setOnClickListener {
                productClickListener?.invoke(holder.absoluteAdapterPosition,model)
            }

            if(list[position].status == "1"){
                ivHeart.setImageResource(R.drawable.selected_heart)
                ivHeart.imageTintList = ContextCompat.getColorStateList(ctx, R.color.star_red_color)
            }else{
                ivHeart.setImageResource(R.drawable.unselected_heart)
                ivHeart.imageTintList = ContextCompat.getColorStateList(ctx,R.color.black)
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