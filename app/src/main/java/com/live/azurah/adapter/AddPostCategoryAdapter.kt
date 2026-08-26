package com.live.azurah.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.databinding.ItemAddPostCategoryBinding
import com.live.azurah.model.CategoryModel

class AddPostCategoryAdapter(
    val ctx: Context,
    val catList: ArrayList<CategoryModel>
) : RecyclerView.Adapter<AddPostCategoryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAddPostCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAddPostCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = catList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = catList[position]
        with(holder.binding) {
            tvCat.text = item.name

            if (item.isSelected) {
                clCat.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#EDF5FE")))
                clCat.strokeColor = Color.parseColor("#2F80ED")
                clCat.strokeWidth = ctx.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._1sdp) * 3 / 2
                tvCat.setTextColor(Color.parseColor("#2F80ED"))
                tvCat.typeface = ResourcesCompat.getFont(ctx, R.font.poppins_semibold)
            } else {
                clCat.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#F8FAFC")))
                clCat.strokeColor = Color.parseColor("#E2E8F0")
                clCat.strokeWidth = ctx.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._1sdp)
                tvCat.setTextColor(Color.parseColor("#7B8B9E"))
                tvCat.typeface = ResourcesCompat.getFont(ctx, R.font.poppins_medium)
            }

            clCat.setOnClickListener {
                val currentPos = holder.adapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    if (catList[currentPos].isSelected) {
                        catList[currentPos].isSelected = false
                    } else {
                        catList.forEach { it.isSelected = false }
                        catList[currentPos].isSelected = true
                    }
                    notifyDataSetChanged()
                }
            }
        }
    }
}
