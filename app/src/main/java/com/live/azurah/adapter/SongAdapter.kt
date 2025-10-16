package com.live.azurah.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.live.azurah.activity.PlayerActivity
import com.live.azurah.databinding.ItemSongBinding
import com.live.azurah.model.SongModel
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.loadImage
import com.live.azurah.util.openUrlInBrowser

class SongAdapter(
    val ctx: Context,
    private val songList: ArrayList<SongModel>,
    val type: Int
):RecyclerView.Adapter<SongAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSongBinding): RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSongBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }
    override fun getItemCount(): Int {
        return when(type){
            1-> if (songList.size >= 2) 2 else songList.size
            2->  if (songList.size == 2) 2 else songList.size
            else->songList.size
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            val model = songList[position]
            tvSongName.text = model.songName
            tvArtistName.text = model.artistName
            ivSongImage.loadImage(ApiConstants.IMAGE_BASE_URL+model.image)

            root.setOnClickListener {
                openUrlInBrowser(ctx,model.song ?: "")
               /* ctx.startActivity(Intent(ctx, PlayerActivity::class.java).apply {
                    putExtra("image",model.image)
                    putExtra("songName",model.songName)
                    putExtra("artistName",model.artistName)
                    putExtra("song",model.song)
                })*/
            }

        }
    }
}