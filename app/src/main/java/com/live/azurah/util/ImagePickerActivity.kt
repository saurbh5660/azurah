package com.live.azurah.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.hi.library.utils.CompressOption
import com.hi.library.utils.TrimType
import com.hi.library.utils.TrimVideo
import com.hi.library.utils.TrimmerUtils
import com.live.azurah.R
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


abstract class ImagePickerActivity : AppCompatActivity() {

    var mActivity: Activity? = null
    var mVideoDialog: Boolean = false
    var mCode = 0
    var isEditProfile = false
    var hasProfileImage = ""
    private var isProfile = 0
    private lateinit var mImageFile: File
    var mCompressor: FileCompressor? = null


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO
    )

    private val permissions1 = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
    )


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            if (permissions.isNotEmpty()) {
                permissions.entries.forEach {
                    Log.d("permissions", "${it.key} = ${it.value}")
                }

                val readStorage = permissions[Manifest.permission.READ_EXTERNAL_STORAGE]
                //      val writeStorage = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]
                val camera = permissions[Manifest.permission.CAMERA]
                val images = permissions[Manifest.permission.READ_MEDIA_IMAGES]
                val video = permissions[Manifest.permission.READ_MEDIA_VIDEO]

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (camera == true && images == true && video == true) {
                        Log.e("permissions", "Permission Granted Successfully")
                        if (mCode == 98) {
                            captureImage()
                        } else if (mCode == 99) {
                            openGalleryForImage()
                        } else {
                            imageDialog()
                        }
                        //    imageDialog()

                    } else {
                        Log.e("permissions", "Permission not granted")
                        checkPermissionDenied(permissions.keys.first())
                    }
                } else {
                    if (readStorage == true && camera == true) {
                        if (mCode == 98) {
                            captureImage()
                        } else if (mCode == 99) {
                            openGalleryForImage()
                        } else {
                            imageDialog()
                        }
                        //   imageDialog()
                    } else {
                        Log.e("permissions", "Permission not granted")
                        checkPermissionDenied(permissions.keys.first())
                    }
                }
            }
        }

    private val videoCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // There are no request codes
                Log.e("VideoSelected", "RESULT_OK")
                val contentURI = result.data?.data
                /*
                                    val selectedVideoPath = getPath(contentURI!!)

                                    val mediaFile: File = File(selectedVideoPath)
                                    val fileSizeInBytes = mediaFile.length()
                                    val fileSizeInKB = fileSizeInBytes / 1024
                                    val fileSizeInMB = fileSizeInKB / 1024

                                    if (fileSizeInMB > 25) {
                                        Toast.makeText(this, "Video files lesser than 25 MB are allowed", Toast.LENGTH_LONG)
                                            .show()

                                    }else{*/
                var bitrate = ""
                val widthHeight = TrimmerUtils.getVideoWidthHeight(this, contentURI)
                var width = widthHeight[0]
                var height = widthHeight[1]

                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(this, contentURI)

                val originalBitrate =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toInt()
                        ?: 1000000
                val originalFrameRate =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
                        ?.toFloat()?.toInt() ?: 30

                retriever.release()

                /*if(widthHeight[0]>800){
                    width/=2
                    height/=2
                    bitrate = "1M"

                }else{
                    bitrate = "400k"
                }*/
                TrimVideo.activity(contentURI.toString())
                    .setCompressOption(
                        CompressOption(
                            30,      // Use original framerate
                            "${originalBitrate / 1000}k",
                            width,
                            height
                        )
                    )
                    .setHideSeekBar(true)
                    .setTrimType(TrimType.MIN_MAX_DURATION)
                    .setMinToMax(5, 60)
                    .setAccurateCut(true)
                    .start(this, startForResult)

            }
        }

    private val imageCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val path = Uri.fromFile(mImageFile)
                val picturePath = getAbsolutePath(path)
                val uri = Uri.fromFile(File(picturePath))

                val uCrop: UCrop
                val option = UCrop.Options()
                if (isProfile == 2) {
                    val uCrop = UCrop.of(
                        uri,
                        Uri.fromFile(File(cacheDir, UUID.randomUUID().toString() + "CropImage.png"))
                    )

                    val options = UCrop.Options().apply {
                        setFreeStyleCropEnabled(false)  // Disable free-style cropping
                        setHideBottomControls(false)    // Show bottom controls
                        setShowCropGrid(true)          // Hide crop grid
                        setShowCropFrame(false)         // Hide crop frame
                        setAllowedGestures(
                            UCropActivity.SCALE,  // Disable gestures in crop mode
                            UCropActivity.ROTATE,  // Allow rotate gestures
                            UCropActivity.SCALE   // Disable gestures in scale mode
                        )
                        setCompressionFormat(Bitmap.CompressFormat.JPEG)
                        setCompressionQuality(80)
                    }

                    uCrop.withAspectRatio(3f, 4f)  // Enforce 3:4 aspect ratio
                        .withOptions(options)
                        .start(this)

                   /* uCrop = UCrop.of(
                        uri,
                        Uri.fromFile(File(cacheDir, UUID.randomUUID().toString() + "CropImage.png"))
                    )
                    option.setFreeStyleCropEnabled(true)
                    option.setHideBottomControls(false)
                    uCrop.withOptions(option)
                    uCrop?.start(mActivity!!)*/
                } else if (isProfile == 3) {
                    val displayMetrics = Resources.getSystem().displayMetrics
                    val screenWidth = displayMetrics.widthPixels
                    val imageViewHeight = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._150sdp)

                    uCrop = UCrop.of(
                        uri,
                        Uri.fromFile(File(cacheDir, UUID.randomUUID().toString() + "CropImage.png"))
                    ).withAspectRatio(screenWidth.toFloat(), imageViewHeight.toFloat())
                    option.setFreeStyleCropEnabled(false)
                    option.setHideBottomControls(false)
                    uCrop.withOptions(option)
                    uCrop?.start(mActivity!!)
                }  else if (isProfile == 4) {
                    uCrop = UCrop.of(
                        uri,
                        Uri.fromFile(File(cacheDir, UUID.randomUUID().toString() + "CropImage.png"))
                    )
                    option.setFreeStyleCropEnabled(true)
                    option.setHideBottomControls(true)
                    uCrop.withOptions(option)
                    uCrop?.start(mActivity!!)
                }else {
                    uCrop = UCrop.of(
                        uri,
                        Uri.fromFile(File(cacheDir, UUID.randomUUID().toString() + "CropImage.png"))
                    ).withAspectRatio(10F, 8f)
                    option.setFreeStyleCropEnabled(false)
                    option.setHideBottomControls(false)
                    option.setCircleDimmedLayer(true)
                    option.setShowCropGrid(false)
                    option.setShowCropFrame(false)
                    uCrop.withOptions(option)
                    uCrop?.start(mActivity!!)
                }

