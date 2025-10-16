package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.R
import com.live.azurah.activity.OtherUserProfileActivity
import com.live.azurah.databinding.ItemFollowBinding
import com.live.azurah.model.FollowFollowingResponse
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.getPreference
import com.live.azurah.util.gone
import com.live.azurah.util.isInternetAvailable
import com.live.azurah.util.loadImage
import com.live.azurah.util.showCustomSnackbar

class FollowUnfollowListAdapter(
    val ctx: Context,
    val list: ArrayList<FollowFollowingResponse.Body.Data>,
    val type: Int = 0,
    var selectedType: Int = 0
) : RecyclerView.Adapter<FollowUnfollowListAdapter.ViewHolder>() {

    var listener: ((pos: Int, view: View) -> Unit)? = null
    var followUnfollowListener: ((pos: Int, model: FollowFollowingResponse.Body.Data,view: View) -> Unit)? =
        null

    class ViewHolder(val binding: ItemFollowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemFollowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            val model = list[holder.absoluteAdapterPosition]

            when (model.isFollowByMe){
                0->{
                    tvFollow.background =
                        AppCompatResources.getDrawable(ctx, R.drawable.round_stroke_with_background)
                    tvFollow.backgroundTintList = ctx.getColorStateList(R.color.shop_cat_color)
                    tvFollow.text = "Requested"
                }
                1->{
                    tvFollow.background =
                        AppCompatResources.getDrawable(ctx, R.drawable.round_stroke_with_background)
                    tvFollow.backgroundTintList = ctx.getColorStateList(R.color.shop_cat_color)
                    tvFollow.text = "Following"
                }
                else->{
                    if (model.profile_type == 1) {
                        if (model.isFollowByOther == 1) {
                            tvFollow.text = "Follow back"
                        } else {
                            tvFollow.text = "Request"
                        }
                    } else {
                        if (model.isFollowByOther == 1) {
                            tvFollow.text = "Follow back"
                        } else {
                            tvFollow.text = "Follow"
                        }
                    }

                    tvFollow.background =
                        AppCompatResources.getDrawable(ctx, R.drawable.round_stroke_with_background)
                    tvFollow.backgroundTintList = ctx.getColorStateList(R.color.shop_cat_color)
//                    tvFollow.text = "Follow"
                }
            }
            Log.d("dsgzdfdfdf",selectedType.toString())

            if (selectedType == 1){
                ivImage.loadImage(
                    ApiConstants.IMAGE_BASE_URL + model.follow_to_user?.image,
                    placeholder = R.drawable.profile_icon
                )
                tvName.text = buildString {
                    append(model.follow_to_user?.username ?: "")
                    if (getPreference("id","") == (model.follow_to_user?.id ?: "")){
                        tvFollow.gone()
                    }

                }
            }else{
                ivImage.loadImage(
                    ApiConstants.IMAGE_BASE_URL + model.follow_by_user?.image,
                    placeholder = R.drawable.profile_icon
                )
                tvName.text = buildString {
                    append(model.follow_by_user?.username ?: "")
                }
                if (getPreference("id","") == (model.follow_by_user?.id ?: "")){
                    tvFollow.gone()
                }
            }

            if (type == 1) {
                ivMore.visibility = View.VISIBLE
            } else {
                ivMore.visibility = View.GONE
            }

            tvFollow.setOnClickListener {
                if (isInternetAvailable(ctx)){
                    val followStatus = model.isFollowByMe ?: 4
                    if (model.profile_type == 1) {
                        if (followStatus != 0){
                            model.isFollowByMe = 0
                        }else{
                            return@setOnClickListener
                        }
                    } else {
                        if (model.isFollowByMe == 1){
                            model.isFollowByMe = 3
                        }else{
                            model.isFollowByMe = 1
                        }
                    }
                    followUnfollowListener?.invoke(holder.absoluteAdapterPosition, model, it)
                    notifyItemChanged(holder.absoluteAdapterPosition)
                }else{
                    showCustomSnackbar(ctx,holder.binding.tvFollow,"Internet not available")
                }
            }

            root.setOnClickListener {
                Log.d("sdvsddsdd","dvfdsgsbsfBf")
                if (selectedType == 1){
                    ctx.startActivity(Intent(ctx, OtherUserProfileActivity::class.java).apply {
                        putExtra("user_id",model.follow_to_user?.id.toString())
                    })
                }else{
                    ctx.startActivity(Intent(ctx, OtherUserProfileActivity::class.java).apply {
                        putExtra("user_id", model.follow_by_user?.id.toString())
                    })
                }

            }

            ivMore.setOnClickListener {
                listener?.invoke(position, it)
            }
        }
    }

    fun updateSelectedType(selected:Int){
        selectedType = selected
    }

}