package com.live.azurah.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.databinding.ItemInterestBinding
import com.live.azurah.databinding.ItemPostBinding
import com.live.azurah.databinding.ItemPostImageVideoBinding
import com.live.azurah.databinding.ItemPromptBinding
import com.live.azurah.model.InterestModel

class ImagesVideoAdapter(val ctx: Context):RecyclerView.Adapter<ImagesVideoAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPostImageVideoBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemPostImageVideoBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            var isSelected = false
            ivSound.setOnClickListener {
                if (isSelected){
                    isSelected = false
                    ivSound.setImageResource(R.drawable.volume_off)
                }else{
                    isSelected = true
                    ivSound.setImageResource(R.drawable.volume)
                }
            }
            tvCount.text = (position+1).toString()+"/3"
        }
    }

}