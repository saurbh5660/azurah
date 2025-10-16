package com.live.azurah.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.live.azurah.R
import com.live.azurah.adapter.AddPostImageAdapter
import com.live.azurah.databinding.ActivityAddPostBinding
import com.live.azurah.databinding.LayoutPlayerBinding
import com.live.azurah.databinding.RoyaltyFreeDialogBinding
import com.live.azurah.model.FullImageModel
import com.live.azurah.model.ImageVideoModel
import com.live.azurah.util.ImagePickerActivity
import com.live.azurah.util.ShowImagesDialogFragment
import com.live.azurah.util.getPreference
import com.live.azurah.util.savePreference
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max

class AddPostActivity : ImagePickerActivity(),AddPostImageAdapter.ClickListener {
    private lateinit var binding: ActivityAddPostBinding
    private var list = ArrayList<ImageVideoModel>()
    private lateinit var adapter: AddPostImageAdapter
    private lateinit var bottomSheetBehavior : BottomSheetBehavior<View>
    private var isVideo = false
    private var player: ExoPlayer? = null

    override fun selectedImage(imagePath: String?, code: Int) {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        if (!imagePath.isNullOrEmpty()){
            if (code == 10 || code == 11){
                val width = binding.ivPosts.width
                val height = binding.ivPosts.height
                val thumb = createVideoThumb(this,imagePath.toUri(),width,height)
                if (list.first().image.isNullOrEmpty()){
                    list[0].image = thumb
                    list[0].video = imagePath
                    list[0].type = "2"
                }else{
                    list.add(ImageVideoModel(image = thumb,video = imagePath, type = "2"))
                }
            }else{
                if (list.first().image.isNullOrEmpty()){
                    list[0].image = imagePath
                    list[0].type = "1"
                }else{
                    list.add(ImageVideoModel(image = imagePath, type = "1"))
                }
            }

        }
        if (list.size == 3){
            binding.btnMorePhotos.backgroundTintList = ActivityCompat.getColorStateList(this,R.color.button_grey)
            binding.btnMorePhotos.isEnabled = false
        }else{
            binding.btnMorePhotos.backgroundTintList = ActivityCompat.getColorStateList(this,R.color.blue)
            binding.btnMorePhotos.isEnabled = true
        }
        binding.btnMorePhotos.visibility = View.VISIBLE
        binding.btnCaption.visibility = View.VISIBLE
        adapter.notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostBinding.inflate(layoutInflater)
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

        bottomSheetBehavior = BottomSheetBehavior.from(binding.inPost.clPostSheet)
        list.clear()
        list.add(ImageVideoModel())

        setAdapter()
        initListener()
        initBottomSheetBehavior()

        val dontShowAgain= getPreference("showAgain",false)
        if (!dontShowAgain){
            confirmationDialog()
        }
    }

