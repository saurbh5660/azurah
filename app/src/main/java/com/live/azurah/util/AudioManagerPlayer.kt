import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log

class AudioManagerPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private val handler = Handler(Looper.getMainLooper())
    private var onProgressUpdate: ((currentPosition: Int, duration: Int) -> Unit)? = null
    private var onBufferingStateChanged: ((isBuffering: Boolean) -> Unit)? = null

    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    fun playMusicFromUrl(
        url: String,
        onProgressUpdate: (currentPosition: Int, duration: Int) -> Unit,
        onBufferingStateChanged: (isBuffering: Boolean) -> Unit
    ) {
        this.onProgressUpdate = onProgressUpdate
        this.onBufferingStateChanged = onBufferingStateChanged

        // Request audio focus
        val result = audioManager?.requestAudioFocus(
            { focusChange -> handleAudioFocusChange(focusChange) },
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer()

            try {
                mediaPlayer?.apply {
                    setAudioStreamType(AudioManager.STREAM_MUSIC)
                    setDataSource(url)
                    prepareAsync()
                    Log.d("kjgfskjdfsdgsdg","bufferingggggggg")
                    onBufferingStateChanged.invoke(true) // Notify buffering started

                    setOnPreparedListener {
                        start() // Start playback as soon as prepared
                        Log.d("kjgfskjdfsdgsdg","playbackstarteddd")

                        onBufferingStateChanged.invoke(false) // Notify buffering ended
                        startProgressUpdates() // Start progress updates
                    }

                    setOnCompletionListener {
                        onBufferingStateChanged.invoke(false)
                        startProgressUpdates()
                    }

                    setOnBufferingUpdateListener { _, percent ->
                        // Notify buffering progress (percent of content buffered)
                      //  onBufferingStateChanged.invoke(percent < 100)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onBufferingStateChanged.invoke(false)
            }
        }
    }

    fun pauseMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            stopProgressUpdates()
        }
    }

    fun completeMusic() {
        mediaPlayer?.pause()
        stopProgressUpdates()
    }

    fun resumeMusic() {
        mediaPlayer?.start()
        startProgressUpdates()
    }

    fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        audioManager?.abandonAudioFocus(null)
        stopProgressUpdates()
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    private fun startProgressUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    val currentPosition = it.currentPosition
                    val duration = it.duration
                    onProgressUpdate?.invoke(currentPosition, duration)
                    handler.postDelayed(this, 1000)
                }
            }
        })
    }

    private fun stopProgressUpdates() {
        handler.removeCallbacksAndMessages(null)
    }

    private fun handleAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> stopMusic()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pauseMusic()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> mediaPlayer?.setVolume(0.2f, 0.2f)
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.setVolume(1.0f, 1.0f)
                mediaPlayer?.start()
            }
        }
    }
}

