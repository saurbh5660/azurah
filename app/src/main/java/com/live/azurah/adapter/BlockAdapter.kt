package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.activity.BlockedProfileActivity
import com.live.azurah.activity.OtherUserProfileActivity
import com.live.azurah.databinding.ItemBlockUserBinding
import com.live.azurah.model.BlockResposne
import com.live.azurah.model.PostLikesResposne
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.gone
import com.live.azurah.util.isInternetAvailable
import com.live.azurah.util.loadImage
import com.live.azurah.util.showCustomSnackbar

class BlockAdapter(val ctx: Context,val blockList: ArrayList<BlockResposne.Body.Data>,var from : String = ""):RecyclerView.Adapter<BlockAdapter.ViewHolder>() {

    var onBlockListener: ((pos: Int,model: BlockResposne.Body.Data) -> Unit)? = null
    var followUnfollowListener: ((pos: Int, model: BlockResposne.Body.Data, view: View) -> Unit)? =null

    class ViewHolder(val binding: ItemBlockUserBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemBlockUserBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return blockList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = blockList[holder.absoluteAdapterPosition]
        with(holder.binding){

            ivImage.loadImage(ApiConstants.IMAGE_BASE_URL+model.blockToUser?.image,placeholder = R.drawable.profile_icon)
            tvName.text = buildString {
                append(model.blockToUser?.username ?: "")
            }

            if (from == "search"){
                when (model.blockToUser?.isFollowByMe) {
                    0 -> {
                        tvFollow.text = "Requested"
                        tvFollow.backgroundTintList = ContextCompat.getColorStateList(ctx,R.color.shop_cat_color)
                        tvFollow.setTextColor(ContextCompat.getColorStateList(ctx,R.color.black))
                    }

                    1 -> {
                        tvFollow.text = "Following"
                        tvFollow.backgroundTintList = ContextCompat.getColorStateList(ctx,R.color.shop_cat_color)
//                        tvFollow.setTextColor(ContextCompat.getColorStateList(ctx,R.color.blue))
                        tvFollow.setTextColor(ContextCompat.getColorStateList(ctx,R.color.black))
                    }

                    else -> {


                        if (model.blockToUser?.profile_type == 1) {
                            if (model.blockToUser.isFollowByOther == 1) {
                                tvFollow.text = "Follow back"
                            } else {
                                tvFollow.text = "Request"
                            }
                        } else {
                            if (model.blockToUser?.isFollowByOther == 1) {
                                tvFollow.text = "Follow back"
                            } else {
                                tvFollow.text = "Follow"
                            }
                        }
                        tvFollow.backgroundTintList = ContextCompat.getColorStateList(ctx,R.color.shop_cat_color)
                        tvFollow.setTextColor(ContextCompat.getColorStateList(ctx,R.color.black))


                     /*   if (model.blockToUser?.profile_type == 1) {
                            tvFollow.text = "Request"
                            tvFollow.backgroundTintList = ContextCompat.getColorStateList(ctx,R.color.divider_grey)
//                            tvFollow.setTextColor(ContextCompat.getColorStateList(ctx,R.color.blue))
                            tvFollow.setTextColor(ContextCompat.getColorStateList(ctx,R.color.black))
                        } else {
                            tvFollow.text = "Follow"
                            tvFollow.backgroundTintList = ContextCompat.getColorStateList(ctx,R.color.divider_grey)
//                            tvFollow.setTextColor(ContextCompat.getColorStateList(ctx,R.color.blue))
                            tvFollow.setTextColor(ContextCompat.getColorStateList(ctx,R.color.black))
                        }*/
                    }
                }
            }
            else{
                tvFollow.backgroundTintList = ContextCompat.getColorStateList(ctx,R.color.shop_cat_color)
                tvFollow.setTextColor(ContextCompat.getColorStateList(ctx,R.color.black))

                if (model.status== "1"){
                    tvFollow.text = "Unblock"
                }else{
                    tvFollow.text = "Block"
                }
            }



            root.setOnClickListener {
                if (from == "search"){
                    ctx.startActivity(Intent(ctx, OtherUserProfileActivity::class.java).apply {
                        putExtra("user_id",model.blockToUser?.id.toString())
                    })
                }else{
                    ctx.startActivity(Intent(ctx, BlockedProfileActivity::class.java).apply {
                        putExtra("user_id",model.blockToUser?.id.toString())
                    })
                }
            }
            tvFollow.setOnClickListener {
                if (from == "search"){
                    if (isInternetAvailable(ctx)){
                        val followStatus = model.blockToUser?.isFollowByMe ?: 4
                        if (model.blockToUser?.profile_type == 1) {
                            if (followStatus != 0){
                                model.blockToUser.isFollowByMe = 0
                            }else{
                                model.blockToUser.isFollowByMe = 3
                            }
                        } else {
                            if (model.blockToUser?.isFollowByMe == 1){
                                model.blockToUser.isFollowByMe = 3
                            }else{
                                model.blockToUser?.isFollowByMe = 1
                            }
                        }
                        followUnfollowListener?.invoke(holder.absoluteAdapterPosition, model, it)
                        notifyItemChanged(holder.absoluteAdapterPosition)
                    }else{
                        showCustomSnackbar(ctx,holder.binding.tvFollow,"Internet not available")
                    }
                }else{
                    onBlockListener?.invoke(holder.absoluteAdapterPosition,model)
                }
            }
        }
    }

}