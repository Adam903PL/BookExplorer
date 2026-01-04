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
    @SerializedName("first_publish_year") val firstPublishYear: Int?
)

data class Author(
    val name: String
)

data class BookDetail(
    val title: String,
    val description: Any?,
    val covers: List<Long>?,
    @SerializedName("number_of_pages") val numberOfPages: Int?
) {
    fun getDescriptionText(): String {
        return when (description) {
            is String -> description
            is Map<*, *> -> description["value"] as? String ?: ""
            else -> ""
        }
    }
}
