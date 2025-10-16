package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.activity.ChallangeDetailActivity
import com.live.azurah.databinding.ItemChallangeBinding
import com.live.azurah.databinding.ItemEventsBinding
import com.live.azurah.databinding.ItemInterestBinding
import com.live.azurah.databinding.ItemPostBinding
import com.live.azurah.databinding.ItemPostImageVideoBinding
import com.live.azurah.databinding.ItemPromptBinding
import com.live.azurah.databinding.ItemSongBinding
import com.live.azurah.databinding.ItemSuggestionsBinding
import com.live.azurah.model.InterestModel

class ChallangeAdapter(val ctx: Context):RecyclerView.Adapter<ChallangeAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemChallangeBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemChallangeBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return 10
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            root.setOnClickListener {
                if (position == 0 || position == 2 || position == 4){
                    ctx.startActivity(Intent(ctx,ChallangeDetailActivity::class.java).apply {
                        putExtra("from","0")
                    })

                }else{
                    ctx.startActivity(Intent(ctx,ChallangeDetailActivity::class.java).apply {
                        putExtra("from","1")
                    })
                }
            }
        }
    }

}