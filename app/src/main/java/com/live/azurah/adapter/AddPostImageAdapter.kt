package com.live.azurah.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.live.azurah.R
import com.live.azurah.databinding.ItemAddPostImageVideosBinding
import com.live.azurah.databinding.ItemAdviceBinding
import com.live.azurah.databinding.ItemImageVideoBinding
import com.live.azurah.databinding.ItemInterestBinding
import com.live.azurah.databinding.ItemPostBinding
import com.live.azurah.databinding.ItemPostImageVideoBinding
import com.live.azurah.databinding.ItemPromptBinding
import com.live.azurah.databinding.ItemSongBinding
import com.live.azurah.databinding.ItemSuggestionsBinding
import com.live.azurah.model.ImageVideoModel
import com.live.azurah.model.InterestModel
import com.live.azurah.util.gone
import com.live.azurah.util.visible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class AddPostImageAdapter(val ctx: Context, val list: ArrayList<ImageVideoModel>, val listener: ClickListener, val type: Int):RecyclerView.Adapter<AddPostImageAdapter.ViewHolder>() {

    var zoomImageListener: ((pos:Int)->Unit)? = null
    var addImageVideo: ((pos:Int)->Unit)? = null
    class ViewHolder(val binding: ItemAddPostImageVideosBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemAddPostImageVideosBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){

            if (list[holder.absoluteAdapterPosition].image.isNullOrEmpty()){
                clAddPhoto.visibility = View.VISIBLE
                clPhotos.visibility = View.GONE
                tvDelete.visibility = View.GONE
                ivPlay.visibility = View.GONE
                Log.d("sdflkdhng","dfgdsgdgdgd")

            }
            else{
                clAddPhoto.visibility = View.GONE
                clPhotos.visibility = View.VISIBLE
                if (type == 1){
                    tvDelete.visibility = View.GONE
                }else{
                    tvDelete.visibility = View.VISIBLE
                }
                if (list[holder.absoluteAdapterPosition].type == "2"){
                    ivPlay.visibility = View.VISIBLE
                    ivPosts.scaleType = ImageView.ScaleType.FIT_CENTER
//                    ivPosts.setBackgroundColor(ContextCompat.getColor(ctx,R.color.black))
                }else{
                    ivPlay.visibility = View.GONE
                    ivPosts.scaleType = ImageView.ScaleType.CENTER_CROP
//                    ivPosts.setBackgroundColor(ContextCompat.getColor(ctx,R.color.black))
                }
            }
            Glide.with(ctx).load(list[holder.absoluteAdapterPosition].image).into(ivPosts)
            if (list.size >1){
                clCount.visible()
            }else{
                clCount.gone()
            }
            tvCount.text = (position+1).toString()+"/"+list.size
            tvDelete.setOnClickListener {
                listener.onClick(holder.absoluteAdapterPosition)
            }
            root.setOnClickListener {
                zoomImageListener?.invoke(holder.absoluteAdapterPosition)
            }

            clAddPhoto.setOnClickListener {
                addImageVideo?.invoke(holder.absoluteAdapterPosition)
            }
        }
    }

    private fun loadImageWithDominantColor(imageView: ImageView, backgroundView: View, url: String) {
        Glide.with(ctx)
            .asBitmap()
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .override(300, 300) // Reduce image size to avoid high memory usage
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    imageView.setImageBitmap(resource)

                    // Extract dominant color in the background thread
                    CoroutineScope(Dispatchers.Default).launch {
                        val color = extractDominantColor(resource)
                        val adjustedColor = adjustColorBrightness(color, 0.8f)

                        withContext(Dispatchers.Main) {
                            backgroundView.setBackgroundColor(adjustedColor)
                        }
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun loadVideoThumbnailWithDominantColor(imageView: ImageView, backgroundView: View, videoUrl: String) {
        Glide.with(ctx)
            .asBitmap()
            .frame(1000000)
            .load(videoUrl)// Extract frame at 1 second
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache video thumbnails
            .override(300, 300) // Reduce memory usage
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    imageView.setImageBitmap(resource)

                    // Extract dominant color in the background thread
                    CoroutineScope(Dispatchers.Default).launch {
                        val color = extractDominantColor(resource)
                        val adjustedColor = adjustColorBrightness(color, 0.8f)

                        withContext(Dispatchers.Main) {
                            backgroundView.setBackgroundColor(adjustedColor)
                        }
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
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

    interface ClickListener{
        fun onClick(position: Int)
    }

}