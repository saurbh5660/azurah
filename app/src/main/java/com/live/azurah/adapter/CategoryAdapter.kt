package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.activity.BookmarkActivity
import com.live.azurah.activity.BookmarkEventActivity
import com.live.azurah.databinding.ItemCategoryBinding
import com.live.azurah.model.CategoryModel
import com.live.azurah.model.PostResponse

class CategoryAdapter(val ctx: Context, val catList: ArrayList<CategoryModel>,val type: Int = 0):RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
    var categoryListener: ((pos: Int,model: CategoryModel) -> Unit)? = null

    class ViewHolder(val binding: ItemCategoryBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return catList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            tvCat.text = catList[position].name

            if (catList[position].isSelected){
                clCat.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#EDF5FE")))
                clCat.strokeColor = Color.parseColor("#2F80ED")
                clCat.strokeWidth = ctx.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._1sdp) * 3 / 2
                tvCat.setTextColor(Color.parseColor("#2F80ED"))
                tvCat.typeface = ctx.resources.getFont(R.font.poppins_semibold)
            }else{
                clCat.setCardBackgroundColor(ColorStateList.valueOf(Color.WHITE))
                clCat.strokeColor = Color.parseColor("#E2E8F0")
                clCat.strokeWidth = ctx.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._1sdp)
                tvCat.setTextColor(Color.parseColor("#64748B"))
                tvCat.typeface = ctx.resources.getFont(R.font.poppins)
            }

            root.setOnClickListener {
                if (type == 1){
                    val catId = if (holder.absoluteAdapterPosition == 0) "0" else catList[position].id.toString()
                    ctx.startActivity(Intent(ctx,BookmarkEventActivity::class.java).apply {
                        putExtra("title",catList[position].name)
                        putExtra("id",catId)
                    })
                }else if (type == 2){
                   if (catList[position].isSelected){
                       catList[position].isSelected = false
                   }else{
                       catList.forEach {
                           it.isSelected = false
                       }
                       catList[position].isSelected = true
                   }
                    notifyDataSetChanged()
                }
                else{
                    catList.forEach {
                        it.isSelected = false
                    }
                    catList[position].isSelected = true
                    notifyDataSetChanged()
                    categoryListener?.invoke(holder.absoluteAdapterPosition,catList[position])
                }

            }
        }


    }

}