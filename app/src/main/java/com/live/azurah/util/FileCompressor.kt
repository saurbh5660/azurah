package com.live.azurah.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import java.io.File
import java.io.IOException
import kotlin.jvm.Throws


class FileCompressor(context: Context?) {
    //max width and height values of the compressed image is taken as 612x816
    private var maxWidth = 612
    private var maxHeight = 816
    private var compressFormat: CompressFormat? = CompressFormat.JPEG
    private var quality = 80
    private var destinationDirectoryPath: String?
    fun setMaxWidth(maxWidth: Int): FileCompressor? {
        this.maxWidth = maxWidth
        return this
    }

    fun setMaxHeight(maxHeight: Int): FileCompressor? {
        this.maxHeight = maxHeight
        return this
    }

    fun setCompressFormat(compressFormat: CompressFormat?): FileCompressor? {
        this.compressFormat = compressFormat
        return this
    }

    fun setQuality(quality: Int): FileCompressor? {
        this.quality = quality
        return this
    }

    fun setDestinationDirectoryPath(destinationDirectoryPath: String?): FileCompressor? {
        this.destinationDirectoryPath = destinationDirectoryPath
        return this
    }

    @Throws(IOException::class)
    fun compressToFile(imageFile: File?): File? {
        return compressToFile(imageFile, imageFile?.name)
    }

    @Throws(IOException::class)
    fun compressToFile(imageFile: File?, compressedFileName: String?): File? {
        return compressImage(
            imageFile!!,
            maxWidth,
            maxHeight,
            compressFormat,
            quality,
            destinationDirectoryPath + File.separator.toString() + compressedFileName
        )
    }

    @Throws(IOException::class)
    fun compressToBitmap(imageFile: File?): Bitmap? {
        return decodeSampledBitmapFromFile(imageFile!!, maxWidth, maxHeight)
    }


    init {
        destinationDirectoryPath =
            context?.cacheDir?.path + File.separator.toString() + "images"
    }
}