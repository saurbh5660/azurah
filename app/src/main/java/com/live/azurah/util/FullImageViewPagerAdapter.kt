package com.live.azurah.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.live.azurah.databinding.ItemZoomImageBinding
import com.live.azurah.model.FullImageModel
import com.live.azurah.retrofit.ApiConstants

class FullImageViewPagerAdapter(val context: Context, val imageList: MutableList<FullImageModel>): RecyclerView.Adapter<FullImageViewPagerAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ItemZoomImageBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemZoomImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
    override fun getItemCount(): Int {
        return imageList.size
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            if (imageList[position].type == 0){
                Glide.with(context).load(imageList[position].image.toString()).into(myZoomageView)
            }else{
                shimmerLayout.startShimmer()
                shimmerLayout.visible()
                myZoomageView.gone()
                Glide.with(context)
                    .asBitmap()
                    .load(ApiConstants.IMAGE_BASE_URL+imageList[position].image)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .format(DecodeFormat.PREFER_RGB_565)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(fullResBitmap: Bitmap, transition: Transition<in Bitmap>?) {
                            shimmerLayout.stopShimmer()
                            shimmerLayout.gone()
                            myZoomageView.visible()
                            myZoomageView.setImageBitmap(fullResBitmap)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            shimmerLayout.stopShimmer()
                            shimmerLayout.gone()
                            myZoomageView.visible()
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            super.onLoadFailed(errorDrawable)
                            shimmerLayout.stopShimmer()
                            shimmerLayout.gone()
                            myZoomageView.visible()
                        }
                    })
            }
        }
    }

}