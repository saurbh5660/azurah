package com.live.azurah.adapter

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.databinding.ItemPromptBinding
import com.live.azurah.model.PromptModel
import com.live.azurah.util.countWords
import com.live.azurah.util.trimToWordLimit

class PromptAdapter(
    val ctx: Context,
    private val promptList: ArrayList<PromptModel>,
    val type: Int,
    val listener: ClickListener,
    val from :Int = 0
):RecyclerView.Adapter<PromptAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPromptBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemPromptBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return promptList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){

            etAnswer.setText(promptList[holder.absoluteAdapterPosition].description)
            if (!promptList[holder.absoluteAdapterPosition].name.isNullOrEmpty()){
                tvPrompt.text = promptList[holder.absoluteAdapterPosition].name
            }else{
                tvPrompt.text = "-- Select a Prompt --"
            }

            if (type == 1){
                ivDrop.visibility = View.GONE
                tvWords.visibility = View.GONE
                tvDelete.visibility = View.GONE
            }else{
                ivDrop.visibility = View.VISIBLE
                tvWords.visibility = View.VISIBLE
                tvDelete.visibility = View.VISIBLE
                if (holder.absoluteAdapterPosition == 0 && from == 0){
                    tvDelete.visibility = View.GONE
                }else{
                    tvDelete.visibility = View.VISIBLE
                }

            }


            tvDelete.setOnClickListener {
                promptList.removeAt(holder.absoluteAdapterPosition)
                notifyItemRemoved(holder.absoluteAdapterPosition)
                listener.onCLick(0,it,holder.absoluteAdapterPosition)
            }

            tvPrompt.setOnClickListener {
                listener.onCLick(1,it,holder.absoluteAdapterPosition)
            }

            ivDrop.setOnClickListener {
                listener.onCLick(1,it,holder.absoluteAdapterPosition)
            }
            tvPromptNo.text = "Prompt: "+(holder.absoluteAdapterPosition+1)
            etAnswer.isEnabled = type != 1

            etAnswer.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s.toString().trim().isNotEmpty()){
                        val wordCount = countWords(s.toString())
                        tvWords.text = (30 - wordCount).toString()+ " words"
                        if (wordCount > 30){
                            val trimmedText = trimToWordLimit(s.toString(), 30)
                            etAnswer.setText(trimmedText)
                            etAnswer.setSelection(trimmedText.length)
                            promptList[holder.absoluteAdapterPosition].description = trimmedText
                        }else{
                            promptList[holder.absoluteAdapterPosition].description = s.toString()
                        }
                    }else{
                        tvWords.text = "30 words"
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })


        }
    }

    interface ClickListener{
        fun onCLick(type: Int,view: View,pos:Int)
    }

}