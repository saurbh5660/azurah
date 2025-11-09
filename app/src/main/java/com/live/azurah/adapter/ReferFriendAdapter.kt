package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.activity.BlockedProfileActivity
import com.live.azurah.activity.OtherUserProfileActivity
import com.live.azurah.databinding.ItemBlockUserBinding
import com.live.azurah.model.BlockResposne
import com.live.azurah.model.ReferralRewardResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.gone
import com.live.azurah.util.isInternetAvailable
import com.live.azurah.util.loadImage
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible

class ReferFriendAdapter(val ctx: Context, val blockList: ArrayList<ReferralRewardResponse.Body.Referral>):RecyclerView.Adapter<ReferFriendAdapter.ViewHolder>() {

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

            ivImage.loadImage(ApiConstants.IMAGE_BASE_URL+model?.image,placeholder = R.drawable.profile_icon)

            if (model?.display_name_preference == "1") {
                tvName.text = buildString {
                    append(model?.firstName ?: "")
                }
            } else {
                tvName.text = buildString {
                    append(model?.firstName ?: "")
                    append(" ")
                    append(model?.lastName ?: "")
                }
            }

            if (model.username.toString().contains("@")) {
                tvName.text = buildString {
                    append(model.username ?: "")
                }
            } else {
                tvUserName.text = buildString {
                    append("@")
                    append(model.username ?: "")
                }
            }
            tvUserName.visible()
            root.setOnClickListener {
                ctx.startActivity(Intent(ctx, OtherUserProfileActivity::class.java).apply {
                    putExtra("user_id",model?.id.toString())
                })
            }
            tvFollow.gone()
        }
    }

}