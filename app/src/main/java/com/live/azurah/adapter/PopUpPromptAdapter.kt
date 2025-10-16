package com.live.azurah.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.databinding.ItemAdviceBinding
import com.live.azurah.databinding.ItemInterestBinding
import com.live.azurah.databinding.ItemPopupBinding
import com.live.azurah.databinding.ItemPostBinding
import com.live.azurah.databinding.ItemPostImageVideoBinding
import com.live.azurah.databinding.ItemPromptBinding
import com.live.azurah.databinding.ItemSongBinding
import com.live.azurah.databinding.ItemSuggestionsBinding
import com.live.azurah.model.InterestModel
import com.live.azurah.model.QuestionResponse

class PopUpPromptAdapter(val ctx: Context,val list: ArrayList<QuestionResponse.Body>,val listener: ClickListener,val pos: Int = 0):RecyclerView.Adapter<PopUpPromptAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPopupBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemPopupBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            tvPrompt.text = list[position].title ?: ""
            root.setOnClickListener {
                listener.onPopClick(position,pos)
            }
        }
    }

    interface ClickListener{
        fun onPopClick(position: Int,questionPos:Int)
    }

}