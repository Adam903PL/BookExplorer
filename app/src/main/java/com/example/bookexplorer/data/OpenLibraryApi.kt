package com.example.bookexplorer.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.google.gson.annotations.SerializedName

interface OpenLibraryApi {

    @GET("subjects/fiction.json")
    suspend fun getFictionBooks(@Query("limit") limit: Int = 20): BookResponse

    @GET("works/{id}.json")
    suspend fun getBookDetails(@Path("id") id: String): BookDetail

    @GET("search.json")
    suspend fun searchBooks(@Query("q") query: String, @Query("limit") limit: Int = 20): SearchResponse
}

data class SearchResponse(
    val docs: List<SearchDoc>
)

data class SearchDoc(
    val key: String,
    val title: String,
    @SerializedName("author_name") val authorNames: List<String>?,
    @SerializedName("cover_i") val coverId: Long?,
    @SerializedName("first_publish_year") val firstPublishYear: Int?
)
