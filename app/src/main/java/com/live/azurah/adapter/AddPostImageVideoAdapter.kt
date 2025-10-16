package com.live.azurah.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.live.azurah.R
import com.live.azurah.databinding.ItemAdviceBinding
import com.live.azurah.databinding.ItemImageVideoBinding
import com.live.azurah.databinding.ItemInterestBinding
import com.live.azurah.databinding.ItemPostBinding
import com.live.azurah.databinding.ItemPostImageVideoBinding
import com.live.azurah.databinding.ItemPromptBinding
import com.live.azurah.databinding.ItemSongBinding
import com.live.azurah.databinding.ItemSuggestionsBinding
import com.live.azurah.model.InterestModel

class AddPostImageVideoAdapter(val ctx: Context,val list: ArrayList<String>,val listener: ClickListener,val type: Int):RecyclerView.Adapter<AddPostImageVideoAdapter.ViewHolder>() {

    var zoomImageListener: ((pos:Int)->Unit)? = null
    class ViewHolder(val binding: ItemImageVideoBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemImageVideoBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            Glide.with(ctx).load(list[holder.absoluteAdapterPosition]).into(ivImages)
            if (type == 1){
                ivCross.visibility = View.GONE
            }else{
                ivCross.visibility = View.VISIBLE
            }
            ivCross.setOnClickListener {
                listener.onClick(holder.absoluteAdapterPosition)
            }

            root.setOnClickListener {
                zoomImageListener?.invoke(holder.absoluteAdapterPosition)
            }
        }
    }

    interface ClickListener{
        fun onClick(position: Int)
    }

}