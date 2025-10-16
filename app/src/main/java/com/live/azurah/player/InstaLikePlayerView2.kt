package com.live.azurah.player

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.live.azurah.R
import com.live.azurah.activity.PlayerActivity
import com.live.azurah.controller.MyApplication
import com.live.azurah.databinding.LayoutPlayerBinding
import com.live.azurah.retrofit.ApiConstants
import com.live.azurah.util.gone
import com.live.azurah.util.visible
import kotlin.math.abs

class InstaLikePlayerView2 @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context!!, attrs, defStyleAttr) {

    private var videoSurfaceView: TextureView? = null
    private var player: ExoPlayer? = null
    private var lastPos: Long? = 0
    private var videoUri: Uri? = null
    private var postId = "0"
    private var thumb = ""
    private var imageView: ImageView? = null
    private var playImage: ImageView? = null
    private var mute: ImageView? = null

    // GestureDetector for handling single/double taps
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            Log.d("vvdvdvdvd","singletapappp")
//            playVideo()
            togglePlayPause()
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            // Play/pause toggle
            Log.d("vvdvdvdvd","doubletpp")

            return true
        }
    })

    // Add scale gesture detector for pinch-to-zoom
    private val scaleGestureDetector = ScaleGestureDetector(context!!, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        private var scaleFactor = 1.0f

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(0.5f, 3.0f) // Limit scale
            videoSurfaceView?.scaleX = scaleFactor
            videoSurfaceView?.scaleY = scaleFactor
            return true
        }
    })

    private fun getPlayer(): ExoPlayer? = player

    private fun setPlayer(player: ExoPlayer?) {
        if (this.player === player) return
        val oldPlayer = this.player
        oldPlayer?.clearVideoSurface()
        this.player = player
        player?.setVideoTextureView(videoSurfaceView)
    }

    private fun adjustAspectRatio(videoWidth: Int, videoHeight: Int) {
        if (videoWidth <= 0 || videoHeight <= 0) return

        val viewWidth = videoSurfaceView?.width ?: return
        val viewHeight = videoSurfaceView?.height ?: return

        val aspectRatio = videoWidth.toFloat() / videoHeight
        val viewAspectRatio = viewWidth.toFloat() / viewHeight

        val scaleX: Float
        val scaleY: Float

        if (aspectRatio > viewAspectRatio) {
            scaleX = aspectRatio / viewAspectRatio
            scaleY = 1f
        } else {
            scaleX = 1f
            scaleY = viewAspectRatio / aspectRatio
        }

        val pivotPointX = viewWidth / 2f
        val pivotPointY = viewHeight / 2f

        val matrix = Matrix()
        matrix.setScale(scaleX, scaleY, pivotPointX, pivotPointY)

        videoSurfaceView?.setTransform(matrix)
        videoSurfaceView?.invalidate()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // Let the gesture detectors process the event first
        gestureDetector.onTouchEvent(ev)
        scaleGestureDetector.onTouchEvent(ev)
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Process touch events with both detectors
        gestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)

        // Always return true to indicate we've handled the event
        return true
    }

    init {
        if (isInEditMode) {
            videoSurfaceView = null
        } else {
            val playerLayoutId = R.layout.exo_simple_player_view_2
            LayoutInflater.from(context).inflate(playerLayoutId, this)
            descendantFocusability = FOCUS_AFTER_DESCENDANTS
            videoSurfaceView = findViewById(R.id.surface_view_2)

            // Make sure the view is clickable and focusable
            isClickable = true
            isFocusable = true
            isFocusableInTouchMode = true

            initPlayer()
        }
    }

    private fun initPlayer() {
        reset()
        val player = ExoPlayer.Builder(context).build()
        player.repeatMode = Player.REPEAT_MODE_ALL
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                mute?.visible()
                mute?.setImageResource(if (ApiConstants.isMute) R.drawable.volume_off else R.drawable.volume)

                if (playbackState == Player.STATE_READY) {
                    alpha = 1f
                    adjustAspectRatio(player.videoSize.width, player.videoSize.height)
                    playImage?.gone()
                } else {
                    playImage?.visible()
                }
            }
        })

        videoSurfaceView?.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                player.setVideoTextureView(videoSurfaceView)
            }
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                player.videoSize.let { adjustAspectRatio(it.width, it.height) }
            }
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }

        setPlayer(player)
    }

    @UnstableApi
    fun startPlaying() {
        val mediaItem = MediaItem.fromUri(videoUri!!)
        val cacheDataSourceFactory: DataSource.Factory =
            CacheDataSource.Factory()
                .setCache(MyApplication.cache)
                .setUpstreamDataSourceFactory(
                    DefaultHttpDataSource.Factory().setUserAgent("Azurah"))

        val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
            .createMediaSource(mediaItem)

        player?.setMediaSource(mediaSource)
        player?.seekTo(lastPos!!)
        player?.prepare()
        player?.play()
    }

    fun removePlayer() {
        getPlayer()?.playWhenReady = false
        lastPos = getPlayer()?.currentPosition
        reset()
        getPlayer()?.stop()
    }

    fun pausePlayer() {
        getPlayer()?.playWhenReady = false
        lastPos = getPlayer()?.currentPosition
        getPlayer()?.pause()
    }

    fun resumePlayer() {
        getPlayer()?.playWhenReady = true
        playImage?.gone()
        lastPos = getPlayer()?.currentPosition
    }

    fun isPlaying(): Boolean = getPlayer()?.isPlaying == true

    fun isMute(): Boolean = getPlayer()?.volume == 0f

    fun setMuted(mute: Boolean) {
        getPlayer()?.volume = if (mute) 0f else 1f
        Log.d("InstaLikePlayerView2", "Mute state: $mute")
    }

    private fun togglePlayPause() {
        if (player != null) {
            if (player!!.isPlaying) {
                pausePlayer()
                playImage?.visible()
            } else {
                resumePlayer()
                playImage?.gone()
            }
        }
    }

    fun reset() {
        alpha = 0f
    }

    fun setVideoUri(uri: Uri?, id: String, imageView: ImageView, thumb: String, mute: ImageView, playImage: ImageView) {
        this.videoUri = uri
        this.postId = id
        this.imageView = imageView
        this.mute = mute
        this.thumb = thumb
        this.playImage = playImage
    }


    fun playVideo() {
        val dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val inflater = LayoutInflater.from(context)
        val videoBinding = LayoutPlayerBinding.inflate(inflater)

        dialog.setContentView(videoBinding.root)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.BLACK))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setGravity(Gravity.TOP) // Start from top
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }

        // Close button
        videoBinding.ivClose.setOnClickListener {
            dialog.dismiss()
        }

        // Detach from feed surface
        getPlayer()?.clearVideoSurface()

        // Attach player to fullscreen PlayerView
        videoBinding.playerView.player = getPlayer()

        // Swipe down to dismiss gesture detection
        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val diffY = e2.y - (e1?.y ?: 0f)
                val diffX = e2.x - (e1?.x ?: 0f)

                if (abs(diffX) > abs(diffY)) {
                    // Horizontal swipe, ignore or handle differently
                    return false
                }

                if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        // Swipe down
                        dialog.dismiss()
                        return true
                    }
                }
                return false
            }
        })

        // Touch listener for the root view to handle swipe gestures
        videoBinding.root.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        // Optional: Add smooth transition animation
        val transition = dialog.window?.attributes
        transition?.windowAnimations = R.style.DialogAnimation
        dialog.window?.attributes = transition

        dialog.setOnDismissListener {
            // Detach from fullscreen view
            videoBinding.playerView.player = null

            // Reattach back to feed
            getPlayer()?.setVideoTextureView(videoSurfaceView)

            // Restore UI state
            if (getPlayer()?.isPlaying == true) {
                playImage?.gone()
            } else {
                playImage?.visible()
            }
        }

        dialog.show()
    }
}