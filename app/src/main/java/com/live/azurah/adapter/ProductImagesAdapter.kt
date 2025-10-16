package com.live.azurah.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.live.azurah.R
import com.live.azurah.databinding.ItemProductImagesBinding
import com.live.azurah.model.InterestModel
import com.live.azurah.model.ProductDetailResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.gone
import com.live.azurah.util.loadImage

class ProductImagesAdapter(val ctx: Context, val list: ArrayList<ProductDetailResponse.Body.ProductImage>):RecyclerView.Adapter<ProductImagesAdapter.ViewHolder>() {
    var onClick: ((pos: Int) -> Unit)? = null

    class ViewHolder(val binding: ItemProductImagesBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemProductImagesBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            if (list[position].isSelected){
                cvProduct.strokeColor = ContextCompat.getColor(ctx,R.color.black)
            }else{
                cvProduct.strokeColor = ContextCompat.getColor(ctx,R.color.divider_grey)
            }
            rvImage.loadImage(ApiConstants.IMAGE_BASE_URL+list[position].image)

            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL) // cache resized + original
                .downsample(com.bumptech.glide.load.resource.bitmap.DownsampleStrategy.CENTER_INSIDE)
                .dontAnimate()

            Glide.with(ctx)
                .load(ApiConstants.IMAGE_BASE_URL+list[position].image)
                .apply(requestOptions)
                .thumbnail(0.1f) // load 10% first
                .listener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false

                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable?>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                })
                .into(holder.binding.rvImage)

            root.setOnClickListener {
                list.forEach {
                    it.isSelected = false
                }
                list[position].isSelected = true
                notifyDataSetChanged()
                onClick?.invoke(position)
            }
        }
    }

}