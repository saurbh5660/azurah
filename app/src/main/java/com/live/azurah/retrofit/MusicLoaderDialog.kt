package com.live.azurah.retrofit

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.live.azurah.R

class MusicLoaderDialog (context: Context) : Dialog(context) {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_music_loader)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        Glide.with(context).asGif().load(R.drawable.music_loading_anim).into(findViewById(R.id.musicLoader))
        setCancelable(false)
    }
}