    private fun setAdapter() {
        adapter = AddPostImageAdapter(this,list,this,0)
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

        adapter.addImageVideo = { pos ->
            imageVideoPickerDialog()
        }

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initListener() {
        with(binding){
            backIcon.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            btnCaption.setOnClickListener {
                startActivity(Intent(this@AddPostActivity,AddPostCaptionActivity::class.java).apply {
                    putExtra("images",list)
                })
            }
            inPost.clCamera.setOnClickListener {
                if (isVideo){
                    askStorageManagerPermission(this@AddPostActivity,10,true,2)
                }else{
                    askStorageManagerPermission(this@AddPostActivity,98,false,2)
                }
            }

            inPost.clGallery.setOnClickListener {
                if (isVideo){
                    askStorageManagerPermission(this@AddPostActivity,11,true,2)
                }else{
                    askStorageManagerPermission(this@AddPostActivity,99,false,2)
                }
            }

            btnMorePhotos.setOnClickListener {
                imageVideoPickerDialog()
            }
        }
    }

    private fun initBottomSheetBehavior() {
        bottomSheetBehavior.peekHeight = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                Log.d("kjbdgjfg",slideOffset.toString())
            }
        })
    }

    private fun imageVideoPickerDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.image_video_picker)
        val window = dialog.window
        window!!.setGravity(Gravity.BOTTOM)
        window.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val camera = dialog.findViewById<TextView>(R.id.select_camera)
        val cancel = dialog.findViewById<TextView>(R.id.cancel)
        val gallery = dialog.findViewById<TextView>(R.id.select_photo_library)
        cancel.setOnClickListener { dialog.dismiss() }

        camera.setOnClickListener {
            dialog.dismiss()
            isVideo = false
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        gallery.setOnClickListener {
            dialog.dismiss()
            isVideo = true
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        }
        dialog.show()
    }

    override fun onClick(position: Int) {
        if (list.size == 1){
            list[0] = ImageVideoModel()
            binding.btnMorePhotos.visibility = View.GONE
            binding.btnCaption.visibility = View.GONE
        }else{
            list.removeAt(position)
        }
        adapter.notifyDataSetChanged()
        if (list.size == 3){
            binding.btnMorePhotos.backgroundTintList = ActivityCompat.getColorStateList(this,R.color.button_grey)
            binding.btnMorePhotos.isEnabled = false
        }else{
            binding.btnMorePhotos.backgroundTintList = ActivityCompat.getColorStateList(this,R.color.blue)
            binding.btnMorePhotos.isEnabled = true
        }
    }


  /*  private fun createVideoThumb(context: Context, uri: Uri, viewWidth: Int, viewHeight: Int): String? {
        try {
            var path = ""
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(context, uri)

            // Capture a frame at a specific time (e.g., at 1 second) for better quality
            val thumbnail = mediaMetadataRetriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST)

            if (thumbnail != null) {
                // Get the original dimensions of the thumbnail
                val originalWidth = thumbnail.width
                val originalHeight = thumbnail.height

                Log.d("dfkjghdjd", originalWidth.toString())
                Log.d("dfkjghdjd", originalHeight.toString())

                // Calculate the scale factor to maintain the aspect ratio
                val scaleFactor: Float
                var scaledWidth: Int
                var scaledHeight: Int

                val viewAspectRatio = viewWidth.toFloat() / viewHeight.toFloat()
                val videoAspectRatio = originalWidth.toFloat() / originalHeight.toFloat()

                if (videoAspectRatio > viewAspectRatio) {
                    // Scale based on the width (video is wider than the view)
                    scaledWidth = viewWidth
                    scaledHeight = (viewWidth / videoAspectRatio).toInt()
                } else {
                    // Scale based on the height (video is taller than the view)
                    scaledHeight = viewHeight
                    scaledWidth = (viewHeight * videoAspectRatio).toInt()
                }

                // Create a new bitmap with the target size
                val newBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(newBitmap)
                val rect = Rect(0, 0, scaledWidth, scaledHeight)
                canvas.drawBitmap(thumbnail, null, rect, null)

                // Convert the final bitmap to a file
                val thumbFile = convertBitmapToFile(newBitmap, Calendar.getInstance().time.time.toString())
                if (thumbFile != null) {
                    path = thumbFile.absolutePath
                }
                Log.d("VideoThumbnail", "Thumbnail created: $path")
            } else {
                Toast.makeText(context, "Thumbnail could not be generated", Toast.LENGTH_SHORT).show()
            }

            return path
        } catch (ex: Exception) {
            Log.e("VideoThumbnail", "Error generating thumbnail: ${ex.message}", ex)
            Toast.makeText(context, "Error generating thumbnail", Toast.LENGTH_SHORT).show()
        }
        return null
    }

    private fun convertBitmapToFile(bitmap: Bitmap, fileName: String): File? {
     *//*   val directory = File(cacheDir, "thumbnails")
        if (!directory.exists()) {
            directory.mkdirs()
        }*//*

//        val file = File(directory, "$fileName.png")
        val file = createImageTempFile(this,System.currentTimeMillis().toString()+"_thubmnail",".jpg")
        try {
            val outStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream) // Ensure PNG format
            outStream.flush()
            outStream.close()
            return file
        } catch (e: Exception) {
            Log.e("VideoThumbnail", "Error saving bitmap: ${e.message}", e)
        }
        return null
    }*/

    private fun createVideoThumb(context: Context, uri: Uri, viewWidth: Int, viewHeight: Int): String? {
        return try {
            val mediaMetadataRetriever = MediaMetadataRetriever().apply {
                setDataSource(context, uri)
            }

            // Get frame at 1 second (1,000,000 microseconds)
            val originalBitmap = mediaMetadataRetriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST)
                ?: throw Exception("Failed to capture video frame")

            // Calculate scale for center crop
            val scale = max(
                viewWidth.toFloat() / originalBitmap.width,
                viewHeight.toFloat() / originalBitmap.height
            )

            // Calculate scaled dimensions
            val scaledWidth = (originalBitmap.width * scale).toInt()
            val scaledHeight = (originalBitmap.height * scale).toInt()

            // First scale to cover the view dimensions
            val scaledBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                scaledWidth,
                scaledHeight,
                true
            )

            // Then crop to the exact view dimensions
            val x = (scaledWidth - viewWidth) / 2
            val y = (scaledHeight - viewHeight) / 2
            val croppedBitmap = Bitmap.createBitmap(
                scaledBitmap,
                x.coerceAtLeast(0),
                y.coerceAtLeast(0),
                viewWidth.coerceAtMost(scaledWidth),
                viewHeight.coerceAtMost(scaledHeight)
            )

            // Save to file
            convertBitmapToFile(croppedBitmap, "thumb_${System.currentTimeMillis()}")?.absolutePath
        } catch (ex: Exception) {
            Log.e("VideoThumbnail", "Error generating thumbnail", ex)
            Toast.makeText(context, "Error generating thumbnail", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun convertBitmapToFile(bitmap: Bitmap, fileName: String): File? {
        return try {
            val filesDir = cacheDir
            val file = File(filesDir, "$fileName.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            file
        } catch (e: IOException) {
            null
        }
    }

    private fun createImageTempFile(context: Context, name: String, extension: String): File {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            name,
            extension,
            storageDir
        )
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

    private fun confirmationDialog() {
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val confirmationBinding = RoyaltyFreeDialogBinding.inflate(layoutInflater)
        customDialog.setContentView(confirmationBinding.root)
        customDialog.window?.setGravity(Gravity.CENTER)
        customDialog.setCancelable(false)
        customDialog.setCanceledOnTouchOutside(false)
        customDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        customDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        confirmationBinding.tvNo.setOnClickListener {
            customDialog.dismiss()
            savePreference("showAgain",true)
        }

        confirmationBinding.tvYes.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()
    }
}

