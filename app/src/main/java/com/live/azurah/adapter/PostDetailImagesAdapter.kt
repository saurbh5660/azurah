package com.live.azurah.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.facebook.shimmer.ShimmerFrameLayout
import com.live.azurah.R
import com.live.azurah.databinding.ItemPostImageVideoBinding
import com.live.azurah.model.PostResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.retrofit.ApiConstants.isMute
import com.live.azurah.util.gone
import com.live.azurah.util.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PostDetailImagesAdapter(
    var context: Context
) : ListAdapter<PostResponse.Body.Data.PostImage, PostDetailImagesAdapter.FeedImageVideoViewHolder>(
    DIFF_CALLBACK
) {
    var onImages: ((pos: Int) -> Unit)? = null

    companion object {
        val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<PostResponse.Body.Data.PostImage>() {
                override fun areItemsTheSame(
                    oldItem: PostResponse.Body.Data.PostImage,
                    newItem: PostResponse.Body.Data.PostImage
                ): Boolean {
                    return false
                }

                override fun areContentsTheSame(
                    oldItem: PostResponse.Body.Data.PostImage,
                    newItem: PostResponse.Body.Data.PostImage
                ): Boolean {
                    return false
                }
            }
    }

    inner class FeedImageVideoViewHolder(val binding: ItemPostImageVideoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedImageVideoViewHolder {
        return FeedImageVideoViewHolder(
            ItemPostImageVideoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FeedImageVideoViewHolder, position: Int) {
        val item = getItem(position)

        with(holder.binding) {
            if (currentList.size > 1){
                clCount.visible()
            }else{
                clCount.gone()
            }
            tvCount.text = buildString {
                append(position + 1)
                append("/")
                append(currentList.size)
            }
            if (item.type == 1) {
                Log.d(
                    "fhbjdfg",
                    "imageeeeeeeeeeeeeeee-------${ApiConstants.IMAGE_BASE_URL + item.image}"
                )
                ivPostImage.visible()
                ivSound.gone()
                ivPlay.gone()
                playerView.gone()
//                view.gone()

                (ivPostImage.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio =
                    "640:840"
                ivPostImage.scaleType = ImageView.ScaleType.CENTER_CROP

                if (item.image.toString().contains("i.ibb")){
                    loadImageWithDominantColor(ivPostImage,blurBackground, item.image.toString(),ivProgressBar,shimmerLayout)

                }else{
                    loadImageWithDominantColor(ivPostImage,blurBackground,ApiConstants.IMAGE_BASE_URL+item.image,ivProgressBar,shimmerLayout)

                }

                ivPostImage.setOnClickListener {
                    Log.d("sgsdgdssd","gSGSH")
                    onImages?.invoke(holder.absoluteAdapterPosition)
                }

            } else {
                Log.d("sdfsdgsdg",ApiConstants.IMAGE_BASE_URL + item.image)
                ivPostImage.visible()
                playerView.visible()
                playerView.reset()
                playerView.id = View.generateViewId()
                (ivPostImage.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio =
                    "640:840"
                playerView.setVideoUri(
                    Uri.parse(ApiConstants.IMAGE_BASE_URL + item.image),
                    "0",
                    ivPostImage,
                    ApiConstants.IMAGE_BASE_URL + item.image_thumb,
                    ivSound,
                    ivPlay
                )
                ivPostImage.scaleType = ImageView.ScaleType.CENTER_CROP
//                ivPostImage.loadImage(ApiConstants.IMAGE_BASE_URL+item.image_thumb)
                loadVideoThumbnailWithDominantColor(ivPostImage,blurBackground,ApiConstants.IMAGE_BASE_URL+item.image,shimmerLayout)
                ivSound.setImageResource(if (isMute) R.drawable.volume_off else R.drawable.volume)
                playerView.setMuted(false)
                playerView.setBackgroundColor(Color.TRANSPARENT)

                if (playerView.isPlaying()){
                    ivPlay.gone()
                }else{
                    ivPlay.visible()
                }
                ivSound.setOnClickListener {
                    isMute = !isMute
                    playerView.setMuted(isMute)
                    ivSound.setImageResource(if (isMute) R.drawable.volume_off else R.drawable.volume)
                }

                ivFullScreen.setOnClickListener {
                    playerView.playVideo()
                }
            }
        }
    }

    private fun loadImageWithDominantColor(
        imageView: ImageView,
        backgroundView: View,
        url: String,
        ivProgressBar: ProgressBar,
        shimmerLayout: ShimmerFrameLayout
    ) {
        ivProgressBar.gone()
        shimmerLayout.startShimmer()
        shimmerLayout.visible()
        imageView.gone()
        Glide.with(context)
            .asBitmap()
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .format(DecodeFormat.PREFER_RGB_565)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(fullResBitmap: Bitmap, transition: Transition<in Bitmap>?) {
                    ivProgressBar.gone()
                    shimmerLayout.stopShimmer()
                    shimmerLayout.gone()
                    imageView.visible()
                    imageView.setImageBitmap(fullResBitmap)

                  /*  CoroutineScope(Dispatchers.IO).launch {
                        val color = extractDominantColor(fullResBitmap)
                        val adjustedColor = adjustColorBrightness(color, 0.8f)

                        withContext(Dispatchers.Main) {
                            backgroundView.setBackgroundColor(adjustedColor)
                        }
                    }*/
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    ivProgressBar.gone()
                    shimmerLayout.stopShimmer()
                    shimmerLayout.gone()
                    imageView.visible()
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    ivProgressBar.gone()
                    shimmerLayout.stopShimmer()
                    shimmerLayout.gone()
                    imageView.visible()
                }
            })
    }

    private fun loadVideoThumbnailWithDominantColor(imageView: ImageView, backgroundView: View, videoUrl: String,
    shimmerLayout: ShimmerFrameLayout) {
        shimmerLayout.startShimmer()
        shimmerLayout.visible()
        imageView.gone()
        Glide.with(context)
            .asBitmap()
            .frame(0)
            .load(videoUrl)
            .override(200,200)
            .format(DecodeFormat.PREFER_RGB_565)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    shimmerLayout.stopShimmer()
                    shimmerLayout.gone()
                    imageView.visible()
                    imageView.setImageBitmap(resource)

                  /*  CoroutineScope(Dispatchers.Default).launch {
                        val color = extractDominantColor(resource)
                        val adjustedColor = adjustColorBrightness(color, 0.8f)

                        withContext(Dispatchers.Main) {
                            backgroundView.setBackgroundColor(adjustedColor)
                        }
                    }*/
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    shimmerLayout.stopShimmer()
                    shimmerLayout.gone()
                    imageView.visible()
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    shimmerLayout.stopShimmer()
                    shimmerLayout.gone()
                    imageView.visible()
                }
            })
    }
    private suspend fun extractDominantColor(bitmap: Bitmap): Int {
        return withContext(Dispatchers.Default) {
            val palette = Palette.from(bitmap).generate()
            palette.getDominantColor(0xFFCCCCCC.toInt())
        }
    }

    private fun adjustColorBrightness(color: Int, factor: Float): Int {
        val red = ((color shr 16 and 0xFF) * factor).toInt().coerceIn(0, 255)
        val green = ((color shr 8 and 0xFF) * factor).toInt().coerceIn(0, 255)
        val blue = ((color and 0xFF) * factor).toInt().coerceIn(0, 255)
        return (color and 0xFF000000.toInt()) or (red shl 16) or (green shl 8) or blue
    }

   /* override fun onViewRecycled(holder: FeedImageVideoViewHolder) {
        Glide.with(holder.itemView.context).clear(holder.binding.ivPosts)
        super.onViewRecycled(holder)
    }*/
}
