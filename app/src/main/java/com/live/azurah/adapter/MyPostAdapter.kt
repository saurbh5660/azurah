package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.live.azurah.activity.MyPostsActivity
import com.live.azurah.databinding.ItemMyPostBinding
import com.live.azurah.model.PostResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.gone
import com.live.azurah.util.loadImage
import com.live.azurah.util.visible

class MyPostAdapter(val ctx: Context,val list: ArrayList<PostResponse.Body.Data>):RecyclerView.Adapter<MyPostAdapter.ViewHolder>() {
    var onClickListener: ((pos: Int,model: PostResponse.Body.Data) -> Unit)? = null

    class ViewHolder(val binding: ItemMyPostBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMyPostBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            val model = list[holder.absoluteAdapterPosition]
            if (model.post_images?.firstOrNull()?.type == 1) {
                idPlay.gone()
                rvImage.loadImage(ApiConstants.IMAGE_BASE_URL+model.post_images?.firstOrNull()?.image.toString())
            }else{
                idPlay.visible()
                val url = ApiConstants.IMAGE_BASE_URL + model.post_images?.firstOrNull()?.image
                /*val requestOptions = RequestOptions()
                    .frame(1000000)
                    .centerCrop() // or .fitCenter()

                Glide.with(ctx)
                    .asBitmap()
                    .load(url)
                    .apply(requestOptions)
                    .into(rvImage)*/
                Glide.with(ctx)
                    .asBitmap()
                    .load(url)
                    .frame(1000000)
                    .override(Target.SIZE_ORIGINAL)
                    .into(rvImage)

            }
            root.setOnClickListener {
                onClickListener?.invoke(holder.absoluteAdapterPosition,model)
            }
        }
    }

}