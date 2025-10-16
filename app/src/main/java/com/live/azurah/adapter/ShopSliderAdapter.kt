package com.live.azurah.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.live.azurah.databinding.BannerImageSliderBinding
import com.live.azurah.model.ShopBannerModel
import com.live.azurah.model.ShopBannerResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.gone
import com.live.azurah.util.loadImage

class ShopSliderAdapter(val context: Context, val bannerList: ArrayList<ShopBannerModel>) :
    RecyclerView.Adapter<ShopSliderAdapter.SliderAdapterVH>() {


    override fun onBindViewHolder(holder: SliderAdapterVH, position: Int) {
        with(holder.binding) {

            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL) // cache resized + original
                .downsample(com.bumptech.glide.load.resource.bitmap.DownsampleStrategy.CENTER_INSIDE)
                .dontAnimate()

            Glide.with(context)
                .load(ApiConstants.IMAGE_BASE_URL + bannerList[position].image)
                .apply(requestOptions)
                .thumbnail(0.1f) // load 10% first
                .listener(object : RequestListener<Drawable> {

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.binding.shimmerLayout.stopShimmer()
                        holder.binding.shimmerLayout.hideShimmer()
                        holder.binding.shimmerLayout.gone()
                        return false

                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable?>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.binding.shimmerLayout.stopShimmer()
                        holder.binding.shimmerLayout.hideShimmer()
                        holder.binding.shimmerLayout.gone()
                        return false
                    }
                })
                .into(holder.binding.ivImage)

            holder.binding.shimmerLayout.startShimmer()


        }
    }

    class SliderAdapterVH(val binding: BannerImageSliderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderAdapterVH {
        return SliderAdapterVH(
            BannerImageSliderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return bannerList.size
    }

    fun ImageView.loadImage(
        url: String?,
        placeholder: Int? = null,
        error: Int? = null,
        cacheStrategy: DiskCacheStrategy = DiskCacheStrategy.AUTOMATIC
    ) {
        val options = RequestOptions().apply {
            placeholder?.let { placeholder(it) }
            error?.let { error(it) }
            diskCacheStrategy(cacheStrategy)   // disk caching
            skipMemoryCache(false)             // memory caching
            centerCrop()                       // scale to fill ImageView
        }

        Glide.with(this.context)
            .load(url)
            .thumbnail(0.1f) // load a smaller version first
            .apply(options)
            .into(this)
    }
}