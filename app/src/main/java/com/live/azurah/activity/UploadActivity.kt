package com.live.azurah.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.live.azurah.R
import com.live.azurah.databinding.ActivityUploadBinding
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.FileUploadResponse
import com.live.azurah.model.ImageVideoModel
import com.live.azurah.retrofit.LoaderDialog
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.Status
import com.live.azurah.util.ProgressRequestBody
import com.live.azurah.util.showCustomSnackbar
import com.live.azurah.util.visible
import com.live.azurah.viewmodel.CommonViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import kotlin.coroutines.resumeWithException

@AndroidEntryPoint
class UploadActivity : AppCompatActivity(){
    private lateinit var binding: ActivityUploadBinding
    private var list = ArrayList<ImageVideoModel>()
    private var uploadedList = ArrayList<FileUploadResponse.Body>()
    private var desc = ""
    //   private val loaderDialog by lazy { LoaderDialog(this) }

    private var fileProgress : LongArray? = null
    private var totalFilesSize: Long = 0
    private val viewModel by viewModels<CommonViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.blue)
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
        fileProgress = LongArray(list.size)
        binding.circularDeterminativePb.progress = 0
        binding.progressTv.text = buildString {
            append(0)
            append("%")
        }
        binding.btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finishAffinity()
        }
        uploadAllFilesInSequence()
    }

    private fun uploadAllFilesInSequence() {
        totalFilesSize = list.sumOf {
            val file = if (it.type == "1") File(it.image.toString()) else File(it.video.toString())
            file.length()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            for (index in list.indices) {
                Log.d("dfkdnakldsf","kjdgsdf______"+index)
                try {
                    val result = uploadFileAtIndex(index)
                    if (result.status == Status.SUCCESS) {
                        when(result.data){
                            is FileUploadResponse->{
                                result.data.body?.let { uploadedList.addAll(it) }
                                if (index == list.size - 1) {
                                Log.d("UPLOAD_POST", "All files uploaded successfully")
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        addPost()
                                    }
                                    Log.d("UPLOAD_POST", "All uploaded successfully: ${list[index].type}")
                                }
                                Log.d("UPLOAD_POST", "File uploaded successfully: ${list[index].type}")
                            }
                        }

                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("UPLOAD_POST", "Upload failed for file at index $index: ${e.message}")
                        showCustomSnackbar(this@UploadActivity, binding.root, e.message ?: "Error uploading file")
                    }
                    return@launch // Stop further uploads on failure
                }
            }
        }
    }


    private suspend fun uploadFileAtIndex(index: Int): Resource<Any> {
        val file: File = if (list[index].type == "1") {
            File(list[index].image.toString())
        } else {
            File(list[index].video.toString())
        }

        Log.d("UPLOAD_DEBUG", "Uploading file: ${file.name}")

        val mediaType = if (list[index].type == "1") "image/*" else "video/*"
        val map = HashMap<String, RequestBody>().apply {
            this["type"] = if (list[index].type == "1") "image".toRequestBody() else "video".toRequestBody()
            this["folder"] = "posts".toRequestBody()
        }

        val progressRequestBody = ProgressRequestBody(
            file,
            mediaType.toMediaTypeOrNull(),
            object : ProgressRequestBody.ProgressListener {
                override fun onProgress(percentage: Int) {
                    val uploadedBytesForThisFile = (percentage.toLong() * file.length()) / 100
                    fileProgress!![index] = uploadedBytesForThisFile
                    val totalUploadedBytes = fileProgress!!.sum()
                    val totalProgress = (totalUploadedBytes * 100 / totalFilesSize).toInt()

                    lifecycleScope.launch(Dispatchers.Main) {
                        binding.circularDeterminativePb.progress = totalProgress
                        binding.progressTv.text = "$totalProgress%"
                    }
                }
            }
        )

        val filePart = MultipartBody.Part.createFormData("image", file.name, progressRequestBody)

        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                val observer = Observer<Resource<Any>> { value ->
                    when (value.status) {
                        Status.SUCCESS -> {
                            Log.d("UPLOAD_DEBUG", "File uploaded successfully: ${file.name}")
                            continuation.resume(value) { throwable ->
                                continuation.resumeWithException(throwable)
                            }
                        }
                        Status.ERROR -> {
                            Log.e("UPLOAD_DEBUG", "Upload failed for file: ${file.name}")
                            continuation.resumeWithException(Exception(value.message ?: "Unknown error"))
                        }
                        else -> Unit
                    }
                }

                // Adding the observer only if it's not already added
                Log.d("djkflhdfg","hfsdhjdfhdf")
                viewModel.postFileUpload(map, filePart, this@UploadActivity).observe(this@UploadActivity, observer)

                // Ensure to remove the observer when the coroutine is cancelled
                continuation.invokeOnCancellation {
                    viewModel.postFileUpload(map, filePart, this@UploadActivity).removeObserver(observer)
                }
            }
        }
    }