//                    val uCrop : UCrop

                /*if (isProfile == 2){
                    uCrop   = UCrop.of(uri, Uri.fromFile(File(cacheDir, UUID.randomUUID().toString()+"CropImage.png"))).withAspectRatio(8f,8f)
                }else{
                    uCrop = UCrop.of(uri, Uri.fromFile(File(cacheDir, UUID.randomUUID().toString()+"CropImage.png"))).withAspectRatio(10F,6f)
                }
                val option= UCrop.Options()
                option.setFreeStyleCropEnabled(false)
                if (isProfile == 1){
                    option.setCircleDimmedLayer(true)
                }
                option.setHideBottomControls(true)
                uCrop.withOptions(option)
                uCrop?.start(mActivity!!)*/

            }
        }

    private val videoGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // There are no request codes
                Log.e("VideoSelected", "RESULT_OK")
                val contentURI = result.data?.data
                /*
                                    val selectedVideoPath = getPath(contentURI!!)

                                    val mediaFile: File = File(selectedVideoPath)
                                    val fileSizeInBytes = mediaFile.length()
                                    val fileSizeInKB = fileSizeInBytes / 1024
                                    val fileSizeInMB = fileSizeInKB / 1024

                                    if (fileSizeInMB > 25) {
                                        Toast.makeText(this, "Video files lesser than 25 MB are allowed", Toast.LENGTH_LONG)
                                            .show()
                                    }else{
                                        selectedImage(selectedVideoPath, mCode)
                                    }
                                    // selectedImage(selectedVideoPath, mCode)*/

                var bitrate = ""
                val widthHeight = TrimmerUtils.getVideoWidthHeight(this, contentURI)
                var width = widthHeight[0]
                var height = widthHeight[1]

                /*  val retriever = MediaMetadataRetriever()
                  retriever.setDataSource(this, contentURI)

                  val originalBitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toInt() ?: 1000000
                  val originalFrameRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toFloat()?.toInt() ?: 30

                  retriever.release()*/


                if (widthHeight[0] > 800) {
                    width /= 2
                    height /= 2
                    bitrate = "1M"

                } else {
                    bitrate = "400k"
                }
                TrimVideo.activity(contentURI.toString())
                    /*  .setCompressOption(CompressOption(
                          30,      // Use original framerate
                          "${originalBitrate / 1000}k",
                          width,
                          height)
                      )*/
                    /*  .setCompressOption(CompressOption(
                          30,      // Use original framerate
                          bitrate,
                          width,
                          height))*/
                    .setHideSeekBar(true)
                    .setTrimType(TrimType.MIN_MAX_DURATION)
                    .setMinToMax(5, 60)
