package com.live.azurah.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.live.azurah.R
import com.live.azurah.databinding.ItemAdviceBinding
import com.live.azurah.databinding.ItemChallengeImagesBinding
import com.live.azurah.databinding.ItemImageVideoBinding
import com.live.azurah.databinding.ItemInterestBinding
import com.live.azurah.databinding.ItemPostBinding
import com.live.azurah.databinding.ItemPostImageVideoBinding
import com.live.azurah.databinding.ItemPromptBinding
import com.live.azurah.databinding.ItemSongBinding
import com.live.azurah.databinding.ItemSuggestionsBinding
import com.live.azurah.model.InterestModel
import com.live.azurah.retrofit.ApiConstants

class ChallengeImageAdapter(val ctx: Context, val list: ArrayList<String>):RecyclerView.Adapter<ChallengeImageAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemChallengeImagesBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemChallengeImagesBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            Glide.with(ctx).load(ApiConstants.IMAGE_BASE_URL+list[holder.absoluteAdapterPosition]).placeholder(R.drawable.placeholder).into(ivChallange)
        }
    }

    interface ClickListener{
        fun onClick(position: Int)
    }

}