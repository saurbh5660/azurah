package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.activity.EventDetailActivity
import com.live.azurah.activity.QuestActivity
import com.live.azurah.databinding.ItemEventsBinding
import com.live.azurah.databinding.ItemInterestBinding
import com.live.azurah.databinding.ItemPostBinding
import com.live.azurah.databinding.ItemPostImageVideoBinding
import com.live.azurah.databinding.ItemPromptBinding
import com.live.azurah.databinding.ItemRecentSearchBinding
import com.live.azurah.databinding.ItemSongBinding
import com.live.azurah.databinding.ItemSuggestionsBinding
import com.live.azurah.databinding.ItemTrendingBinding
import com.live.azurah.model.InterestModel

class TrendingSearchAdapter(val ctx: Context, val list: ArrayList<InterestModel>):RecyclerView.Adapter<TrendingSearchAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemTrendingBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemTrendingBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            tvName.text = list[position].name
        }
    }

}