//                        .setAccurateCut(true)
                    .start(this, startForResult)
            }
        }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK &&
                result.data != null
            ) {
                val uri = Uri.parse(TrimVideo.getTrimmedVideoPath(result.data))
                selectedImage(uri.toString(), mCode)
            } else
                Log.d("kjldbjkgdf", "videoTrimResultLauncher data is null")
        }

    private val imageGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {

                val path = result.data?.data
                val picturePath = getAbsolutePath(path!!)
                val uri = Uri.fromFile(File(picturePath))

                var uCrop: UCrop
                val option = UCrop.Options()

                if (isProfile == 2) {
                    val uCrop = UCrop.of(
                        uri,
                        Uri.fromFile(File(cacheDir, UUID.randomUUID().toString() + "CropImage.png"))
                    )

                    val options = UCrop.Options().apply {
                        setFreeStyleCropEnabled(false)  // Disable free-style cropping
                        setHideBottomControls(false)    // Show bottom controls
                        setShowCropGrid(true)          // Hide crop grid
                        setShowCropFrame(false)         // Hide crop frame
                        setAllowedGestures(
                            UCropActivity.SCALE,  // Disable gestures in crop mode
                            UCropActivity.ROTATE,  // Allow rotate gestures
                            UCropActivity.SCALE   // Disable gestures in scale mode
                        )
                        setCompressionFormat(Bitmap.CompressFormat.JPEG)
                        setCompressionQuality(80)
                    }

                    uCrop.withAspectRatio(3f, 4f)  // Enforce 3:4 aspect ratio
                        .withOptions(options)
                        .start(this)
               /*     uCrop = UCrop.of(
                        uri,
                        Uri.fromFile(File(cacheDir, UUID.randomUUID().toString() + "CropImage.png"))
                    )
                    option.setFreeStyleCropEnabled(true)
                    option.setHideBottomControls(false)
                    uCrop.withOptions(option)
                    uCrop?.start(mActivity!!)*/
                } else if (isProfile == 3) {
                    val displayMetrics = Resources.getSystem().displayMetrics
                    val screenWidth = displayMetrics.widthPixels
                    val imageViewHeight = resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._150sdp)

                    uCrop = UCrop.of(
                        uri,
                        Uri.fromFile(File(cacheDir, UUID.randomUUID().toString() + "CropImage.png"))
                    ).withAspectRatio(screenWidth.toFloat(), imageViewHeight.toFloat())
                    option.setFreeStyleCropEnabled(false)
                    option.setHideBottomControls(false)
                    uCrop.withOptions(option)
                    uCrop?.start(mActivity!!)
                } else if (isProfile == 4) {
                    uCrop = UCrop.of(
                        uri,
                        Uri.fromFile(File(cacheDir, UUID.randomUUID().toString() + "CropImage.png"))
                    )
                    option.setFreeStyleCropEnabled(true)
                    option.setHideBottomControls(true)
                    uCrop.withOptions(option)
                    uCrop?.start(mActivity!!)
                } else {
                    uCrop = UCrop.of(
                        uri,
                        Uri.fromFile(File(cacheDir, UUID.randomUUID().toString() + "CropImage.png"))
                    ).withAspectRatio(10F, 8f)
                    option.setFreeStyleCropEnabled(false)
                    option.setHideBottomControls(false)
                    option.setCircleDimmedLayer(true)
                    option.setShowCropGrid(false)
                    option.setShowCropFrame(false)
                    uCrop.withOptions(option)
                    uCrop?.start(mActivity!!)
                }


                /*  val uri = Uri.fromFile(File(picturePath))
                  val uCrop : UCrop
                  if (isProfile == 2){
                      uCrop   = UCrop.of(uri, Uri.fromFile(File(cacheDir, UUID.randomUUID().toString()+"CropImage.png"))).withAspectRatio(8f,8f)
                  }else{
                       uCrop = UCrop.of(uri, Uri.fromFile(File(cacheDir, UUID.randomUUID().toString()+"CropImage.png"))).withAspectRatio(10F,6f)
                  }
                  val option= UCrop.Options()
                  option.setFreeStyleCropEnabled(false)
                  option.setHideBottomControls(false)
                  if (isProfile == 1){
                      option.setCircleDimmedLayer(true)
                  }
                  uCrop.withOptions(option)
                  uCrop?.start(mActivity!!)*/

            }
        }

    /*  private val cropImage = registerForActivityResult(CropImageContract()) { result ->
          if (result.isSuccessful) {
              val uriContent = result.uriContent
              val uriFilePath = result.getUriFilePath(mActivity!!.applicationContext)
              selectedImage(uriFilePath.toString(),mCode)
          }
      }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                UCrop.REQUEST_CROP -> {
                    val resultUri = UCrop.getOutput(requireNotNull(data))
                    Log.d("kffbkfsdbvsg", resultUri.toString())
                    selectedImage(resultUri?.path.toString(), mCode)
                }

                UCrop.RESULT_ERROR -> {

                    val cropError = data?.let { UCrop.getError(it) }
                    cropError?.printStackTrace()
                    Log.d("kffbkfsdbvsg", cropError?.message.toString())

                }
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getImage() {

        //*****videoDialog -> put false for pick the Image.*****
        //*****videoDialog -> put true for pick the Video.*****

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (hasPermissions(permissions)) {
                if (mCode == 98) {
                    captureImage()
                } else if (mCode == 99) {
                    openGalleryForImage()
                } else if (mCode == 10) {
                    captureVideo()
                } else if (mCode == 11) {
                    openGalleryForVideo()
                } else {
                    imageDialog()
                }
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                checkPermissionDenied(Manifest.permission.CAMERA)
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)) {
                checkPermissionDenied(Manifest.permission.READ_MEDIA_IMAGES)
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_VIDEO)) {
                checkPermissionDenied(Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                requestPermission()
            }

        } else {
            if (hasPermissions(permissions1)) {
                if (mCode == 98) {
                    captureImage()
                } else if (mCode == 99) {
                    openGalleryForImage()
                } else if (mCode == 10) {
                    captureVideo()
                } else if (mCode == 11) {
                    openGalleryForVideo()
                } else {
                    imageDialog()
                }
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                checkPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
            } /*else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    checkPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }*/ else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                checkPermissionDenied(Manifest.permission.CAMERA)
            } else {
                requestPermission()
            }
        }
    }

    private fun imageDialog() {
        val dialog = Dialog(mActivity!!)
        dialog.setContentView(R.layout.image_picker_dialog)
        val window = dialog.window
        window!!.setGravity(Gravity.BOTTOM)
        window.setLayout(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val camera = dialog.findViewById<TextView>(R.id.select_camera)
        val cancel = dialog.findViewById<TextView>(R.id.cancel)
        val gallery = dialog.findViewById<TextView>(R.id.select_photo_library)
        val removePhoto = dialog.findViewById<TextView>(R.id.remove_photo)
        val removeBin = dialog.findViewById<ImageView>(R.id.ivRemove)
        val divider = dialog.findViewById<View>(R.id.divider8)
        cancel.setOnClickListener { dialog.dismiss() }

        if (isEditProfile) {
            removePhoto.visible()
            removeBin.visible()
            divider.visible()
        } else {
            removePhoto.gone()
            removeBin.gone()
            divider.gone()
        }

        removePhoto.setOnClickListener {
            if (hasProfileImage.isNotEmpty()) {
                if (mCode == 1){
                    selectedImage("", 3)
                }else{
                    selectedImage("", 4)
                }
                dialog.dismiss()
            }
        }

        camera.setOnClickListener {
            dialog.dismiss()
            if (mVideoDialog) {
                captureVideo()
            } else {
                captureImage()
            }
        }

        gallery.setOnClickListener {
            dialog.dismiss()
            if (mVideoDialog) {
                openGalleryForVideo()
            } else {
                openGalleryForImage()
            }
        }
        dialog.show()
    }

    private fun captureVideo() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        videoCameraLauncher.launch(intent)
    }

    private fun captureImage() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        try {
            createImageFile(mActivity!!, imageFileName, ".jpg")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val fileUri = FileProvider.getUriForFile(
            Objects.requireNonNull(mActivity!!), "com.live.azurah.provider",
            mImageFile
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        imageCameraLauncher.launch(intent)
    }

    private fun openGalleryForVideo() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_PICK
        videoGalleryLauncher.launch(
            Intent.createChooser(intent, "Select Video")
        )
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imageGalleryLauncher.launch(intent)

    }

    @Throws(IOException::class)
    fun createImageFile(context: Context, name: String, extension: String) {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        mImageFile = File.createTempFile(
            name,
            extension,
            storageDir
        )
    }

    // util method
    private fun hasPermissions(permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(mActivity!!, it) == PackageManager.PERMISSION_GRANTED
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissionDenied(permissions: String) {
        if (shouldShowRequestPermissionRationale(permissions)) {
            val mBuilder = AlertDialog.Builder(mActivity)
            val dialog: AlertDialog =
                mBuilder.setTitle(R.string.alert).setMessage(R.string.permissionRequired)
                    .setPositiveButton(
                        R.string.ok
                    ) { _, _ -> requestPermission() }
                    .setNegativeButton(
                        R.string.cancel
                    ) { _, _ ->

                    }.create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(
                        mActivity!!, R.color.blue
                    )
                )
            }
            dialog.show()
        } else {
            val builder = AlertDialog.Builder(mActivity)
            val dialog: AlertDialog =
                builder.setTitle(R.string.alert).setMessage(R.string.permissionRequired)
                    .setCancelable(
                        false
                    )
                    .setPositiveButton(R.string.openSettings) { dialog, which ->
                        //finish()
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts(
                                "package",
                                "com.live.azurah",
                                null
                            )
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }.create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(
                        mActivity!!, R.color.blue
                    )
                )
            }
            dialog.show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestMultiplePermissions.launch(permissions)
        } else {
            requestMultiplePermissions.launch(permissions1)
        }
    }


    //------------------------Return Uri file to String Path ------------------//
    @SuppressLint("Recycle")
    fun getAbsolutePath(uri: Uri): String {
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            val projection = arrayOf("_data")
            val cursor: Cursor?
            try {
                cursor = mActivity!!.contentResolver.query(uri, projection, null, null, null)
                val columnIndex = cursor!!.getColumnIndexOrThrow("_data")
                if (cursor.moveToFirst()) {
                    return cursor.getString(columnIndex)
                }
            } catch (e: Exception) {
                // Eat it
                e.printStackTrace()
            }

        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path!!
        }
        return ""
    }

    private fun getPath(uri: Uri?): String? {
        return try {
            val projection = arrayOf(MediaStore.Video.Media.DATA)
            val cursor: Cursor? =
                mActivity!!.contentResolver.query(uri!!, projection, null, null, null)
            if (cursor != null) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                cursor.moveToFirst()
                cursor.getString(columnIndex)
            } else null
        } catch (e: Exception) {
            null
        }

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    open fun askStorageManagerPermission(
        activity: Activity,
        code: Int,
        videoDialog: Boolean,
        from: Int = 0,
        isEdit: Boolean = false,
        hasImage: String = ""
    ) {
        //*****videoDialog -> put false for pick the Image.*****
        //*****videoDialog -> put true for pick the Video.*****
        mActivity = activity
        mCode = code
        isEditProfile = isEdit
        hasProfileImage = hasImage
        isProfile = from
        mCompressor = FileCompressor(activity)
        mVideoDialog = videoDialog

        // 98 for camera and 99 form gallery 10 camera video 11 gallery video
        getImage()
    }

    private fun handleUri(context: Context, uri: Uri): String? {
        context.apply {
            val type = when (contentResolver.getType(uri)) {
                "image/jpeg" -> ".jpeg"
                //another types
                else -> return null
            }
            val dir = File(cacheDir, "dir_name").apply { mkdir() }
            val imageFile = File(dir, "${System.currentTimeMillis()}$type")
            copyStreamToFile(
                contentResolver.openInputStream(uri)!!,
                imageFile
            )
            return imageFile.absolutePath
        }
    }

    private fun copyStreamToFile(inputStream: InputStream, outputFile: File) {
        inputStream.use { input ->
            val outputStream = FileOutputStream(outputFile)
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024) // buffer size
                while (true) {
                    val byteCount = input.read(buffer)
                    if (byteCount < 0) break
                    output.write(buffer, 0, byteCount)
                }
                output.flush()
            }
        }
    }

    abstract fun selectedImage(imagePath: String?, code: Int)
}