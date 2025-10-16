package com.live.azurah.util

import android.util.Log
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.source
import java.io.File

class ProgressRequestBody(
    private val file: File,
    private val mediaType: MediaType?,
    private val listener: ProgressListener
) : RequestBody() {

    interface ProgressListener {
        fun onProgress(percentage: Int)
    }

    override fun contentType() = mediaType
    override fun contentLength() = file.length()

    override fun writeTo(sink: okio.BufferedSink) {
        Log.d("PROGRESS_REQUEST_BODY", "writeTo() called")
        val source = file.source()
        val buffer = okio.Buffer()

        var totalBytesRead: Long = 0
        var bytesRead: Long
        var lastReportedProgress = -1
        try {
            while (source.read(buffer, 8192).also { bytesRead = it } != -1L) {
                sink.write(buffer, bytesRead)
                totalBytesRead += bytesRead

                val progress = (totalBytesRead * 100 / contentLength()).toInt()
                if (progress != lastReportedProgress && progress % 5 == 0) {
                    listener.onProgress(progress)
                    lastReportedProgress = progress
                    Log.d("PROGRESS_REQUEST_BODY", "Uploaded $totalBytesRead bytes, Progress: $progress%")
                }
                Log.d("PROGRESS_REQUEST_BODY", "Uploaded $totalBytesRead bytes, Progress: $progress%")
            }
        } catch (e: Exception) {
            Log.e("PROGRESS_REQUEST_BODY", "Error during upload: ${e.message}", e)
        } finally {
            source.close()
        }
    }
}