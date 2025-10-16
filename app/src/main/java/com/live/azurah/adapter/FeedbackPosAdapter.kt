package com.live.azurah.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.databinding.ItemAdviceBinding
import com.live.azurah.databinding.ItemFeedbackPosBinding
import com.live.azurah.databinding.ItemInterestBinding
import com.live.azurah.databinding.ItemPostBinding
import com.live.azurah.databinding.ItemPostImageVideoBinding
import com.live.azurah.databinding.ItemPromptBinding
import com.live.azurah.databinding.ItemSongBinding
import com.live.azurah.databinding.ItemSuggestionsBinding
import com.live.azurah.model.InterestModel

class FeedbackPosAdapter(val ctx: Context,val list: ArrayList<String>):RecyclerView.Adapter<FeedbackPosAdapter.ViewHolder>() {

    var clickListener: ((name:String)-> Unit)?= null
    class ViewHolder(val binding: ItemFeedbackPosBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemFeedbackPosBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            tvName.text = list[position]
            root.setOnClickListener {
                clickListener?.invoke( list[position])
            }
        }
    }

}