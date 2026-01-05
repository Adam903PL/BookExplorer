package com.example.bookexplorer.data

import com.google.gson.annotations.SerializedName

data class BookResponse(
    val works: List<BookWork>
)

data class BookWork(
    val key: String,
    val title: String,
    val authors: List<Author>?,
    @SerializedName("cover_id") val coverId: Long?,
    @SerializedName("first_publish_year") val firstPublishYear: Int?,
    val subject: List<String>?,
    @SerializedName("edition_count") val editionCount: Int?
)

data class Author(
    val name: String
)

data class BookDetail(
    val title: String,
    val description: Any?,
    val covers: List<Long>?,
    @SerializedName("number_of_pages") val numberOfPages: Int?,
    val subjects: List<String>?,
    @SerializedName("first_publish_date") val firstPublishDate: String?
) {
    fun getDescriptionText(): String {
        return when (description) {
            is String -> description
            is Map<*, *> -> description["value"] as? String ?: ""
            else -> ""
        }
    }
}
