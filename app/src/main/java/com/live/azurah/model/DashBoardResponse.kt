package com.live.azurah.model

data class DashBoardResponse(
    val body: Body? = null,
    val code: Int? = null,
    val message: String? = null,
    val success: Boolean? = null
) {
    data class Body(
        val bibleVerse: BibleVerse? = null,
        val songOfTheDay: SongOfTheDay? = null,
        val songOfTheWeek: SongOfTheWeek? = null
    ) {
        data class BibleVerse(
            val current_page: Int? = null,
            val data: List<Data>? = null,
            val per_page: Int? = null,
            val total_count: Int? = null,
            val total_pages: Int? = null
        ) {
            data class Data(
                val created_at: String? = null,
                val deleted_at: Any? = null,
                val description: String? = null,
                val version: String? = null,
                val id: Int? = null,
                val is_deleted: String? = null,
                val status: String? = null,
                val title: String? = null,
                val updated_at: String? = null
            )
        }

        data class SongOfTheDay(
            val current_page: Int? = null,
            val data: List<Data>? = null,
            val per_page: Int? = null,
            val total_count: Int? = null,
            val total_pages: Int? = null
        ) {
            data class Data(
                val created_at: String? = null,
                val deleted_at: Any? = null,
                val description: String? = null,
                val duration: String? = null,
                val id: Int? = null,
                val image: String? = null,
                val image_thumb: String? = null,
                val is_deleted: String? = null,
                val music: String? = null,
                val name: String? = null,
                val singer_name: String? = null,
                val status: String? = null,
                val type: String? = null,
                val updated_at: String? = null
            )
        }

        data class SongOfTheWeek(
            val current_page: Int? = null,
            val data: List<Data>? = null,
            val per_page: Int? = null,
            val total_count: Int? = null,
            val total_pages: Int? = null
        ) {
            data class Data(
                val created_at: String? = null,
                val deleted_at: Any? = null,
                val description: String? = null,
                val duration: String? = null,
                val id: Int? = null,
                val image: String? = null,
                val image_thumb: String? = null,
                val is_deleted: String? = null,
                val music: String? = null,
                val name: String? = null,
                val singer_name: String? = null,
                val status: String? = null,
                val type: String? = null,
                val updated_at: String? = null
            )
        }
    }
}