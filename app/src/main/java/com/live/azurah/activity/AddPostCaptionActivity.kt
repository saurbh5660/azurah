package com.live.azurah.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.live.azurah.databinding.ActivityAddPostCaptionBinding
import com.live.azurah.databinding.LayoutPlayerBinding
import com.live.azurah.model.FullImageModel
import com.live.azurah.model.ImageVideoModel
import com.live.azurah.util.ShowImagesDialogFragment
import com.live.azurah.util.containsBannedWord
import com.live.azurah.util.showCustomSnackbar

class AddPostCaptionActivity : AppCompatActivity(), AddPostImageAdapter.ClickListener {
    private lateinit var binding: ActivityAddPostCaptionBinding
    private var list = ArrayList<ImageVideoModel>()
    private lateinit var adapter: AddPostImageAdapter
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostCaptionBinding.inflate(layoutInflater)
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
        setAdapter()
        initListener()

    }

    private fun setAdapter() {
        adapter = AddPostImageAdapter(this, list, this, 1)
        binding.rvPostImages.adapter = adapter

        adapter.zoomImageListener = { pos ->
            if (list[pos].type == "2") {
                playVideo(list[pos].video.toString())
            } else {
                val imageList = list.filter { it.type == "1" }
                    .map { FullImageModel(image = it.image, type = 0) } as ArrayList
                val fullImageDialog = ShowImagesDialogFragment.newInstance(imageList, pos)
                fullImageDialog.show(supportFragmentManager, "FullImageDialog")
//                fullImage.showImages(this, imageList, pos)
            }
        }

    }

    private fun initListener() {
        with(binding) {
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            btnPost.setOnClickListener {
                if (containsBannedWord(binding.etDes.text.toString().trim())) {
                    showCustomSnackbar(
                        this@AddPostCaptionActivity,
                        it,
                        "Your post contains banned or inappropriate words. Please remove them before posting."
                    )
                    return@setOnClickListener
                }
                startActivity(
                    Intent(
                        this@AddPostCaptionActivity,
                        PreviewPostActivity::class.java
                    ).apply {
                        putExtra("images", list)
                        putExtra("desc", binding.etDes.text.toString())
                    })
            }

            etDes.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if (s.toString().isNotEmpty() && list.isNotEmpty()) {
                        binding.btnPost.backgroundTintList = getColorStateList(R.color.blue)
                        binding.btnPost.isEnabled = true
                    } else {
                        binding.btnPost.backgroundTintList = getColorStateList(R.color.button_grey)
                        binding.btnPost.isEnabled = false
                    }

                    if (s.toString().trim().isNotEmpty()) {
                        val wordCount = countWords(s.toString())
                        tvWords.text = (50 - wordCount).toString() + " words"
                        if (wordCount > 50) {
                            val trimmedText = trimToWordLimit(s.toString(), 50)
                            binding.etDes.setText(trimmedText)
                            binding.etDes.setSelection(trimmedText.length)
                        }
                    } else {
                        tvWords.text = "50 words"
                    }
                }

            })
        }
    }

    override fun onClick(position: Int) {

    }

    private fun countWords(text: String): Int {
        val words = text.trim().split("\\s+".toRegex())
        return words.size
    }

    private fun trimToWordLimit(text: String, limit: Int): String {
        val words = text.trim().split("\\s+".toRegex())
        return words.take(limit).joinToString(" ")
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
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }
}