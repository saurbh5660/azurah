package com.live.azurah.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.databinding.ItemAdviceBinding
import com.live.azurah.databinding.ItemInterestBinding
import com.live.azurah.databinding.ItemPostBinding
import com.live.azurah.databinding.ItemPostImageVideoBinding
import com.live.azurah.databinding.ItemPromptBinding
import com.live.azurah.databinding.ItemSongBinding
import com.live.azurah.databinding.ItemSuggestionsBinding
import com.live.azurah.model.BibleQuestViewModel
import com.live.azurah.model.InterestModel
import com.live.azurah.model.ProductResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.loadImage

class AdviceAdapter(val ctx: Context,val list : ArrayList<BibleQuestViewModel.Body.BibleQuestAdvice>):RecyclerView.Adapter<AdviceAdapter.ViewHolder>() {
    var heartListener : ((pos:Int, model: BibleQuestViewModel.Body.BibleQuestAdvice)->Unit)? = null
    var productClickListener : ((pos:Int, model:BibleQuestViewModel.Body.BibleQuestAdvice)->Unit)? = null

    class ViewHolder(val binding: ItemAdviceBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemAdviceBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[holder.absoluteAdapterPosition]
        with(holder.binding){

            ivProduct.loadImage(ApiConstants.IMAGE_BASE_URL+model.product?.productImages?.firstOrNull()?.image)
            tvProductName.text = model.product?.name ?: ""
            tvOverview.text = model.product?.description ?: ""
            tvPrice.text = buildString {
                append("£")
                append(model.product?.price ?: "")
            }

            val isFav =  model.isWishlist ?: 0
            if (isFav == 1) {
                ivHeart.setImageResource(R.drawable.selected_heart)
                ivHeart.imageTintList = ContextCompat.getColorStateList(ctx, R.color.star_red_color)
            }else{
                ivHeart.setImageResource(R.drawable.unselected_heart)
                ivHeart.imageTintList = ContextCompat.getColorStateList(ctx,R.color.black)
            }

            ivHeart.setOnClickListener {
                if (model.isWishlist == 1){
                    model.isWishlist = 0
                }else{
                    model.isWishlist = 1
                }

                notifyItemChanged(holder.absoluteAdapterPosition)
                heartListener?.invoke(holder.absoluteAdapterPosition,model)
            }

            root.setOnClickListener {
                productClickListener?.invoke(holder.absoluteAdapterPosition,model)
            }
        }
    }

}