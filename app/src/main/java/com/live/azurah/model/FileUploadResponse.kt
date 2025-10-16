package com.live.azurah.model

data class FileUploadResponse(
    val body: List<Body>? = null,
    val code: Int? = null,
    val message: String? = null,
    val status: Boolean? = null
) {
    data class Body(
        val fileName: String? = null,
        val file_type: String? = null,
        val folder: String? = null,
        val image: String? = null,
        val thumbnail: String? = null
    )
}