//    private fun uploadAllFilesInSequence() {
//        totalFilesSize = list.sumOf {
//            val file = if (it.type == "1") File(it.image.toString()) else File(it.video.toString())
//            file.length()
//        }
//
//        uploadFileAtIndex(0)
//
//    }
//
//    private  fun uploadFileAtIndex(index: Int) {
//        if (index >= list.size) {
//            Log.d("UPLOAD_POST", "All files uploaded successfully")
//            // Only call addPost when all uploads are complete
//            addPost()
//            return
//        }
//
//        val file: File = if (list[index].type == "1") {
//            File(list[index].image.toString())
//        } else {
//            File(list[index].video.toString())
//        }
//
//        Log.d("UPLOAD_POST", "Uploading file ${index + 1} of ${list.size}: ${file.name}")
//
//        var mediaType = ""
//        val map = HashMap<String, RequestBody>().apply {
//            if (list[index].type == "1") {
//                this["type"] = "image".toRequestBody()
//                mediaType = "image/*"
//            } else {
//                mediaType = "video/*"
//                this["type"] = "video".toRequestBody()
//            }
//            this["folder"] = "posts".toRequestBody()
//        }
//
//        val progressRequestBody = ProgressRequestBody(
//            file,
//            mediaType.toMediaTypeOrNull(),
//            object : ProgressRequestBody.ProgressListener {
//                override fun onProgress(percentage: Int) {
//                    // Calculate the uploaded bytes for this file as a percentage of its own size
//                    Log.d("kljhdgasgd",percentage.toString())
//                    val uploadedBytesForThisFile = (percentage.toLong() * file.length()) / 100
//
//                    // Update progress for this file
//                    fileProgress!![index] = uploadedBytesForThisFile
//
//                    // Calculate total uploaded bytes dynamically
//                    val totalUploadedBytes = fileProgress!!.sum()
//
//                    // Calculate overall progress as a percentage
//                    val totalProgress = (totalUploadedBytes * 100 / totalFilesSize).toInt()
//
//                    Log.d("Progress Update", "Total Progress: $totalProgress%")
//                    lifecycleScope.launch(Dispatchers.Main) {
//                        binding.circularDeterminativePb.progress = totalProgress
//                        binding.progressTv.text = "$totalProgress%"
//                    }
//                }
//            }
//        )
//
//        val filePart = MultipartBody.Part.createFormData("image", file.name, progressRequestBody)
//
//        // Observe the upload status on the main thread
//        viewModel.postFileUpload(map, filePart, this@UploadActivity).observe(this@UploadActivity, Observer { value ->
//            when (value.status) {
//                Status.SUCCESS -> {
//                   LoaderDialog.dismiss()
//                    Log.d("UPLOAD_POST", "File uploaded successfully: ${file.name}")
//                    when (value.data) {
//                        is FileUploadResponse -> {
//                            value.data.body?.let { uploadedList.addAll(it) }
//                            // Check if all uploads are complete
//                            Log.d("dfdsafadsfgdgd","nexttttttttttttttt")
//                            uploadFileAtIndex(index+1)
//                           /* if (index == list.size - 1) {
//                                Log.d("UPLOAD_POST", "All files uploaded successfully")
//                                addPost()
//                            }*/
//                        }
//                    }
//                }
//                Status.LOADING -> {
//                   LoaderDialog.dismiss()
//                }
//                Status.ERROR -> {
//                   LoaderDialog.dismiss()
//                    Log.e("UPLOAD_POST", "Upload failed for file: ${file.name}, Error: ${value.message}")
//                    showCustomSnackbar(this@UploadActivity, binding.root, value.message.toString())
//                }
//            }
//        })
//    }


    private fun addPost(){
        Log.d("dfsgsdgsd",Gson().toJson(uploadedList))
        val map = HashMap<String, String>().apply {
            this["description"] = desc
            this["images"] = Gson().toJson(uploadedList)
        }
        viewModel.addPost(map,this).observe(this){value->
            when (value.status) {
                Status.SUCCESS -> {
                   LoaderDialog.dismiss()
                    when (value.data) {
                        is CommonResponse -> {
                          /*  showCustomSnackbar(this, binding.root, "Post added Successfully.")*/
                            binding.btnHome.visible()

                        }
                    }

                }
                Status.LOADING -> {
                    LoaderDialog.show(this)
                }

                Status.ERROR -> {
                   LoaderDialog.dismiss()
                    showCustomSnackbar(this, binding.root, value.message.toString())
                }
            }
        }
    }
}
