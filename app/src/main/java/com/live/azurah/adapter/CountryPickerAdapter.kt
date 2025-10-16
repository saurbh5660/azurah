package com.live.azurah.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.databinding.ItemAdviceBinding
import com.live.azurah.databinding.ItemCountryBinding
import com.live.azurah.databinding.ItemInterestBinding
import com.live.azurah.databinding.ItemPostBinding
import com.live.azurah.databinding.ItemPostImageVideoBinding
import com.live.azurah.databinding.ItemPromptBinding
import com.live.azurah.databinding.ItemSongBinding
import com.live.azurah.databinding.ItemSuggestionsBinding
import com.live.azurah.model.CountryModel
import com.live.azurah.model.InterestModel

class CountryPickerAdapter(val ctx: Context,val list:ArrayList<CountryModel>):RecyclerView.Adapter<CountryPickerAdapter.ViewHolder>() {

    var listener : ((model:CountryModel,pos:Int)->Unit)? = null
    class ViewHolder(val binding: ItemCountryBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemCountryBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            tvSelectedCountry.text = list[position].name
            ivCountryFlag.setImageResource(list[position].flag!!)
            ivCountryFlag.setLayerType(View.LAYER_TYPE_HARDWARE,null)
            ivCountryFlag.elevation = 16f
            root.setOnClickListener {
                listener?.invoke(list[position],position)
            }
        }
    }

}