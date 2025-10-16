package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
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
        return ViewHolder(ItemCategoryBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return catList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
         //   clCat.setCardBackgroundColor(ctx.getColorStateList(catList[position].backgroundColor))
            clCat.setCardBackgroundColor(ctx.getColorStateList(R.color.folder_color))
            tvCat.setTextColor(ctx.getColorStateList(R.color.price__text_color))
            tvCat.text = catList[position].name

            if (catList[position].isSelected){
                clCat.setStrokeColor(ctx.getColorStateList(R.color.selected_outline))
                tvCat.typeface = ctx.resources.getFont(R.font.poppins_semibold)
            }else{
                clCat.setStrokeColor(ctx.getColorStateList(android.R.color.transparent))
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