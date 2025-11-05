package com.live.azurah.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.RelativeLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.live.azurah.R
import com.live.azurah.adapter.AddPostImageAdapter
import com.live.azurah.databinding.ActivityPreviewPostBinding
import com.live.azurah.databinding.LayoutPlayerBinding
import com.live.azurah.model.FullImageModel
import com.live.azurah.model.ImageVideoModel
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.ShowImagesDialogFragment
import com.live.azurah.util.colorOnlyHashtagsLine
import com.live.azurah.util.getPreference
import com.live.azurah.util.loadImage

class PreviewPostActivity : AppCompatActivity(),AddPostImageAdapter.ClickListener {
    private lateinit var binding: ActivityPreviewPostBinding
    private var list = ArrayList<ImageVideoModel>()
    private lateinit var adapter: AddPostImageAdapter
    private var desc = ""
    private var player: ExoPlayer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityPreviewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.white)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
              val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            view.updatePadding(
                left = systemBars.left,
                bottom = systemBars.bottom,
                right = systemBars.right,
                top = systemBars.top
            )
            insets
        }

        list = intent.getSerializableExtra("images") as ArrayList<ImageVideoModel>
        desc = intent.getStringExtra("desc") ?: ""
        binding.tvDescription.text = colorOnlyHashtagsLine(desc,this)
        setAdapter()
        initListener()
    }

    private fun setAdapter() {
        adapter = AddPostImageAdapter(this,list,this,1)
        binding.rvPostImages.adapter = adapter

        adapter.zoomImageListener = { pos ->
            if (list[pos].type == "2"){
                playVideo(list[pos].video.toString())
            }else{
                val imageList = list.filter { it.type == "1" }.map { FullImageModel(image = it.image, type = 0) } as ArrayList
                val fullImageDialog = ShowImagesDialogFragment.newInstance(imageList, pos)
                fullImageDialog.show(supportFragmentManager, "FullImageDialog")
            }
        }


    }

    private fun initListener() {
        with(binding){
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            btnPost.setOnClickListener {
                startActivity(Intent(this@PreviewPostActivity,UploadActivity::class.java).apply {
                    putExtra("images",list)
                    putExtra("desc",desc)
                })
            }

        }
    }

    override fun onClick(position: Int) {

    }

    private fun playVideo(video: String) {
        val dialog = Dialog(this, R.style.Theme_Dialog)
        val videoBinding = LayoutPlayerBinding.inflate(layoutInflater)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(videoBinding.root)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val window = dialog.window
        window!!.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT
        )
        window.setGravity(Gravity.CENTER)
        videoBinding.ivClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            releasePlayer()
        }

        startPlayer(videoBinding, video)
        dialog.show()
    }


    private fun startPlayer(binding: LayoutPlayerBinding, url: String) {
        var playerView = binding.playerView
        player = ExoPlayer.Builder(this).build()
        val mediaItem = MediaItem.fromUri(url)
        player?.setMediaItem(mediaItem)
        playerView.player = player
        player!!.prepare()
        player!!.repeatMode = Player.REPEAT_MODE_OFF
        player!!.playWhenReady = true
    }
    private fun releasePlayer() {
        if (player != null) {
            player!!.release()
        }
    }

    private fun pausePlayer() {
        if (player != null) {
            player!!.pause()
        }
    }
    private fun resumePlayer() {
        if (player != null) {
            player!!.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    override fun onResume() {
        super.onResume()
        resumePlayer()
        binding.ivPosts.loadImage(ApiConstants.IMAGE_BASE_URL+ getPreference("image",""),placeholder = R.drawable.profile_icon)
        binding.tvName.text = buildString {
            append("@")
            append(getPreference("username",""))
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }


}