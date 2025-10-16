package com.live.azurah.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.databinding.ItemSuggestionBinding
import com.live.azurah.model.InterestModel

class SuggestionUsernameAdapter(val ctx: Context,val list: ArrayList<InterestModel>):RecyclerView.Adapter<SuggestionUsernameAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSuggestionBinding): RecyclerView.ViewHolder(binding.root)
    var nameListener : ((String) -> Unit)? = {name-> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSuggestionBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            clName.setOnClickListener {
                nameListener?.invoke(list[position].name)
            }
            tvName.text = list[position].name
            tvName.setTextColor(ContextCompat.getColor(ctx,R.color.black))
            clName.backgroundTintList = ContextCompat.getColorStateList(ctx,R.color.grey)


        }
    }

}