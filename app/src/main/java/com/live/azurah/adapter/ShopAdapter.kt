package com.live.azurah.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.databinding.ItemProductBinding
import com.live.azurah.model.ProductResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.gone
import com.live.azurah.util.loadImage
import com.live.azurah.util.visible

class ShopAdapter(
    val ctx: Context,
    val productList: ArrayList<ProductResponse.Body.Data>
) : RecyclerView.Adapter<ShopAdapter.ViewHolder>() {
    var heartListener: ((pos: Int, model: ProductResponse.Body.Data) -> Unit)? = null
    var productClickListener: ((pos: Int, model: ProductResponse.Body.Data) -> Unit)? = null

    class ViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = productList[position]
        with(holder.binding) {
            root.setOnClickListener {
                productClickListener?.invoke(holder.absoluteAdapterPosition, model)
            }
            ivProduct.loadImage(ApiConstants.IMAGE_BASE_URL + model.product_images?.firstOrNull()?.image)
            tvProductName.text = model.name ?: ""
            tvPrice.text = buildString {
                append("£")
                append((model.price ?: ""))
            }

            val isPopular = model.is_popular ?: 0
            val isBestSeller = model.is_best_seller ?: 0

            if (isPopular == 1 && isBestSeller == 1) {
                tvType.text = "Popular"
                tvType.visible()
            } else if (isBestSeller == 0 && isPopular == 1) {
                tvType.text = "Popular"
                tvType.visible()
            } else if (isBestSeller == 1 && isPopular == 0) {
                tvType.text = "Best Seller"
                tvType.visible()
            } else if (isBestSeller == 0 && isPopular == 0) {
                tvType.text = ""
                tvType.gone()
            }else{
                tvType.gone()
            }

            if (model.is_wishlist == 1) {
                ivHeart.setImageResource(R.drawable.selected_heart)
                ivHeart.imageTintList =
                    ContextCompat.getColorStateList(ctx, R.color.star_red_color)
            } else {
                ivHeart.setImageResource(R.drawable.unselected_heart)
                ivHeart.imageTintList = ContextCompat.getColorStateList(ctx, R.color.black)
            }

            ivHeart.setOnClickListener {
                if (model.is_wishlist == 1) {
                    model.is_wishlist = 0
                } else {
                    model.is_wishlist = 1
                }
                notifyItemChanged(holder.absoluteAdapterPosition)
                heartListener?.invoke(holder.absoluteAdapterPosition, model)
            }


        }
    }

}