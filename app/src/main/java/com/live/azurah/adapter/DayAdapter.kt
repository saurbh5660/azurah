package com.live.azurah.adapter

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.databinding.ItemDayBinding
import com.live.azurah.databinding.ItemEventsBinding
import com.live.azurah.databinding.ItemInterestBinding
import com.live.azurah.databinding.ItemPostBinding
import com.live.azurah.databinding.ItemPostImageVideoBinding
import com.live.azurah.databinding.ItemPromptBinding
import com.live.azurah.databinding.ItemSongBinding
import com.live.azurah.databinding.ItemSuggestionsBinding
import com.live.azurah.model.BibleQuestViewModel
import com.live.azurah.model.InterestModel
import com.live.azurah.util.getCurrentDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class DayAdapter(
    val ctx: Context,
    val list: ArrayList<BibleQuestViewModel.Body.BibleQuestChallenge>
) : RecyclerView.Adapter<DayAdapter.ViewHolder>() {

    var onClick: ((pos: Int) -> Unit)? = null
    var onToastClick: ((pos: Int,type:Int) -> Unit)? = null

    class ViewHolder(val binding: ItemDayBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemDayBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[holder.absoluteAdapterPosition]
        with(holder.binding) {
            tvDay.text = list[position].dayNo.toString()
            if (model.isCompleted == 1) {
                clDay.background =
                    AppCompatResources.getDrawable(ctx, R.drawable.round_corner_background)
                clDay.backgroundTintList = ctx.getColorStateList(R.color.green)
                tvDay.setTextColor(ctx.getColorStateList(R.color.white))
                txtDay.setTextColor(ctx.getColorStateList(R.color.white))
            }else if (model.isSelected == true) {
                clDay.background =
                    AppCompatResources.getDrawable(ctx, R.drawable.round_corner_background)
                clDay.backgroundTintList = ctx.getColorStateList(R.color.blue)
                tvDay.setTextColor(ctx.getColorStateList(R.color.white))
                txtDay.setTextColor(ctx.getColorStateList(R.color.white))

            }else if (model.isPassed == true) {
                clDay.background =
                    AppCompatResources.getDrawable(ctx, R.drawable.round_corner_background)
                clDay.backgroundTintList = ctx.getColorStateList(R.color.star_red_color)
                tvDay.setTextColor(ctx.getColorStateList(R.color.white))
                txtDay.setTextColor(ctx.getColorStateList(R.color.white))

            }  else {
                clDay.background =
                    AppCompatResources.getDrawable(ctx, R.drawable.full_round_stroke_background)
                clDay.backgroundTintList = ctx.getColorStateList(R.color.day_unselected_color)
                tvDay.setTextColor(ctx.getColorStateList(R.color.black))
                txtDay.setTextColor(ctx.getColorStateList(R.color.black))
            }

            clDay.setOnClickListener {

                val currentDayPos = list.indexOfFirst { it.currentDay == 1 }
                if (currentDayPos != -1){
                    if (position <= currentDayPos){
                            list.forEach {
                                it.isSelected = false
                            }

                            list[holder.absoluteAdapterPosition].isSelected = true
                            onClick?.invoke(holder.absoluteAdapterPosition)
                        notifyDataSetChanged()
                    }else{
                        if (position != 0){
                            if (list[position-1].isCompleted == 1){
                                onToastClick?.invoke(holder.absoluteAdapterPosition,0)
                            }else{
                                onToastClick?.invoke(holder.absoluteAdapterPosition,1)

                            }
                        }else{
                            if (list[position].isCompleted == 1){
                                onToastClick?.invoke(holder.absoluteAdapterPosition,0)
                            }else{
                                onToastClick?.invoke(holder.absoluteAdapterPosition,1)
                            }
                        }
                    }
                }



/*
                if (firstNonEmptyDate != null) {
                    val daysBetween = ChronoUnit.DAYS.between(
                        firstNonEmptyDate,
                        LocalDate.parse(getCurrentDate(), dateFormatter)
                    )
                    if ((list[holder.absoluteAdapterPosition].dayNo ?: 0) <= daysBetween.toInt()+1) {
                        list.forEach {
                            it.isSelected = false
                        }

                        list[holder.absoluteAdapterPosition].isSelected = true
                        onClick?.invoke(holder.absoluteAdapterPosition)
                        Log.d("dsfdsjkbgdsg","1111111111111")

                        notifyDataSetChanged()
                    }else{
                        Log.d("dsfdsjkbgdsg","22222222222222")
                        val currentDay = list.any{ it.currentDay == 1 && it.isCompleted == 1}
                        if (currentDay){
                            onToastClick?.invoke(holder.absoluteAdapterPosition,0)
                        }else{
                            onToastClick?.invoke(holder.absoluteAdapterPosition,1)
                        }
                    }
                }
                else{
                    if (holder.absoluteAdapterPosition != 0) {
                        if (list[holder.absoluteAdapterPosition - 1].isCompleted == 0) {
                            val currentDay = list.any{ it.currentDay == 1 && it.isCompleted == 1}
                            if (currentDay){
                                onToastClick?.invoke(holder.absoluteAdapterPosition,0)
                            }else{
                                onToastClick?.invoke(holder.absoluteAdapterPosition,1)
                            }
//                            onToastClick?.invoke(holder.absoluteAdapterPosition,0)
                            Log.d("dsfdsjkbgdsg","333333333333333")

                        }else{
                            list.forEach {
                                it.isSelected = false
                            }
                            list[holder.absoluteAdapterPosition].isSelected = true
                            onClick?.invoke(holder.absoluteAdapterPosition)
                            notifyDataSetChanged()
                            Log.d("dsfdsjkbgdsg","444444444444444444")
                        }
                    }else{
                        list.forEach {
                            it.isSelected = false
                        }
                        list[holder.absoluteAdapterPosition].isSelected = true
                        onClick?.invoke(holder.absoluteAdapterPosition)
                        notifyDataSetChanged()
                        Log.d("dsfdsjkbgdsg","5555555555555555555")

                    }
                }*/


            }
        }
    }

}