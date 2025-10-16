package com.live.azurah.activity

import AudioManagerPlayer
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import com.live.azurah.R
import com.live.azurah.databinding.ActivityPlayerBinding
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.MusicLoaderDialog
import com.live.azurah.util.loadImage

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var audioPlayer : AudioManagerPlayer
    private var image = ""
    private var songName = ""
    private var artistName = ""
    private var song = ""
    private var isUserSeeking = false
    private val loaderDialog by lazy { MusicLoaderDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
        audioPlayer = AudioManagerPlayer(this)
        image = intent.getStringExtra("image") ?: ""
        songName = intent.getStringExtra("songName") ?: ""
        artistName = intent.getStringExtra("artistName") ?: ""
        song = intent.getStringExtra("song") ?: ""

        initListener()
    }
    private fun initListener(){
        with(binding){
            audioPlayer.playMusicFromUrl("https://app.azurah.co.uk$song",
                onProgressUpdate = { currentPosition, duration ->
                    if (!isUserSeeking) {
                        updateSeekBar(currentPosition, duration)
                    }
                },
                onBufferingStateChanged = { isBuffering ->
                    showLoader(isBuffering)
                }
            )
            seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        updateTimer(progress, seekBar?.max ?: 0)
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    isUserSeeking = true
                }
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    isUserSeeking = false
                    audioPlayer.seekTo(seekBar?.progress ?: 0)
                }
            })
            tvSongName.text = songName
            tvSongWeek.text = artistName
            ivImage.loadImage(ApiConstants.IMAGE_BASE_URL+image)
            backIcon.setOnClickListener {
                finish()
            }

            ivPlayPause.setOnClickListener {
                if (audioPlayer.isPlaying()){
                    audioPlayer.pauseMusic()
                    ivPlayPause.setImageResource(R.drawable.play)
                }
                else{
                    ivPlayPause.setImageResource(R.drawable.pause)
                    audioPlayer.resumeMusic()
                }
            }

            ivForward.setOnClickListener {
                adjustPlaybackPosition(10 * 1000) // Move forward by 10 seconds
            }

            ivReverse.setOnClickListener {
                adjustPlaybackPosition(-10 * 1000) // Move backward by 10 seconds
            }
        }
    }

    private fun updateSeekBar(currentPosition: Int, duration: Int) {
        binding.seekbar.max = duration
        binding.seekbar.progress = currentPosition
        updateTimer(currentPosition, duration)
    }

    private fun updateTimer(currentPosition: Int, duration: Int) {
        val currentMinutes = (currentPosition / 1000) / 60
        val currentSeconds = (currentPosition / 1000) % 60
        val totalMinutes = (duration / 1000) / 60
        val totalSeconds = (duration / 1000) % 60

        binding.tvStart.text = String.format("%02d:%02d", currentMinutes, currentSeconds)
        binding.tvEnd.text = String.format("%02d:%02d", totalMinutes, totalSeconds)
    }

    private fun showLoader(isBuffering: Boolean) {
       if (isBuffering){
           LoaderDialog.show(this)
           binding.ivPlayPause.setImageResource(R.drawable.play)
       }
       else{
          LoaderDialog.dismiss()
           binding.ivPlayPause.setImageResource(R.drawable.pause)
       }
    }

    private fun adjustPlaybackPosition(offset: Int) {
        val currentPosition = audioPlayer.getCurrentPosition()
        val duration = audioPlayer.getDuration()
        val newPosition = (currentPosition + offset).coerceIn(0, duration)

        audioPlayer.seekTo(newPosition)
        binding.seekbar.progress = newPosition
        updateTimer(newPosition, duration)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.stopMusic()
    }

}